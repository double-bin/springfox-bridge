package com.github.doublebin.springfox.bridge.demo.service;

import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeModelProperty;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;
import com.github.doublebin.springfox.bridge.core.util.JsonUtil;
import com.github.doublebin.springfox.bridge.core.util.ReflectUtil;
import com.github.doublebin.springfox.bridge.demo.model.CommonResponse;
import com.github.doublebin.springfox.bridge.demo.model.TestRequest1;
import com.github.doublebin.springfox.bridge.demo.model.TestResult;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Service
@BridgeApi(value = "TestService4 Apis", description = "测试服务4")
@BridgeGroup("test-group4")
public class TestService4 {


    @BridgeOperation(value = "测试查询LONG", notes = "返回泛型测试")
    public List<Long> testQueryLong( @BridgeModelProperty("年龄")int age) {
        return Arrays.asList(1L);
    }

    @BridgeOperation(value = "测试查询LONG1", notes = "返回泛型测试")
    public List<Long> testQueryLong1( @RequestBody()int age) {
        return Arrays.asList(1L);
    }

    @BridgeOperation(value = "测试查询LONG11", notes = "返回泛型测试")
    public Long testQueryLong11( @RequestBody()int age) {
        return 1L;
    }

    @BridgeOperation(value = "测试查询Void", notes = "返回泛型测试")
    public void testQueryLongVoid( @RequestBody()int age) {
        return ;
    }


    @BridgeOperation(value = "测试查询Void", notes = "返回泛型测试")
    public void testQueryLongVoid( @RequestBody() CommonResponse<TestResult, String> request) {
        return ;
    }

    @BridgeOperation(value = "测试查询Void1", notes = "返回泛型测试")
    public void testQueryLongVoid( CommonResponse<TestResult, String> request, Long id) {
        return ;
    }


    @BridgeOperation(value = "测试查询", notes = "返回泛型测试")
    public CommonResponse<TestResult, String>  testQuery(@BridgeModelProperty("请求体") CommonResponse<TestResult, String> request, @BridgeModelProperty("名称")String name,@BridgeModelProperty("id") List<Long> id, @BridgeModelProperty("年龄")int age) {
        return request;
    }

    @BridgeOperation(value = "测试查询1", notes = "返回泛型测试1")
    public CommonResponse<TestResult, String>  testQuery() {
        return new CommonResponse<TestResult, String>();
    }

    @BridgeOperation(value = "测试查询2", notes = "返回泛型测试1")
    public CommonResponse<TestResult, String> testQuery(@RequestBody CommonResponse<TestResult, String> request) {
        return request;
    }

    @BridgeOperation(value = "测试查询3", notes = "返回泛型测试1")
    public CommonResponse testQuery(@BridgeModelProperty("id") List<Long> ids, @BridgeModelProperty("年龄")int age) {
        return new CommonResponse<TestResult, String>();
    }

    @BridgeOperation(value = "测试查询4", notes = "返回泛型测试1")
    public List<Long> testQuery1(@BridgeModelProperty("id") List<Long> ids, @BridgeModelProperty("年龄")int age) {
        return Arrays.asList(1L, 2L);
    }

    @BridgeOperation(value = "测试查询5", notes = "返回泛型测试1")
    public CommonResponse<TestResult, String> testQuery5(@RequestBody List<CommonResponse<TestResult, String>> requests) throws Exception{
        return requests.get(0);
    }

    @BridgeOperation(value = "测试查询55", notes = "返回泛型测试1")
    public CommonResponse<TestResult, String> testQuery55(List<CommonResponse<TestResult, String>> requests) {
        return requests.get(0);
    }

    @BridgeOperation(value = "测试查询555", notes = "返回泛型测试1")
    public CommonResponse testQuery555(List<CommonResponse> requests) {
        return requests.get(0);
    }

    @BridgeOperation(value = "测试查询6", notes = "返回泛型测试1")
    public List<CommonResponse<TestResult, String>> testQuery1(@RequestBody CommonResponse<TestResult, String> request) {
        return Arrays.asList(request);
    }


    public static void main(String[] args) throws NoSuchMethodException {
        Method[] methods = TestService4.class.getMethods();
        for (Method method : methods) {
            System.out.println(method.getName());
        }

        Method testMethod = TestService4.class.getMethod("testQuery", CommonResponse.class);
        Parameter[] parameters = testMethod.getParameters();
        String className = null;
        for (Parameter parameter: parameters) {
            className =  parameter.getType().getName();
        }
        Method testMethod1 = TestService4.class.getMethod("testQuery", ReflectUtil.getClassByName(className));
        System.out.println("end");


        TestService4 testService4 = new TestService4();
        Method testMethod2 = testService4.getClass().getMethod(testMethod1.getName());

        Object ids = new ArrayList<Long>();

        List idds = (List)ids;
        testService4.testQuery(idds,1);


        Method testMethod5 = TestService4.class.getMethod("testQuery5", List.class);

        List<CommonResponse<TestResult, String>> requests = new ArrayList<>();
        CommonResponse<TestResult, String> request = new CommonResponse<TestResult, String>();
        requests.add(request);

        String jsonStr = JsonUtil.writeValueAsString(requests);
        List<CommonResponse<TestResult, String>> requestsss=  JsonUtil.readValue(jsonStr, testMethod5.getParameters()[0].getParameterizedType());

        System.out.println(requestsss);
    }

}
