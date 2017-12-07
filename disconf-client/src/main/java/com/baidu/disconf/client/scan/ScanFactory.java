package com.baidu.disconf.client.scan;

import com.baidu.disconf.client.scan.impl.ScanMgrImpl;
import com.baidu.disconf.client.support.registry.Registry;

/**
 * 扫描器工厂
 *
 * @author liaoqiqi
 * @version 2014-7-29
 *
 * 2017-12-07
 */
public class ScanFactory {
    public static ScanMgr getScanMgr(Registry registry) throws Exception {
        return new ScanMgrImpl(registry);
    }
}