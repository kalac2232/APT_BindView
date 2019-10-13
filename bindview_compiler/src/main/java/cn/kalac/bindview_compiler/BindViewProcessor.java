package cn.kalac.bindview_compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import cn.kalac.bindview_annotation.BindView;

@AutoService(Processor.class)    //帮我们生成 META-INF 信息
@SupportedAnnotationTypes("cn.kalac.bindview_annotation.BindView")    //要处理的注解类型
@SupportedSourceVersion(SourceVersion.RELEASE_7)    //支持的源码版本

/**
 * @author kalac.
 * @date 2019/10/13 15:54
 */
public class BindViewProcessor extends AbstractProcessor {

    /**
     * 基于元素进行操作的工具方法
     */
    /**
     * Element 的子类
     - VariableElement //一般代表成员变量
     - ExecutableElement //一般代表类中的方法
     - TypeElement //一般代表代表类
     - PackageElement //一般代表Package
     */
    private Elements mElementUtils;

    /**
     * 代码创建者
     */
    private Filer mFileCreator;

    /**
     * 日志，提示者，提示错误、警告
     */
    private Messager mMessager;

    private Map<String, ProxyInfo> mProxyMap = new HashMap<>();


    /**
     * @param processingEnvironment 注解处理环境，通过它可以获取很多功能类
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mElementUtils = processingEnv.getElementUtils();
        mFileCreator = processingEnv.getFiler();
        mMessager = processingEnv.getMessager();
    }

    /**
     * @param set
     * @param roundEnvironment 提供一个注解处理器，在编译时可以查询类的信息
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process...");
        //避免生成重复的代理类
        mProxyMap.clear();

        //拿到被 @BindView 注解修饰的元素，应该是 VariableElement
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        //1.收集信息
        for (Element element : elements) {
            //去除不合格的元素
            if (!checkAnnotationValid(element, BindView.class)) {
                continue;
            }

            //类中的成员变量
            VariableElement variableElement = (VariableElement) element;
            //类或者接口
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            //完整的名称
            String qualifiedName = typeElement.getQualifiedName().toString();

            ProxyInfo proxyInfo = mProxyMap.get(qualifiedName);
            if (proxyInfo == null) {
                //将该类中被注解修饰的变量加入到 ProxyInfo 中
                proxyInfo = new ProxyInfo(mElementUtils, typeElement);
                mProxyMap.put(qualifiedName, proxyInfo);
            }

            BindView annotation = variableElement.getAnnotation(BindView.class);
            if (annotation != null) {
                //取得设置的id
                int id = annotation.value();
                proxyInfo.mInjectElements.put(id, variableElement);
            }
        }

        //2.生成代理类
        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                //创建文件对象
                JavaFileObject sourceFile = mFileCreator.createSourceFile(
                        proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement());
                Writer writer = sourceFile.openWriter();
                writer.write(proxyInfo.generateJavaCode());     //写入文件
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                error(proxyInfo.getTypeElement(), "Unable to write injector for type %s: %s", proxyInfo.getTypeElement(), e.getMessage());
            }
        }

        return true;
    }

    /**
     * 检查 element 类型是否规范
     *
     * @param element
     * @param clazz
     * @return
     */
    private boolean checkAnnotationValid(final Element element, final Class<?> clazz) {
        if (element == null || element.getKind() != ElementKind.FIELD) {
            error(element, "%s must be declared on field !", clazz.getSimpleName());
            return false;
        }
        if (element.getModifiers().contains(Modifier.PRIVATE)) {
            error(element, "%s must not be private !", element.getSimpleName());
            return false;
        }
        return true;
    }


    private void error(final Element element, String msg, final Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
            mMessager.printMessage(Diagnostic.Kind.ERROR, msg, element);
        }
    }
}
