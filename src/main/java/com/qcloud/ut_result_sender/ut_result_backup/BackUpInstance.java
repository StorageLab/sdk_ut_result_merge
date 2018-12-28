package com.qcloud.ut_result_sender.ut_result_backup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;

public class BackUpInstance {
    private static final Logger log = LoggerFactory.getLogger(BackUpInstance.class);

    public static BackUpInstance INSTANCE = new BackUpInstance();
    private static COSClient cosclient = null;
    private static String region = null;
    private static String bucketName = null;
    private static String backupFolder = null;

    private BackUpInstance() {}

    public void init(String buildNumber) {
        String ak = System.getenv("backupxml_secretId");
        String sk = System.getenv("backupxml_secretKey");
        region = System.getenv("backupxml_region");
        bucketName = System.getenv("backupxml_bucketName");
        if (ak == null || sk == null || bucketName == null || region == null) {
            throw new IllegalArgumentException("miss backupxml config");
        }
        COSCredentials cred = new BasicCOSCredentials(ak, sk);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProxyIp("web-proxy.oa.com");
        clientConfig.setHttpProxyPort(8080);
        cosclient = new COSClient(cred, clientConfig);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        backupFolder = String.format("%s/%s/", sdf.format(new Date()), buildNumber); 
    }

    public void shutdown() {
        if (cosclient != null) {
            cosclient.shutdown();
        }
    }

    public String backUpXml(File xmlFile) {
        String key = backupFolder + xmlFile.getName();
        try {
            cosclient.putObject(bucketName, key, xmlFile);
        } catch (Exception e) {
            log.error("backup xmlfile {} occur a exception {}", key, e.toString());
            return null;
        }

        String xmlLink = String.format("http://%s.cos.%s.myqcloud.com/%s", bucketName, region, key);
        return xmlLink;
    }
}
