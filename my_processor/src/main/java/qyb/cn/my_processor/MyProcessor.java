package qyb.cn.my_processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

import qyb.cn.qyb_anno.BindView;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"qyb.cn.qyb_anno.MyTest", "qyb.cn.qyb_anno.BindView"})
public class MyProcessor extends AbstractProcessor {

    /**
     * 生成文件的工具类
     */
    private Filer filer;
    /**
     * 打印信息
     */
    private Messager messager;

    private Elements elementUtils;

    private Map<String, ProxyInfo> proxyInfoMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    private boolean process = false;
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("process start----");
//        if (process) {
//            return false;
//        }
//        process = true;

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        if (elements == null || elements.size() < 1) {
            return true;
        }

        for (Element element: elements) {
            VariableElement variableElement = (VariableElement) element; //注解标记的变量
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement(); //获取activity
            String className = typeElement.getQualifiedName().toString();
            String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
            int resId = element.getAnnotation(BindView.class).value();

            System.out.println("packageName: " + packageName + " className: " + className + " ,type: " + typeElement.getSimpleName()
            + " va: " + variableElement.getKind());

            ProxyInfo info = proxyInfoMap.get(className);
            if (info == null) {
                info = new ProxyInfo(typeElement, packageName);
                proxyInfoMap.put(className, info);
            }
            info.views.put(resId, variableElement);
        }

        for (String key: proxyInfoMap.keySet()) {
            ProxyInfo proxyInfo = proxyInfoMap.get(key);
            JavaFile javaFile = JavaFile.builder(proxyInfo.packageName, proxyInfo.generateCode()).build();
            try {
                javaFile.writeTo(filer);
            } catch (Exception e) {

            }
        }

        return true;
    }
}