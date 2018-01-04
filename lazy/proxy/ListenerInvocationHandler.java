package com.simple.fastjson.lazy.proxy;

import android.content.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/2.
 */

public class ListenerInvocationHandler implements InvocationHandler {

    private Context context;
    private Map<String , Method> methodMap;

    public ListenerInvocationHandler(Context context, Map<String, Method> methodMap) {
        this.context = context;
        this.methodMap = methodMap;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        Method method1 = methodMap.get(name);
        if (method1 != null){
            return method1.invoke(context , args);
        }else {
            return method.invoke(proxy , args);
        }
    }
}
