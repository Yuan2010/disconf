package com.baidu.disconf.client.store;

import com.baidu.disconf.client.common.update.IDisconfUpdatePipeline;

/**
 *2017-12-09
 */
public interface DisconfStorePipelineProcessor {
    void setDisconfUpdatePipeline(IDisconfUpdatePipeline iDisconfUpdatePipeline);
}