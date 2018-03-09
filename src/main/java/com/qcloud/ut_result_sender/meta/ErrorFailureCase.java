package com.qcloud.ut_result_sender.meta;

public class ErrorFailureCase {
    private String type = "failure";
    private String caseName = "";

    public ErrorFailureCase(String type, String caseName) {
        super();
        this.type = type;
        this.caseName = caseName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }


}
