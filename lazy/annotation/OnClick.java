package com.simple.fastjson.lazy.annotation;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2018/1/2.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@BaseEvent(listenerSetter = "setOnClickListener" , listenerType = View.OnClickListener.class , callBackMethod = "onClick")
public @interface OnClick{
    int[] value();
}
