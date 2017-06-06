package pig.dream.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

import freelifer.zeus.mjollnir.annotations.MembersInjector;
import freelifer.zeus.mjollnir.annotations.Provider;

/**
 * @author zhukun on 2017/6/6.
 */
public class MembersInjectorGenerater {

    public static void generate(BindAnnotatedClass bindAnnotatedClass, ModuleAnnotatedClass moduleAnnotatedClass) throws IOException {
        List<BindTargetField> bindTargetFields = bindAnnotatedClass.getBindTargetFields();
        if (bindTargetFields == null || bindTargetFields.size() == 0) {
            return;
        }
        generateMembersInjector(bindAnnotatedClass, moduleAnnotatedClass);
    }


    private static void generateMembersInjector(BindAnnotatedClass bindAnnotatedClass, ModuleAnnotatedClass moduleAnnotatedClass) throws IOException {
        String simpleName = bindAnnotatedClass.classElement.getSimpleName().toString();
        List<BindTargetField> bindTargetFields = bindAnnotatedClass.getBindTargetFields();
        String thisClassName = String.format("%s_MembersInjector", simpleName);
        ParameterizedTypeName returnsType = ParameterizedTypeName.get(ClassName.get(MembersInjector.class),
                TypeName.get(bindAnnotatedClass.classElement.asType()));

        ArrayList<ModuleAnnotatedClass.MembersInjectorField> membersInjectorFields = new ArrayList<>();
        for (BindTargetField bindTargetField : bindTargetFields) {
            ProvideField provideField = moduleAnnotatedClass.findProvideField(bindTargetField.getFieldType().toString());
            if (provideField == null) {
                bindAnnotatedClass.processorHelper.e("class %s ' %s is not found, remove this or goto Module class add this target ",
                        simpleName, bindTargetField.getFieldType().toString());
                return;
            }

            ParameterizedTypeName fieldType = ParameterizedTypeName.get(ClassName.get(Provider.class), TypeName.get(bindTargetField.getFieldType()));
            // provideApplicationProvider
            String fieldName = provideField.getSimpleName() + "Provider";
            membersInjectorFields.add(ModuleAnnotatedClass.MembersInjectorField.create(fieldType, fieldName, bindTargetField.getFieldName()));
        }

        MethodSpec.Builder temp = MethodSpec.methodBuilder("create" + thisClassName)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnsType);

        MethodSpec.Builder createMethodBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .returns(returnsType);

        StringBuilder statement = new StringBuilder();
        for (ModuleAnnotatedClass.MembersInjectorField field : membersInjectorFields) {
            statement.append(field.fieldName);
            statement.append(",");
            createMethodBuilder.addParameter(field.fieldType, field.fieldName);
        }
        if (statement.length() == 0) {
            createMethodBuilder.addStatement("return new $N()", thisClassName);
            temp.addStatement("return $N.create()", thisClassName);
        } else {
            int len = statement.length();
            createMethodBuilder.addStatement("return new $N($N)", thisClassName, statement.substring(0, len - 1));
            temp.addStatement("return $N.create($N)", thisClassName, statement.substring(0, len - 1));
        }
        moduleAnnotatedClass.putModuleToMembersInjectorMethods(temp.build());

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE);
        for (ModuleAnnotatedClass.MembersInjectorField field : membersInjectorFields) {
            constructorBuilder.addParameter(field.fieldType, field.fieldName);
            constructorBuilder.addStatement("this.$N = $N", field.fieldName, field.fieldName);
        }

        MethodSpec.Builder injectMembersMethodBuilder = MethodSpec.methodBuilder("injectMembers")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(bindAnnotatedClass.classElement.asType()), "instance")
                .returns(void.class);
        for (ModuleAnnotatedClass.MembersInjectorField field : membersInjectorFields) {
            injectMembersMethodBuilder.addStatement("instance.$N = $N.get()", field.targetName, field.fieldName);
        }

        TypeSpec.Builder membersInjectorTypeBuilder = TypeSpec.classBuilder(thisClassName)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.FINAL)
                .addSuperinterface(returnsType)
                .addMethod(constructorBuilder.build())
                .addMethod(createMethodBuilder.build())
                .addMethod(injectMembersMethodBuilder.build());

        for (ModuleAnnotatedClass.MembersInjectorField field : membersInjectorFields) {
            membersInjectorTypeBuilder.addField(field.fieldType, field.fieldName, Modifier.PRIVATE);
        }

        String packageName = bindAnnotatedClass.processorHelper.getPackageOf(bindAnnotatedClass.classElement);
        // generate file
        bindAnnotatedClass.processorHelper.toWrite(packageName, membersInjectorTypeBuilder.build());
    }
}
