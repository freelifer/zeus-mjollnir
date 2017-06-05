package freelifer.zeus.mjollnir.inject;

import android.view.View;

/**
 * 用于绑定View和Object对象
 *
 * @author zhukun on 2017/6/5.
 * @version 1.0
 */
public interface Binder<T> {
    void bind(T host, View source);
}