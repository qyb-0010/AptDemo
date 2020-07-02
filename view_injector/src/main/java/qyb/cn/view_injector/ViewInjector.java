package qyb.cn.view_injector;

import android.app.Activity;
import android.util.Log;
import android.view.View;

public class ViewInjector {

    public static final String PROXY = "_ViewBinding";

    public static void inject(Activity activity) {
        IViewInjector injector = findInjector(activity);
        if (injector != null) {
            injector.inject(activity, activity);
        }
    }

    public static void inject(Object obj, View view) {
        IViewInjector<Object> injector = findInjector(obj);
        if (injector != null) {
            injector.inject(obj, view);
        }
    }

    public static IViewInjector findInjector(Object obj) {
        String proxyClassName = obj.getClass().getName() + PROXY;
        Log.e("dx","proxyClassName: " + proxyClassName);
        try {
            Class clzz = Class.forName(proxyClassName);
            Log.e("dx","");
            return (IViewInjector) clzz.newInstance();
        } catch (ClassNotFoundException e) {
            Log.e("dx","");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
