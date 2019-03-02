package com.github.doublebin.springfox.bridge.demo.service;

import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeModelProperty;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;
import com.github.doublebin.springfox.bridge.demo.model.TestRequest1;
import org.springframework.stereotype.Service;

@Service
@BridgeApi(value = "TestService3 Apis", description = "测试服务3")
@BridgeGroup("test-group1")
public class TestService3 {
    @BridgeOperation(value = "测试查询", notes = "测试查询方法说明")
    public String testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest1 request){
        return "Test query success, id is " + id;
    }
}
