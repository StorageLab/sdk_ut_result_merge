package com.qcloud.ut_result_sender.ut_result_parse;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.ut_result_sender.meta.LanguageStaticsInfo;

public class TaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
    private String localFolderPath;

    private LanguageStaticsInfo totalStaticsInfo = new LanguageStaticsInfo();
    private Map<String, LanguageStaticsInfo> languageStaticsInfo = new TreeMap<>();
    private Map<String, LanguageStaticsInfo> detailLanguageStaticsInfo = new TreeMap<>();

    public TaskExecutor(String localFolderPath) {
        super();
        this.localFolderPath = localFolderPath;
    }

    public void scanLocalFolder() {
        SimpleFileVisitor<Path> finder = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                String filePath = file.toString();
                if (filePath.endsWith(".xml")) {
                    ResultParser resultParser = new ResultParser(new File(filePath));
                    MergeResult(resultParser);
                }
                return super.visitFile(file, attrs);
            }
        };

        try {
            java.nio.file.Files.walkFileTree(Paths.get(localFolderPath), finder);
        } catch (IOException e) {
            log.error("walk file tree error", e);
        }
    }

    private void addParseError(String language) {
        if (languageStaticsInfo.get(language) == null) {
            LanguageStaticsInfo tempInfo = new LanguageStaticsInfo();
            languageStaticsInfo.put(language, tempInfo);
        }
        LanguageStaticsInfo staticsInfo = languageStaticsInfo.get(language);
        staticsInfo.increaseParseFailed();
        totalStaticsInfo.increaseParseFailed();
    }

    private void addLanguageStaticsInfo(LanguageStaticsInfo staticsInfo) {
        String language = staticsInfo.getLanguage();
        String version = staticsInfo.getVersion();
        String key = language + "-v" + version;
        detailLanguageStaticsInfo.put(key, staticsInfo);

        if (languageStaticsInfo.get(language) == null) {
            LanguageStaticsInfo tempStatticsInfo = new LanguageStaticsInfo();
            languageStaticsInfo.put(language, tempStatticsInfo);
        }

        LanguageStaticsInfo staticsInfoMerge = languageStaticsInfo.get(language);
        staticsInfoMerge.addTests(staticsInfo.getTests());
        staticsInfoMerge.addFailures(staticsInfo.getFailures());
        staticsInfoMerge.addErrors(staticsInfo.getErrors());
        staticsInfoMerge.addSkipped(staticsInfo.getSkipped());
        staticsInfoMerge.addTime(staticsInfo.getTime());

        totalStaticsInfo.addTests(staticsInfo.getTests());
        totalStaticsInfo.addFailures(staticsInfo.getFailures());
        totalStaticsInfo.addErrors(staticsInfo.getErrors());
        totalStaticsInfo.addSkipped(staticsInfo.getSkipped());
        totalStaticsInfo.addTime(staticsInfo.getTime());
    }


    private void MergeResult(ResultParser resultParser) {
        LanguageStaticsInfo staticsInfo = resultParser.getStaticsInfo();
        if (!resultParser.parse()) {
            String language = staticsInfo.getLanguage();
            if (language.isEmpty()) {
                language = "Unknown-Language";
            }
            addParseError(language);
            return;
        } else {
            addLanguageStaticsInfo(staticsInfo);
        }
    }

    private void printStaticsInfo(String key, LanguageStaticsInfo staticsInfo) {
        String printStr = String.format(
                "%s : [tests: %d], [failures: %d], [errors: %d], [skipped: %d], [time: %f]<br/>", key,
                staticsInfo.getTests(), staticsInfo.getFailures(), staticsInfo.getErrors(),
                staticsInfo.getSkipped(), staticsInfo.getTime());
        System.out.println(printStr);
    }

    private void printResult() {
        printStaticsInfo("total", totalStaticsInfo);
        for (Entry<String, LanguageStaticsInfo> entry : languageStaticsInfo.entrySet()) {
            printStaticsInfo(entry.getKey(), entry.getValue());
        }
        for (Entry<String, LanguageStaticsInfo> entry : detailLanguageStaticsInfo.entrySet()) {
            printStaticsInfo(entry.getKey(), entry.getValue());
        }
    }

    public void run() {
        scanLocalFolder();
        System.out.println("ut_result_start");
        printResult();
        System.out.println("ut_result_end");
    }
}
