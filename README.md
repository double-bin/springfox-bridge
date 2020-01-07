# springfox-bridge
A plugin to create swagger documents for no restful apis.

### 一、引言

&nbsp;&nbsp;&nbsp;&nbsp;目前，利用swagger框架为restful接口编写API文档非常流行，在spring web项目中，利用springfox+swagger更是可以通过注解的方式直接进行API文档的生成，这样开发者在项目开发的同时就直接把文档准备好了，利用springfox的配置，可以在项目启动后直接浏览器访问查看API文档，同时还能在界面直接进行API的测试。springfox的使用本文不在此赘述了，现在引出一个问题: **非restful接口能否采用swagger生成接口文档**？

&nbsp;&nbsp;&nbsp;&nbsp;在项目中集成**springfox-bridge**可以快速的为非restful接口生成API文档，编写文档的方式跟springfox一样简单，在相关类或者接口上采用注解的方式定义文档信息即可。

&nbsp;&nbsp;&nbsp;&nbsp;**springfox-bridge相当于架设了一座与springfox之间的桥梁，通过动态生成配置了springfox注解的mvc接口并进行注册，形成对非restful接口生成swagger文档的能力。**

### 二、springfox-bridge特性说明
1. **启动简单**
- 在springboot项目中，集成springfox-bridge-spring-boot-starter即可自动启动；
- 在非springboot项目中，通过实现ApplicationContextAware接口，通过SpringfoxBridge.start(ApplicationContext context)方法,并配置@EnableSwagger2注解即可快速启动。

2. **兼容性强**
- 与协议无关，不挑协议，无论你是使用dubbo、ServiceComb还是其它种种，只要项目本身启用了springmvc, 相应的接口注册了spring bean, 就能像使用springfox那样使用springfox-brige，用注解的方式为接口生成文档。
- 更进一步的讲，只要满足上述条件的spring bean, 即使不是controller层的接口，也能使用springfox-bridge进行文档生成。

3. **简单的注解**
- springfox-bridge提供了几个简单的注解供开发使用，注解的使用方式与springfox的类似，主要在类/接口、方法上进行文档的定义。

4. **方便的分组**
- 采用@BridgeGroup注解可以方便的为项目的接口文档进行分组，而无需手动的配置Docket，springfox-bridge自动按照@BridgeGroup的注解值将文档进行分组归类。

5. **不影响原有文档**
- springfox-bridge通过分组隔离，项目中原先使用springfox为restful接口生成的文档，不会受到springfox-bridge的影响

6. **方法入参不限定请求体的数量**
- 原生springfox对restful请求生成文档，而restful只支持一个请求体入参（用@RequestBody注解标识）。springfox-bridge没有这个限制。

7. **支持界面测试**
- 跟springfox生成文档可以通过界面直接调用一样，springfox-bridge同样支持


### 三、使用说明
>使用springfox-bridge需要项目本身启用了springmvc框架， spring相关依赖版本在spring3.1以上

#### 3.1 配置maven依赖
1）使用了springboot的项目：
```
        <dependency>
            <groupId>com.github.double-bin</groupId>
            <artifactId>springfox-bridge-spring-boot-starter</artifactId>
            <version>1.0.8</version>
        </dependency>
```
2) 非springboot项目：
```
        <dependency>
            <groupId>com.github.double-bin</groupId>
            <artifactId>springfox-bridge-core</artifactId>
            <version>1.0.8</version>
        </dependency>
```

#### 3.2 启动配置
1）使用了springboot的项目

- 配置了springfox-bridge-spring-boot-starter后，默认开启springfox-bridge。
- 如果需要关闭，可以在application.properties文件(或yml文件)中配置springfox.bridge.enabled的值为false即可

2) 非springboot项目：
可通过配置类实现ApplicationContextAware接口的setApplicationContext方法，方法实现中通过SpringfoxBridge.start()方法启动springfox-bridge, 配置类上通过@EnableSwagger2启动springfox基本功能， 可参考：

```
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import com.github.doublebin.springfox.bridge.core.SpringfoxBridge;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@EnableSwagger2
@Configuration
public class MyXXXConfiguration implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringfoxBridge.start(applicationContext);
    }
}
```

#### 3.3 使用示例
> 下面示例代码演示如何使用springfox-bridge的注解，如何定义的文档，如果设置分组等，展示结果请看3.4

##### 1, 定义两个请求的model类：TestRquest1和TestRequest2
> model类中可以使用springfox的原生注解：io.swagger.annotations.ApiModel和io.swagger.annotations.ApiModelProperty
```
package com.github.doublebin.springfox.bridge.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="TestRequest1", description = "测试请求体1")
public class TestRequest1 {

    @ApiModelProperty(value = "唯一id", required = true)
    private long uuid;

    @ApiModelProperty(value = "名字", required = true)
    private String name;
}

```

```
package com.github.doublebin.springfox.bridge.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="TestRequest2", description = "测试请求体2")
public class TestRequest2 {

    @ApiModelProperty(value = "名字", required = true)
    private String name;

    @ApiModelProperty(value = "描述", required = true)
    private String desc;
}

```

##### 2, 定义三个service类，并标注@Service供spring扫描并注册bean。

```
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
```

```
package com.github.doublebin.springfox.bridge.demo.service;

import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeModelProperty;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;
import com.github.doublebin.springfox.bridge.demo.model.TestRequest1;
import org.springframework.stereotype.Service;

@Service
@BridgeApi(value = "TestService2 Apis", description = "测试服务2")
@BridgeGroup("test-group2")
public class TestService2 {
    @BridgeOperation(value = "测试查询", notes = "测试查询方法说明")
    public String testQuery(@BridgeModelProperty(value = "用户id", required = true) long id, @BridgeModelProperty(value = "请求2", required = false) TestRequest1 request){
        return "Test query success, id is " + id;
    }
}

```

```
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

```
&nbsp;

&nbsp;&nbsp;&nbsp;&nbsp;示例中定义了2个分组：test-group1 和 test-group2， 其中TestService1和TestService3归属于test-group1分组，TestService2归属于test-group2分组，其中TestService1中定义了多个不同的方法，在3.4节中会展示这些情况下的多个效果


#### 3.4 示例效果展示
> 浏览器访问地址：http:${host}:${port}/${server.context-path}/swagger-ui.html

1. 首先看下面两个分组的截图，其中test-group1:

![test-group1.png](https://upload-images.jianshu.io/upload_images/12996941-8bedba6783a1e882.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

test-group2:

![test-group2.png](https://upload-images.jianshu.io/upload_images/12996941-89a6f49bbf3fdf5a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


说明:

- 用spring-fox对某个类生成API文档，必须用在类上使用@BridgeApi注解，并在需要生成文档的方法上使用@BridgeOperation注解。

- 如果要对某个类分组，可以在类上标识@BridgeGroup注解。前面示例中，通过@BridgeGroup注解定义了2个分组，将3个Service类进行了归类，在上面两图可以看到，通过下拉框切换分组后，将分别展示@BridgeGroup注解定义的不同分组的页面。当然@BridgeGroup如果不定义，springfox-bridge会生成一个名为defafult的分组，将没有显式定义@BridgeGroup注解的文档归类到default分组下。

- swagger页面上类的tag采用类全名的方式展示，@BridgeApi注解的description值也会展示在界面上描述类的作用。

- @BridgeOperation注解定义方法，springfox-bridge会在对应的类tag下生成对应的方法tag，由图可以看出，path路径的组成格式为：“/类全名/方法名/入参的类名首字母组合”。

- 如果@BridgeOperation定义的方法没有入参，那么path路径中则没有“入参的类首字母组合”；如果两个同名方法入参的类名首字母组合相同，那么第二个及之后的同名方法的path路径会追加“/index数字”以区分不同的方法，index的排序以springfox-bridge内部对方法加载的顺序进行排序。

- @BridgeOperation注解的value值标识该方法的简要说明，跟path在同一行展示。

- @BridgeModelProperty注解可以对方法入参进行标识，用以对入参加以说明

- 方法入参类型如果是一个model类，该model类可以用io.swagger包的原生注解@ApiModel（标识类）和@ApiModelProperty（标识字段）对model类进行说明，之所以用原生注解，是为了兼容原生springfox，不必重复定义注解。

- 在swagger界面上可以看到，springfox-bridge对每个标识了@BridgeOperation的方法都动态生成一个post请求，并动态生成一个body请求体，方法的所有入参都作为新请求体的字段。

### 四、springfox-bridge注解说明

#### 4.1 springfox-bridge自定义注解

注解 | 标注位置 | 主要作用字段 | 说明 | 对标的原生注解
---|---|---|---|---
BridgeApi | 类 | description |对类进行说明 | io.swagger.annotations.Api
BridgeGroup | 类 | value |标识分组 | 无
BridgeOperation | 方法 | value、notes |分别标识方法概要和详细说明 | io.swagger.annotations.ApiOperation
BridgeModelProperty | 入参 | value | 标识入参说明 | io.swagger.annotations.ApiModelProperty

#### 4.2 兼容的springfox swagger原生注解
目前兼容入参的请求体的model用io.swagger包的原生注解@ApiModel@ApiModelProperty ，后续会提供其它支持
