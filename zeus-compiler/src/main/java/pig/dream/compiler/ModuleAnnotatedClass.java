package pig.dream.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import freelifer.zeus.mjollnir.annotations.Preconditions;
import freelifer.zeus.mjollnir.annotations.Provider;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.ParameterizedTypeName.get;

/**
 * @author zhukun on 2017/6/5.
 */
public class ModuleAnnotatedClass {

    private final static String AppModule = "AppModule";
    public final static String MjollnirAppModule = "MjollnirAppModule";

    private Map<String, ModuleField> moduleFieldMap;
    private Elements elements;
    private Filer filer;
    private Messager messager;
    private List<MethodSpec> moduleToMembersInjectorMethods = new ArrayList<>();
    private ModuleAnnotatedClass() {
    }

    private ModuleAnnotatedClass(Elements elements, Filer filer, Messager messager) {
        this.elements = elements;
        this.filer = filer;
        this.messager = messager;
        this.moduleFieldMap = new HashMap<>();
    }

    public static ModuleAnnotatedClass create(Elements elements, Filer filer, Messager messager) {
        return new ModuleAnnotatedClass(elements, filer, messager);
    }

    public void putModuleField(String key, ModuleField value) {
        moduleFieldMap.put(key, value);
    }

    public ModuleField getModuleField(String key) {
        return moduleFieldMap.get(key);
    }

    public void clearMap() {
        moduleFieldMap.clear();
        moduleToMembersInjectorMethods.clear();
    }

    public void generateMainModule() throws IOException {
        if (moduleFieldMap == null || moduleFieldMap.size() == 0) {
            return;
        }

        ModuleField appModuleField = findAppModule();
        if (appModuleField == null) {
            Logger.e(messager, "Please first define %s class", AppModule);
            return;
        }
        generateModules(appModuleField);
        generateProvidesClass();
    }

    private void generateMjollnirAppModuleFirst() throws IOException {
    }

    private void generateModules(ModuleField appModuleField) throws IOException {
        List<ProvideField> provideFields = extractProvideField();

        TypeElement typeElement = appModuleField.getTypeElement();
        String className = typeElement.getSimpleName().toString();
        String lowerCaseClassName = Utils.lowerCase(className);

        // Builder add Module method
        MethodSpec addModuleMethod = MethodSpec.methodBuilder(lowerCaseClassName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.bestGuess(typeElement.getQualifiedName().toString()), lowerCaseClassName)
                .addStatement("this.$N = $N", lowerCaseClassName, lowerCaseClassName)
                .addStatement("return this")
                .returns(ClassName.bestGuess("Builder")).build();

        // Builder build method
        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return new $T(this)", ClassName.bestGuess(MjollnirAppModule))
                .returns(ClassName.bestGuess("Mjollnir" + className)).build();

        // Builder class
        TypeSpec builderType = TypeSpec.classBuilder("Builder")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addModifiers(Modifier.STATIC)
                .addField(ClassName.bestGuess(typeElement.getQualifiedName().toString()), lowerCaseClassName, Modifier.PRIVATE)
                .addMethod(constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addMethod(addModuleMethod)
                .addMethod(buildMethod)
                .build();

        // Core Module Class private constructed function
        MethodSpec constructor = constructorBuilder()
                .addParameter(ClassName.bestGuess(builderType.name), "builder")
                .addStatement("assert builder != null")
                .addStatement("initialize(builder)")
                .addStatement("m" + MjollnirAppModule + " = this")
                .addModifiers(Modifier.PRIVATE).build();

        // Core Module Class initialize Method
        MethodSpec.Builder initializeMethodBuilder = MethodSpec.methodBuilder("initialize")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ClassName.bestGuess(builderType.name), "builder", Modifier.FINAL)
                .returns(void.class);

        for (ProvideField provideField : provideFields) {
            String typeName = provideField.getFatherSimpleName() + "_" + Utils.upperCase(provideField.getSimpleName());
            initializeMethodBuilder.addStatement("this.$N = $N.create(builder.$N)", provideField.getSimpleName() + "Provider", typeName, lowerCaseClassName);
//                    provideField.getSimpleName() + "Provider", Modifier.PRIVATE);
        }

        // static Builder builder()
        MethodSpec builder = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.STATIC)
                .addStatement("return new $T()", ClassName.bestGuess(builderType.name))
                .returns(ClassName.bestGuess(builderType.name)).build();

        // static MjollnirAppModule instance()
        MethodSpec instance = MethodSpec.methodBuilder("instance")
                .addModifiers(Modifier.PUBLIC).addModifiers(Modifier.STATIC)
                .beginControlFlow("if ($N == null)", "m" + MjollnirAppModule)
                .addStatement("throw new NullPointerException(\"Please $N.builder().build();\")", MjollnirAppModule)
                .endControlFlow()
                .addStatement("return $N", "m" + MjollnirAppModule)
                .returns(ClassName.bestGuess(MjollnirAppModule)).build();

        // generate whole clas
        TypeSpec.Builder mainTypeBuilder = TypeSpec.classBuilder(MjollnirAppModule)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addField(ClassName.bestGuess(MjollnirAppModule), "m" + MjollnirAppModule, Modifier.PRIVATE, Modifier.STATIC)
                .addMethod(constructor)
                .addMethod(builder)
                .addMethod(instance)
                .addMethod(initializeMethodBuilder.build())
                .addType(builderType);

        for (ProvideField provideField : provideFields) {
            mainTypeBuilder.addField(ParameterizedTypeName.get(ClassName.get(Provider.class),
                    ClassName.bestGuess(provideField.getReturnType())), provideField.getSimpleName() + "Provider", Modifier.PRIVATE);
        }
        for (MethodSpec methodSpec: moduleToMembersInjectorMethods) {
            mainTypeBuilder.addMethod(methodSpec);
        }

        String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
        // generate file
        JavaFile.builder(packageName, mainTypeBuilder.build()).build().writeTo(filer);
    }

    private ModuleField findAppModule() {
        for (Map.Entry<String, ModuleField> entry : moduleFieldMap.entrySet()) {
            Logger.i(messager, "findAppModule....." + entry.getKey());
            if (entry.getKey().contains(AppModule)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private List<ProvideField> extractProvideField() {
        ArrayList<ProvideField> provideFields = new ArrayList<>();
        for (Map.Entry<String, ModuleField> entry : moduleFieldMap.entrySet()) {
            ModuleField moduleField = entry.getValue();
            provideFields.addAll(moduleField.getProvides());
        }
        return provideFields;
    }

    public ProvideField findProvideField(String fieldName) {
        List<ProvideField> provideFields = extractProvideField();
        for (ProvideField field : provideFields) {
            if (field.getReturnType().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    public void generateProvidesClass() {
        for (Map.Entry<String, ModuleField> entry : moduleFieldMap.entrySet()) {
            ModuleField moduleField = entry.getValue();
            List<ProvideField> provides = moduleField.getProvides();
            if (provides != null && provides.size() != 0) {
                for (ProvideField provide : provides) {
                    try {
                        generateProvide(moduleField, provide).writeTo(filer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private JavaFile generateProvide(ModuleField moduleField, ProvideField provideField) {
        String typeName = moduleField.getTypeElement().getSimpleName().toString() + "_" + Utils.upperCase(provideField.getSimpleName());
        String upperCaseClassName = Utils.upperCase(typeName);
        ClassName returnType = ClassName.bestGuess(provideField.getReturnType());

        // 传参Appmodule module
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.get(provideField.getFatherElement().asType()), "module")
                .build();

        // private constructed function
        MethodSpec constructor = constructorBuilder()
                .addParameter(parameterSpec)
                .addStatement("this.module = module")
                .addModifiers(Modifier.PRIVATE).build();

        // create method
        MethodSpec createMethod = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addParameter(parameterSpec)
                .addStatement("return new $N(module)", typeName)
                .returns(get(ClassName.get(Provider.class), returnType)).build();

        MethodSpec getMethod = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addStatement("return $T.checkNotNull(\n" +
                        "module.$N(),\n\"Cannot return null from a non-@Nullable @Provides method\")", TypeName.get(Preconditions.class), provideField.getSimpleName())
                .returns(returnType).build();

        // generate whole clas
        TypeSpec finderClass = TypeSpec.classBuilder(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addField(TypeName.get(provideField.getFatherElement().asType()), "module", Modifier.FINAL, Modifier.PRIVATE)
                .addSuperinterface(get(ClassName.get(Provider.class), returnType))
                .addMethod(constructor)
                .addMethod(createMethod)
                .addMethod(getMethod)
                .build();

        String packageName = elements.getPackageOf(provideField.getElement()).getQualifiedName().toString();
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
                .addMethod(constructorBuilder().addModifiers(Modifier.PRIVATE).build())
                .addMethod(addModuleMethod)
                .addMethod(buildMethod)
                .build();

        // private constructed function
        MethodSpec constructor = constructorBuilder()
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

    public void putModuleToMembersInjectorMethods(MethodSpec methodSpec) {
        moduleToMembersInjectorMethods.add(methodSpec);
    }

    public static class MembersInjectorField {
        final ParameterizedTypeName fieldType;
        final String fieldName;
        final String targetName;

        private MembersInjectorField(ParameterizedTypeName fieldType,
                                     String fieldName, String targetName) {
            this.fieldType = fieldType;
            this.fieldName = fieldName;
            this.targetName = targetName;
        }

        public static MembersInjectorField create(final ParameterizedTypeName fieldType,
                                                  final String fieldName, final String targetName) {
            return new MembersInjectorField(fieldType, fieldName, targetName);
        }
    }
}
