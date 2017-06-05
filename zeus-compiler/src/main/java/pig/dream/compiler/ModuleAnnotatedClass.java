package pig.dream.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import freelifer.zeus.mjollnir.annotations.Provider;

/**
 * @author zhukun on 2017/6/5.
 */
public class ModuleAnnotatedClass {

    private ModuleField moduleField;

    private ModuleAnnotatedClass() {
    }

    public static ModuleAnnotatedClass create(ModuleField moduleField) {
        ModuleAnnotatedClass moduleAnnotatedClass = new ModuleAnnotatedClass();
        moduleAnnotatedClass.moduleField = moduleField;
        return moduleAnnotatedClass;
    }

    public void generateProvidesClass(Elements elementUtils, Filer filer) {
        if (moduleField != null) {
            List<Element> provides = moduleField.getProvides();
            if (provides != null && provides.size() != 0) {
                for (Element provide : provides) {
                    try {
                        generateProvide(provide, elementUtils).writeTo(filer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

//    public final class AppModule_ProvideApplicationFactory implements Factory<Application> {
//        private final AppModule module;
//
//        public AppModule_ProvideApplicationFactory(AppModule module) {
//            assert module != null;
//            this.module = module;
//        }
//
//        @Override
//        public Application get() {
//            return Preconditions.checkNotNull(
//                    module.provideApplication(), "Cannot return null from a non-@Nullable @Provides method");
//        }
//        public static Factory<Application> create(AppModule module) {
//            return new AppModule_ProvideApplicationFactory(module);
//        }
//    }

    private JavaFile generateProvide(Element element, Elements elementUtils) {
        String typeName = moduleField.getTypeElement().getSimpleName().toString() + "_" + element.getSimpleName();
        String upperCaseClassName = Utils.upperCase(typeName);

        // private constructed function
        MethodSpec constructor = MethodSpec.constructorBuilder()
//                .addParameter(ClassName.bestGuess(typeName), "builder")
                .addModifiers(Modifier.PRIVATE).build();

        // create method
        MethodSpec createMethod = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addStatement("return new $N()", typeName)
                .returns(ParameterizedTypeName.get(ClassName.get(Provider.class), ClassName.get("android.app", "Application"))).build();

        MethodSpec getMethod = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("return null")
                .returns(ClassName.get("android.app", "Application")).build();

        // generate whole clas
        TypeSpec finderClass = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(Provider.class), ClassName.get("android.app", "Application")))
                .addMethod(constructor)
                .addMethod(createMethod)
                .addMethod(getMethod)
                .build();

        String packageName = elementUtils.getPackageOf(element).getQualifiedName().toString();
        // generate file
        return JavaFile.builder(packageName, finderClass).build();
    }

    public static JavaFile generateModulsClass(TypeElement classElement, Elements elementUtils) {

        String className = classElement.getSimpleName().toString();
        String lowerCaseClassName = Utils.lowerCase(className);

        // Builder add Module method
        MethodSpec addModuleMethod = MethodSpec.methodBuilder(lowerCaseClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.bestGuess(classElement.getQualifiedName().toString()), lowerCaseClassName)
                .addStatement("this.$N = $N", lowerCaseClassName, lowerCaseClassName)
                .addStatement("return this")
                .returns(ClassName.bestGuess("Builder")).build();

        // Builder build method
        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return new $T(this)", ClassName.bestGuess("Mjollnir" + className))
                .returns(ClassName.bestGuess("Mjollnir" + className)).build();

        //Builder class
        TypeSpec builderType = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addModifiers(Modifier.STATIC)
                .addField(ClassName.bestGuess(classElement.getQualifiedName().toString()), lowerCaseClassName, Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addMethod(addModuleMethod)
                .addMethod(buildMethod)
                .build();

        // private constructed function
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addParameter(ClassName.bestGuess(builderType.name), "builder")
                .addModifiers(Modifier.PRIVATE).build();

        // static Builder builder()
        MethodSpec builder = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.STATIC)
                .addStatement("return new $T()", ClassName.bestGuess(builderType.name))
                .returns(ClassName.bestGuess(builderType.name)).build();

        // generate whole clas
        TypeSpec finderClass = TypeSpec.classBuilder("Mjollnir" + classElement.getSimpleName())
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addMethod(constructor)
                .addMethod(builder)
                .addType(builderType)
                .build();

        String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
        // generate file
        return JavaFile.builder(packageName, finderClass).build();
    }
}
