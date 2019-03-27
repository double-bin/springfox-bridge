package com.github.doublebin.springfox.bridge.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.github.doublebin.springfox.bridge.core.exception.BridgeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import sun.reflect.generics.repository.AbstractRepository;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.tree.ClassSignature;

@Slf4j
public class ReflectUtil {
    public static Class<?> getClass(Object object, boolean turnToExist) {
        if (turnToExist) {
            String className = object.getClass().getName();
            try {
                return Class.forName(className);
            } catch (java.lang.ClassNotFoundException e) {
                throw new BridgeException("Class not found.", e);
            }
        }
        return object.getClass();
    }

    public static List<Method> getMethods(String fullMethodName) {
        String methodName = StringUtils.substringAfterLast(fullMethodName, ".");
        Class<?> clazz = getClass(fullMethodName);

        List<Method> methods = new ArrayList<Method>();
        Method[] allMethods = clazz.getMethods();
        for (Method method : allMethods) {
            if (method.getName().equals(methodName)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static Method getDeclaredMethod(String fullMethodName, Object[] args, boolean isIgnorePrimitive) {
        return getDeclaredMethod(getMethods(fullMethodName), args, isIgnorePrimitive);
    }

    public static Method getDeclaredMethod(String fullMethodName, Object[] args) {
        return getDeclaredMethod(getMethods(fullMethodName), args, true);
    }

    public static Class<?> getClass(String fullMethodName) {
        String className = StringUtils.substringBeforeLast(fullMethodName, ".");
        try {
            return Class.forName(className);
        } catch (java.lang.ClassNotFoundException e) {
            throw new BridgeException("Class not found.", e);
        }
    }

    public static boolean hasNull(Object[] args) {
        for (Object arg : args) {
            if (null == arg) {
                return true;
            }
        }

        return false;
    }

    private static Method getDeclaredMethod(List<Method> methods, Object[] args, boolean isIgnorePrimitive) {
        List<Class<?>> clazzes = getClassList(args);

        for (Method method : methods) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != clazzes.size()) {
                continue;
            }
            boolean isEqual = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> clazz = clazzes.get(i);
                Class<?> parameterType = parameterTypes[i];

                boolean isAssignable = true;

                if (isIgnorePrimitive) {
                    isAssignable = ClassUtils.isAssignable(clazz, parameterType);
                } else {
                    isAssignable = parameterType.isAssignableFrom(clazz);
                }
                if (null == clazz || isAssignable) {
                    continue;
                } else {
                    isEqual = false;
                    break;
                }
            }

            if (isEqual) {
                return method;
            }
        }

        return null;
    }

    public static List<Class<?>> getClassList(Object[] args) {
        List<Class<?>> clazzes = new ArrayList<Class<?>>();
        for (Object arg : args) {
            Class<?> clazz = null;
            if (null != arg) {
                clazz = arg.getClass();
            }
            clazzes.add(clazz);
        }

        return clazzes;
    }

    public static Class<?>[] getClassArray(Object[] args) {
        Class<?>[] classes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (null == args[i]) {
                classes[i] = null;
            }
            classes[i] = args[i].getClass();
        }
        return classes;
    }

    public static <T extends Annotation> List<Object> getParamsByAnnotation(Method method, Object[] args,
                                                                            Class<T> annotationClass) {
        List<Object> pathParams = new ArrayList<Object>();
        Annotation[][] allParamAnnotations = method.getParameterAnnotations();
        if (allParamAnnotations.length == 0) {
            return pathParams;
        }
        int paramIndex = 0;
        for (Annotation[] annotations : allParamAnnotations) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationClass)) {
                    pathParams.add(args[paramIndex]);
                    break;
                }
            }
            paramIndex++;
        }
        return pathParams;
    }

    public static boolean hasAnnotationAtClass(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return clazz.isAnnotationPresent(annotationClass);
    }

    public static boolean hasAnnotationAtMethod(Method method, Class<? extends Annotation> annotationClass) {
        return method.isAnnotationPresent(annotationClass);
    }

    public static <T extends Annotation> Object getAnnotationValue(Method method, Class<T> annotationClass,
                                                                   String annotationMethodName) {
        if (method.isAnnotationPresent(annotationClass)) {
            Annotation annotation = method.getAnnotation(annotationClass);
            try {
                return annotation.getClass().getDeclaredMethod(annotationMethodName).invoke(annotation);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException
                | SecurityException e) {
                throw new BridgeException(MessageFormat.format(
                    "Get annotation value at method [{0}] failed, annotation name is [{1}], annotation method is "
                        + "[{2}].",
                    method.getName(),
                    annotationClass.getName(),
                    annotationMethodName), e);
            }
        } else {
            return null;
        }
    }

    public static <T extends Annotation> Object getAnnotationValue(Class<?> clazz, Class<T> annotationClass,
                                                                   String annotationMethodName) {
        if (clazz.isAnnotationPresent(annotationClass)) {
            Annotation annotation = clazz.getAnnotation(annotationClass);
            try {
                return annotation.getClass().getDeclaredMethod(annotationMethodName).invoke(annotation);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
                NoSuchMethodException
                | SecurityException e) {
                log.error(
                    "Get annotation value at class [{}] failed, annotation name is [{}], annotation method is [{}].",
                    clazz.getName(),
                    annotationClass.getName(),
                    annotationMethodName,
                    e);
                throw new BridgeException(e);
            }
        } else {
            return null;
        }
    }

    public static <T extends Annotation> Object invokeMethod(Object object, String methodName) {
        try {
            if (null == object) {
                throw new BridgeException("Invoke method failed, object is null.");
            }
            return object.getClass().getDeclaredMethod(methodName).invoke(object);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException |
            NoSuchMethodException
            | SecurityException e) {
            log.error(
                "Invoke method failed, object class is [{}], method is [{}].",
                object.getClass().getName(),
                methodName,
                e);
            throw new BridgeException("Invoke method failed.", e);
        }
    }

    public static <T extends Annotation> List<Object> getAnnotationValuesByParams(Method method,
                                                                                  Class<T> annotationClass,
                                                                                  String annotationMethodName) {
        List<Object> annotationValues = new ArrayList<Object>();
        Annotation[][] allParamAnnotations = method.getParameterAnnotations();
        if (allParamAnnotations.length == 0) {
            return annotationValues;
        }
        for (Annotation[] annotations : allParamAnnotations) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationClass)) {
                    try {
                        annotationValues.add(
                            annotation.getClass().getDeclaredMethod(annotationMethodName).invoke(annotation));
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
                        log.error(
                            "Get annotation values at Method [{}] failed, annotation name is [{}], annotation method "
                                + "is [{}].",
                            method.getName(),
                            annotationClass.getName(),
                            annotationMethodName,
                            e);
                        throw new BridgeException(e);
                    }
                    break;
                }
            }
        }
        return annotationValues;

    }

    public static Method getDeclaredMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    public static Field getDeclaredField(Class clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
        try {
            return method.getAnnotation(annotationClass);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T extends Annotation> T getAnnotation(Field field, Class<T> annotationClass) {
        try {
            return field.getAnnotation(annotationClass);
        } catch (Exception e) {
            return null;
        }
    }

    public static Class<? extends java.lang.annotation.Annotation> getAnnotation(Object o) {
        if (null == o) {
            return null;
        }
        Class proxyClass = o.getClass();
        if (proxyClass.getName().startsWith("com.sun.proxy.")) {
            Class<?>[] interfaces = proxyClass.getInterfaces();
            if (ArrayUtils.isEmpty(interfaces)) {
                return null;
            }
            for (Class interf : interfaces) {
                if (interf.isAnnotation()) {
                    return interf;
                }
            }
        }
        return null;
    }

    public static boolean isAnnotationObject(Object o) {
        if (null == getAnnotation(o)) {
            return false;
        }
        return true;
    }

    public static ClassRepository getClassRepository(Class clazz) {
        try {
            Method getGenericInfoMethod = Class.class.getDeclaredMethod("getGenericInfo");
            getGenericInfoMethod.setAccessible(true);
            return (ClassRepository)getGenericInfoMethod.invoke(clazz);
        } catch (Exception e) {
            throw new BridgeException("Get ClassRepository failed for calss : " + clazz.getName(), e);
        }
    }

    public static TypeVariable<?>[] getTypeVariables(Class clazz) {

        ClassRepository classRepository = getClassRepository(clazz);
        if (null == classRepository) {
            return null;
        }
        return classRepository.getTypeParameters();
    }

    public static String[] getGenericTypeNames(Class clazz) {
        TypeVariable<?>[] typeVariables = getTypeVariables(clazz);

        if (null == typeVariables) {
            return null;
        }

        String[] genericTypeNames = new String[typeVariables.length]; //获取定义的泛型占位符
        for (int i = 0; i < typeVariables.length; i++) {
            genericTypeNames[i] = typeVariables[i].getName();
        }

        return genericTypeNames;
    }

    public static String getGenericSignature(Field field) {
        try {
            Method getGenericSignatureMethod = Field.class.getDeclaredMethod("getGenericSignature");
            getGenericSignatureMethod.setAccessible(true);
            Object o = getGenericSignatureMethod.invoke(field);
            if (null == o || !(o instanceof String)) {
                return null;
            }
            return (String)o;
        } catch (Exception e) {
            throw new BridgeException("Get genericSignature failed for field : " + field.getName(), e);
        }
    }

    public static ClassSignature getClassSignatureTree(ClassRepository classRepository) {
        try {
            if (null == classRepository) {
                return null;
            }
            Method getTreeMethod = AbstractRepository.class.getDeclaredMethod("getTree");
            getTreeMethod.setAccessible(true);
            Object o = getTreeMethod.invoke(classRepository);
            ClassSignature classSignature = (ClassSignature)o;
            return classSignature;
        } catch (Exception e) {
            throw new BridgeException("Get classSignature failed for classRepository : " + classRepository.toString(),
                e);
        }
    }

    public static Class getArrayClass(Class clazz) {
        String className = clazz.getName();
        if (StringUtils.startsWith(className, "[")) {
            className = "[" + className;
        } else {
            className = "[L" + className + ";";
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new BridgeException("Get arrayClass failed for class : " + clazz.getName(), e);
        }
    }

}
