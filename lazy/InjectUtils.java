package com.simple.fastjson.lazy;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.simple.fastjson.lazy.annotation.BaseEvent;
import com.simple.fastjson.lazy.annotation.BindView;
import com.simple.fastjson.lazy.annotation.ContentView;
import com.simple.fastjson.lazy.proxy.ListenerInvocationHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/12/29.
 */

public class InjectUtils {

    private final static String TAG = "InjectUtils";

    public static void inject(Context context){
        injectLayout(context);
        injectView(context);
        injectEvent(context);
    }

    private static void injectEvent(Context context) {
        Class<?> aClass = context.getClass();
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation myAnnotation: annotations) {
                Class<?> annotationType = myAnnotation.annotationType();
                BaseEvent annotation = annotationType.getAnnotation(BaseEvent.class);
                if (annotation == null){
                    continue;
                }
                Log.e(TAG, "injectEvent: "+method.getName());
                String listenerSetter = annotation.listenerSetter();
                Class<?> listenerType = annotation.listenerType();
                String callBackMethod = annotation.callBackMethod();

                Map<String , Method> methodMap = new HashMap<>();
                methodMap.put(callBackMethod , method);

                try {
                    Method valueMethod = annotationType.getDeclaredMethod("value");
                    int value[] = (int[]) valueMethod.invoke(myAnnotation);
                    for (int i : value) {
                        Method findViewById = aClass.getMethod("findViewById", int.class);
                        View view = (View) findViewById.invoke(context , i);
                        if (view == null){
                            continue;
                        }
                        Method setOnEventListener = view.getClass().getMethod(listenerSetter, listenerType);
                        ListenerInvocationHandler handler = new ListenerInvocationHandler(context , methodMap);
                        Object proxy = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class[]{listenerType}, handler);
                        setOnEventListener.invoke(view , proxy);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void injectView(Context context) {
        Class<? extends Context> aClass = context.getClass();
        Field[] fields = aClass.getDeclaredFields();
        for (Field field : fields) {
            BindView annotation = field.getAnnotation(BindView.class);
            if (annotation != null){
                int viewId = annotation.value();
                try {
                    Method findViewById = aClass.getMethod("findViewById", int.class);
                    View view = (View) findViewById.invoke(context , viewId);
                    field.setAccessible(true);
                    field.set(context , view);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void injectLayout(Context context) {
        Class<?> aClass = context.getClass();
        Log.e(TAG, "injectLayout>aClass:"+aClass);
        Log.e(TAG, "injectLayout>context:"+context);
        ContentView annotation = aClass.getAnnotation(ContentView.class);
        if (annotation != null){
            int layoutId = annotation.value();
            try {
                Method setContentView = aClass.getMethod("setContentView", int.class);
                setContentView.invoke(context , layoutId);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
