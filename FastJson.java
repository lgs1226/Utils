package com.simple.fastjson;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2017/12/26.
 */

public class FastJson {

    private final static int JSON_TYPE_ARRAY = 1;
    private final static int JSON_TYPE_OBJECT = 2;
    private final static int JSON_TYPE_ERROR = 3;

    private final static String TAG = "FastJson";

    /**
     * 解析Json字符串
     * @param json
     * @param clazz
     * @return
     */
    public static Object parseObject(String json , Class clazz) {
        Object object = null;
        Class<?> jsonClazz = null;
        if (json.charAt(0) == '[') {
            //数组类型
            try {
                object = toList(json , clazz);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if (json.charAt(0) == '{') {
            //对象类型
            try {
                JSONObject jsonObject = new JSONObject(json);
                object = clazz.newInstance();
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()){
                    String key = keys.next();
                    Object fieldValue = null;
                    List<Field> allFields = getAllFields(clazz, null);
                    for (Field field:allFields) {
                        if (field.getName().equalsIgnoreCase(key)){
                            field.setAccessible(true);
                            //得到 key所对应的值   值 可以基本类型  类类型
                            fieldValue=getFieldValue(field,jsonObject,key);
                            if (fieldValue != null){
                                field.set(object , fieldValue);
                            }
                            field.setAccessible(false);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    /**
     * 将对象转换成Json字符串
     * @param obj
     * @return
     */
    public static String toJson(Object obj){
        StringBuilder strBuild = new StringBuilder();
        if (obj instanceof List<?>){
            List list = (List) obj;
            strBuild.append("[");
            for (int i = 0; i < list.size(); i++) {
                //解析成JSONObject类型  {"name":"lisi"}
                //把JsonObject类型全部抽取出成一个方法  方便递归调用
                addObjectToJson(strBuild,list.get(i));
                strBuild.append(",");
            }
            strBuild.deleteCharAt(strBuild.length() - 1);
            strBuild.append("]");
        }
        else {
            addObjectToJson(strBuild , obj);
        }
        return strBuild.toString();
    }

    private static void addObjectToJson(StringBuilder strB , Object o){
        strB.append("{");
        List<Field> allFields = getAllFields(o.getClass(), null);
        for (int i = 0; i < allFields.size(); i++) {
            Method method = null;
            //反射拿到变量的值
            Object filedValue=null;
            Field field = allFields.get(i);
            Log.i(TAG, "field: "+field);
            String fieldName = field.getName();
            Log.i(TAG, "fieldName: "+fieldName);
            //拼接时拿到反射的Method对象
            String methodName="get"+((char)(fieldName.charAt(0)-32))+fieldName.substring(1);
            Log.e(TAG, "methodName: "+methodName);
            try {
                method=o.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e) {
                methodName="is"+((char)(fieldName.charAt(0)-32))+fieldName.substring(1);

                try {
                    method=o.getClass().getMethod(methodName);
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
            if (method != null){
                try {
                    filedValue=method.invoke(o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (filedValue != null){
                strB.append("\"");
                strB.append(fieldName);
                strB.append("\":");
                if(filedValue instanceof
                        Integer||filedValue instanceof Long||
                        filedValue instanceof Double||
                        filedValue instanceof  Boolean)
                {
                    strB.append(filedValue);
                }else if(filedValue instanceof String)
                {
                    strB.append("\"");
                    strB.append(filedValue.toString());
                    strB.append("\"");
                }else if(filedValue instanceof List<?>)
                {
                    //集合类型
                    addListToBuffer(strB,filedValue);
                }else if (filedValue instanceof Object){
                    addObjectToJson(strB , filedValue);
                }
                strB.append(",");
            }
        }
        strB.deleteCharAt(strB.length()-1);
        strB.append("}");
    }

    private static void addListToBuffer(StringBuilder jsonBuffer, Object filedValue) {
        jsonBuffer.append("[");
        List<?> list= (List<?>) filedValue;
        for (int i=0;i<list.size();i++)
        {
            addObjectToJson(jsonBuffer,list.get(i));
            if(i<list.size()-1)
            {
                jsonBuffer.append(",");
            }
        }
        jsonBuffer.append("]");
    }

    private static Object getFieldValue(Field field , JSONObject jsonObj , String key) throws JSONException {
        Object fieldObject = null;
        //得到当前成员变量的类型
        Class<?> type = field.getType();
        Log.e(TAG, "getFieldValue1: "+type);
        switch (type.getSimpleName().toString()){
            case "int":
            case "Integer":
                fieldObject = jsonObj.getInt(key);
                break;
            case "String":
                fieldObject = jsonObj.getString(key);
                break;
            case "double":
                fieldObject = jsonObj.getDouble(key);
                break;
            case "boolean":
                fieldObject = jsonObj.getBoolean(key);
                break;
            case "long":
                fieldObject = jsonObj.getLong(key);
                break;
            default:
                //判断集合类型 和对象类型 jsonValue 代表完整的json字符串  里面一层
                String str = jsonObj.getString(key);
                switch (getJsonType(str)){
                    case JSON_TYPE_ARRAY:
                        Type genericType = field.getGenericType();
                        Log.e("==========" , type+"");
                        if (genericType instanceof ParameterizedType){
                            ParameterizedType parameterizedType = (ParameterizedType) genericType;
                            //List 当前类 所实现的泛型  User
                            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                            for (Type type1:actualTypeArguments) {
                                Log.e(TAG, "getFieldValue2: "+type1);
                                //fieldArgClass  代表着User.class
                                Class<?> fieldArgClass= (Class<?>) type;
                                fieldObject=toList(str,fieldArgClass);
                            }
                        }
                        break;
                    case JSON_TYPE_OBJECT:
                        fieldObject = parseObject(str , type);
                        break;
                    case JSON_TYPE_ERROR:
                        break;
                }
                break;
        }
        return fieldObject;
    }

    /**
     * 将当前的类类型的成员变量转换成list集合中
     * @param clazz
     * @param fields
     * @return
     */
    private static List<Field> getAllFields(Class<?> clazz , List<Field> fields){
        if (fields == null){
            fields = new ArrayList<>();
        }
        //递归时排除Object类型
        if (clazz.getSuperclass() != null){
            Field[] fieldsSelf = clazz.getDeclaredFields();
            for (Field field : fieldsSelf) {
                //排除final修饰的成员变量
                if (!Modifier.isFinal(field.getModifiers()) && !field.isSynthetic()){
                    fields.add(field);
                }
            }
            //当前类型遍历完成之后 开始遍历父类型成员变量
            getAllFields(clazz.getSuperclass(),fields);
        }
        return fields;
    }

    /**
     * 解析JsonArray
     * @param json
     * @param clazz
     * @return
     */
    private static Object toList(String json , Class clazz) throws JSONException {
        List<Object> list = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            String jsonStr = jsonArray.getJSONObject(i).toString();
            switch (getJsonType(jsonStr)){
                case JSON_TYPE_ARRAY:
                    List<?> infoList = (List<?>) toList(jsonStr , clazz);
                    list.add(infoList);
                    break;
                case JSON_TYPE_OBJECT:
                    list.add(parseObject(json , clazz));
                    break;
            }
        }
        return list;
    }

    private static int getJsonType(String json){
        char firstChar = json.charAt(0);
        if (firstChar == '['){
            return JSON_TYPE_ARRAY;
        }
        else if (firstChar == '{'){
            return JSON_TYPE_OBJECT;
        }
        else {
            return JSON_TYPE_ERROR;
        }
    }

}
