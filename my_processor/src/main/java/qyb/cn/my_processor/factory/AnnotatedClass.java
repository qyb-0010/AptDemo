package qyb.cn.my_processor.factory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

import qyb.cn.qyb_anno.Factory;

public class AnnotatedClass {
    private TypeElement annotatedClassElement;
    private String typeQualifiedName;
    private String typeSimpleName;
    private String id;

    public AnnotatedClass(TypeElement element) {
        this.annotatedClassElement = element;
        Factory factory = element.getAnnotation(Factory.class);
        id = factory.id();
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("id cannot be null");
        }
        try {
            Class<?> type = factory.type();
            typeQualifiedName = type.getCanonicalName();
            typeSimpleName = type.getSimpleName();
        } catch (MirroredTypeException e) {
            DeclaredType declaredType = (DeclaredType) e.getTypeMirror();
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            typeQualifiedName = typeElement.getQualifiedName().toString();
            typeSimpleName = typeElement.getSimpleName().toString();
        }
    }

    /**
     * @return @Factory注解中id()
     */
    public String getId() {
        return id;
    }

    /**
     * @return @Factory注解中type类的名字
     */
    public String getTypeName() {
        return typeSimpleName;
    }

    /**
     * @return @Factory注解中type类的全名
     */
    public String getTypeQualifedName() {
        return typeQualifiedName;
    }

    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}
