package qyb.cn.my_processor.factory;

import com.google.auto.service.AutoService;

import java.io.IOException;
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

import qyb.cn.my_processor.exception.AnnotationException;
import qyb.cn.qyb_anno.Factory;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"qyb.cn.qyb_anno.Factory"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FactoryProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;

    private Map<String, AnnotatedClassGroup> annotatedGroups = new LinkedHashMap<>();

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
        try {
            Set<? extends Element> factoryElements = roundEnvironment.getElementsAnnotatedWith(Factory.class);
            for (Element element: factoryElements) {
                if (element.getKind() != ElementKind.CLASS) {
                    throw new AnnotationException(element, "注解只能用于类");
                }
                TypeElement typeElement = (TypeElement)element;
                AnnotatedClass clzz = new AnnotatedClass(typeElement);
                if (isValid(clzz)) {
                    AnnotatedClassGroup group = annotatedGroups.get(clzz.getTypeQualifedName());
                    if (group == null) {
                        group = new AnnotatedClassGroup(clzz.getTypeQualifedName());
                        annotatedGroups.put(clzz.getTypeQualifedName(), group);
                    }
                    group.addAnnotatedClass(clzz);
                }
            }
            for (AnnotatedClassGroup group: annotatedGroups.values()) {
                group.generateCode(elementUtils, filer);
            }
            annotatedGroups.clear();
        } catch (AnnotationException e) {
            error(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean isValid(AnnotatedClass annotatedClass) throws AnnotationException {
        TypeElement annotatedClassElement = annotatedClass.getTypeElement();
        //被注解的类必须为public
        if (!annotatedClassElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new AnnotationException(annotatedClassElement, "被注解的类必须为public");
        }
        //被注解的类不能是抽象类
        if (annotatedClassElement.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new AnnotationException(annotatedClassElement, "被注解的类不能为抽象类");
        }
        TypeElement superClass = elementUtils.getTypeElement(annotatedClass.getTypeQualifedName());
        // @Factory注解中，type指明的类是否为接口
        if (superClass.getKind() == ElementKind.INTERFACE) {
            //如果是接口，检查被注解的类是否实现了这个接口
            if (!annotatedClassElement.getInterfaces().contains(superClass.asType())) {
                throw new AnnotationException(annotatedClassElement, "被注解的类必须继承type接口");
            }
        } else {
            TypeElement currElement = annotatedClassElement;
            //检查父类中是否有
            while (true) {
                TypeMirror superClassType = currElement.getSuperclass();
                //找到头也没找到
                if (superClassType.getKind() == TypeKind.NONE) {
                    throw new AnnotationException(annotatedClassElement, "被注解的类必须继承或实现type接口");
                }
                //找到了
                if (superClassType.toString().equals(annotatedClass.getTypeQualifedName())) {
                    break;
                }
                currElement = (TypeElement) typeUtils.asElement(superClassType);
            }
        }
        for (Element e: annotatedClassElement.getEnclosedElements()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement cons = (ExecutableElement)e;
                if (cons.getParameters().size() == 0 && cons.getModifiers().contains(Modifier.PUBLIC)) {
                    return true;
                }
            }
        }
        throw new AnnotationException(annotatedClassElement, "被注解的类必须有一个无参的构造函数");
    }

    public void error(AnnotationException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), e.element);
    }
}
