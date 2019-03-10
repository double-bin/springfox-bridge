package com.github.doublebin.springfox.bridge.core.handler;

import com.github.doublebin.springfox.bridge.core.builder.BridgeClassNameBuilder;
import com.github.doublebin.springfox.bridge.core.component.tuple.Tuple2;
import com.github.doublebin.springfox.bridge.core.exception.BridgeException;
import com.github.doublebin.springfox.bridge.core.model.GenericInfo;
import com.github.doublebin.springfox.bridge.core.util.FileUtil;
import com.github.doublebin.springfox.bridge.core.util.JavassistUtil;
import com.github.doublebin.springfox.bridge.core.util.ReflectUtil;
import com.github.doublebin.springfox.bridge.core.util.StringUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javassist.*;
import javassist.Modifier;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.repository.ClassRepository;
import sun.reflect.generics.tree.*;

import java.lang.reflect.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class GenericSubClassHandler {
    private static final ClassPool pool = ClassPool.getDefault();
    private static String classFilePath = FileUtil.getCurrentFilePath();

    public Class buildSubClass(ParameterizedTypeImpl parameterizedType, String apiModelValue) {
        try {
            Set<String> fieldNameSet = new HashSet<>();

            Class oldReturnClass = parameterizedType.getRawType();
            String newSubClassName = BridgeClassNameBuilder.buildNewSubClassName(parameterizedType.getRawType().getSimpleName());
            CtClass newReturnCtClass = pool.makeClass(newSubClassName);

            JavassistUtil.setSuperClassForCtClass(newReturnCtClass, oldReturnClass);//设置父类

            Map<String, Class> genericClassMap = getGenericClassMap(parameterizedType); //TODO 判空

            this.buildCtClassFromClass(fieldNameSet, genericClassMap, oldReturnClass, newReturnCtClass);


            ConstPool constpool = newReturnCtClass.getClassFile().getConstPool();
            Annotation apiModelAnnotation = new Annotation(ApiModel.class.getName(), constpool);
            if(StringUtils.isEmpty(apiModelValue)) {
                apiModelValue = newSubClassName;
            }
            apiModelAnnotation.addMemberValue("value", new StringMemberValue(apiModelValue, constpool));
            JavassistUtil.addAnnotationForCtClass(newReturnCtClass, apiModelAnnotation);
            newReturnCtClass.writeFile(classFilePath);

            Class newSubClass = newReturnCtClass.toClass();

            return newSubClass;
        } catch (Exception e) {
            log.error("New generic's sub class for old class [{}] failed.", parameterizedType.getRawType().getName(), e);
            throw new BridgeException("New generic's sub class failed", e);
        }
    }


    private Map<String, Class> getGenericClassMap(Map<String, Class> oldGenericClassMap, GenericInfo genericInfo) {

        try {
            String[] genericTypeNames = ReflectUtil.getGenericTypeNames(genericInfo.getClazz());
            List features = genericInfo.getFeatures();
            int length = genericTypeNames.length;

            Class[] classes = new Class[length];

            for (int i = 0; i < length; i++) {
                Object feature = features.get(i);
                if (feature instanceof String) {
                    classes[i] = oldGenericClassMap.get((String)feature);
                } else if (feature instanceof Class) {
                    classes[i] = (Class) feature;
                } else if (feature instanceof GenericInfo) {
                    classes[i] = buildSubClass(oldGenericClassMap, (GenericInfo)feature); //递归
                }
            }
            return getGenericClassMap(genericTypeNames,classes);
        } catch (Exception e) {
            throw new BridgeException("Get genericClassMap failed.", e);
        }
    }

    private Class buildSubClass(Map<String, Class> oldGenericClassMap, GenericInfo genericInfo) {
        try {
            Set<String> fieldNameSet = new HashSet<>();
            String newSubClassName = BridgeClassNameBuilder.buildNewSubClassName(genericInfo.getClazz().getSimpleName());

            CtClass newReturnCtClass = pool.makeClass(newSubClassName);

            JavassistUtil.setSuperClassForCtClass(newReturnCtClass, genericInfo.getClazz());//设置父类


            this.buildCtClassFromClass(fieldNameSet, oldGenericClassMap, genericInfo, newReturnCtClass);


            ConstPool constpool = newReturnCtClass.getClassFile().getConstPool();
            Annotation apiModelAnnotation = new Annotation(ApiModel.class.getName(), constpool);
            apiModelAnnotation.addMemberValue("value", new StringMemberValue(newSubClassName, constpool));
            JavassistUtil.addAnnotationForCtClass(newReturnCtClass, apiModelAnnotation);
            newReturnCtClass.writeFile(classFilePath);

            Class newSubClass = newReturnCtClass.toClass();

            return newSubClass;
        } catch (Exception e) {
            log.error("New generic's sub class for old class [{}] failed.", genericInfo.getClazz().getName(), e);
            throw new BridgeException("New generic's sub class failed", e);
        }
    }


    private void buildCtClassFromClass(Set<String> fieldNameSet, Map<String, Class> oldGenericClassMap, GenericInfo genericInfo, CtClass newReturnCtClass) throws Exception {

        Map<String, Class> genericClassMap = getGenericClassMap(oldGenericClassMap, genericInfo);

        if (!CollectionUtils.isEmpty(genericClassMap)) {
            Map<String, List<Field>> genericTypeFieldsMap = getGenericTypeFieldsMap(genericInfo.getClazz());

            Map<Field, Class> fieldNewClassMap = getFieldNewClassMap(genericInfo.getClazz(), genericClassMap);

            for (Field field : fieldNewClassMap.keySet()) {
                if (fieldNameSet.contains(field.getName())) {
                    continue;
                }
                fieldNameSet.add(field.getName());
                buildFieldInfos(field, fieldNewClassMap.get(field), genericInfo.getClazz(), newReturnCtClass);
            }

            for (String genericTypeName : genericClassMap.keySet()) {
                Class genericRealClass = genericClassMap.get(genericTypeName);
                List<Field> genericFields = genericTypeFieldsMap.get(genericTypeName);
                if (CollectionUtils.isEmpty(genericFields)) {
                    continue;
                }
                for (Field genericField : genericFields) {
                    if (fieldNameSet.contains(genericField.getName())) {
                        continue;
                    }
                    fieldNameSet.add(genericField.getName());
                    buildFieldInfos(genericField, genericRealClass, genericInfo.getClazz(), newReturnCtClass);
                }
            }
        }

        Class superClass = genericInfo.getClazz().getSuperclass();
        if (superClass.equals(Object.class)) {
            return;
        }

        Map<String, Class> superGenericClassMap = getGenericClassMap(genericInfo.getClazz(), genericClassMap);

        buildCtClassFromClass(fieldNameSet, superGenericClassMap, superClass, newReturnCtClass);
    }

    private Map<String, Class> getGenericClassMap(ParameterizedTypeImpl parameterizedType) {

        try {
            Map<String, Class> genericClassMap = new HashMap<String, Class>();

            Class oldReturnClass = parameterizedType.getRawType();
            Type[] genericTypes = parameterizedType.getActualTypeArguments();
            if (null == genericTypes || genericTypes.length == 0) {
                return genericClassMap;
            }
            String[] genericTypeNames = ReflectUtil.getGenericTypeNames(oldReturnClass);

            for (int i = 0; i < genericTypeNames.length; i++) {
                if (genericTypes[i] instanceof Class) {
                    genericClassMap.put(genericTypeNames[i], (Class) genericTypes[i]);
                } else if (genericTypes[i] instanceof ParameterizedTypeImpl) {
                    ParameterizedTypeImpl tempParameterizedTypeImpl = (ParameterizedTypeImpl) genericTypes[i];
                    genericClassMap.put(genericTypeNames[i], buildSubClass(tempParameterizedTypeImpl, null));
                }

            }

            return genericClassMap;
        } catch (Exception e) {
            throw new BridgeException("Get genericClassMap failed.", e);
        }
    }

    public Tuple2<GenericInfo, Boolean> getGenericFeature(String genericSignature) {
        try {
            boolean needSub = false;
            GenericInfo genericInfo = new GenericInfo();
            String className = StringUtils.replace(StringUtils.substringAfter(StringUtils.substringBefore(genericSignature, "<"), "L"), "/", ".");
            genericInfo.setClazz(Class.forName(className));
            List features = new ArrayList<>();

            String listStr = StringUtils.substringBeforeLast(StringUtils.substringAfter(genericSignature, "<"), ">");

            String[] strs = StringUtils.split(listStr, ";");

            String tmpStr = "";

            for (String str : strs) {
                tmpStr += str;
                if (!StringUtils.containsAny(tmpStr, "<", ">")) {
                    if (StringUtils.startsWith(tmpStr, "L")) {
                        String tempClassName = StringUtils.replace(StringUtils.substringAfter(tmpStr, "L"), "/", ".");
                        Class tempClass = Class.forName(tempClassName);
                        features.add(tempClass);
                    } else if (StringUtils.startsWith(tmpStr, "T")) {
                        features.add(StringUtils.substringAfter(tmpStr, "T"));
                        needSub = true;
                    }
                    tmpStr = "";
                } else {
                    int left = StringUtil.numberOfChar(tmpStr, '<');
                    int right = StringUtil.numberOfChar(tmpStr, '>');
                    if (StringUtil.numberOfChar(tmpStr, '<') == StringUtil.numberOfChar(tmpStr, '>')) {
                        Tuple2<GenericInfo, Boolean> tuple2 = this.getGenericFeature(tmpStr);
                        GenericInfo tempGenericFeature = tuple2.getFst();
                        if (tuple2.getSnd()) {
                            needSub = true;
                        }
                        features.add(tempGenericFeature);
                        tmpStr = "";
                    } else {
                        tmpStr += ";";
                    }
                }
            }

            genericInfo.setFeatures(features);

            return new Tuple2<GenericInfo, Boolean>(genericInfo, needSub);
        } catch (Exception e) {
            throw new BridgeException("Get genericFeature failed, genericSignature is " + genericSignature, e);
        }

    }

    private Map<Field, Class> getFieldNewClassMap(Class oldReturnClass, Map<String, Class> genericClassMap) throws Exception {
        Map<Field, Class> fieldNewClassMap = new HashMap<>();
        Field[] fields = oldReturnClass.getDeclaredFields();

        if (ArrayUtils.isNotEmpty(fields)) {
            for (Field field : fields) {
                String genericSignature = ReflectUtil.getGenericSignature(field);
                if (StringUtils.contains(genericSignature, "<")) {

                    Tuple2<GenericInfo, Boolean> tuple2 = this.getGenericFeature(genericSignature);
                    if (!tuple2.getSnd()) {
                        continue;
                    }
                    Class newFiledClass = buildSubClass(genericClassMap,tuple2.getFst() );
                    fieldNewClassMap.put(field, newFiledClass);
                }
            }
        }
        return fieldNewClassMap;
    }

    private Map<String, List<Field>> getGenericTypeFieldsMap(Class oldReturnClass) {
        Map<String, List<Field>> genericTypeFieldsMap = new HashMap<String, List<Field>>();

        Field[] fields = oldReturnClass.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            for (Field field : fields) {
                String genericSignature = ReflectUtil.getGenericSignature(field);
                if (null == genericSignature) {
                    continue;
                }
                if (StringUtils.contains(genericSignature, "<")) {
                    continue;
                }
                String genericFilterName = StringUtils.substring(genericSignature, 1, genericSignature.length() - 1);
                if (genericTypeFieldsMap.containsKey(genericFilterName)) {
                    genericTypeFieldsMap.get(genericFilterName).add(field);
                } else {
                    List<Field> fieldList = new ArrayList<>();
                    fieldList.add(field);
                    genericTypeFieldsMap.put(genericFilterName, fieldList);
                }
            }
        }
        return genericTypeFieldsMap;
    }


    private void buildCtClassFromClass(Set<String> fieldNameSet, Map<String, Class> genericClassMap, Class clazz, CtClass newReturnCtClass) throws Exception {
        if (!CollectionUtils.isEmpty(genericClassMap)) {
            Map<String, List<Field>> genericTypeFieldsMap = getGenericTypeFieldsMap(clazz);

            Map<Field, Class> fieldNewClassMap = getFieldNewClassMap(clazz, genericClassMap);

            for (Field field : fieldNewClassMap.keySet()) {
                if (fieldNameSet.contains(field.getName())) {
                    continue;
                }
                fieldNameSet.add(field.getName());
                buildFieldInfos(field, fieldNewClassMap.get(field), clazz, newReturnCtClass);
            }

            for (String genericTypeName : genericClassMap.keySet()) {
                Class genericRealClass = genericClassMap.get(genericTypeName);
                List<Field> genericFields = genericTypeFieldsMap.get(genericTypeName);
                if (CollectionUtils.isEmpty(genericFields)) {
                    continue;
                }
                for (Field genericField : genericFields) {
                    if (fieldNameSet.contains(genericField.getName())) {
                        continue;
                    }
                    fieldNameSet.add(genericField.getName());
                    buildFieldInfos(genericField, genericRealClass, clazz, newReturnCtClass);
                }
            }
        }

        Class superClass = clazz.getSuperclass();
        if (superClass.equals(Object.class)) {
            return;
        }

        Map<String, Class> superGenericClassMap = getGenericClassMap(clazz, genericClassMap);
        buildCtClassFromClass(fieldNameSet, superGenericClassMap, superClass, newReturnCtClass);
    }

    private Map<String, Class> getGenericClassMap(Class subClass, Map<String, Class> subGenericClassMap) throws ClassNotFoundException {
        List<SimpleClassTypeSignature> simpleClassTypeSignatures = getSimpleClassTypeSignaturesForSuper(subClass);
        Class superClass = subClass.getSuperclass();
        Map<String, Class> superGenericClassMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(simpleClassTypeSignatures)) {
            for (SimpleClassTypeSignature simpleClassTypeSignature : simpleClassTypeSignatures) {
                if (simpleClassTypeSignature.getName().equals(superClass.getName())) {
                    TypeArgument[] typeArguments = simpleClassTypeSignature.getTypeArguments();
                    if (ArrayUtils.isNotEmpty(typeArguments)) {
                        Class[] clazzes = getClasses(typeArguments, subGenericClassMap);
                        String[] genericTypeNames = ReflectUtil.getGenericTypeNames(superClass);
                        superGenericClassMap = getGenericClassMap(genericTypeNames, clazzes);
                    }
                } else {
                    continue;
                }
            }
        }
        return superGenericClassMap;
    }


    private List<SimpleClassTypeSignature> getSimpleClassTypeSignaturesForSuper(Class clazz) {
        ClassRepository classRepository = ReflectUtil.getClassRepository(clazz);
        ClassSignature classSignature = ReflectUtil.getClassSignatureTree(classRepository);
        ClassTypeSignature classTypeSignature = classSignature.getSuperclass();
        List<SimpleClassTypeSignature> simpleClassTypeSignatures = classTypeSignature.getPath();
        return simpleClassTypeSignatures;
    }

    private Class[] getClasses(TypeArgument[] typeArguments, Map<String, Class> genericClassMap) throws ClassNotFoundException {
        Class[] clazzes = new Class[typeArguments.length];
        for (int i = 0; i < typeArguments.length; i++) {
            if (typeArguments[i] instanceof TypeVariableSignature) {
                TypeVariableSignature typeVariableSignature = (TypeVariableSignature) typeArguments[i];
                String identifier = typeVariableSignature.getIdentifier();
                clazzes[i] = genericClassMap.get(identifier);
            } else if ((typeArguments[i] instanceof ClassTypeSignature)) {
                ClassTypeSignature tempclassTypeSignature = (ClassTypeSignature) typeArguments[i];
                List<SimpleClassTypeSignature> tempSimpleClassTypeSignatures = tempclassTypeSignature.getPath();
                SimpleClassTypeSignature tempSimpleClassTypeSignature = tempSimpleClassTypeSignatures.get(0);
                clazzes[i] = Class.forName(tempSimpleClassTypeSignature.getName());
            }
        }
        return clazzes;
    }

    private Map<String, Class> getGenericClassMap(String[] genericTypeNames, Class[] clazzes) {

        Map<String, Class> genericClassMap = new HashMap<String, Class>();
        for (int j = 0; j < genericTypeNames.length; j++) {
            genericClassMap.put(genericTypeNames[j], clazzes[j]);
        }
        return genericClassMap;
    }

    private void buildFieldInfos(Field genericField, Class genericRealClass, Class fieldAtClass, CtClass newReturnCtClass) {
        try {
            CtField ctField = new CtField(pool.get(genericRealClass.getName()), genericField.getName(), newReturnCtClass);
            ctField.setModifiers(Modifier.PRIVATE);
            newReturnCtClass.addField(ctField); //添加属性

            ApiModelProperty apiModelProperty = genericField.getAnnotation(ApiModelProperty.class);

            if (null == apiModelProperty) {
                Method getterMethod = fieldAtClass.getDeclaredMethod("get" + StringUtil.toCapitalizeCamelCase(genericField.getName()));
                if (null != getterMethod) {
                    apiModelProperty = getterMethod.getAnnotation(ApiModelProperty.class);
                }
            }

            if (null != apiModelProperty) {
                Annotation apiModelParopertyAnnotation = getApiModelPropertyAnnotation(apiModelProperty, newReturnCtClass.getClassFile().getConstPool());
                JavassistUtil.addAnnotationForCtField(ctField, apiModelParopertyAnnotation);
            }

            JavassistUtil.addGetterForCtField(ctField);
            JavassistUtil.addSetterForCtField(ctField);
        } catch (Exception e) {
            throw new BridgeException("Build field infos failed, field is " + genericField.getName(), e);
        }

    }

    private static Annotation getApiModelPropertyAnnotation(ApiModelProperty apiModelProperty, ConstPool constpool) {
        String[] annotationMethodNames = new String[]{"value", "name", "allowableValues", "access", "notes",
                "dataType", "required", "position", "hidden", "example", "readOnly", "reference", "allowEmptyValue"};
        return JavassistUtil.copyAnnotationValues(apiModelProperty, ApiModelProperty.class, constpool, annotationMethodNames);
    }
}
