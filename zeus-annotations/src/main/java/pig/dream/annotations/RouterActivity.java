package pig.dream.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zhukun on 2017/4/17.
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RouterActivity {
    String value();
}
