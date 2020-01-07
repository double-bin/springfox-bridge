package com.github.doublebin.springfox.bridge.demo.service;

import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeModelProperty;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;
import com.github.doublebin.springfox.bridge.demo.model.CommonResponse;
import com.github.doublebin.springfox.bridge.demo.model.TestGeneric;
import com.github.doublebin.springfox.bridge.demo.model.TestRequest1;
import com.github.doublebin.springfox.bridge.demo.model.TestResult;
import org.springframework.stereotype.Service;

@Service
//@BridgeApi(value = "TestService2 Apis", description = "测试服务2")
@BridgeGroup("test-group2")
public class TestService2 {
    @BridgeOperation(value = "测试查询", notes = "返回泛型测试")
    public CommonResponse<TestResult,String> testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest1 request){
        return new CommonResponse<TestResult,String>(true, new TestResult("doublebin", 27), "hehehe", "0","success");
    }

    @BridgeOperation(value = "测试查询", notes = "返回泛型测试")
    public TestGeneric<TestResult,String,String> testQuery1(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest1 request){

        CommonResponse resp = new CommonResponse<TestResult,String>(true, new TestResult("doublebin", 27), "hehehe", "0","success");
        return new TestGeneric<TestResult,String,String>(new TestResult("doublebin", 27), "double", "desc", resp);
    }

}
