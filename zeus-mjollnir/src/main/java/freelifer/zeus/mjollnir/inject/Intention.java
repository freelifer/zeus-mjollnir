package freelifer.zeus.mjollnir.inject;

import android.content.Intent;
import android.os.Bundle;

/**
 * 用于解析Intent和保存Intent数据
 *
 * @author zhukun on 2017/6/5.
 * @version 1.0
 */
public interface Intention<T> {
    void parseIntent(T host, Bundle savedInstanceState, Intent intent);

    void saveInstanceState(T host, Bundle outState);
}
