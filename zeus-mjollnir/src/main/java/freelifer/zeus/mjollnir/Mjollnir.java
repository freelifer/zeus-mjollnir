package freelifer.zeus.mjollnir;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import freelifer.zeus.mjollnir.inject.Core;
import freelifer.zeus.mjollnir.inject.EmptyCore;

/**
 * 雷神之锤
 *
 * @author zhukun on 2017/6/2.
 */
public class Mjollnir {

    private static final Map<String, Core> FINDER_MAP = new HashMap<>();

    @NonNull
    public static void bind(@NonNull Object target) {
        createBinding(target, null);
    }

    @NonNull
    public static void bind(@NonNull Activity target) {
        View sourceView = target.getWindow().getDecorView();
        createBinding(target, sourceView);
    }

    public static void parseIntent(final Activity target, Bundle savedInstanceState, Intent intent) {
        getCore(target).parseIntent(target, savedInstanceState, intent);
    }

    public static void saveInstanceState(final Activity host, Bundle outState) {
        getCore(host).saveInstanceState(host, outState);
    }

    @NonNull
    public static void bind(@NonNull View target) {
        createBinding(target, target);
    }

    private static void createBinding(Object host, View source) {
        getCore(host).inject(host, source);
    }

    @NonNull
    private static Core getCore(Object object) {
        String className = object.getClass().getName();
        Core core = FINDER_MAP.get(className);
        if (core != null) {
            return core;
        }
        Class<?> targetClass = null;
        try {
            targetClass = Class.forName(className + "$$Core");
        } catch (ClassNotFoundException e) {
            targetClass = EmptyCore.class;
        }

        try {
            core = (Core) targetClass.newInstance();
            if (core != null) {
                FINDER_MAP.put(className, core);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject for " + className, e);
        }

        return core;
    }
}
