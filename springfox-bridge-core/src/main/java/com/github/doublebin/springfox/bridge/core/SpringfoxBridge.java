package com.github.doublebin.springfox.bridge.core;

import com.github.doublebin.springfox.bridge.core.builder.BridgeControllerBuilder;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeGroup;
import com.github.doublebin.springfox.bridge.core.component.tuple.Tuple2;
import com.github.doublebin.springfox.bridge.core.exception.BridgeException;
import com.github.doublebin.springfox.bridge.core.util.FileUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import com.github.doublebin.springfox.bridge.core.util.ReflectUtil;
import com.github.doublebin.springfox.bridge.core.util.StringUtil;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SpringfoxBridge {
    private static ApplicationContext applicationContext;

    private static DefaultListableBeanFactory defaultListableBeanFactory;

    private static RequestMappingHandlerMapping requestMappingHandlerMapping;

    private static final String DEFAULT_GROUP = "default";

    //private static Map<String, List<String>> groupList = new HashMap<String, List<String>>();

    /**
     * <gourpName:<version:<fst: mapUrls, snd: autors>>>
     */
    private static Map<String, Tuple2<List<String>, Set<String>>> groupInfos= new HashMap<>();

    private static String bridgeClassFilePath = FileUtil.getCurrentFilePath() + File.separator + "bridge-classes";

    public static void initPath() {
        FileUtils.deleteQuietly(new File(bridgeClassFilePath));
        try {
            FileUtils.forceMkdir(new File(bridgeClassFilePath));
        } catch (IOException e) {
            throw new BridgeException("Init bridge-classes directory failed.", e);
        }
    }

    public static void start(ApplicationContext context) {
        try {
            initPath();
            applicationContext = context;

            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext
                    .getBeanFactory();

            requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");


            Map<String, Object> beanMap = applicationContext.getBeansOfType(Object.class);

            for (String beanName : beanMap.keySet()) {
                Object bean = beanMap.get(beanName);
                if (null == bean) {
                    continue;
                }
                Class clazz = bean.getClass();
                String clazzName = clazz.getName();

                if (StringUtil.startsWith(clazzName, "org.spring")) {
                    continue;
                }

                log.info(clazzName);
                if (ReflectUtil.hasAnnotationAtClass(clazz, BridgeApi.class)) {
                    Class newControllerClass = BridgeControllerBuilder.newControllerClass(clazz);
                    registerNewBean(newControllerClass);
                    registerRequestMapping(newControllerClass);
                    groupNewController(newControllerClass, clazz);

                    continue;
                }

                Class superClass = clazz.getSuperclass();
                if (ReflectUtil.hasAnnotationAtClass(superClass, BridgeApi.class)) {
                    Class newControllerClass = BridgeControllerBuilder.newControllerClass(superClass);
                    registerNewBean(newControllerClass);
                    registerRequestMapping(newControllerClass);
                    groupNewController(newControllerClass, superClass);

                    continue;
                }

                Class<?> interfaces[] = clazz.getInterfaces();
                Class<?> superInterfaces[] = superClass.getInterfaces();
                if (ArrayUtils.isEmpty(interfaces) && ArrayUtils.isEmpty(superInterfaces)) {
                    continue;
                }

                for (Class face : interfaces) {
                    if (ReflectUtil.hasAnnotationAtClass(face, BridgeApi.class)) {
                        Class newControllerClass = BridgeControllerBuilder.newControllerClass(face);
                        registerNewBean(newControllerClass);
                        registerRequestMapping(newControllerClass);
                        groupNewController(newControllerClass, face);
                    }
                }

                for (Class face : superInterfaces) {
                    if (ReflectUtil.hasAnnotationAtClass(face, BridgeApi.class)) {
                        Class newControllerClass = BridgeControllerBuilder.newControllerClass(face);
                        registerNewBean(newControllerClass); //
                        registerRequestMapping(newControllerClass); //
                        groupNewController(newControllerClass, face);
                    }
                }

            }

            for (String group : groupInfos.keySet()) {
                registerGroupDocket(group);
            }
            log.info("Start springfox-bridge success.");
        } catch (Exception e) {
            log.error("Start springfox-bridge failed.", e);
        }
    }

    private static void registerNewBean(Class newControllerClass) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(newControllerClass);
        beanDefinitionBuilder.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);

        String newBeanName = StringUtil.toCamelCase(newControllerClass.getSimpleName());
        defaultListableBeanFactory.registerBeanDefinition(newBeanName, beanDefinitionBuilder.getBeanDefinition()); //
    }

    private static void registerRequestMapping(Class newControllerClass) {
        Object newBean = applicationContext.getBean(newControllerClass);

        if (requestMappingHandlerMapping != null) {

            Method getMappingForMethod = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class, "getMappingForMethod", Method.class, Class.class);
            getMappingForMethod.setAccessible(true);

            Method[] methods = ReflectionUtils.getAllDeclaredMethods(newControllerClass);

            for (Method method : methods) {
                if (ReflectUtil.hasAnnotationAtMethod(method, RequestMapping.class)) {
                    try {
                        RequestMappingInfo mapping_info = (RequestMappingInfo) getMappingForMethod.invoke(
                                requestMappingHandlerMapping, method, newControllerClass);
                        requestMappingHandlerMapping.registerMapping(mapping_info, newBean, method);

                    } catch (Exception e) {
                        log.error("Register requestMapping failed for new controller class [{}].", newControllerClass, e);
                        throw new BridgeException("Register requestMapping failed for new controller class.", e);
                    }
                }
            }
        } else {
            log.error("Register requestMapping failed for new controller class, no bean for class RequestMappingHandlerMapping.");
            throw new BridgeException("Register requestMapping failed for new controller class, no bean for class RequestMappingHandlerMapping.");
        }
    }

    private static void registerGroupDocket(String group) {

        Tuple2<List<String>, Set<String>> tuple2 = groupInfos.get(group);
        List<String> mappingUrls = tuple2.getFst();
        Set<String> autors = tuple2.getSnd();
        String regex = "/(";
        int i = 0;
        int size = mappingUrls.size();
        for (String mappingUrl : mappingUrls) {
            regex += StringUtil.substringAfter(mappingUrl, "/");
            if (i != size - 1) {
                regex += "|";
            }
            i++;
        }
        regex += ")(/.*)*";

        Docket docket = new Docket(DocumentationType.SWAGGER_2).groupName("【Bridge】" + group)
            .genericModelSubstitutes(DeferredResult.class)
            .useDefaultResponseMessages(false)
            .forCodeGeneration(true)
            .pathMapping("/")
            .select()
            .paths(PathSelectors.regex(regex))
            .build()
            .apiInfo(apiInfo(group, autors));

        defaultListableBeanFactory.registerSingleton(group + "GroupDocket", docket);

    }

    private static void groupNewController(Class newControllerClass, Class originalClass) {
        String[] mappingUrls = (String[]) ReflectUtil.getAnnotationValue(newControllerClass, RequestMapping.class, "value");
        if (ArrayUtils.isNotEmpty(mappingUrls)) {
            String mappingUrl = mappingUrls[0];

            if (ReflectUtil.hasAnnotationAtClass(originalClass, BridgeGroup.class)) {
                String groupName = (String)ReflectUtil.getAnnotationValue(originalClass, BridgeGroup.class, "value");
                String[] groupAuthors = (String[])ReflectUtil.getAnnotationValue(originalClass, BridgeGroup.class, "authors");

                addGroupInfos(groupName, groupAuthors, mappingUrl);
            } else {
                addGroupInfos(DEFAULT_GROUP, null, mappingUrl);
            }
        }
    }

    private static ApiInfo apiInfo(String group, Set<String> authors) {
        String author = "springfox-bridge";
        if (CollectionUtils.isNotEmpty(authors)) {
            int i = 0;
            for (String s : authors) {
                if (0 == i) {
                    author = s;
                } else if(StringUtils.isNotBlank(s)){
                    author += ", " + s;
                }
                i++;
            }
        }


        return new ApiInfoBuilder().title("【Bridge service】" + group)
                .description("Restful apis for Bridge group : " + group)
                .contact(author)
                .version(null)
                .build();
    }

    private static void addGroupInfos(String groupName, String[] groupAuthors, String mappingUrl) {

        if (StringUtils.isBlank(groupName)) {
            groupName = DEFAULT_GROUP;
        }

        Tuple2<List<String>, Set<String>> tuple2 = groupInfos.get(groupName);
        if (null == tuple2) {
            tuple2 = new Tuple2<>();
            groupInfos.put(groupName, tuple2);
        }

        List<String> mappingUrls = tuple2.getFst();
        if(null == mappingUrls) {
            mappingUrls = new ArrayList<>();
            tuple2.setFst(mappingUrls);
        }
        mappingUrls.add(mappingUrl);

        Set<String> autors = tuple2.getSnd();
        if (null == autors) {
            autors = new HashSet<>();
            tuple2.setSnd(autors);
        }

        if (ArrayUtils.isNotEmpty(groupAuthors)) {
            for (String groupAuthor : groupAuthors) {
                if (StringUtils.isNotBlank(groupAuthor)) {
                    autors.add(StringUtils.trim(groupAuthor));
                }
            }
        }

    }

    public static String getBridgeClassFilePath() {
        return bridgeClassFilePath;
    }

    public static void setBridgeClassFilePath(String bridgeClassFilePath) {
        SpringfoxBridge.bridgeClassFilePath = bridgeClassFilePath;
    }

}


