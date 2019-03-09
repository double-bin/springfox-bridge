package com.github.doublebin.springfox.bridge.demo;

import java.util.List;

import com.github.doublebin.springfox.bridge.core.util.JsonUtil;
import com.github.doublebin.springfox.bridge.demo.model.CommonResponse;
import com.github.doublebin.springfox.bridge.demo.model.TestGeneric;
import com.github.doublebin.springfox.bridge.demo.model.TestGenericSub;
import com.github.doublebin.springfox.bridge.demo.model.TestRequest1;
import com.github.doublebin.springfox.bridge.demo.model.TestResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by juyan on 2019/3/6.
 */
@Api(description = "测试接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    @ApiOperation(value = "查询")
    public TestGenericSub<TestResult,String> query(){

        return null;
    }
}
