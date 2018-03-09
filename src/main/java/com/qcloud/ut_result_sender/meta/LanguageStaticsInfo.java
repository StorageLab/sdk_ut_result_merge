package com.qcloud.ut_result_sender.meta;

import java.util.ArrayList;
import java.util.List;

public class LanguageStaticsInfo {
    private long parseFailed = 0;

    private String language = "";
    private String version = "";
    private long tests;
    private long errors;
    private long skipped;
    private long failures;
    private double time = 0.0;
    private String state = "success";
    private String xmlLink = "";

    private List<ErrorFailureCase> errorFailureCaseList = new ArrayList<>();

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public long getParseFailed() {
        return parseFailed;
    }

    public void increaseParseFailed() {
        this.parseFailed += 1;
    }

    public long getTests() {
        return tests;
    }

    public void setTests(long tests) {
        this.tests = tests;
    }

    public void addTests(long tests) {
        this.tests += tests;
    }

    public long getErrors() {
        return errors;
    }

    public void setErrors(long errors) {
        this.errors = errors;
    }

    public void addErrors(long errors) {
        this.errors += errors;
    }

    public long getSkipped() {
        return skipped;
    }

    public void setSkipped(long skipped) {
        this.skipped = skipped;
    }

    public void addSkipped(long skipped) {
        this.skipped += skipped;
    }

    public long getFailures() {
        return failures;
    }

    public void setFailures(long failures) {
        this.failures = failures;
    }

    public void addFailures(long failures) {
        this.failures += failures;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void addTime(double time) {
        this.time += time;
    }

    public void addErrorFailureCase(ErrorFailureCase errorFailureCase) {
        this.errorFailureCaseList.add(errorFailureCase);
        this.state = "error";
    }

    public List<ErrorFailureCase> getErrorFailureCaseList() {
        return errorFailureCaseList;
    }

    public String getState() {
        return state;
    }

    public String getXmlLink() {
        return xmlLink;
    }

    public void setXmlLink(String xmlLink) {
        this.xmlLink = xmlLink;
    }
}
