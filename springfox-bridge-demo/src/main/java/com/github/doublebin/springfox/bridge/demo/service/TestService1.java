package com.github.doublebin.springfox.bridge.demo.service;

import com.github.doublebin.springfox.bridge.demo.model.TestRequest1;
import com.github.doublebin.springfox.bridge.demo.model.TestRequest2;
import org.springframework.stereotype.Service;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeModelProperty;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;

@Service
@BridgeApi(value = "TestService1 Apis", description = "测试服务1")
@BridgeGroup("test-group1")
public class TestService1 {

    @BridgeOperation(value = "测试查询1", notes = "测试查询方法1说明")
    public String testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest1 request){
        return "Test query success, id is " + id;
    }

    @BridgeOperation(value = "测试查询2", notes = "测试查询方法2说明")
    public String testQuery(@BridgeModelProperty(value = "用户id", required = true) long id){
        return "Test query success, id is " + id;
    }

    @BridgeOperation(value = "测试查询3", notes = "测试查询方法3说明")
    public String testQuery(){
        return "Test query success.";
    }

    @BridgeOperation(value = "测试查询4", notes = "测试查询方法4说明")
    public String testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest2 request){
        return "Test query success, id is " + id;
    }

}
