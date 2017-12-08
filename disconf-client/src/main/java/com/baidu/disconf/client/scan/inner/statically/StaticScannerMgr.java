package com.baidu.disconf.client.scan.inner.statically;

import java.util.Set;

import com.baidu.disconf.client.scan.inner.statically.model.ScanStaticModel;

/**
 * @author liaoqiqi
 * @version 2014-9-9
 *
 * 2017-12-08
 */
public interface StaticScannerMgr {

    void scanData2Store(ScanStaticModel scanModel);
    void exclude(Set<String> keySet);
}
