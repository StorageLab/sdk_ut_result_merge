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
import org.xml.sax.SAXException;

import com.qcloud.ut_result_sender.meta.LanguageStaticsInfo;

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
        } catch (SAXException | IOException | ParserConfigurationException
                | NumberFormatException e) {
            log.error("xml parse result file failed! file_path: {}, exception: {}",
                    resultFile.getAbsolutePath(), e.toString());
            staticsInfo.increaseParseFailed();
            return false;
        }
        return true;
    }

    public LanguageStaticsInfo getStaticsInfo() {
        return staticsInfo;
    }
}


