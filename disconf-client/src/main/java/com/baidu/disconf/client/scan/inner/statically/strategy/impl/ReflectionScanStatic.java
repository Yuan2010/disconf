package com.baidu.disconf.client.scan.inner.statically.strategy.impl;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.disconf.client.common.annotations.DisconfActiveBackupService;
import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfItem;
import com.baidu.disconf.client.common.annotations.DisconfUpdateService;
import com.baidu.disconf.client.common.constants.Constants;
import com.baidu.disconf.client.common.update.IDisconfUpdatePipeline;
import com.baidu.disconf.client.scan.inner.common.ScanVerify;
import com.baidu.disconf.client.scan.inner.statically.model.ScanStaticModel;
import com.baidu.disconf.client.scan.inner.statically.strategy.ScanStaticStrategy;
import com.google.common.base.Predicate;

/**
 * Created by knightliao on 15/1/23.
 * <p/>
 * 扫描静态注解，并且进行分析整合数据
 * <p/>
 * 使用 Reflection Lib
 *
 * 2017-12-08
 */
public class ReflectionScanStatic implements ScanStaticStrategy {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ScanStaticStrategy.class);

    /**
     * 扫描想要的类
     */
    @Override
    public ScanStaticModel scan(List<String> packNameList) {
        ScanStaticModel scanModel = scanBasicInfo(packNameList); // 基本信息
        analysis(scanModel);  // 分析
        return scanModel;
    }

    /**
     * 通过扫描，获取反射对象
     */
    private Reflections getReflection(List<String> packNameList) {
        FilterBuilder filterBuilder = new FilterBuilder().includePackage(Constants.DISCONF_PACK_NAME); // filter
        for (String packName : packNameList) {
            // 第 1 步：pkgName.replace(".", "\\.") + ".*"
            // 第 2 步：编译替换后的包名：Pattern.compile(regex)
            // 第 3 步：添加到：List<Predicate<String>>
            filterBuilder = filterBuilder.includePackage(packName);  // 流式编程(返回this)
        }
        Predicate<String> filter = filterBuilder;                    // FilterBuilder implements Predicate<String>

        Collection<URL> urlTotals = new ArrayList<URL>();            // urls
        for (String packName : packNameList) {
            Set<URL> urls = ClasspathHelper.forPackage(packName);    // 点进去学到：ContextClassLoader与StaticClassLoader的区别，为啥有这样的区别。 web项目调试时发现只有一个WebAppClassLoader。 还有URL的解析也值得学习
            urlTotals.addAll(urls);
        }
        Reflections reflections = new Reflections(new ConfigurationBuilder().filterInputsBy(filter).setScanners(  // 变长参数，众多Scanner
                        new SubTypesScanner().filterResultsBy(filter),
                        new TypeAnnotationsScanner().filterResultsBy(filter),
                        new FieldAnnotationsScanner().filterResultsBy(filter),
                        new MethodAnnotationsScanner().filterResultsBy(filter),
                        new MethodParameterScanner()).setUrls(urlTotals));
        return reflections;
    }

    private void analysis(ScanStaticModel scanModel) {
        analysis4DisconfFile(scanModel);  // 分析出配置文件MAP
    }

    /**
     * 分析出配置文件与配置文件中的Field的Method的MAP
     */
    private void analysis4DisconfFile(ScanStaticModel scanModel) {
        Map<Class<?>, Set<Method>> disconfFileItemMap = new HashMap<Class<?>, Set<Method>>();
        Set<Class<?>> classdata = scanModel.getDisconfFileClassSet();  // 配置文件MAP
        for (Class<?> classFile : classdata) {
            disconfFileItemMap.put(classFile, new HashSet<Method>());
        }

        Set<Method> af1 = scanModel.getDisconfFileItemMethodSet();    // 将配置文件与配置文件中的配置项进行关联
        for (Method method : af1) {
            Class<?> thisClass = method.getDeclaringClass();
            if (disconfFileItemMap.containsKey(thisClass)) {
                Set<Method> fieldSet = disconfFileItemMap.get(thisClass);
                fieldSet.add(method);
                disconfFileItemMap.put(thisClass, fieldSet);
            } else {
                LOGGER.error("cannot find CLASS ANNOTATION " + DisconfFile.class.getName() + " for disconf file item: " + method.toString());
            }
        }

        // 最后的校验
        Iterator<Class<?>> iterator = disconfFileItemMap.keySet().iterator();
        while (iterator.hasNext()) {
            Class<?> classFile = iterator.next();
            // 校验是否所有配置文件都含有配置
            if (disconfFileItemMap.get(classFile).isEmpty()) {
                LOGGER.info("disconf file hasn't any items: " + classFile.getName());
                continue;
            }
            // 校验配置文件类型是否合适(目前只支持.properties类型)
            DisconfFile disconfFile = classFile.getAnnotation(DisconfFile.class);
            boolean fileTypeRight = ScanVerify.isDisconfFileTypeRight(disconfFile);
            if (!fileTypeRight) {
                LOGGER.warn("do not support this file type" + disconfFile.toString());
                continue;
            }
        }
        // 设置
        scanModel.setDisconfFileItemMap(disconfFileItemMap);
    }

    /**
     * 扫描基本信息
     */
    private ScanStaticModel scanBasicInfo(List<String> packNameList) {
        ScanStaticModel scanModel = new ScanStaticModel();
        Reflections reflections = getReflection(packNameList);  // 扫描对象
        scanModel.setReflections(reflections);
        // 下面都是通过ClassLoader中查找，核心代码在下边
        scanModel.setDisconfFileClassSet(reflections.getTypesAnnotatedWith(DisconfFile.class));
        scanModel.setDisconfFileItemMethodSet(reflections.getMethodsAnnotatedWith(DisconfFileItem.class));
        scanModel.setDisconfItemMethodSet(reflections.getMethodsAnnotatedWith(DisconfItem.class));
        scanModel.setDisconfActiveBackupServiceClassSet(reflections.getTypesAnnotatedWith(DisconfActiveBackupService.class));
        scanModel.setDisconfUpdateService(reflections.getTypesAnnotatedWith(DisconfUpdateService.class));
        Set<Class<? extends IDisconfUpdatePipeline>> pipeline = reflections.getSubTypesOf(IDisconfUpdatePipeline.class);
        if (pipeline != null && pipeline.size() != 0) {
            scanModel.setiDisconfUpdatePipeline((Class<IDisconfUpdatePipeline>) pipeline.toArray()[0]);
        }
        return scanModel;
    }

    /*

    // 因为没有传入ClassLoader，所以这里边经过调试结果如下：classLoader是WebAppClassLoader, type是由@DisconfFile(filename = "demo.properties")标记的类的全称
    for (ClassLoader classLoader : ClasspathHelper.classLoaders(classLoaders)) {
          if (type.contains("[")) {
              try {
                    return Class.forName(type, false, classLoader);
              } catch (Throwable ex) { /*continue/ }
          }
          try {
                return classLoader.loadClass(type);  // 调试时，执行了这里
          } catch (Throwable e) { /*continue/ }
     }
     throw new ReflectionsException("could not get type for name " + typeName);

     后来写一个普通的Java程序调试发现classLoader是AppClassLoader

     */
}