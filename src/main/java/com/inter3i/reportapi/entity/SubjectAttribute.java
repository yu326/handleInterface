package com.inter3i.reportapi.entity;

/**
 * Created by koreyoshi on 2017/9/18.
 */
public class SubjectAttribute {
    private int    id;
    private int    subjectid;
    private int    charttype;
    private String  dimension;
    private String  quota;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubjectid() {
        return subjectid;
    }

    public void setSubjectid(int subjectid) {
        this.subjectid = subjectid;
    }

    public int getCharttype() {
        return charttype;
    }

    public void setCharttype(int charttype) {
        this.charttype = charttype;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public String getQuota() {
        return quota;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }
}
