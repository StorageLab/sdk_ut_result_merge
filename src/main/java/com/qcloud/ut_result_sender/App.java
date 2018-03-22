package com.qcloud.ut_result_sender;

import com.qcloud.ut_result_sender.ut_result_backup.BackUpInstance;
import com.qcloud.ut_result_sender.ut_result_parse.TaskExecutor;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("args num wrong");
            return;
        }

        String inputFolder = args[0];
        String buildUrl = args[1];
        String buildConsoleUrl = args[2];
        String buildNumber = args[3];
        String buildRegion = args[4];

        BackUpInstance.INSTANCE.init(buildNumber);
        TaskExecutor taskExecutor =
                new TaskExecutor(inputFolder, buildUrl, buildConsoleUrl, buildNumber, buildRegion);
        taskExecutor.run();
        BackUpInstance.INSTANCE.shutdown();

    }
}
