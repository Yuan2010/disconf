package com.baidu.disconf.client.support.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import com.baidu.disconf.client.common.annotations.DisconfItem;
import com.baidu.disconf.core.common.constants.DisConfigTypeEnum;

/**
 * @author liaoqiqi
 * @version 2014-9-9
 *
 * 2018-01-06
 */
public class MethodUtils {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MethodUtils.class);

    /**
     * 对于一个 get/is 方法，返回其相对应的Field
     */
    public static Field getFieldFromMethod(Method method, Field[] expectedFields, DisConfigTypeEnum disConfigTypeEnum) {

        String fieldName;
        if (disConfigTypeEnum.equals(DisConfigTypeEnum.FILE)) {
            fieldName = method.getAnnotation(DisconfFileItem.class).associateField();  // 根据用户设定的注解来获取
        } else {
            fieldName = method.getAnnotation(DisconfItem.class).associateField();      // 根据用户设定的注解来获取
        }

        // 如果用户未设定注解，则猜其名字
        if (StringUtils.isEmpty(fieldName)) {
            fieldName = ClassUtils.getFieldNameByGetMethodName(method.getName());     // 从方法名 获取其 Field 名
        }

        // 确认此Field名是正确的
        for (Field field : expectedFields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }

        LOGGER.error(method.toString() + " cannot get its related field name. ");
        return null;
    }

    public static Method getSetterMethodFromField(Class<?> curClass, Field field) {
        String fieldName = field.getName().toLowerCase();
        Set<Method> methods = ClassUtils.getAllMethod(curClass);  // 获取一个类的所有方法（包括所有父类的，Object除外）
        for (Method method : methods) {
            if (method.getName().toLowerCase().equals("set" + fieldName)) {
                return method;
            }
        }
        return null;
    }
}
