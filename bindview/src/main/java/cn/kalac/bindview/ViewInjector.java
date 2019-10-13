package cn.kalac.bindview;

/**
 * 生成类将是该接口的实现类
 * @author kalac.
 * @date 2019/10/13 16:43
 */
public interface ViewInjector<T> {
    /**
     * 在方法实现中完成findViewById的操作
     * @param host
     * @param source
     */
    void inject(T host, Object source);
}