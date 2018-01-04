package com.simple.fastjson.lazy.annotation;

import android.widget.AdapterView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2018/1/2.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@BaseEvent(listenerSetter = "setOnItemClickListener" , listenerType = AdapterView.OnItemClickListener.class , callBackMethod = "onItemClick")
public @interface OnItemClick {
    int[] value();
}
