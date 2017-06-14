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

import freelifer.zeus.mjollnir.annotations.BindIntent;
import freelifer.zeus.mjollnir.annotations.BindTarget;
import freelifer.zeus.mjollnir.annotations.BindView;
import freelifer.zeus.mjollnir.annotations.Module;
import freelifer.zeus.mjollnir.annotations.Provides;
import freelifer.zeus.mjollnir.annotations.RouterActivity;

/**
 * @author zhukun on 2017/4/15.
 */

@AutoService(Processor.class)
public class ZeusProcesser extends AbstractProcessor {

    private ProcessorHelper processorHelper;
    private Filer filer; //文件相关的辅助类
    private Elements elementUtils; //元素相关的辅助类
    private Messager messager;

    private Map<String, BindAnnotatedClass> bindAnnotatedClassMap = new HashMap<>();
    private Map<String, RouteActivityField> routeActivityMap = new HashMap<>();
    private ModuleAnnotatedClass moduleAnnotatedClass;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();

        processorHelper = ProcessorHelper.create(filer, elementUtils, messager);
        moduleAnnotatedClass = ModuleAnnotatedClass.create(elementUtils, filer, messager);
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
        types.add(Module.class.getCanonicalName());
        types.add(BindTarget.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // process() will be called several times
        bindAnnotatedClassMap.clear();
        routeActivityMap.clear();
        moduleAnnotatedClass.clearMap();

        for (TypeElement te : annotations) {
            processorHelper.i("Printing type: " + te.toString());
            for (Element e : roundEnv.getElementsAnnotatedWith(te)) {
                processorHelper.i("Printing: " + e.toString() + " " + e.getSimpleName());
            }
        }
        try {
            processBindView(roundEnv);
            processBindIntent(roundEnv);
            processRouteActivity(roundEnv);
            processBindTarget(roundEnv);
            processModule(roundEnv);
            processProvide(roundEnv);
        } catch (IllegalArgumentException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            return true; // stop process
        }

        generateBinder();
        generateRouteActivity();
        generateModules();
        return true;
    }

    private void generateBinder() {
        for (BindAnnotatedClass bindAnnotatedClass : bindAnnotatedClassMap.values()) {
            try {
                bindAnnotatedClass.generateActivityCoreClass().writeTo(filer);
                MembersInjectorGenerater.generate(bindAnnotatedClass, moduleAnnotatedClass);
            } catch (IOException e) {
                processorHelper.e("generateBinder file failed, reason: " + e.getMessage());
            }
        }
    }

    private void generateRouteActivity() {
        if (routeActivityMap.size() <= 0) {
            return;
        }
        try {
            RouterAnnotatedClass.generateRouteActivityClass(bindAnnotatedClassMap, routeActivityMap).writeTo(filer);
        } catch (IOException e) {
            processorHelper.e("generateRouteActivity file failed, reason: " + e.getMessage());
        }
    }

    private void generateModules() {
        try {
            moduleAnnotatedClass.generateMainModule();
        } catch (IOException e) {
            processorHelper.e("generateModules file failed, reason: " + e.getMessage());
        }
    }

    private void processBindView(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(BindView.class)) {
            BindAnnotatedClass bindAnnotatedClass = getAnnotatedClass(element);
            BindViewField field = new BindViewField(element);
            bindAnnotatedClass.addBindViewField(field);
        }
    }

    private void processBindIntent(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(BindIntent.class)) {
            BindAnnotatedClass bindAnnotatedClass = getAnnotatedClass(element);
            TypeElement classElement = (TypeElement) element.getEnclosingElement();
            String simpleName = classElement.getSimpleName().toString();
            BindIntentField field = new BindIntentField(element, simpleName);
            bindAnnotatedClass.addBindIntentField(field);
        }
    }

    private void processRouteActivity(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(RouterActivity.class)) {
            RouteActivityField routeActivityField = new RouteActivityField(element);
            String fullClassName = routeActivityField.getFullClassName();
            processorHelper.i("RouteActivity " + fullClassName);
            if (!routeActivityMap.containsKey(fullClassName)) {
                routeActivityMap.put(fullClassName, routeActivityField);
            }
        }
    }

    private void processBindTarget(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(BindTarget.class)) {
            BindAnnotatedClass bindAnnotatedClass = getAnnotatedClass(element);
            BindTargetField field = new BindTargetField(element);
            bindAnnotatedClass.addBindTargetField(field);
        }
    }

    private void processModule(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(Module.class)) {
            TypeElement typeElement = (TypeElement) element;
            String fullClassName = typeElement.getQualifiedName().toString();
            ModuleField moduleField = moduleAnnotatedClass.getModuleField(fullClassName);
            if (moduleField == null) {
                moduleField = new ModuleField(typeElement, elementUtils);
                moduleAnnotatedClass.putModuleField(fullClassName, moduleField);
            }
        }
    }

    private void processProvide(RoundEnvironment roundEnv) throws IllegalArgumentException {
        for (Element element : roundEnv.getElementsAnnotatedWith(Provides.class)) {
            ModuleField moduleField = getModuleField(element);

            if (moduleField == null) {
                processorHelper.e("Annotated Provides can not use alone");
                continue;
            }
            moduleField.addProvide(ProvideField.create(element).println(messager));
        }
    }

    private BindAnnotatedClass getAnnotatedClass(Element element) {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = classElement.getQualifiedName().toString();
        processorHelper.i("Printing fullClassName: " + fullClassName + " type " + classElement.toString() + " " + classElement.asType().getClass());
        BindAnnotatedClass bindAnnotatedClass = bindAnnotatedClassMap.get(fullClassName);
        if (bindAnnotatedClass == null) {
            bindAnnotatedClass = BindAnnotatedClass.create(classElement, processorHelper);
            bindAnnotatedClassMap.put(fullClassName, bindAnnotatedClass);
        }
        return bindAnnotatedClass;
    }

    private ModuleField getModuleField(Element element) {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();
        String fullClassName = classElement.getQualifiedName().toString();
        return moduleAnnotatedClass.getModuleField(fullClassName);
    }
}
