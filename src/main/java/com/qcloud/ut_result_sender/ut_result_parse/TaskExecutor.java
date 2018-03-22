package com.qcloud.ut_result_sender.ut_result_parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import javax.mail.*;
import javax.mail.internet.*;
import javax.print.attribute.standard.RequestingUserName;
import javax.activation.*;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.ut_result_sender.meta.LanguageStaticsInfo;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutor.class);
    private String localFolderPath;
    private String buildUrl;
    private String buildConsoleOutputUrl;
    private String buildNumber;
    private String buildRegion;

    private LanguageStaticsInfo totalInfo = new LanguageStaticsInfo();
    private Map<String, List<LanguageStaticsInfo>> detailInfo = new TreeMap<>();

    public TaskExecutor(String localFolderPath, String buildUrl, String buildConsoleUrl,
            String buildNumber, String buildRegion) {
        super();
        this.localFolderPath = localFolderPath;
        this.buildUrl = buildUrl;
        this.buildConsoleOutputUrl = buildConsoleUrl;
        this.buildNumber = buildNumber;
        this.buildRegion = buildRegion;
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

    private void addLanguageStaticsInfo(LanguageStaticsInfo staticsInfo) {
        String language = staticsInfo.getLanguage();
        List<LanguageStaticsInfo> versionDetailList = detailInfo.get(language);
        if (versionDetailList == null) {
            versionDetailList = new ArrayList<LanguageStaticsInfo>();
            detailInfo.put(language, versionDetailList);
        }

        versionDetailList.add(staticsInfo);

        totalInfo.addTests(staticsInfo.getTests());
        totalInfo.addFailures(staticsInfo.getFailures());
        totalInfo.addErrors(staticsInfo.getErrors());
        totalInfo.addSkipped(staticsInfo.getSkipped());
        totalInfo.addTime(staticsInfo.getTime());
    }


    private void MergeResult(ResultParser resultParser) {
        resultParser.parse();
        LanguageStaticsInfo staticsInfo = resultParser.getStaticsInfo();
        addLanguageStaticsInfo(staticsInfo);
    }

    private void printStaticsInfo(String key, LanguageStaticsInfo staticsInfo) {
        String printStr = String.format(
                "%s : [tests: %d], [failures: %d], [errors: %d], [skipped: %d], [time: %f]</br>",
                key, staticsInfo.getTests(), staticsInfo.getFailures(), staticsInfo.getErrors(),
                staticsInfo.getSkipped(), staticsInfo.getTime());
        System.out.println(printStr);
    }

    private void buildEmailContent() {
        // 创建一个合适的Configration对象
        try {
            Configuration configuration = new Configuration();
            configuration.setDirectoryForTemplateLoading(new File("HtmlTemplate"));
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.setDefaultEncoding("UTF-8"); // 这个一定要设置，不然在生成的页面中 会乱码
            Template template = configuration.getTemplate("mail.tpl.html");

            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("buildNumber", buildNumber);
            paramMap.put("buildRegion", buildRegion);
            paramMap.put("buildUrl", buildUrl);
            paramMap.put("buildConsoleUrl", buildConsoleOutputUrl);
            paramMap.put("testTime", currentTime);
            paramMap.put("totalInfo", totalInfo);
            paramMap.put("detailInfo", detailInfo);

            Writer writer = new OutputStreamWriter(
                    new FileOutputStream("output/email_content.html"), "UTF-8");
            template.process(paramMap, writer);
            writer.close();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
        // 获取或创建一个模版。
    }

    private String getEmailContent() {
        StringBuffer strBuf = new StringBuffer();
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(new FileInputStream("output/email_content.html"), "UTF-8");
            BufferedReader reader = new BufferedReader(in);
            String line = reader.readLine();
            while (line != null) {
                strBuf.append(line);
                strBuf.append("\n");
                line = reader.readLine();
            }
            reader.close();
            in.close();
        } catch (UnsupportedEncodingException e) {
            log.error("getEmailContent", e);
        } catch (FileNotFoundException e) {
            log.error("getEmailContent", e);
        } catch (IOException e) {
            log.error("getEmailContent", e);
        }
        return strBuf.toString();
    }

    private void sendEmail() {
        String emailHost = System.getenv("email_host");
        String emailFrom = System.getenv("email_from");
        String emailFromName = System.getenv("email_from_name");
        String emailFromPasswd = System.getenv("email_from_passwd");
        String emailSendToArrStr = System.getenv("email_sendto");
        String[] emailSendToArray = emailSendToArrStr.split(",");

        
        // 不要使用SimpleEmail,会出现乱码问题
        HtmlEmail email = new HtmlEmail();
        email.setSSLOnConnect(true);
        try {
            // 这里是SMTP发送服务器的名字：
            email.setHostName(emailHost);
            // 字符编码集的设置
            email.setCharset("UTF-8");
            // 收件人的邮箱
            for (String emailSendTo : emailSendToArray) {
                log.info("add email recever {}", emailSendTo);
                email.addTo(emailSendTo);
            }
            // 发送人的邮箱
            email.setFrom(emailFrom, emailFromName);
            // 如果需要认证信息的话，设置认证：用户名-密码。分别为发件人在邮件服务器上的注册名称和密码
            email.setAuthentication(emailFrom, emailFromPasswd);
            email.setSubject("[OneBox] SDK UT REPORT");
            // 要发送的信息，由于使用了HtmlEmail，可以在邮件内容中使用HTML标签
            email.setHtmlMsg(getEmailContent());
            // 发送
            log.info("ready to send email");
            email.send();
            log.info("send success");
        } catch (Exception e) {
            log.error("send email failed", e);
        }
    }

    public void run() {
        scanLocalFolder();
        buildEmailContent();
        sendEmail();
    }
}
