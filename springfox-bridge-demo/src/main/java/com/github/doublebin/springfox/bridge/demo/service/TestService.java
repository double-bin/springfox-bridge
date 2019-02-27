package com.github.doublebin.springfox.bridge.demo.service;


import com.github.doublebin.springfox.bridge.demo.model.TestRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeModelProperty;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;

@Service
@BridgeApi(value = "TestService Apis", description = "测试服务")
@BridgeGroup("test")
public class TestService {

    @Getter
    @Setter
    private String name;

    @BridgeOperation(value = "测试查询", notes = "这里是一个简单的提示：本方法为一个测试查询方法")
    public String testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求体", required = false) TestRequest request){
        return "Test query success, id is " + id;
    }

    @BridgeOperation(value = "测试查询2", notes = "只需要传入id")
    public String testQuery(@BridgeModelProperty(value = "用户id", required = true) long id){
        return "Test query success, id is " + id;
    }

    @BridgeOperation(value = "测试查询3", notes = "无参数")
    public String testQuery(){
        return "Test query success, id is " + 1111;
    }

    @BridgeOperation(value = "测试查询4", notes = "这里是一个简单的提示：本方法为一个测试查询方法")
    public String testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求service", required = false) TestService service){
        return "Test query success, id is " + id;
    }

    private void add(String request){


    }

    private String get(){
        return null;
    }

}
