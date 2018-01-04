package com.simple.fastjson.lazy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2018/1/2.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface BaseEvent {

    /**
     * 设置监听方法(setOnclickListener or setOnItemClickListener)
     * @return
     */
    String listenerSetter();

    /**
     * 设置监听的类型(View.OnclickListener)
     * @return
     */
    Class<?> listenerType();

    /**
     * 设置回调方法(onClick)
     * @return
     */
    String callBackMethod();
}
