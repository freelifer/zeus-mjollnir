package pig.dream.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * 注解生成类 实现freelifer.zeus.mjollnir.inject.Core接口
 * 自动生成BindView、BindIntent、Inject对应的方法
 *
 * @author zhukun on 2017/4/15.
 * @version 1.0
 */
public class AnnotatedClass {

    /** Core接口所在的包名 */
    private static final String CORE_PACKAGE_NAME = "freelifer.zeus.mjollnir.inject";

    private Messager messager;
    public Elements elementUtils;

    public TypeElement classElement;
    private List<BindViewField> bindViewFields;
    private List<BindIntentField> bindIntentFields;

    public AnnotatedClass(TypeElement classElement, Elements elementUtils, Messager messager) {
        this.messager = messager;
        this.classElement = classElement;
        this.elementUtils = elementUtils;
    }

    // omit some easy methods
    public JavaFile generateActivityCoreClass() {

        // method inject(final T host, Object source, Provider provider)
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(classElement.asType()), "host", Modifier.FINAL)
                .addParameter(ClassName.get("android.view", "View"), "source");

        if (bindViewFields != null && bindViewFields.size() != 0) {
            for (BindViewField field : bindViewFields) {
                // find views
                injectMethodBuilder.addStatement("host.$N = ($T)(source.findViewById($L))", field.getFieldName(),
                        ClassName.get(field.getFieldType()), field.getResId());
            }
        }
        // method  void parseIntent(T host, Bundle savedInstanceState, Intent intent);
        MethodSpec.Builder parseIntentBuilder = MethodSpec.methodBuilder("parseIntent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(classElement.asType()), "host", Modifier.FINAL)
                .addParameter(ClassName.get("android.os", "Bundle"), "savedInstanceState")
                .addParameter(ClassName.get("android.content", "Intent"), "intent");

        if (bindIntentFields != null && bindIntentFields.size() != 0) {
            parseIntentBuilder.beginControlFlow("if (savedInstanceState == null)");
            for (BindIntentField field : bindIntentFields) {
                // parse Intent
                parseIntentBuilder.addStatement(parseIntentType(field), field.getFieldName(), field.getValue());
            }
            parseIntentBuilder.endControlFlow();
            parseIntentBuilder.beginControlFlow("else");
            for (BindIntentField field : bindIntentFields) {
                parseIntentBuilder.addStatement(parseBundleType(field), field.getFieldName(), field.getValue());
            }
            parseIntentBuilder.endControlFlow();
        }

        // void saveInstanceState(T host, Bundle outState);
        MethodSpec.Builder saveInstanceStateBuilder = MethodSpec.methodBuilder("saveInstanceState")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(classElement.asType()), "host", Modifier.FINAL)
                .addParameter(ClassName.get("android.os", "Bundle"), "outState");

        if (bindIntentFields != null && bindIntentFields.size() != 0) {
            for (BindIntentField field : bindIntentFields) {
                // save InstanceState
                saveInstanceStateBuilder.addStatement(saveInstanceState(field), field.getValue(), field.getFieldName());
            }
        }

        // generate whole class
        TypeSpec finderClass = TypeSpec.classBuilder(classElement.getSimpleName() + "$$Core")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(CORE_PACKAGE_NAME, "Core"), TypeName.get(classElement.asType())))
                .addMethod(injectMethodBuilder.build())
                .addMethod(parseIntentBuilder.build())
                .addMethod(saveInstanceStateBuilder.build())
                .build();

        String packageName = elementUtils.getPackageOf(classElement).getQualifiedName().toString();
        // generate file
        return JavaFile.builder(packageName, finderClass).build();
    }

    private String parseIntentType(BindIntentField bindIntentField) {
        String result = "host.$N = intent.getIntExtra($S, 0)";
        VariableElement element = bindIntentField.getFieldElement();
        TypeMirror typeMirror = bindIntentField.getFieldType();
        final TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case INT:
                break;
            case BOOLEAN:
                result = "host.$N = intent.getBooleanExtra($S, false)";
                break;
            case DECLARED:
                if ("java.lang.String".equals(typeMirror.toString())) {
                    result = "host.$N = intent.getStringExtra($S)";
                } else if ("java.lang.Integer".equals(typeMirror.toString())) {
                    result = "host.$N = intent.getIntExtra($S, 0)";
                } else {
                    return "host.$N = intent.getSerializableExtra($S)";
                }
                break;
            default:
                break;
        }

        return result;
    }

    private String parseBundleType(BindIntentField bindIntentField) {
        String result = "host.$N = savedInstanceState.getInt($S, 0)";
        VariableElement element = bindIntentField.getFieldElement();
        TypeMirror typeMirror = bindIntentField.getFieldType();
        TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case INT:
                break;
            case BOOLEAN:
                result = "host.$N = savedInstanceState.getBoolean($S, false)";
                break;
            case DECLARED:
                if ("java.lang.String".equals(typeMirror.toString())) {
                    result = "host.$N = savedInstanceState.getString($S)";
                } else if ("java.lang.Integer".equals(typeMirror.toString())) {
                    result = "host.$N = savedInstanceState.getInt($S, 0)";
                } else {
                    return "host.$N = savedInstanceState.getSerializable($S)";
                }
                break;
            default:
                break;
        }

        return result;
    }

    private String saveInstanceState(BindIntentField bindIntentField) {
        String result = "outState.putInt($S, host.$N);";
        VariableElement element = bindIntentField.getFieldElement();
        TypeMirror typeMirror = bindIntentField.getFieldType();
        TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case INT:
                break;
            case BOOLEAN:
                result = "outState.putBoolean($S, host.$N);";
                break;
            case DECLARED:
                if ("java.lang.String".equals(typeMirror.toString())) {
                    result = "outState.putString($S, host.$N);";
                } else if ("java.lang.Integer".equals(typeMirror.toString())) {
                    result = "outState.putInt($S, host.$N);";
                } else {
                    return "outState.putSerializable($S, host.$N);";
                }
                break;
            default:
                break;
        }

        return result;
    }

    public void addBindViewField(BindViewField bindViewField) {
        if (bindViewFields == null) {
            bindViewFields = new ArrayList<>();
        }
        bindViewFields.add(bindViewField);
    }

    public void addBindIntentField(BindIntentField bindIntentField) {
        if (bindIntentFields == null) {
            bindIntentFields = new ArrayList<>();
        }
        bindIntentFields.add(bindIntentField);
    }

}
