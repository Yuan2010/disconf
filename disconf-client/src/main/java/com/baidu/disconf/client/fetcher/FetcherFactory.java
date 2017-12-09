package com.baidu.disconf.client.fetcher;

import com.baidu.disconf.client.config.ConfigMgr;
import com.baidu.disconf.client.config.DisClientConfig;
import com.baidu.disconf.client.config.DisClientSysConfig;
import com.baidu.disconf.client.fetcher.impl.FetcherMgrImpl;
import com.baidu.disconf.core.common.restful.RestfulFactory;

/**
 * 抓取器工厂
 *
 * @author liaoqiqi
 * @version 2014-7-29
 *
 * 2017-12-09
 */
public class FetcherFactory {

    /**
     * 获取抓取器实例，记得释放资源, 它依赖Conf模块
     */
    public static FetcherMgr getFetcherMgr() throws Exception {
        if (!ConfigMgr.isInit()) {
            throw new Exception("ConfigMgr should be init before FetcherFactory.getFetcherMgr");
        }
        FetcherMgr fetcherMgr =
                new FetcherMgrImpl(
                        RestfulFactory.getRestfulMgrNomal(),  // 获取一个默认的抓取器
                        DisClientConfig.getInstance().CONF_SERVER_URL_RETRY_TIMES,
                        DisClientConfig.getInstance().confServerUrlRetrySleepSeconds,
                        DisClientConfig.getInstance().enableLocalDownloadDirInClassPath,
                        DisClientConfig.getInstance().userDefineDownloadDir,
                        DisClientSysConfig.getInstance().LOCAL_DOWNLOAD_DIR,
                        DisClientConfig.getInstance().getHostList());

        return fetcherMgr;
    }
}