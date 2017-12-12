package com.baidu.disconf.core.common.restful;

import com.baidu.disconf.core.common.restful.impl.RestfulMgrImpl;
import com.baidu.disconf.core.common.restful.retry.impl.RetryStrategyRoundBin;

/**
 * @author liaoqiqi
 * @version 2014-8-4
 *
 * 2017-12-11
 */
public class RestfulFactory {

    /**
     * 获取一个默认的抓取器
     */
    public static RestfulMgr getRestfulMgrNomal() throws Exception {
        return new RestfulMgrImpl(new RetryStrategyRoundBin());
    }
}