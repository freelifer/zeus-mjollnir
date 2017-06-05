package pig.dream.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * @author zhukun on 2017/6/5.
 */
public class ProvideAnnotatedClass {

    public static JavaFile generateProvidesClass(TypeElement classElement, Elements elementUtils) {
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
