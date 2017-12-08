package com.baidu.disconf.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.disconf.client.config.inner.DisInnerConfigAnnotation;
import com.baidu.disconf.client.support.DisconfAutowareConfig;

/**
 * Disconf 系统自带的配置
 *
 * @author liaoqiqi
 * @version 2014-6-6
 *
 * 2017-12-08
 */
public class DisClientSysConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(DisClientSysConfig.class);

    private boolean isLoaded = false;
    protected static final String filename = "disconf_sys.properties";
    protected static final DisClientSysConfig INSTANCE = new DisClientSysConfig();

    public static DisClientSysConfig getInstance() {
        return INSTANCE;
    }

    private DisClientSysConfig() {
    }

    public synchronized boolean isLoaded() {
        return isLoaded;
    }

    /**
     * load config normal
     */
    public synchronized void loadConfig(String filePath) throws Exception {
        if (isLoaded) {
            return;
        }
        String propertyFilePath = filePath != null ? filePath : filename;
        DisconfAutowareConfig.autowareConfig(INSTANCE, propertyFilePath);
        isLoaded = true;
    }

    /**
     * STORE URL
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_store_action")
    public String CONF_SERVER_STORE_ACTION;

    /**
     * STORE URL
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_zoo_action")
    public String CONF_SERVER_ZOO_ACTION;

    /**
     * 获取远程主机个数的URL
     */
    @DisInnerConfigAnnotation(name = "disconf.conf_server_master_num_action")
    public String CONF_SERVER_MASTER_NUM_ACTION;

    /**
     * 下载文件夹, 远程文件下载后会放在这里
     */
    @DisInnerConfigAnnotation(name = "disconf.local_download_dir")
    public String LOCAL_DOWNLOAD_DIR;
}