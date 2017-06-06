package pig.dream.compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * AbstractProcessor 编译期生成代码 辅助类
 *
 * @author zhukun on 2017/6/6.
 */
public class ProcessorHelper {

    private final static boolean DBG = true;

    /**
     * 文件相关的辅助类
     */
    private final Filer filer;

    /**
     * 元素相关的辅助类
     */
    private final Elements elements;

    /**
     * 消息处理辅助类
     */
    private final Messager messager;

    private ProcessorHelper(final Filer filer, final Elements elements, final Messager messager) {
        this.filer = filer;
        this.elements = elements;
        this.messager = messager;
    }

    public static ProcessorHelper create(final Filer filer, final Elements elements, final Messager messager) {
        return new ProcessorHelper(filer, elements, messager);
    }

    public String getPackageOf(Element element) {
        return elements.getPackageOf(element).getQualifiedName().toString();
    }

    public void toWrite(String packageName, TypeSpec typeSpec) throws IOException {
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }

    public void i(String format, Object... args) {
        if (!DBG) {
            return;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(format, args));
    }

    public void e(String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
    }

}
