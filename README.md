### android apt
apt全称是annotation process tool是一种处理注解的工具，它对源代码文件进行检测找出其中的Annotation，对其进行额外处理。Annotation处理器在处理Annotation的时候可以根据注解生成额外的源文件和其他文件，并且apt还会编译生成的源文件和原来的源文件

### AbstractProcessor
每一个注解处理器都必须继承自AbstractProcessor
#```
public class TestProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }
}
#```
每一个注解处理器类都必须提供一个空的构造函数。init方法会被注解处理工具调用，输入ProcessingEnvironment参数。这个参数提供了很多有用的工具类，比如Elements, Types和Filer

Elements： 一个用来处理Element的工具类

Types： 一个用来处理TypeMirror的工具类

Filer： 使用filer可以创建文件
#```
public synchronized void init(ProcessingEnvironment processingEnvironment)
#```

process相当于处理器的主函数main()，在这个方法里完成扫描，评估和处理注解代码，以及生成java文件，参数roundEnvironment可以查询出特定注解的被注解元素。
#```
public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment)
#```

这里必须制定此注解处理器是用于处理哪些注解的。返回值为一个set，包含要处理注解的类的全称
#```
public Set<String> getSupportedAnnotationTypes()
#```

指定使用的Java版本
#```
public SourceVersion getSupportedSourceVersion()
#```

在java7中也可以使用注解来代替getSupportedAnnotationTypes()和getSupportedSourceVersion()两个方法
#```
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({"com.example.annotation.Test"})
public class TestProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        return false;
    }
}
#```
### 注册注解处理器
如何告诉系统TestProcessor是一个注解处理器呢？在android中需要在main/resources目录下建立META-INF/services目录，在此目录中新建一个名为
#```
javax.annotation.processing.Processor
#```
的文件, 内容是注解处理器的合法全名列表，每一个元素换行分割，比如：
#```
com.example.anno_processor.TestProcessor
com.example.anno_processor.BindViewProcessor
com.example.anno_processor.MyProcessor
#```
谷歌提供了一种简单的方法，让我们不用去维护这个文件，在processor的类上加入@AutoService(Processor.class)注解
#```
@AutoService(Processor.class)
public class TestProcessor extends AbstractProcessor 
#```

### 示例：工厂模式
下面以一个例子来说明注解处理器是怎么使用的。
假如麦当劳给顾客提供两种食物汉堡hamburger和炸鸡fried chicken, 以及一种甜点冰淇淋icecream.
#```
public interface Food {
    float getPrice();
}

public class Hamburger implements Food {
    @Override
    public float getPrice() {
        return 15.5F;
    }
}

public class FriedChicken implements Food {
    @Override
    public float getPrice() {
        return 10F;
    }
}

public class IceCream implements Food {
    @Override
    public float getPrice() {
        return 5F;
    }
}
#```
为了在麦当劳下单，需要输入食物的名字：
#```
public class Mcdonalds {

    public Food order(String name) {
        if (TextUtils.isEmpty(name)) {
            throw new RuntimeException("name cannot be empty");
        }
        if (name.equals("fired_chicken")) {
            return new FriedChicken();
        }
        if (name.equals("hamburger")) {
            return new Hamburger();
        }
        if (name.equals("ice_cream")) {
            return new IceCream();
        }
        throw new RuntimeException("no such food " + name);
    }
}
#```
Mcdonalds类的缺点显而易见，如果我们增加食物种类，都需要增加一个if分支。如果我们使用注解去自动生成这个类那将会节省很多工作。

我们来定义一个注解 @Factory, 首先新建一个annotation module
#```
@Retention(RetentionPolicy.CLASS)//注解只在编译期间保留
@Target(ElementType.TYPE)//目标是在类上面使用
public @interface Factory {
    String id();//对象唯一id
    Class<?> type();
}
#```
我的想法是同样的type会放在同一个工厂类中，用唯一表示id做映射。
#```
@Factory(id = "hamburger", type = Food.class)
public class Hamburger implements Food {
    @Override
    public float getPrice() {
        return 15.5F;
    }
}

@Factory(id = "fried_chicken", type = Food.class)
public class FriedChicken implements Food {
    @Override
    public float getPrice() {
        return 10F;
    }
}
#```
因为注解并不会被继承，所以并不能把注解写在接口上，而且我的目的是找到被注解的类并创建出一个对象，所以被注解的类需要遵循以下规则：
1. @Factory只能注解类
2. 被注解的类不能是抽象类，而且必须有个默认的构造函数
3. 被注解的类必须间接或者直接继承于type()类型
4. id只能为string类型，并且在同一个type中唯一

### Processor
接下来就开始编写注解处理器了，一般的惯例是单独新建一个module来处理，新建module annotation_processor
#```
@AutoService(Processor.class)
@SupportedAnnotationTypes({"qyb.cn.qyb_anno.Factory"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class FactoryProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Types types;

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
        return false;
    }
}
#```
在init方法中，我们获取一些工具类：
- Elements

在注解的处理过程中，会扫描java源代码，java源代码的每个部分都是特定的element，具体如下所示：
#```
package com.example;    // PackageElement

public class Foo {      // TypeElement

    private int a;      // VariableElement
    private Foo other;  // VariableElement

    public Foo () {     // ExecuteableElement
    }    

    public void setA (  // ExecuteableElement
        int newA         // VariableElement
    ) {
    }
}
#```
同时，每个element还可以访问到它的父或者子元素上
```
TypeElement fooClass = ... ;  
for (Element e : fooClass.getEnclosedElements()){ // iterate over children  
    Element parent = e.getEnclosingElement();  // parent == fooClass
}
```
所以Element代表源代码，TypeElement代表源代码中的类型元素，比如类或者变量。但是TypeElement并不包含类本身的信息，虽然可用通过TypeElement获取类的名字，但是获取不到类的信息，比如他的父类等，这些信息需要通过TypeMirror获取，可用通过element.asType()获取。

### 搜索@Factory注解
```
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> factoryElements = roundEnvironment.getElementsAnnotatedWith(Factory.class);
        for (Element element: factoryElements) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "factory注解必须注解在类上",
                        element);
            }
        }
        return false;
    }
```
搜索注解很简单，使用getElementsAnnotatedWith(Factory.class)就可以找到，但是需要判断注解是否被正确使用，如果没有正确使用则最好用messager工具类来抛出信息，如果直接抛出异常会导致jvm崩溃，从而打印出晦涩难懂的方法栈，不利于找错误。

### 数据模型
我们将被注解的元素信息存放于一个对象中，以方便后续生成代码。
```
public class AnnotatedClass {
    private TypeElement annotatedClassElement;
    private String typeFullName;
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
            typeFullName = type.getCanonicalName();
            typeSimpleName = type.getSimpleName();
        } catch (MirroredTypeException e) {
            DeclaredType declaredType = (DeclaredType) e.getTypeMirror();
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            typeFullName = typeElement.getQualifiedName().toString();
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

    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }
}
```
Class<?> type = factory.type();获取的是Class类型，意味着这是一个真正的Class对象。因为注解处理是在java源码编译之前，所以需要考虑两种情况：
1. 这个类已经被编译：这种情况是三方库中的类被@Factory注解，可以直接获取Class.
2. 这个类还没有被编译：这种情况是我们尝试编译被@Factory注解的源代码，这种情况下，直接获取Class会抛出MirroredTypeException异常。在这个异常中包含一个TypeMirror，它表示我们未编译的类。因为之前检查了他是否为一个Class类型，所以这儿可用放心的强转成DeclaredType然后读取TypeElement的合法名字。

还需要一个对象，这个对象会将所有拥有相同类型Type的类组合到一起：
```
public class AnnotatedClassGroup {
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

    public void generateCode() {
        //生成代码
    }
}
```
按照面向对象的思想，其实就是把具有相同type的被注解类收集到一个group中，方便后续生成代码。

继续实现process中代码，接下来是检查被注解的类是否合法:
```
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
        TypeElement superClzz = elements.getTypeElement(clzz.getTypeFullName());
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

                currElement = (TypeElement) types.asElement(superClassType);
            }

            for (Element e: typeElement.getEnclosedElements()) {
                if (e.getKind() == ElementKind.CONSTRUCTOR) {
                    ExecutableElement cons = (ExecutableElement)e;
                    if (cons.getParameters().size() == 0 && cons.getModifiers().contains(Modifier.PUBLIC)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
```
一旦被注解类的合法性检查成功，就会将这个类加入group中
```
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> factoryElements = roundEnvironment.getElementsAnnotatedWith(Factory.class);
        for (Element element: factoryElements) {
            if (element.getKind() != ElementKind.CLASS) {
                error("注解必须在类上使用", element);
                return true;
            }
            try {
                TypeElement typeElement = (TypeElement)element;
                AnnotatedClass clzz = new AnnotatedClass(typeElement);
                if (!isValid(clzz)) {
                    return true;
                }

                AnnotatedClassGroup group = map.get(clzz.getTypeQualifedName());
                if (group == null) {
                    group = new AnnotatedClassGroup(clzz.getTypeQualifedName());
                    map.put(clzz.getTypeQualifedName(), group);
                }
                group.addAnnotatedClass(clzz);
            } catch (Exception e) {
            }
        }
        try {
            for (AnnotatedClassGroup group: map.values()) {
                group.generateCode(elementUtils, filer);
            }
        } catch (Exception e) {
        }
        return true;
    }
```

### 代码生成
完成了group分类之后就可以进行代码的生成了，代码生成可以使用javaPoet库。
```
    public void generateCode(Elements elementUtils, Filer filer) {
        TypeElement superClass = elementUtils.getTypeElement(typeQualifiedName);
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

        for (AnnotatedClass clzz: annotatedClassGroup.values()) {
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
            e.printStackTrace();
        }
    }
```

### 注意事项
注解处理的过程可能会不止一次，也就是process方法可能会执行多次，原因上生成的代码文件中还可能包含@Factory注解，然后这些注解还是会被FactoryProcessor处理。所以当build项目的时候很可能发生异常：
```
javax.annotation.processing.FilerException: Attempt to recreate a file for type qyb.cn.myapt.food.FoodFactory
	at com.sun.tools.javac.processing.JavacFiler.checkNameAndExistence(JavacFiler.java:522)
	at com.sun.tools.javac.processing.JavacFiler.createSourceOrClassFile(JavacFiler.java:396)
	at com.sun.tools.javac.processing.JavacFiler.createSourceFile(JavacFiler.java:378)
	at com.squareup.javapoet.JavaFile.writeTo(JavaFile.java:113)
```
这是因为在第二轮process的适合仍然保留着上次的数据，所以可以简单的修复下这个问题，在process的最后加上
```
map.clear()
```
到此为止，编译下之后 就可以生成需要的代码了。
有一个需要注意的地方，我们引入autoservice注解，而autoservice注解本身也需要一个注解处理器，所以需要在build.gradle中添加
```
implementation 'com.google.auto.service:auto-service:1.0-rc2'
annotationProcessor 'com.google.auto.service:auto-service:1.0-rc2'
```
参考资料：http://hannesdorfmann.com/annotation-processing/annotationprocessing101
