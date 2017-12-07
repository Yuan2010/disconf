package com.baidu.disconf.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.baidu.disconf.client.store.aspect.DisconfAspectJ;
import com.baidu.disconf.client.store.inner.DisconfCenterHostFilesStore;
import com.baidu.disconf.client.support.utils.StringUtil;

/**
 * 第一次扫描，静态扫描
 *
 * @author liaoqiqi
 * @version 2014-6-17
 *
 * 2017-12-07  依赖disconf时在spring-disconf.xml中配置这个类了
 *
 * spring-disconf.xml的完全版在：DisconfMgrBeanSecond类中
 *
 */
public class DisconfMgrBean implements BeanDefinitionRegistryPostProcessor, PriorityOrdered, ApplicationContextAware {

    public final static String SCAN_SPLIT_TOKEN = ",";
    private ApplicationContext applicationContext;
    private String scanPackage = null;

    public void destroy() {
        DisconfMgr.getInstance().close();
    }

    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

    /**
     * 这个函数无法达到最高优先级，例如PropertyPlaceholderConfigurer
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * 第一次扫描<br/>
     * 在Spring内部的Bean定义初始化后执行，这样是最高优先级的。   yuanhy 认真阅读本类所实现接口的描述和层次
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 为了做兼容
        DisconfCenterHostFilesStore.getInstance().addJustHostFileSet(fileList); // 仅对HashSet进行get/set ，但有个JVM保证线程安全的类级内部类

        List<String> scanPackList = StringUtil.parseStringToStringList(scanPackage, SCAN_SPLIT_TOKEN); // 应该在这个方法中去重
        // unique（去重）
        Set<String> hs = new HashSet<String>();
        hs.addAll(scanPackList);
        scanPackList.clear();
        scanPackList.addAll(hs);
        // 扫描
        DisconfMgr.getInstance().setApplicationContext(applicationContext);
        DisconfMgr.getInstance().firstScan(scanPackList);
        // register java bean
        registerAspect(registry);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * register aspectJ for disconf get request
     *
     * @param registry
     */
    private void registerAspect(BeanDefinitionRegistry registry) {
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DisconfAspectJ.class);
        beanDefinition.setLazyInit(false);
        beanDefinition.setAbstract(false);
        beanDefinition.setAutowireCandidate(true);
        beanDefinition.setScope("singleton");
        registry.registerBeanDefinition("disconfAspectJ", beanDefinition);
    }

    /*
     * 已经废弃了，不推荐使用
     */
    @Deprecated
    private Set<String> fileList = new HashSet<String>();

    @Deprecated
    public Set<String> getFileList() {
        return fileList;
    }

    @Deprecated
    public void setFileList(Set<String> fileList) {
        this.fileList = fileList;
    }
}
