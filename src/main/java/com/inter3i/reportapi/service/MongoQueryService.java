package com.inter3i.reportapi.service;

import com.inter3i.reportapi.domain.QueryParams;
import com.inter3i.reportapi.util.DynamicBuildMongoAggregationUtils;
import com.inter3i.reportapi.util.FhIndicatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.inter3i.reportapi.util.DynamicBuildMongoAggregationUtils.*;
import static com.inter3i.reportapi.util.FhIndicatorUtils.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;

/**
 * Created by zhuguowei on 9/14/17.
 */
@Service
public class MongoQueryService {
    @Autowired
    private MongoOperations mongoOperations;

    public List<Map> stat(QueryParams queryParams){
        List<String> whereList = queryParams.getWhereList();
        final String groupBy = queryParams.getGroupBy();
        final String rawSelect = queryParams.getSelect();
        final String processedSelect = completeSelect(rawSelect);
        final String sort = queryParams.getSort();
        final String unwindKey = DynamicBuildMongoAggregationUtils.getUnwindKey(groupBy);

        MatchOperation match1 = match(buildCriteria1(whereList,unwindKey));
        UnwindOperation unwind = buildUnwindOperation(groupBy);
        Criteria criteria2 = buildCriteria2(whereList, unwindKey);
        MatchOperation match2 = criteria2!=null ? match(criteria2) : null;
        String processedGroupBy = queryParams.getProcessedGroupBy();
        GroupOperation group = buildGroupOperation(processedGroupBy, processedSelect);
        ProjectionOperation project = buildProjectOperation(processedGroupBy, processedSelect);

        SortOperation sortOperation = buildSortOperation(sort,extractFhKeys(processedSelect));

        List<AggregationOperation> aggregationOperations = getAggregationOperations(match1, unwind, match2, group, project,sortOperation);
        Aggregation aggregation = newAggregation(aggregationOperations);

        AggregationResults<Map> result = mongoOperations.aggregate(aggregation, "v3_test_data", Map.class);

        List<Map> mappedResults = result.getMappedResults();
        // 基于el表达式 计算复合指标
        List<String> fhKeys = extractFhKeys(processedSelect);
        Map<String, String> key2ElMap = getFhKey2ElMap(fhKeys);
        populateFhValues(mappedResults, key2ElMap);

        // 如果排序字段中 存在复合指标 重新排序
        List<String> sortKeyList = extractSortKey(sort);
        List<Map> sortedMappedResults = new ArrayList(mappedResults);
        boolean isExistFhKey = sortKeyList.stream().anyMatch(e -> fhKeys.contains(e));
        if(isExistFhKey){
            sortByFhKeys(sort, sortedMappedResults);
        }

        // 只保留原始select中的指标
        List<String> retainedKeys = extractKeys(rawSelect);
        // 同时维度也需要保留
        retainedKeys.addAll(Arrays.asList(processedGroupBy.split(",")));
        List<Map> resultList = pruneMapperResult(sortedMappedResults, retainedKeys);

        return resultList;
    }

    public void sortByFhKeys(String sort, List<Map> sortedMappedResults) {
        Collections.sort(sortedMappedResults,(o1, o2) -> {
            int result1 = 0;
            String[] split = sort.split(",");
            for (String s : split) {
                String[] keyAndDirectionFlag = s.split(" ");
                String key = keyAndDirectionFlag[0];
                String directionFlag = keyAndDirectionFlag[1];

                if (o1.get(key) instanceof String){
                    result1 = ((String) o1.get(key)).compareTo((String) o2.get(key));
                }else if (o1.get(key) instanceof Integer){
                    result1 = ((Integer) o1.get(key)).compareTo((Integer) o2.get(key));
                }
                if(!"1".equals(directionFlag)){ // 降序
                    result1 *= -1;
                }
                if(result1 != 0){
                    return result1;
                }
            }
            return result1;
        });
    }

    public List<String> extractSortKey(String sort) {
        List<String> sortKeyList = new ArrayList<>();
        if(!StringUtils.isEmpty(sort)) {
            String[] split = sort.split(",");
            for (String s : split) {
                String[] keyDirection = s.split(" ");
                String key = keyDirection[0];
                sortKeyList.add(key);
            }
        }
        return Collections.unmodifiableList(sortKeyList);
    }

    private List<Map> pruneMapperResult(List<Map> mappedResults, List<String> keys) {
        List<Map> resultList = new ArrayList<>(mappedResults.size());
        for (Map mappedResult : mappedResults) {
            Map<String, Object> map = new HashMap<>(keys.size());
            for (String key : keys) {
                map.put(key, mappedResult.get(key));
            }
            resultList.add(map);
        }
        return resultList;
    }

    private List<String> extractKeys(String rawSelect) {
        String[] split = rawSelect.split(",");
        List<String> keys = new ArrayList<>(split.length);
        for (String s : split) {
            String[] operKey = s.split(" ");
            String key = operKey[1];
            keys.add(key);
        }
        return keys;
    }

    /**
     * 完善select 如果只有复合指标 需要显式添加它所依赖的指标
     *
     * 如 fh interaction_amount
     * ==> sum reposts_count,sum comments_count,sum praises_count,fh interaction_amount
     * @param select
     * @return
     */
    private String completeSelect(String select) {
        List<String> fhKeys = FhIndicatorUtils.extractFhKeys(select);
        if(fhKeys.isEmpty()){
            return select;
        }
        String dependedSelect = FhIndicatorUtils.getDependedSelect(fhKeys);
        Set<String> selectSet = new HashSet<>();
        selectSet.addAll(Arrays.asList(select.split(",")));
        selectSet.addAll(Arrays.asList(dependedSelect.split(",")));

        return selectSet.stream().collect(Collectors.joining(","));
    }

    private List<AggregationOperation> getAggregationOperations(MatchOperation match1, UnwindOperation unwind, MatchOperation match2, GroupOperation group, ProjectionOperation project, SortOperation sortOperation) {
        List<AggregationOperation> aggregationOperations = newArrayList();
        aggregationOperations.add(match1);
        if(unwind!=null) {
            aggregationOperations.add(unwind);
        }
        if(match2 != null){
            aggregationOperations.add(match2);
        }
        aggregationOperations.add(group);
        aggregationOperations.add(project);
        if(sortOperation != null){
            aggregationOperations.add(sortOperation);
        }
        return aggregationOperations;
    }


}
