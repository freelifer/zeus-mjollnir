package freelifer.zeus.mjollnir.inject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * @author zhukun on 2017/5/21.
 */

public class EmptyCore implements Core<Activity> {

    @Override
    public void inject(Activity host, View source) {

    }

    @Override
    public void parseIntent(Activity host, Bundle savedInstanceState, Intent intent) {

    }

    @Override
    public void saveInstanceState(Activity host, Bundle outState) {

    }
}
