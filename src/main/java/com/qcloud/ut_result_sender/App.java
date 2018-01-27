package com.qcloud.ut_result_sender;

import com.qcloud.ut_result_sender.ut_result_parse.TaskExecutor;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("args num wrong");
            return;
        }
        TaskExecutor taskExecutor = new TaskExecutor(args[0]);
        taskExecutor.run();
    }
}
