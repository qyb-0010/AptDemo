package qyb.cn.my_processor.exception;

import javax.lang.model.element.Element;

public class AnnotationException extends Exception {

    public Element element;

    public AnnotationException(Element element, String errorMsg) {
        super(errorMsg);
        this.element = element;
    }
}
