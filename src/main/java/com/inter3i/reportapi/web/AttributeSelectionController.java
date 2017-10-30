package com.inter3i.reportapi.web;

import com.inter3i.reportapi.domain.AttributeSelectionResult;
import com.inter3i.reportapi.service.MongoDistinctService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by koreyoshi on 2017/9/21.
 */
@RestController
@RequestMapping("/attribute-select-api")
@Slf4j
public class AttributeSelectionController {

    @Autowired
    private MongoDistinctService mongoDistinctService;

    @CrossOrigin
    @PostMapping("/getData")
    public AttributeSelectionResult getData(@RequestBody String param) {
        log.info(param.toString());
        AttributeSelectionResult attributeSelectionResult = new AttributeSelectionResult();

        List<Map<String, List<Object>>> responseArr = mongoDistinctService.handleDistinctField(param);

        attributeSelectionResult.setSuccess(true);
        attributeSelectionResult.setDatas(responseArr);
        return attributeSelectionResult;
    }
}
