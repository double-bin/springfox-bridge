package com.github.doublebin.springfox.bridge.core.util;

import java.io.IOException;

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

}
