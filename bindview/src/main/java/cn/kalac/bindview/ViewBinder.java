package cn.kalac.bindview;

import android.app.Activity;

/**
 * @author kalac.
 * @date 2019/10/13 16:18
 */
public class ViewBinder {

    /**
     * 生成class后缀
     */
    private static final String SUFFIX = "$$ViewInjector";

    public static void bind(Activity activity) {
        //找到生成类
        ViewInjector proxyActivity = findProxyActivity(activity);

        if (proxyActivity != null) {
            proxyActivity.inject(activity,activity);
        }

    }

    /**
     * 通过当前activity找到通过apt生成的类
     * @param activity
     * @return
     */
    private static ViewInjector findProxyActivity(Object activity) {
        try {
            Class clazz = activity.getClass();
            Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
            return (ViewInjector) injectorClazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException(String.format("can not find %s , something when compiler.", activity.getClass().getSimpleName() + SUFFIX));
    }
}
