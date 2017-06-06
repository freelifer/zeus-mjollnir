package pig.dream.mjollnir;

import android.app.Application;

/**
 * @author zhukun on 2017/6/6.
 */
public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        MjollnirAppModule.builder().appModule(new AppModule(this)).build();
    }
}
