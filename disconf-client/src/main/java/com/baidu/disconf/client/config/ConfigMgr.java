package com.baidu.disconf.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.disconf.client.config.inner.DisClientComConfig;
import com.baidu.disconf.client.config.inner.DisInnerConfigHelper;

/**
 * 配置模块
 *
 * @author liaoqiqi
 * @version 2014-6-6
 *
 *  2017-12-07
 */
public class ConfigMgr {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ConfigMgr.class);

    private static boolean isInit = false;

    /**
     * 初始化配置
     */
    public synchronized static void init() throws Exception {
        LOGGER.info("--------------- LOAD CONFIG START ---------------");
        LOGGER.info("Finer print: " + DisClientComConfig.getInstance().getInstanceFingerprint()); // 格式： host_port_uuid

        DisClientSysConfig.getInstance().loadConfig(null); // 导入系统配置
        DisInnerConfigHelper.verifySysConfig();                    // 校验 系统配置

        DisClientConfig.getInstance().loadConfig(null);    // 导入用户配置
        DisInnerConfigHelper.verifyUserConfig();                   // 校验 用户配置
        isInit = true;

        LOGGER.info("--------------- LOAD CONFIG END ---------------");
    }

    public synchronized static boolean isInit() {
        return isInit;
    }

    public static void main(String[] args) {
        try {
            ConfigMgr.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}