package com.baidu.disconf.client;

/**
 * 第二次扫描，动态扫描
 *
 * @author liaoqiqi
 * @version 2014-6-18
 *
 * 2017-12-07
 *
 */
public class DisconfMgrBeanSecond {
    public void init() {
        DisconfMgr.getInstance().secondScan();
    }

    public void destroy() {
        DisconfMgr.getInstance().close();
    }
}


/*       一个完整的spring-disconf.xml配置如下

        <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:p="http://www.springframework.org/schema/p"
            xmlns:context="http://www.springframework.org/schema/context" xmlns:aop="http://www.springframework.org/schema/aop"
            xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
            http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
            http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">

    <context:component-scan base-package="com.example.demo"/>
    <aop:aspectj-autoproxy proxy-target-class="true"/>

    <bean id="disconfMgrBean" class="com.baidu.disconf.client.DisconfMgrBean" destroy-method="destroy" p:scanPackage="om.example.demo"/>
    <bean id="disconfMgrBeanSecond" class="com.baidu.disconf.client.DisconfMgrBeanSecond" init-method="init" destroy-method="destroy"/>

    <!-- Auto Reload -->
    <bean id="reloadingProperties" class="com.baidu.disconf.client.addons.properties.ReloadablePropertiesFactoryBean">
        <property name="locations">
            <list>
                 <value>classpath*:mysql.properties</value>
            </list>
        </property>
    </bean>
    <bean id="reloadingPropertyPlaceholderConfigurer" class="com.baidu.disconf.client.addons.properties.ReloadingPropertyPlaceholderConfigurer">
        <property name="ignoreResourceNotFound" value="true"/>
        <property name="ignoreUnresolvablePlaceholders" value="true"/>
        <property name="propertiesArray">
            <list>
                <ref bean="reloadingProperties"/>
            </list>
        </property>
    </bean>

    <!-- No Reloading-->
    <!--
    <bean id="noReloadingProperties" class="com.baidu.disconf.client.addons.properties.ReloadablePropertiesFactoryBean">
        <property name="locations">
            <list>
                <value>classpath*:mysql.properties</value>
            </list>
        </property>
    </bean>

    <bean id="placeholderConfigurer"class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer"
            p:ignoreResourceNotFound="false"
            p:ignoreUnresolvablePlaceholders="false"
            p:propertiesArray-ref="noReloadingProperties"/>

    -->
    </beans>

 */