package com.baidu.disconf.core.common.restful.retry;

import com.baidu.disconf.core.common.restful.core.UnreliableInterface;

/**
 * 重试的策略
 *
 * @author liaoqiqi
 * @version 2014-6-10
 *
 * 2017-12-11
 */
public interface RetryStrategy {

    <T> T retry(UnreliableInterface unreliableImpl, int retryTimes, int sleepSeconds) throws Exception;
}