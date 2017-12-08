package com.baidu.disconf.client.scan.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.disconf.client.config.DisClientConfig;
import com.baidu.disconf.client.scan.ScanMgr;
import com.baidu.disconf.client.scan.inner.dynamic.ScanDynamicStoreAdapter;
import com.baidu.disconf.client.scan.inner.statically.StaticScannerMgr;
import com.baidu.disconf.client.scan.inner.statically.StaticScannerMgrFactory;
import com.baidu.disconf.client.scan.inner.statically.impl.StaticScannerNonAnnotationFileMgrImpl;
import com.baidu.disconf.client.scan.inner.statically.model.ScanStaticModel;
import com.baidu.disconf.client.scan.inner.statically.strategy.ScanStaticStrategy;
import com.baidu.disconf.client.scan.inner.statically.strategy.impl.ReflectionScanStatic;
import com.baidu.disconf.client.store.inner.DisconfCenterHostFilesStore;
import com.baidu.disconf.client.support.registry.Registry;

/**
 * 扫描模块
 *
 * @author liaoqiqi
 * @version 2014-6-6
 *
 * 2017-12-07
 */
public class ScanMgrImpl implements ScanMgr {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ScanMgrImpl.class);

    // 扫描对象
    private volatile ScanStaticModel scanModel = null;
    private Registry registry = null;
    private List<StaticScannerMgr> staticScannerMgrList = new ArrayList<StaticScannerMgr>();
    private ScanStaticStrategy scanStaticStrategy = new ReflectionScanStatic();

    public ScanMgrImpl(Registry registry) {
        this.registry = registry;
        staticScannerMgrList.add(StaticScannerMgrFactory.getDisconfFileStaticScanner());               // 配置文件
        staticScannerMgrList.add(StaticScannerMgrFactory.getDisconfItemStaticScanner());               // 配置项
        staticScannerMgrList.add(StaticScannerMgrFactory.getDisconfNonAnnotationFileStaticScanner());  // 非注解 托管的配置文件
    }

    /**
     * 扫描并存储(静态)
     */
    public void firstScan(List<String> packageNameList) throws Exception {
        LOGGER.debug("start to scan package: " + packageNameList.toString());

        scanModel = scanStaticStrategy.scan(packageNameList);                                      // 获取扫描对象并分析整合
        scanModel.setJustHostFiles(DisconfCenterHostFilesStore.getInstance().getJustHostFiles());  // 增加非注解的配置
        for (StaticScannerMgr scannerMgr : staticScannerMgrList) {                                 // 放进仓库
            scannerMgr.scanData2Store(scanModel);                                                  // 扫描进入仓库
            scannerMgr.exclude(DisClientConfig.getInstance().getIgnoreDisconfKeySet());            // 忽略哪些KEY
        }
    }

    /**
     * 第二次扫描(动态)
     */
    public void secondScan() throws Exception {

        if (DisClientConfig.getInstance().ENABLE_DISCONF) {  // 开启disconf才需要处理回调
            if (scanModel == null) {
                synchronized(scanModel) {
                    if (scanModel == null) {  // 下载模块必须先初始化
                        throw new Exception("You should run first scan before second Scan");
                    }
                }
            }
            ScanDynamicStoreAdapter.scanUpdateCallbacks(scanModel, registry);  // 将回调函数实例化并写入仓库
        }
    }

    /**
     * reloadable file scan
     */
    @Override
    public void reloadableScan(String fileName) throws Exception {
        StaticScannerNonAnnotationFileMgrImpl.scanData2Store(fileName);
    }
}