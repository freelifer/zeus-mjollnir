package pig.dream.mjollnir;

import freelifer.zeus.mjollnir.Mjollnir;
import freelifer.zeus.mjollnir.annotations.BindTarget;

/**
 * @author zhukun on 2017/6/6.
 */
public class OtherObject {

    @BindTarget
    App app;

    public OtherObject() {
        Mjollnir.bind(this);
    }
}
