package pig.dream.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Map;

import javax.lang.model.element.Modifier;

/**
 * @author zhukun on 2017/5/15.
 */

public class RouterAnnotatedClass {

    private static final String packageName = "pig.dream.zeuslibs.router";

    public static JavaFile generateRouteActivityClass(Map<String, AnnotatedClass> annotatedClassMap, Map<String, RouteActivityField> routeActivityMap) {

        ParameterizedTypeName p = ParameterizedTypeName.get(ClassName.get("java.util", "Map"), ClassName.get(String.class), ClassName.get(packageName,"RouteMeta"));
        MethodSpec.Builder loadIntoMethodBuilder = MethodSpec.methodBuilder("loadInto")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(p, "map", Modifier.FINAL);
        for (String fullClass : routeActivityMap.keySet()) {
//            AnnotatedClass annotatedClass = annotatedClassMap.get(fullClass);
            RouteActivityField raf = routeActivityMap.get(fullClass);
            loadIntoMethodBuilder.addCode("map.put($S, $T.build($S));\n", raf.getValue(), ClassName.get(packageName, "RouteMeta"), raf.getFullClassName());
        }

        // generate whole clas
        TypeSpec finderClass = TypeSpec.classBuilder("Router$$ModuleName$$App")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(packageName, "IRouteRoot"))
                .addMethod(loadIntoMethodBuilder.build())
                .build();

        // generate file
        return JavaFile.builder("pig.dream.zeuslibs.routes", finderClass).build();
    }
}
