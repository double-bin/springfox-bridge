package com.github.doublebin.springfox.bridge.core.builder;

import com.github.doublebin.springfox.bridge.core.SpringfoxBridge;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeApi;
import com.github.doublebin.springfox.bridge.core.builder.annotations.BridgeOperation;
import com.github.doublebin.springfox.bridge.core.component.tuple.Tuple2;
import com.github.doublebin.springfox.bridge.core.exception.BridgeException;
import com.github.doublebin.springfox.bridge.core.util.JavassistUtil;
import com.github.doublebin.springfox.bridge.core.util.ReflectUtil;
import com.github.doublebin.springfox.bridge.core.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javassist.*;
import javassist.bytecode.*;
import javassist.bytecode.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class BridgeControllerBuilder {

    private static ConcurrentHashMap<String, AtomicInteger> methodNameMap = new ConcurrentHashMap<String, AtomicInteger>();

    //private static String classFilePath = FileUtil.getCurrentFilePath();

    private static final ClassPool pool = ClassPool.getDefault();

    public static Class newControllerClass(Class oldClass) {

        if (ReflectUtil.hasAnnotationAtClass(oldClass, BridgeApi.class)) {

            CtClass newControllerCtClass = pool.makeClass(BridgeClassNameBuilder.buildNewClassName(BridgeClassNameBuilder.NEW_CONTROLLER_CLASS_NAME_PRE, oldClass.getSimpleName()));

            try {
                CtField beanCtField = addAndGetBeanField(oldClass, newControllerCtClass);

                addAutowiredAnnotationAtField(beanCtField);

                Method[] allMethods = oldClass.getMethods();
                for (Method method : allMethods) {
                    if (ReflectUtil.hasAnnotationAtMethod(method, BridgeOperation.class)) {
                        String methodName = method.getName();


                        Tuple2<Class, Boolean> requestBodyClassTuple = BridgeRequestBuilder.newRequestClass(method, oldClass.getSimpleName() + StringUtil.toCapitalizeCamelCase(methodName) + "Request");

                        CtMethod newCtMethod = addAndGetHomonymicMethod(method, newControllerCtClass, requestBodyClassTuple);

                        addAnnotationsAtMethod(newCtMethod, method);

                        if (null != requestBodyClassTuple && null != requestBodyClassTuple.getFst()) {
                            addAnnotationsAtMethodParameters(newCtMethod);
                        }
                    }
                }

                addAnnotationsAtClass(newControllerCtClass, oldClass);

                newControllerCtClass.writeFile(SpringfoxBridge.getBridgeClassFilePath());
                return newControllerCtClass.toClass();

            } catch (CannotCompileException | IOException e) {
                log.error("New controller class for old class [{}] failed.", oldClass.getName(), e);
                throw new BridgeException("New controller class failed", e);
            }
        }

        log.error("New controller class for old class [{}] failed, old class has no BridgeApi annotation.", oldClass.getName());
        throw new BridgeException(oldClass.getName() + " has no BridgeApi annotation, cannot new controller class.");
    }


    private static CtMethod addAndGetHomonymicMethod(Method method, CtClass newControllerCtClass, Tuple2<Class, Boolean> requestBodyClassTuple) {
        try {
            Class requestBodyClass = null;
            boolean isOrignalClass = false;
            if (null != requestBodyClassTuple) {
                requestBodyClass = requestBodyClassTuple.getFst();
                isOrignalClass = requestBodyClassTuple.getSnd();
            }
            String methodName = method.getName();
            Class returnType = method.getReturnType();
            Parameter[] parameters = method.getParameters();

            List<Class> parameterClassess = new ArrayList<>();
            for (Parameter parameter : parameters) {
                parameterClassess.add(parameter.getType());
            }


            Class newReplaceClass = BridgeGenericReplaceBuilder.buildReplaceClass(method.getGenericReturnType(), null);
            if (returnType.equals(newReplaceClass)) {

                pool.insertClassPath(new ClassClassPath(returnType));
                if (null != requestBodyClass) {
                    pool.insertClassPath(new ClassClassPath(requestBodyClass));
                }
                CtMethod newCtMethod = new CtMethod(pool.get(returnType.getName()), methodName,
                        null == requestBodyClass ? new CtClass[]{} : new CtClass[]{pool.get(requestBodyClass.getName())}, newControllerCtClass);
                newCtMethod.setModifiers(Modifier.PUBLIC);

                int size = parameters.length;

                String body = "{java.lang.reflect.Method originalMethod = this.bean.getClass().getMethod(\"" + methodName + "\",";


                if (isOrignalClass) {

                    body += "new Class[]{";
                    body += parameterClassess.get(0).getName() + ".class";
                    body += "});";

                    body += "java.lang.reflect.Parameter[] parameters = originalMethod.getParameters();";

                    if (parameters[0].getParameterizedType() instanceof Class) {

                        body += "return this.bean." + methodName + "($1";
                    } else {
                        body += "java.lang.String jsonStr = com.github.doublebin.springfox.bridge.core.util.JsonUtil.writeValueAsString($1);";


                        body += "Object orignalRequestValue = com.github.doublebin.springfox.bridge.core.util.JsonUtil.readValue(jsonStr,parameters[0].getParameterizedType());";

                    /*  body += getObjectCreateBody(parameterClassess.get(0), "requestValue")
                            + getArrayCreateBody(parameterClassess.get(0), "requestValues", "requestValue")
                            + "com.github.doublebin.springfox.bridge.core.util.JsonUtil.copyValue(orignalRequestValue, requestValues);";*/

                        body += parameters[0].getType().getName() + " requestValue"  + " = (" + parameters[0].getType().getName()+")orignalRequestValue" + ";";

                        body += "return this.bean." + methodName + "(requestValue";
                    }


                } else {


                    if (0 == size) {
                        body += "null";
                    } else {
                        body += "new Class[]{";
                        for (int i = 0; i < size; i++) {
                            body += parameterClassess.get(i).getName() + ".class";
                            if (i != size - 1) {
                                body += ",";
                            }
                        }
                        body += "}";
                    }


                    body += ");";
                    body += "java.lang.reflect.Parameter[] parameters = originalMethod.getParameters();";

                    body += "java.lang.String[] jsonStrs = new java.lang.String[" + size + "];";
                    for (int i = 0; i < size; i++) {
                        if (parameters[i].getParameterizedType() instanceof Class) {

                        } else {
                            body += "jsonStrs[" + i + "]=com.github.doublebin.springfox.bridge.core.util.JsonUtil.writeValueAsString($1.getParam" + i + "());";
                        }

                    }

                    for (int i = 0; i < size; i++) {

                        if (parameters[i].getParameterizedType() instanceof Class) {

                           /* if (StringUtils.equalsAny(parameterClassess.get(i).getName(), "int", "long", "char", "boolean", "short", "byte", "float", "double")) {
                                body += parameterClassess.get(i).getName() + " requestValue" + i + "=$1.getParam" + i + "();";
                            } else {
                                body += getObjectCreateBody(parameterClassess.get(i), "requestValue" + i)
                                        + "requestValue" + i + "=$1.getParam" + i + "();";
                            }*/

                            body +=parameters[i].getType().getName() + " requestValue" + i + "=$1.getParam" + i + "();";

                        } else {
                            body += "Object orignalRequestValue" + i + " = com.github.doublebin.springfox.bridge.core.util.JsonUtil.readValue(jsonStrs[" + i + "],parameters[" + i + "].getParameterizedType());";

                          /*  body += getObjectCreateBody(parameterClassess.get(i), "requestValue" + i)
                                    + getArrayCreateBody(parameterClassess.get(i), "requestValues" + i, "requestValue" + i)
                                    + "com.github.doublebin.springfox.bridge.core.util.JsonUtil.copyValue(orignalRequestValue" + i + ", requestValues" + i + ");";*/

                            body += parameters[i].getType().getName() + " requestValue" +i + " = (" + parameters[i].getType().getName()+")orignalRequestValue" +i+ ";";
                        }


                    }


                    body += "return this.bean." + methodName + "(";


                    for (int i = 0; i < size; i++) {
                        body += ("requestValue" + i); //$1 means the first parameter

                        /*if (parameters[i].getParameterizedType() instanceof Class) {
                            body += ("requestValue" + i); //$1 means the first parameter
                        } else {
                            body += ("requestValues" + i + "[0]"); //$1 means the first parameter
                        }*/
                        if (i != size - 1) {
                            body += ",";
                        }
                    }
                }

                body += ");}";

                newCtMethod.setBody(body);
                newControllerCtClass.addMethod(newCtMethod);
                return newCtMethod;
            } else {

                pool.insertClassPath(new ClassClassPath(newReplaceClass));
                if (null != requestBodyClass) {
                    pool.insertClassPath(new ClassClassPath(requestBodyClass));
                }
                CtMethod newCtMethod = new CtMethod(pool.get(newReplaceClass.getName()), methodName,
                        null == requestBodyClass ? new CtClass[]{} : new CtClass[]{pool.get(requestBodyClass.getName())}, newControllerCtClass);
                newCtMethod.setModifiers(Modifier.PUBLIC);

                int size = parameters.length;

                String body = "{java.lang.reflect.Method originalMethod = this.bean.getClass().getMethod(\"" + methodName + "\",";

                if (isOrignalClass) {

                    body += "new Class[]{";
                    body += parameterClassess.get(0).getName() + ".class";
                    body += "});";

                    body += "java.lang.reflect.Parameter[] parameters = originalMethod.getParameters();";


                    if (parameters[0].getParameterizedType() instanceof Class) {
                        body += "Object orignalValue = this.bean." + methodName + "($1";
                    } else {
                        body += "java.lang.String jsonStr = com.github.doublebin.springfox.bridge.core.util.JsonUtil.writeValueAsString($1);";


                        body += "Object orignalRequestValue = com.github.doublebin.springfox.bridge.core.util.JsonUtil.readValue(jsonStr,parameters[0].getParameterizedType());";

                   /* body += getObjectCreateBody(parameterClassess.get(0), "requestValue")
                            + getArrayCreateBody(parameterClassess.get(0), "requestValues", "requestValue")
                            + "com.github.doublebin.springfox.bridge.core.util.JsonUtil.copyValue(orignalRequestValue, requestValues);";*/

                        body += parameters[0].getType().getName() + " requestValue"  + " = (" + parameters[0].getType().getName()+")orignalRequestValue" + ";";

                        body += "Object orignalValue = this.bean." + methodName + "(requestValue";
                    }



                } else {

                    if (0 == size) {
                        body += "null";
                    } else {
                        body += "new Class[]{";
                        for (int i = 0; i < size; i++) {
                            body += parameterClassess.get(i).getName() + ".class";
                            if (i != size - 1) {
                                body += ",";
                            }
                        }
                        body += "}";
                    }


                    body += ");";
                    body += "java.lang.reflect.Parameter[] parameters = originalMethod.getParameters();";

                    body += "java.lang.String[] jsonStrs = new java.lang.String[" + size + "];";
                    for (int i = 0; i < size; i++) {
                        if (parameters[i].getParameterizedType() instanceof Class) {

                        } else {
                            body += "jsonStrs[" + i + "]=com.github.doublebin.springfox.bridge.core.util.JsonUtil.writeValueAsString($1.getParam" + i + "());";
                        }

                    }

                    for (int i = 0; i < size; i++) {

                        if (parameters[i].getParameterizedType() instanceof Class) {

                            /*if (StringUtils.equalsAny(parameterClassess.get(i).getName(), "int", "long", "char", "boolean", "short", "byte", "float", "double")) {
                                body += parameterClassess.get(i).getName() + " requestValue" + i + "=$1.getParam" + i + "();";
                            } else {
                                body += getObjectCreateBody(parameterClassess.get(i), "requestValue" + i)
                                        + " requestValue" + i + "=$1.getParam" + i + "();";
                            }*/

                            body +=parameters[i].getType().getName() + " requestValue" + i + "=$1.getParam" + i + "();";

                        } else {
                            body += "Object orignalRequestValue" + i + " = com.github.doublebin.springfox.bridge.core.util.JsonUtil.readValue(jsonStrs[" + i + "],parameters[" + i + "].getParameterizedType());";


                            body += parameters[i].getType().getName() + " requestValue" +i + " = (" + parameters[i].getType().getName()+")orignalRequestValue" +i+ ";";

                            /*body += getObjectCreateBody(parameterClassess.get(i), "requestValue" + i)
                                    + getArrayCreateBody(parameterClassess.get(i), "requestValues" + i, "requestValue" + i)
                                    + "com.github.doublebin.springfox.bridge.core.util.JsonUtil.copyValue(orignalRequestValue" + i + ", requestValues" + i + ");";*/
                        }


                    }


                    body += "Object orignalValue = this.bean." + methodName + "(";

                    for (int i = 0; i < size; i++) {
                        body += ("requestValue" + i); //$1 means the first parameter
/*
                        if (parameters[i].getParameterizedType() instanceof Class) {
                            body += ("requestValue" + i); //$1 means the first parameter
                        } else {
                            body += ("requestValues" + i + "[0]"); //$1 means the first parameter
                        }*/
                        if (i != size - 1) {
                            body += ",";
                        }
                    }
                }

                body += ");"
                        + getObjectCreateBody(newReplaceClass, "objectValue")
                        + getArrayCreateBody(newReplaceClass, "objectValues", "objectValue")
                        + "com.github.doublebin.springfox.bridge.core.util.JsonUtil.copyValue(orignalValue, objectValues);"
                        + "return objectValues[0];"
                        + "}";

                newCtMethod.setBody(body);
                newControllerCtClass.addMethod(newCtMethod);
                return newCtMethod;
            }

        } catch (Exception e) {
            log.error("Add new homonymic method failed.", e);
            throw new BridgeException("Add new homonymic method failed.", e);
        }
    }

    private static String getClassBodyName(Class clazz) {
        String className = clazz.getName();
        if (!StringUtils.startsWith(className, "[")) {
            return className;
        } else {
            int count = StringUtils.lastIndexOf(className, "[") + 1;
            String classBodyName = StringUtils.substring(className, count + 1, className.length() - 1);
            for (int i = 0; i < count; i++) {
                classBodyName += "[]";
            }

            return classBodyName;
        }
    }

    private static String getObjectCreateBody(Class clazz, String objectName) {


        if (StringUtils.equals("int", clazz.getName())) {
            clazz = Integer.class;
        } else if (StringUtils.equals("long", clazz.getName())) {
            clazz = Long.class;
        } else if (StringUtils.equals("float", clazz.getName())) {
            clazz = Float.class;
        } else if (StringUtils.equals("double", clazz.getName())) {
            clazz = Double.class;
        } else if (StringUtils.equals("char", clazz.getName())) {
            clazz = Character.class;
        } else if (StringUtils.equals("short", clazz.getName())) {
            clazz = Short.class;
        } else if (StringUtils.equals("byte", clazz.getName())) {
            clazz = byte.class;
        } else if (StringUtils.equals("boolean", clazz.getName())) {
            clazz = Boolean.class;
        }


        String classBodyName = getClassBodyName(clazz);
        String body = classBodyName + " " + objectName + " = new ";
        if (StringUtils.endsWith(classBodyName, "[]")) {
            String tempInitName = StringUtils.replaceFirst(classBodyName, "\\x5B+?", "[1");
            return body + tempInitName + ";";
        } else {
            //TODO
            if (hasNoArgsConstructor(clazz)) {
                return body + classBodyName + "();";
            } else {
                Constructor[] constructors = clazz.getConstructors();
                body += classBodyName + "(";

                Constructor constructor = constructors[0];
                Class<?>[] clazzes = constructor.getParameterTypes();


                for (int i = 0; i < clazzes.length; i++) {
                    Class clazze = clazzes[i];
                    if (clazze.equals(String.class)) {
                        body += "\"0\"";
                    } else if (clazze.equals(Integer.class) || StringUtils.equals("int", clazze.getName())) {
                        body += "0";
                    } else if (clazze.equals(Long.class) || StringUtils.equals("long", clazze.getName())) {
                        body += "0L";
                    } else if (clazze.equals(Float.class) || StringUtils.equals("float", clazze.getName())) {
                        body += "0.0F";
                    } else if (clazze.equals(Double.class) || StringUtils.equals("double", clazze.getName())) {
                        body += "0.0";
                    } else if (clazze.equals(Character.class) || StringUtils.equals("char", clazze.getName())) {
                        body += "\'0\'";
                    } else if (clazze.equals(Short.class) || StringUtils.equals("short", clazze.getName())) {
                        body += "0";
                    } else if (clazze.equals(Byte.class) || StringUtils.equals("byte", clazze.getName())) {
                        body += "0";
                    } else if (clazze.equals(Boolean.class) || StringUtils.equals("boolean", clazze.getName())) {
                        body += "false";
                    } else {
                        body += "null";
                    }


                    if (i != clazzes.length - 1) {
                        body += ",";
                    }
                }

                return body + ");";
            }

        }
    }

    private static String getArrayCreateBody(Class baseClass, String arrayName, String initObjectName) {
        Class arrayClass = ReflectUtil.getArrayClass(baseClass);
        String body = getObjectCreateBody(arrayClass, arrayName) + arrayName + "[0]=" + initObjectName + ";";

        return body;
    }


    private static void addAnnotationsAtMethod(CtMethod newCtMethod, Method method) {
        ConstPool constpool = newCtMethod.getDeclaringClass().getClassFile().getConstPool();
        AnnotationsAttribute methodAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);

        addRequestMappingAnnotationAtMethod(methodAttr, method, constpool);

        addApiOperationAnnotationAtMethod(methodAttr, method, constpool);

        MethodInfo methodInfo = newCtMethod.getMethodInfo();
        methodInfo.addAttribute(methodAttr);
    }


    private static void addApiOperationAnnotationAtMethod(AnnotationsAttribute methodAttr, Method method, ConstPool constpool) {

        String[] annotationMethodNames = new String[]{"value", "notes", "tags", "response", "responseContainer", "responseReference",
                "httpMethod", "produces", "consumes", "protocols", "hidden", "responseHeaders", "code", "extensions"};

        Annotation apiOperationAnnotation = JavassistUtil.copyAnnotationValues(method, BridgeOperation.class, ApiOperation.class, constpool, annotationMethodNames);

        methodAttr.addAnnotation(apiOperationAnnotation);
    }

    private static void addRequestMappingAnnotationAtMethod(AnnotationsAttribute methodAttr, Method method, ConstPool constpool) {
        Annotation requestMappingAnnotation = new Annotation(RequestMapping.class.getName(), constpool);

        Parameter[] parameters = method.getParameters();
        String requestMappingValue = "/" + method.getName();
        if (ArrayUtils.isNotEmpty(parameters)) {
            requestMappingValue += "/";

            String suffix = "";
            for (Parameter parameter : parameters) {
                Class parameterType = parameter.getType();
                String typeName = parameterType.getSimpleName();
                suffix += typeName.substring(0, 1);
            }

            requestMappingValue += suffix;

            String methodNameIncludeSuffix = method.getDeclaringClass().getName() + "." + method.getName() + "." + suffix;
            methodNameMap.putIfAbsent(methodNameIncludeSuffix, new AtomicInteger(0));
            int count = methodNameMap.get(methodNameIncludeSuffix).incrementAndGet();

            if (count > 1) {
                requestMappingValue += "/" + (count - 1);
            }
        }


        MemberValue[] values = new StringMemberValue[]{new StringMemberValue(requestMappingValue, constpool)};
        ArrayMemberValue arrayStringMemberValue = new ArrayMemberValue(constpool);
        arrayStringMemberValue.setValue(values);
        requestMappingAnnotation.addMemberValue("value", arrayStringMemberValue);

        EnumMemberValue methodEnumberValue = new EnumMemberValue(constpool);
        methodEnumberValue.setType(RequestMethod.class.getName());
        methodEnumberValue.setValue("POST");
        ArrayMemberValue arrayEnumMemberValue = new ArrayMemberValue(constpool);
        MemberValue[] enumValues = new EnumMemberValue[]{methodEnumberValue};
        arrayEnumMemberValue.setValue(enumValues);
        requestMappingAnnotation.addMemberValue("method", arrayEnumMemberValue);

        methodAttr.addAnnotation(requestMappingAnnotation);
    }

    private static void addAnnotationsAtMethodParameters(CtMethod newCtMethod) {
        ConstPool constpool = newCtMethod.getDeclaringClass().getClassFile().getConstPool();
        ParameterAnnotationsAttribute parameterAnnotationsAttribute = new ParameterAnnotationsAttribute(constpool, ParameterAnnotationsAttribute.visibleTag);
        Annotation[][] paramArrays = new Annotation[1][1];
        Annotation[] addAnno = new Annotation[1];
        addAnno[0] = new Annotation(RequestBody.class.getName(), constpool);
        paramArrays[0] = addAnno;
        parameterAnnotationsAttribute.setAnnotations(paramArrays);
        newCtMethod.getMethodInfo().addAttribute(parameterAnnotationsAttribute);
    }

    private static void addTagsMember(Annotation apiAnnotation, Class oldClass, ConstPool constpool) {
        String[] tags = (String[]) ReflectUtil.getAnnotationValue(oldClass, BridgeApi.class, "tags");
        List<StringMemberValue> tagsList = new ArrayList<StringMemberValue>();

        // 仅当没有指定 tags 时，才添加默认值：当前类名
        if (tags.length == 1 && tags[0].equals("")) {
            tagsList.add(new StringMemberValue(oldClass.getName(), constpool));
        }

        if (ArrayUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                if (StringUtil.isNoneBlank(tag)) {
                    tagsList.add(new StringMemberValue(tag, constpool));
                }
            }
        }
        ArrayMemberValue tagArrayMemberValue = new ArrayMemberValue(constpool);
        tagArrayMemberValue.setValue(tagsList.toArray(new MemberValue[0]));
        apiAnnotation.addMemberValue("tags", tagArrayMemberValue);
    }

    private static void addAnnotationsAtClass(CtClass newControllerCtClass, Class oldClass) {
        ClassFile ccFile = newControllerCtClass.getClassFile();
        ConstPool constpool = ccFile.getConstPool();

        AnnotationsAttribute classAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation restControllerAnnotation = new Annotation(RestController.class.getName(), constpool);
        classAttr.addAnnotation(restControllerAnnotation);


        String[] annotationMethodNames = new String[]{"value", "description", "tags", "coverAll", "produces", "consumes", "protocols", "hidden"};
        Annotation apiAnnotation = JavassistUtil.copyAnnotationValues(oldClass, BridgeApi.class, Api.class, constpool, annotationMethodNames);
        addTagsMember(apiAnnotation, oldClass, constpool);
        classAttr.addAnnotation(apiAnnotation);

        Annotation requestMappingAnnotation = new Annotation(RequestMapping.class.getName(), constpool);
        MemberValue[] values = new StringMemberValue[]{new StringMemberValue("/" + oldClass.getName(), constpool)};
        ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constpool);
        arrayMemberValue.setValue(values);
        requestMappingAnnotation.addMemberValue("value", arrayMemberValue);
        classAttr.addAnnotation(requestMappingAnnotation);

        ccFile.addAttribute(classAttr);
    }

    private static void addAutowiredAnnotationAtField(CtField beanCtField) {
        ConstPool constpool = beanCtField.getDeclaringClass().getClassFile().getConstPool();
        Annotation autowiredAnnotation = new Annotation(Autowired.class.getName(), constpool);
        JavassistUtil.addAnnotationForCtField(beanCtField, autowiredAnnotation);
    }

    private static CtField addAndGetBeanField(Class oldClass, CtClass newControllerCtClass) {
        try {
            pool.insertClassPath(new ClassClassPath(oldClass));
            CtField beanCtField = new CtField(pool.get(oldClass.getName()), "bean", newControllerCtClass);
            beanCtField.setModifiers(Modifier.PRIVATE);
            newControllerCtClass.addField(beanCtField);
            return beanCtField;
        } catch (Exception e) {
            log.error("Add bean property failed for new class {}, bean type is {}.", newControllerCtClass.getName(), oldClass.getName(), e);
            throw new BridgeException("Add bean property failed for new class " + newControllerCtClass.getName(), e);
        }

    }

    public static boolean hasNoArgsConstructor(Class clazz) {
        try {
            clazz.getConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }


    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InstantiationException {
        int i = 12;
        Object a = i;
        Class clazz = a.getClass();
        Constructor<?>[] constructors = clazz.getConstructors();
        System.out.println(constructors);


    }
}
