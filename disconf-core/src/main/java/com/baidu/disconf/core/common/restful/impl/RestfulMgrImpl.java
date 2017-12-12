package com.baidu.disconf.core.common.restful.impl;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.disconf.core.common.restful.RestfulMgr;
import com.baidu.disconf.core.common.restful.core.RemoteUrl;
import com.baidu.disconf.core.common.restful.core.UnreliableInterface;
import com.baidu.disconf.core.common.restful.retry.RetryStrategy;
import com.baidu.disconf.core.common.restful.type.FetchConfFile;
import com.baidu.disconf.core.common.restful.type.RestfulGet;
import com.baidu.disconf.core.common.utils.ClassLoaderUtil;
import com.baidu.disconf.core.common.utils.MyStringUtils;
import com.baidu.disconf.core.common.utils.OsUtil;
import com.baidu.disconf.core.common.utils.http.HttpClientUtil;

/**
 * RestFul的一个实现, 独立模块
 *
 * @author liaoqiqi
 * @version 2014-6-10
 *
 * 2017-12-11
 */
public class RestfulMgrImpl implements RestfulMgr {
    protected static final Logger LOGGER = LoggerFactory.getLogger(RestfulMgrImpl.class);

    /**
     * 重试策略
     */
    private RetryStrategy retryStrategy;

    public RestfulMgrImpl(RetryStrategy retryStrategy) {
        this.retryStrategy = retryStrategy;
        HttpClientUtil.init();
    }

    /**
     * 获取JSON数据
     */
    public <T> T getJsonData(Class<T> clazz, RemoteUrl remoteUrl, int retryTimes, int retrySleepSeconds) throws Exception {
        Exception ex = null;
        for (URL url : remoteUrl.getUrls()) {
            // 可重试的下载
            UnreliableInterface unreliableImpl = new RestfulGet<T>(clazz, url);
            try {
                T t = retryStrategy.retry(unreliableImpl, retryTimes, retrySleepSeconds);
                return t;
            } catch (Exception e) {
                ex = e;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    LOGGER.info("pass");
                }
            }
        }
        throw new Exception("cannot get: " + remoteUrl, ex);
    }

    /**
     * @param remoteUrl         远程地址
     * @param fileName          文件名
     * @param localFileDir      本地文件地址
     * @param targetDirPath     下载完后，配置文件指定的文件目录
     */
    @Override
    public String downloadFromServer(RemoteUrl remoteUrl,
                                     String fileName,
                                     String localFileDir,
                                     String localFileDirTemp,
                                     String targetDirPath,
                                     boolean enableLocalDownloadDirInClassPath,
                                     int retryTimes,
                                     int retrySleepSeconds) throws Exception {

        File localFile = null;  // 目标地址文件
        // 进行下载、mv、copy
        try {
            // 可重试的下载
            File tmpFilePathUniqueFile =
                    retryDownload(localFileDirTemp, fileName, remoteUrl, retryTimes, retrySleepSeconds);
            // 将 tmp file copy localFileDir
            localFile = transfer2SpecifyDir(tmpFilePathUniqueFile, localFileDir, fileName, false);
            // mv 到指定目录
            if (targetDirPath != null) {
                if (enableLocalDownloadDirInClassPath ||
                        !targetDirPath.equals(ClassLoaderUtil.getClassPath())) {
                    localFile = transfer2SpecifyDir(tmpFilePathUniqueFile, targetDirPath, fileName, true);
                }
            }
            LOGGER.debug("Move to: " + localFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.warn("download file failed, using previous download file.", e);
        }

        // 判断是否下载失败
        if (localFile == null || !localFile.exists()) {
            throw new Exception("target file cannot be found! " + fileName);
        }

        // 下面为下载成功
        // 返回相对路径
        String relativePathString = OsUtil.getRelativePath(localFile, new File(localFileDir));
        if (relativePathString != null) {
            if (new File(relativePathString).isFile()) {
                return relativePathString;
            }
        }
        // 否则, 返回全路径
        return localFile.getAbsolutePath();
    }

    @Override
    public void close() {
        HttpClientUtil.close();
    }

    /**
     * 可重试的下载
     */
    private File retryDownload(String localFileDirTemp,
                               String fileName,
                               RemoteUrl remoteUrl,
                               int retryTimes,
                               int retrySleepSeconds) throws Exception {
        if (localFileDirTemp == null) {
            localFileDirTemp = "./disconf/download";
        }
        String tmpFilePath = OsUtil.pathJoin(localFileDirTemp, fileName);
        String tmpFilePathUnique = MyStringUtils.getRandomName(tmpFilePath);
        File tmpFilePathUniqueFile = new File(tmpFilePathUnique);
        retry4ConfDownload(remoteUrl, tmpFilePathUniqueFile, retryTimes, retrySleepSeconds);
        return tmpFilePathUniqueFile;
    }

    /**
     * copy/mv 到指定目录
     */
    private File transfer2SpecifyDir(File srcFile, String targetDirPath, String fileName, boolean isMove) throws Exception {
        OsUtil.makeDirs(targetDirPath);  // make dir
        File targetPath = new File(OsUtil.pathJoin(targetDirPath, fileName));
        OsUtil.transferFileAtom(srcFile, targetPath, isMove);  // 从下载文件 复制/mv 到targetPath 原子性的做转移
        return targetPath;
    }

    /**
     * Retry封装 RemoteUrl 支持多Server的下载
     */
    private Object retry4ConfDownload(RemoteUrl remoteUrl, File localTmpFile, int retryTimes, int sleepSeconds) throws Exception {
        Exception ex = null;
        for (URL url : remoteUrl.getUrls()) {
            // 可重试的下载
            UnreliableInterface unreliableImpl = new FetchConfFile(url, localTmpFile);
            try {
                return retryStrategy.retry(unreliableImpl, retryTimes, sleepSeconds);
            } catch (Exception e) {
                ex = e;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    LOGGER.info("pass");
                }
            }
        }
        throw new Exception("download failed.", ex);
    }
}