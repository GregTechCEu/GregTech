package gregtech.api.modules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GregTechModule {

    String moduleID();

    String containerID();

    String name();

    boolean coreModule() default false;

    String author() default "";

    String version() default "";

    String descriptionKey() default "";
}
