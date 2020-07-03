package qyb.cn.my_processor.factory;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class AnnotatedClassGroup {
    private static final String SUFFIX = "Factory";

    private String quafiedName;

    private Map<String, AnnotatedClass> map = new LinkedHashMap<>();

    public AnnotatedClassGroup(String quafiedName) {
        this.quafiedName = quafiedName;
    }

    public void addAnnotatedClass(AnnotatedClass clzz) {
        AnnotatedClass annotatedClass = map.get(clzz.getId());
        if (annotatedClass != null) {
            throw new IllegalArgumentException("id already exists");
        }
        map.put(clzz.getId(), clzz);
    }

    public void generateCode(Elements elementUtils, Filer filer) {
        TypeElement superClass = elementUtils.getTypeElement(quafiedName);
        String factoryName = superClass.getSimpleName() + SUFFIX;
        String fullFactoryName = superClass.getQualifiedName() + SUFFIX;

        PackageElement packageElement = elementUtils.getPackageOf(superClass);
        String packageName = packageElement.isUnnamed() ? null : packageElement.getQualifiedName().toString();

        MethodSpec.Builder method = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "id", Modifier.FINAL)
                .returns(TypeName.get(superClass.asType()));

        method.beginControlFlow("if (id == null)")
                .addStatement("return null")
                .endControlFlow();

        for (AnnotatedClass clzz: map.values()) {
            method.beginControlFlow("if($S.equals(id))", clzz.getId())
                    .addStatement("return new $L()", clzz.getTypeElement().getQualifiedName().toString())
                    .endControlFlow();
        }
        method.addStatement("return null");
        TypeSpec typeSpec = TypeSpec.classBuilder(factoryName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method.build()).build();
        System.out.println("factoryName: " + factoryName);
        try {
            JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
        } catch (IOException e) {
            System.out.println("io excep----" + e);
            e.printStackTrace();
        }
    }
}
