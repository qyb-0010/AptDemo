package qyb.cn.my_processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public class ProxyInfo {

    public static final String PROXY = "_ViewBinding";

    public TypeElement typeElement;

    public String packageName;

    public String proxyClass;

    public Map<Integer, VariableElement> views = new HashMap<>();

    public ProxyInfo(TypeElement typeElement, String packageName) {
        this.typeElement = typeElement;
        this.packageName = packageName;
        String clazzName = getClassNama(typeElement, packageName);
        System.out.println("clazzname: " + clazzName + " packagename:" + packageName);
        proxyClass = clazzName + PROXY;
    }

    private String getClassNama(TypeElement type, String packageName) {
        int pkgLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(pkgLen).replace('.', '$');
    }

    public TypeSpec generateCode() {
        ClassName viewInjector = ClassName.get("qyb.cn.view_injector", "IViewInjector");
        ClassName className = ClassName.get(typeElement);
        ParameterizedTypeName ptn = ParameterizedTypeName.get(viewInjector, className);

//        TypeVariableName typeVariableName = TypeVariableName.get("T");

//        MethodSpec.Builder consBuilder = MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC)
//                .addParameter(className, "target")
//                .addStatement("this.target = target");

        MethodSpec.Builder builder1 = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
//                .addAnnotation(Override.class)
                .addParameter(className, "target")
                .addParameter(Object.class, "source");

        for (int id: views.keySet()) {
            VariableElement element= views.get(id);
            String fieldName= element.getSimpleName().toString();
            builder1.addStatement("if (source instanceof android.app.Activity) {target.$L = ((android.app.Activity) source).findViewById($L);}"
            + " else {target.$L = ((android.view.View) source).findViewById($L);}", fieldName, id, fieldName, id);
        }
        MethodSpec bindMethodSpec = builder1.build();
//        MethodSpec cons = consBuilder.build();
        return TypeSpec.classBuilder(proxyClass)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ptn)
                .addMethod(bindMethodSpec)
//                .addTypeVariable(typeVariableName)
//                .addMethod(cons)
                .addField(className, "target", Modifier.PRIVATE)
                .build();
    }
}
