package qyb.cn.my_processor.factory;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import qyb.cn.my_processor.exception.AnnotationException;

public class AnnotatedClassGroup {
    private static final String SUFFIX = "Factory";

    private String typeQualifiedName;

    private Map<String, AnnotatedClass> annotatedClassGroup = new LinkedHashMap<>();

    public AnnotatedClassGroup(String qualifiedName) {
        this.typeQualifiedName = qualifiedName;
    }

    public void addAnnotatedClass(AnnotatedClass clzz) throws AnnotationException {
        AnnotatedClass annotatedClass = annotatedClassGroup.get(clzz.getId());
        if (annotatedClass != null) {
            throw new AnnotationException(clzz.getTypeElement(), "id already exists");
        }
        annotatedClassGroup.put(clzz.getId(), clzz);
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        TypeElement superClass = elementUtils.getTypeElement(typeQualifiedName);
        String factoryName = superClass.getSimpleName() + SUFFIX;

        PackageElement packageElement = elementUtils.getPackageOf(superClass);
        String packageName = packageElement.isUnnamed() ? "" : packageElement.getQualifiedName().toString();

        MethodSpec.Builder method = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "id", Modifier.FINAL)
                .returns(TypeName.get(superClass.asType()));

        method.beginControlFlow("if (id == null)")
                .addStatement("return null")
                .endControlFlow();

        for (AnnotatedClass clzz: annotatedClassGroup.values()) {
            method.beginControlFlow("if($S.equals(id))", clzz.getId())
                    .addStatement("return new $L()", clzz.getTypeElement().getQualifiedName().toString())
                    .endControlFlow();
        }
        method.addStatement("return null");
        TypeSpec typeSpec = TypeSpec.classBuilder(factoryName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(method.build()).build();
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }
}
