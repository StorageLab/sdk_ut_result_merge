package com.qcloud.ut_result_sender.ut_result_parse;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.qcloud.ut_result_sender.meta.ErrorFailureCase;
import com.qcloud.ut_result_sender.meta.LanguageStaticsInfo;
import com.qcloud.ut_result_sender.ut_result_backup.BackUpInstance;

public class ResultParser {

    private static final Logger log = LoggerFactory.getLogger(ResultParser.class);

    private static final String tests_attr = "tests";
    private static final String errors_attr = "errors";
    private static final String skipped_attr = "skipped";
    private static final String failures_attr = "failures";
    private static final String time_attr = "time";

    private File resultFile;
    private LanguageStaticsInfo staticsInfo = new LanguageStaticsInfo();

    public ResultParser(File resultFile) {
        super();
        this.resultFile = resultFile;
    }

    private void parseTestSuite(Element testSuiteElement) {
        NodeList testCaseList = testSuiteElement.getElementsByTagName("testcase");
        for (int testCaseIndex = 0; testCaseIndex < testCaseList.getLength(); ++testCaseIndex) {
            Element testCaseElement = (Element) testCaseList.item(testCaseIndex);
            String className = testCaseElement.getAttribute("classname");
            String testCaseName = testCaseElement.getAttribute("name");
            NodeList failureList = testCaseElement.getElementsByTagName("failure");
            NodeList errorList = testCaseElement.getElementsByTagName("error");
            if (failureList != null && failureList.getLength() != 0) {
                staticsInfo.addErrorFailureCase(
                        new ErrorFailureCase("failure", className + " / " + testCaseName));
            }
            if (errorList != null && errorList.getLength() != 0) {
                staticsInfo.addErrorFailureCase(
                        new ErrorFailureCase("error", className + " / " + testCaseName));
            }
        }
    }

    private boolean parseErrorAndFailure(Element rootElement) {
        if (rootElement.getTagName().equals("testsuites")) {
            NodeList testsuiteList = rootElement.getElementsByTagName("testsuite");
            for (int testSuiteIndex = 0; testSuiteIndex < testsuiteList
                    .getLength(); ++testSuiteIndex) {
                if (testsuiteList.item(testSuiteIndex).getNodeType() == Node.ELEMENT_NODE) {
                    Element testSuiteElement = (Element) testsuiteList.item(testSuiteIndex);
                    parseTestSuite(testSuiteElement);
                }
            }
        } else if (rootElement.getTagName().equals("testsuite")) {
            parseTestSuite(rootElement);
        }
        return true;
    }

    public boolean parse() {
        if (!resultFile.exists()) {
            log.error("ut result file not exist. path: {}", resultFile.getAbsolutePath());
            staticsInfo.increaseParseFailed();
            return false;
        }

        String patternStr = "([a-zA-Z0-9]+)-v([0-9.]+).xml";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher m = pattern.matcher(resultFile.getName());
        if (m.find()) {
            String language = m.group(1);
            String version = m.group(2);
            staticsInfo.setLanguage(language);
            staticsInfo.setVersion(version);
        } else {
            log.error("invalid file name. path: {}", resultFile.getAbsolutePath());
            staticsInfo.increaseParseFailed();
            return false;
        }
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        Document document = null;
        DocumentBuilder builder;
        try {
            builder = builderFactory.newDocumentBuilder();
            document = builder.parse(resultFile);
            Element rootElement = document.getDocumentElement();
            if (rootElement.hasAttribute(tests_attr)) {
                String testAttrStr = rootElement.getAttribute(tests_attr);
                staticsInfo.setTests(Long.valueOf(testAttrStr));
            }
            if (rootElement.hasAttribute(errors_attr)) {
                String errorAttrStr = rootElement.getAttribute(errors_attr);
                staticsInfo.setErrors(Long.valueOf(errorAttrStr));
            }
            if (rootElement.hasAttribute(skipped_attr)) {
                String skippedAttrStr = rootElement.getAttribute(skipped_attr);
                staticsInfo.setSkipped(Long.valueOf(skippedAttrStr));
            }
            if (rootElement.hasAttribute(failures_attr)) {
                String failAttrStr = rootElement.getAttribute(failures_attr);
                staticsInfo.setFailures(Long.valueOf(failAttrStr));
            }
            if (rootElement.hasAttribute(time_attr)) {
                String timeAttrStr = rootElement.getAttribute(time_attr);
                staticsInfo.setTime(Double.valueOf(timeAttrStr));
            }
            parseErrorAndFailure(rootElement);
        } catch (SAXException | IOException | ParserConfigurationException
                | NumberFormatException e) {
            log.error("xml parse result file failed! file_path: {}, exception: {}",
                    resultFile.getAbsolutePath(), e.toString());
            staticsInfo.increaseParseFailed();
            return false;
        }
        String xmlLink = BackUpInstance.INSTANCE.backUpXml(resultFile);
        if (xmlLink != null) {
            staticsInfo.setXmlLink(xmlLink);
        }
        return true;
    }

    public LanguageStaticsInfo getStaticsInfo() {
        return staticsInfo;
    }
}


