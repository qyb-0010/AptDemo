package qyb.cn.my_processor.factory;

import com.google.auto.service.AutoService;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
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
    private Elements elements;
    private Types types;

    private boolean process = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
        types = processingEnvironment.getTypeUtils();
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
            }
            TypeElement te = (TypeElement)element;
            new AnnotatedClass(te);
        }
        return true;
    }
}
