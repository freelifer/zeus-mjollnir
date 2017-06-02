package freelifer.zeus.mjollnir.inject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * @author zhukun on 2017/6/2.
 */
public interface Core<T> {
    void inject(T host, View source);

    void parseIntent(T host, Bundle savedInstanceState, Intent intent);

    void saveInstanceState(T host, Bundle outState);
}
