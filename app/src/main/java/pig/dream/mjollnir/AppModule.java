package pig.dream.mjollnir;

import android.app.Application;

import freelifer.zeus.mjollnir.annotations.Module;
import freelifer.zeus.mjollnir.annotations.Provides;

/**
 * @author zhukun on 2017/6/2.
 */
@Module
public class AppModule {
    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    public Application getApplication() {
        return this.application;
    }
}
