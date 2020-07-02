package qyb.cn.view_injector;

public interface IViewInjector<T> {
    void inject(T target, Object source);
}
