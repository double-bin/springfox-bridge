package com.github.doublebin.springfox.bridge.core.util;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.doublebin.springfox.bridge.core.exception.BridgeException;

public class JsonUtil {
    public static final ObjectMapper MAPPER_NOT_INCLUDE_TYPE = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

    public static final ObjectMapper MAPPER_INCLUDE_TYPE = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);


    public static String writeValueAsString(Object object)
    {
        return writeValueAsString(object, false);
    }

    public static String writeValueAsString(Object object, boolean jsonIncludeType)
    {
        try
        {

            if (jsonIncludeType)
            {
                return MAPPER_INCLUDE_TYPE.writeValueAsString(object);
            }
            else
            {
                return MAPPER_NOT_INCLUDE_TYPE.writeValueAsString(object);
            }
        }
        catch (JsonProcessingException e)
        {
            throw new BridgeException("Write value as string failed.", e);
        }
    }


    public static <T> T readValue(String json, Class<T> clazz)
    {
        return readValue(json, clazz, false);

    }

    public static <T> T readValue(String json, Class<T> clazz, boolean jsonIncludeType)
    {
        try
        {
            if (jsonIncludeType)
            {
                return MAPPER_INCLUDE_TYPE.readValue(json, clazz);
            }
            else
            {
                return MAPPER_NOT_INCLUDE_TYPE.readValue(json, clazz);
            }

        }
        catch (IOException e)
        {
            throw new BridgeException("Read value from json failed.", e);
        }

    }

    public static void copyValue( Object orignalValue, Object[] objectValues) {

        if (null != objectValues && objectValues.length != 0) {

            for (int i= 0; i <objectValues.length; i++ ) {

                if(null == objectValues[i]) {
                    objectValues[i] = orignalValue;
                }

                if (null == orignalValue) {
                    objectValues[i] = null;
                } else {
                    String json = writeValueAsString(orignalValue);
                    objectValues[i] = readValue(json, objectValues[i].getClass());
                }
            }
        }
    }

    public static void copyValue( Object orignalValue, Object[][] objectValues) {

        if (null != objectValues && objectValues.length != 0) {

            for (int i= 0; i <objectValues.length; i++ ) {

                if(null == objectValues[i] || null == orignalValue) {
                    objectValues[i] = null;
                } else {
                    String json = writeValueAsString(orignalValue);
                    objectValues[i] = readValue(json, objectValues[i].getClass());
                }
            }
        }
    }

    public static void copyValue( Object orignalValue, List objectValues) {

        if (null != objectValues && objectValues.size() != 0) {

            for (int i= 0; i <objectValues.size(); i++ ) {

                if(null == objectValues.get(i) || null == orignalValue) {
                    objectValues.set(i, orignalValue);
                } else {
                    String json = writeValueAsString(orignalValue);
                    objectValues.set(i, readValue(json, objectValues.get(i).getClass()));
                }
            }
        }
    }

}
