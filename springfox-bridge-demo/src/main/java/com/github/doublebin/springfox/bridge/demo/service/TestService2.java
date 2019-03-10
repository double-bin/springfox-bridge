package com.github.doublebin.springfox.bridge.demo.service;

import com.github.doublebin.springfox.bridge.core.builder.BridgeControllerBuilder;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeModelProperty;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;
import com.github.doublebin.springfox.bridge.core.util.ReflectUtil;
import com.github.doublebin.springfox.bridge.demo.model.CommonResponse;
import com.github.doublebin.springfox.bridge.demo.model.TestGeneric;
import com.github.doublebin.springfox.bridge.demo.model.TestGenericSub;
import com.github.doublebin.springfox.bridge.demo.model.TestRequest1;
import com.github.doublebin.springfox.bridge.demo.model.TestResult;
import javassist.CtClass;
import org.springframework.stereotype.Service;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Service
@BridgeApi(value = "TestService2 Apis", description = "测试服务2")
@BridgeGroup("test-group2")
public class TestService2 {
    @BridgeOperation(value = "测试查询", notes = "测试查询方法说明")
    public CommonResponse<TestResult,String> testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest1 request){
        return null;
    }

    @BridgeOperation(value = "测试查询", notes = "测试查询方法说明")
    public TestGenericSub<TestResult,CommonResponse<TestResult,String>> testQuery1(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest1 request){
        return null;
    }

    public static void main(String[] args) throws Exception {

        TestGenericSub<Boolean,String>  sub= new TestGenericSub<Boolean,String>();




        BridgeControllerBuilder.newSubClassFromGenericReturnType(TestService2.class.getDeclaredMethod("testQuery1", long.class, TestRequest1.class));

       /* TestGeneric<String> generic = new TestGeneric<String>("aaa", "bbb");
        TestGenericSub sub1 = new TestGenericSub();


        getSetterMethods(sub1.getClass());


        Field[] fields = CommonResponse.class.getDeclaredFields();
        for (Field field: fields) {
            System.out.println(field);
        }


        Method[] publicMethods = CommonResponse.class.getMethods();
        for (Method publicMethod :publicMethods) {
            Type type = publicMethod.getGenericReturnType();
            System.out.println(publicMethod.getName());
            try {
                ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) type; //强转成具体的实现类
                Type[] genericTypes = parameterizedType.getActualTypeArguments();  //取得包含的泛型类型
            } catch (Exception e) {

            }
        }



        Type type = TestService2.class.getDeclaredMethod("testQuery", long.class, TestRequest1.class).getGenericReturnType();
        try {
            ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) type; //强转成具体的实现类
            Type[] genericTypes = parameterizedType.getActualTypeArguments();  //取得包含的泛型类型
            System.out.println(type);
        } catch (Exception e) {

        }*/
    }


    public static void copyValueWithGSetter(Object newObj, Object oldObj) {
        Class newClass = newObj.getClass();
        Class oldClass = oldObj.getClass();
    }



    //boolean: getter:都以is开头,setter:都以set开头,后面不接Is
    //Boolean: getter:都以get开头,后面不接Is, setter:都以set开头,后面不接Is

    public static List<Method> getSetterMethods(Class clazz) {
        List<Method> methods = new ArrayList<>();
        Method[] publicMethods = clazz.getMethods();


        return methods;
    }

    /**
     *
     * @param newObj
     * @param oldObj
     */
    @Deprecated
    public static void copyDeclaredFieldsValue(Object newObj, Object oldObj) {
        Class newClass = newObj.getClass();
        Class oldClass = oldObj.getClass();
        Field[] fields = newClass.getDeclaredFields();

        for(Field newField:fields) {
            try {
                newField.setAccessible(true);
                Field oldField = oldClass.getDeclaredField(newField.getName());
                oldField.setAccessible(true);
                Object oldValue = oldField.get(oldObj);

                if(oldValue!=null && oldValue.getClass().equals(newField.getType())) {
                    newField.set(newObj, oldValue);
                }
            }catch (Exception e) {
                //
                continue;
            }
        }
    }


    
}
