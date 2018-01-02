package com.baidu.disconf.client.support.registry.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.baidu.disconf.client.support.registry.Registry;

/**
 * Created by knightliao on 15/11/26.
 *
 * 2017-12-08
 */
public class SpringRegistry implements Registry, ApplicationContextAware {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SpringRegistry.class);

    // Spring应用上下文环境
    private static ApplicationContext applicationContext;
    private SimpleRegistry simpleRegistry = new SimpleRegistry();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> List<T> findByType(Class<T> type, boolean newInstance) {
        if (applicationContext == null) {
            LOGGER.error("Spring Context is null. Cannot autowire " + type.getCanonicalName());
            return new ArrayList<T>(0);
        }
        if (type == null) {
            return new ArrayList<T>(0);
        }

        Map<String, T> map = findByTypeWithName(type);
        if (map == null || map.isEmpty()) {
            if (newInstance) {
                LOGGER.debug("Not found from Spring IoC container for " + type.getSimpleName() + ", and try to init by "
                        + "calling newInstance.");
                return simpleRegistry.findByType(type, newInstance);
            }
        }
        return new ArrayList<T>(map.values());
    }

    @Override
    public <T> T getFirstByType(Class<T> type, boolean newInstance) {
        List<T> list = this.findByType(type, newInstance);
        if (list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> T getFirstByType(Class<T> type, boolean newInstance, boolean withProxy) {
        T object = getFirstByType(type, newInstance);
        if (!withProxy) {
            return object;
        }
        try {
            return getTargetObject(object, type);
        } catch (Exception e) {
            LOGGER.warn(e.toString());
            return object;
        }
    }

    /**
     * 调用Spring工具类获取bean
     * @return 容器托管的bean字典
     */
    public <T> Map<String, T> findByTypeWithName(Class<T> type) {
        return applicationContext.getBeansOfType(type);
    }

    /** 跟踪到Spring中看看怎么判断代理的类型？ */
    protected <T> T getTargetObject(Object proxy, Class<T> targetClass) throws Exception {
        if (AopUtils.isJdkDynamicProxy(proxy)) {                          // 最终调用了JDK的方法
            return (T) ((Advised) proxy).getTargetSource().getTarget();
        } else if (AopUtils.isCglibProxy(proxy)) {                        // 判断是否包含2个$$
            return (T) ((Advised) proxy).getTargetSource().getTarget();
        } else {
            return (T) proxy;
        }

        /*

        第 1个： 判断代理类型：

           JDK代理： object instanceof SpringProxy && Proxy.isProxyClass(object.getClass());  SpringProxy是标记接口
           CGlib代理： object instanceof SpringProxy &&  className.contains("$$")  // 大致是这样，标记接口是关键

        第 2 个： Advised 是啥？调试发现是CglibAopProxy的如下静态内部类（大约552行，接近尾部）

         private static class AdvisedDispatcher implements Dispatcher, Serializable {
             private final AdvisedSupport advised;

             public AdvisedDispatcher(AdvisedSupport advised) {
                 this.advised = advised;
             }

             public Object loadObject() throws Exception {
                return this.advised;
             }
         }

         调试拿到的具体信息：装配DataSourceConfig, 下面的内容是一行，被我分隔了
org.springframework.aop.framework.ProxyFactory: 0 interfaces [];

2 advisors [org.springframework.aop.interceptor.ExposeInvocationInterceptor.ADVISOR, InstantiationModelAwarePointcutAdvisor: expression [anyPublicMethod() && @annotation(disconfFileItem)];

advice method [public java.lang.Object
com.baidu.disconf.client.store.aspect.DisconfAspectJ.decideAccess(org.aspectj.lang.ProceedingJoinPoint,com.baidu.disconf.client.common.annotations.DisconfFileItem) throws java.lang.Throwable]; perClauseKind=SINGLETON];
targetSource [SingletonTargetSource for target object [com.pingpongx.channelmgr.dao.disconf.ChannelMgrDataSourceConfig@3bb299ce]];
proxyTargetClass=true; optimize=false; opaque=false; exposeProxy=false; frozen=false

         第 3 个： 这些方法的调用时机？ 调试后发现，在输入如下日志之后就到了这里，也就是secondScan之前

10:08:49 [DEBUG]-[localhost-startStop-1]-- Returning cached instance of singleton bean 'org.springframework.transaction.config.internalTransactionAdvisor'
10:08:49 [DEBUG]-[localhost-startStop-1]-- Finished creating instance of bean 'channelSerialTypeController'
10:08:49 [DEBUG]-[localhost-startStop-1]-- Returning cached instance of singleton bean 'disconfMgrBean'
10:08:49 [DEBUG]-[localhost-startStop-1]-- Creating shared instance of singleton bean 'disconfMgrBeanSecond'
10:08:49 [DEBUG]-[localhost-startStop-1]-- Creating instance of bean 'disconfMgrBeanSecond'
10:08:49 [DEBUG]-[localhost-startStop-1]-- Returning cached instance of singleton bean 'org.springframework.transaction.config.internalTransactionAdvisor'
10:08:49 [DEBUG]-[localhost-startStop-1]-- Eagerly caching bean 'disconfMgrBeanSecond' to allow for resolving potential circular references
10:08:49 [DEBUG]-[localhost-startStop-1]-- Invoking init method  'init' on bean with name 'disconfMgrBeanSecond'
10:08:49 [ INFO]-[localhost-startStop-1]-- ******************************* DISCONF START SECOND SCAN *******************************
10:08:49 [DEBUG]-[localhost-startStop-1]-- ==============	start to inject value to disconf file item instance: db.properties	=============================

         */
    }
}
