package pig.dream.mjollnir;

import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import freelifer.zeus.mjollnir.Mjollnir;
import freelifer.zeus.mjollnir.annotations.BindTarget;
import freelifer.zeus.mjollnir.annotations.BindView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.all)
    TextView tv;

    @BindTarget
    App app;

    @BindTarget
    Application application01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Mjollnir.bind(this);
    }
}
