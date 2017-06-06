package pig.dream.mjollnir;

import android.app.Application;

import freelifer.zeus.mjollnir.annotations.Module;
import freelifer.zeus.mjollnir.annotations.Provides;

/**
 * @author zhukun on 2017/6/2.
 */
@Module
public class AppModule {
    private App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides
    public App provideApp() {
        return this.app;
    }

    @Provides
    public Application provideApplication() {
        return this.app;
    }
}
