package com.qcloud.ut_result_sender;

import com.qcloud.ut_result_sender.ut_result_backup.BackUpInstance;
import com.qcloud.ut_result_sender.ut_result_parse.TaskExecutor;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("args num wrong");
            return;
        }
        BackUpInstance.INSTANCE.init();
        TaskExecutor taskExecutor = new TaskExecutor(args[0], args[1], args[2]);
        taskExecutor.run();
        BackUpInstance.INSTANCE.shutdown();
        
    }
}
