package pig.dream.mjollnir;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import freelifer.zeus.mjollnir.Mjollnir;
import freelifer.zeus.mjollnir.annotations.BindTarget;
import pig.dream.annotations.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.all)
    TextView tv;

    @BindTarget
    Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MjollnirAppModule.builder().appModule(new AppModule(getApplication())).build();
        Mjollnir.bind(this);
    }
}
