package qyb.cn.qyb_anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)//注解只在编译期间保留
@Target(ElementType.TYPE)//目标是在类上面使用
public @interface Factory {
    String id();//对象唯一id
    Class<?> type();
}
