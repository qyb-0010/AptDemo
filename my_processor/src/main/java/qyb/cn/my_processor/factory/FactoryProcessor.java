package qyb.cn.my_processor.factory;

import com.google.auto.service.AutoService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import qyb.cn.qyb_anno.Factory;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"qyb.cn.qyb_anno.Factory"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FactoryProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;

    private boolean process = false;

    private Map<String, AnnotatedClassGroup> map = new LinkedHashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (process) {
            return true;
        }
        process = true;
        Set<? extends Element> factoryElements = roundEnvironment.getElementsAnnotatedWith(Factory.class);
        for (Element element: factoryElements) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "factory注解必须注解在类上",
                        element);
                return true;
            }
            try {
                TypeElement typeElement = (TypeElement)element;
                AnnotatedClass clzz = new AnnotatedClass(typeElement);
                if (!isValid(clzz)) {
                    return true;
                }

                AnnotatedClassGroup group = map.get(clzz.getTypeFullName());
                if (group == null) {
                    group = new AnnotatedClassGroup(clzz.getTypeFullName());
                    map.put(clzz.getTypeFullName(), group);
                }
                group.addAnnotatedClass(clzz);
            } catch (Exception e) {
                System.out.println("excep11----" + e);
            }
        }

        try {
            for (AnnotatedClassGroup group: map.values()) {
                group.generateCode(elementUtils, filer);
            }
        } catch (Exception e) {
            System.out.println("generate code excep----" + e);
        }

        return true;
    }

    private boolean isValid(AnnotatedClass clzz) {
        TypeElement typeElement = clzz.getTypeElement();
        //被注解的类必须为public
        if (!typeElement.getModifiers().contains(Modifier.PUBLIC)) {
            return false;
        }
        //被注解的类不能是抽象类
        if (typeElement.getModifiers().contains(Modifier.ABSTRACT)) {
            return false;
        }
        TypeElement superClzz = elementUtils.getTypeElement(clzz.getTypeFullName());
        // @Factory注解中，type指明的类是否为接口
        if (superClzz.getKind() == ElementKind.INTERFACE) {
            //如果是接口，检查被注解的类是否实现了这个接口
            if (!typeElement.getInterfaces().contains(superClzz.asType())) {
                return false;
            }
        } else {
            TypeElement currElement = typeElement;
            //检查父类中是否有
            while (true) {
                TypeMirror superClassType = currElement.getSuperclass();
                //找到头也没找到
                if (superClassType.getKind() == TypeKind.NONE) {
                    return false;
                }
                //找到了
                if (superClassType.toString().equals(clzz.getTypeFullName())) {
                    break;
                }

                currElement = (TypeElement) typeUtils.asElement(superClassType);
            }
        }
        for (Element e: typeElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement cons = (ExecutableElement)e;
                if (cons.getParameters().size() == 0 && cons.getModifiers().contains(Modifier.PUBLIC)) {
                    return true;
                }
            }
        }
        return false;
    }
}
