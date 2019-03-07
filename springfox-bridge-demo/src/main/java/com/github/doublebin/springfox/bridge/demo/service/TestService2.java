package com.github.doublebin.springfox.bridge.demo.service;

import com.github.doublebin.springfox.bridge.core.builder.BridgeControllerBuilder;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeModelProperty;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;
import com.github.doublebin.springfox.bridge.core.util.ReflectUtil;
import com.github.doublebin.springfox.bridge.demo.model.CommonResponse;
import com.github.doublebin.springfox.bridge.demo.model.TestRequest1;
import com.github.doublebin.springfox.bridge.demo.model.TestResult;
import javassist.CtClass;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

@Service
@BridgeApi(value = "TestService2 Apis", description = "测试服务2")
@BridgeGroup("test-group2")
public class TestService2 {
    @BridgeOperation(value = "测试查询", notes = "测试查询方法说明")
    public CommonResponse<String, TestResult> testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest1 request){
        return null;
    }

    public static void main(String[] args) throws Exception {
        Method[] methods = TestService2.class.getMethods();
        for (Method method: methods) {
            if (method.getName().equals("testQuery")){
                Type type= method.getGenericReturnType();
                System.out.println(method.getName());
                ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) type; //强转成具体的实现类
                Type[] genericTypes = parameterizedType.getActualTypeArguments();  //取得包含的泛型类型

                CtClass newCtClass = BridgeControllerBuilder.newSubCtClassFromGenericReturnType(method);
                Object o = newCtClass.toClass().newInstance();

                System.out.println(parameterizedType.getTypeName());





            }
        }
    }
}
