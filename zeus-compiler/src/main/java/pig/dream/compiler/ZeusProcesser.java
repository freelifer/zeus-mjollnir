package pig.dream.compiler;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import pig.dream.annotations.BindIntent;
import pig.dream.annotations.BindView;
import pig.dream.annotations.RouterActivity;

/**
 * @author zhukun on 2017/4/15.
 */

@AutoService(Processor.class)
public class ZeusProcesser extends AbstractProcessor {

    private Filer filer; //文件相关的辅助类
    private Elements elementUtils; //元素相关的辅助类
    private Messager messager;

    private Map<String, AnnotatedClass> mAnnotatedClassMap = new HashMap<>();
    private Map<String, RouteActivityField> mRouteActivityMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    /**
     * @return 指定哪些注解应该被注解处理器注册
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        types.add(RouterActivity.class.getCanonicalName());
        types.add(BindIntent.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // process() will be called several times
        mAnnotatedClassMap.clear();
        mRouteActivityMap.clear();

        messager.printMessage(Diagnostic.Kind.NOTE, "Printing: 111");
        for (TypeElement te : annotations) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Printing type: " + te.toString());
            for (Element e : roundEnv.getElementsAnnotatedWith(te)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "Printing: " + e.toString() + " " + e.getSimpleName());
            }
        }
        try {
            processBindView(roundEnv);
            processBindIntent(roundEnv);
            processRouteActivity(roundEnv);
        } catch (IllegalArgumentException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            return true; // stop process
        }

        generateBinder();
        generateRouteActivity();

        return true;
    }

    private void generateBinder() {
        for (AnnotatedClass annotatedClass : mAnnotatedClassMap.values()) {
            try {
                annotatedClass.generateActivityCoreClass().writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Generate file failed, reason: " + e.getMessage());
            }
        }
    }

    private void generateRouteActivity() {
        if (mRouteActivityMap.size() <= 0) {
            return;
        }
        try {
            RouterAnnotatedClass.generateRouteActivityClass(mAnnotatedClassMap, mRouteActivityMap).writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Generate file failed, reason: " + e.getMessage());
        }
    }

    private void processBindView(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(BindView.class)) {
            AnnotatedClass annotatedClass = getAnnotatedClass(element);
            BindViewField field = new BindViewField(element);
            annotatedClass.addBindViewField(field);
        }
    }

    private void processBindIntent(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(BindIntent.class)) {
            AnnotatedClass annotatedClass = getAnnotatedClass(element);
            BindIntentField field = new BindIntentField(element);
            annotatedClass.addBindIntentField(field);
        }
    }

    private void processRouteActivity(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(RouterActivity.class)) {
            RouteActivityField routeActivityField = new RouteActivityField(element);
            String fullClassName = routeActivityField.getFullClassName();
            messager.printMessage(Diagnostic.Kind.NOTE, "RouteActivity " + fullClassName);
            if (!mRouteActivityMap.containsKey(fullClassName)) {
                mRouteActivityMap.put(fullClassName, routeActivityField);
            }
        }

    }

    private AnnotatedClass getAnnotatedClass(Element element) {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = classElement.getQualifiedName().toString();
        messager.printMessage(Diagnostic.Kind.NOTE, "Printing fullClassName: " + fullClassName + " type " + classElement.toString());
        AnnotatedClass annotatedClass = mAnnotatedClassMap.get(fullClassName);
        if (annotatedClass == null) {
            annotatedClass = new AnnotatedClass(classElement, elementUtils, messager);
            mAnnotatedClassMap.put(fullClassName, annotatedClass);
        }
        return annotatedClass;
    }

    private void error(String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
    }
}
