package com.github.doublebin.utils.springfox.bridge.core.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import com.github.doublebin.utils.springfox.bridge.core.filter.wrapper.BridgeResponseWrapper;
import com.github.doublebin.utils.springfox.bridge.core.util.JsonUtil;
import com.github.doublebin.utils.springfox.bridge.core.util.StringUtil;

/**
 *
 */
@Slf4j
public class Swagger2ControllerFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletResponse httpResp = (HttpServletResponse) response;
        BridgeResponseWrapper wrapper = new BridgeResponseWrapper(httpResp);
        chain.doFilter(request, wrapper);

        byte[] data = wrapper.getResponseData();
        String dataStr = new String(data);
        log.debug("Swagger api-docs : "+ dataStr);

        Map<String, Object> dataMap = JsonUtil.readValue(dataStr, Map.class);
        if(! CollectionUtils.isEmpty(dataMap)){
           Map<String, Object> definitions =(Map<String, Object>) dataMap.get("definitions");
            if(! CollectionUtils.isEmpty(definitions)){
                for(String key: definitions.keySet()){
                   Map<String, Object> defMap =(Map<String, Object>) definitions.get(key);

                        Map<String, Object> oringalMap = (Map<String, Object>)defMap.get("properties");
                        for(String oringalMapKey: oringalMap.keySet()){
                            Map<String, Object> oringalMapValueMap = (Map<String, Object>)oringalMap.get(oringalMapKey);
                            Object description = oringalMapValueMap.get("description");
                            if(null == description){
                                description = "";
                            }

                            if(StringUtil.equals((String)oringalMapValueMap.get("format"), "int64")){
                                oringalMapValueMap.put("description", "[int64]"+description);
                            }else if(StringUtil.equals((String)oringalMapValueMap.get("format"), "int32")){
                                oringalMapValueMap.put("description", "[int32]"+description);
                            }else if(StringUtil.equals((String)oringalMapValueMap.get("format"), "float")){
                                oringalMapValueMap.put("description", "[float]"+description);
                            }else if(StringUtil.equals((String)oringalMapValueMap.get("format"), "double")){
                                oringalMapValueMap.put("description", "[double]"+description);
                            }
                        }

                }
            }
        }

        //dataStr = StringUtil.replace(dataStr, "\"type\":\"integer\",\"format\":\"int64\",\"description\":\"", "\"type" + "\":\"integer\",\"format\":\"int64\",\"description\":\"[int64]");
        response.getWriter().write(JsonUtil.writeValueAsString(dataMap));
    }

    @Override
    public void destroy() {

    }
}
