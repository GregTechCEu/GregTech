package gregtech.api.recipes.registration;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProcessingHandler {

    /**
     * @return the name of this handler
     */
    @Nonnull String name() default "";

    /**
     * @return the {@link Type} of this handler
     */
    @Nonnull Type type() default Type.MATERIAL;

    @Nonnull String[] prefixes() default {};

    enum Type {
        /**
         * A processing handler which only accepts a {@link gregtech.api.unification.material.Material}
         */
        MATERIAL,
        /**
         * A processing handler which accepts an {@link gregtech.api.unification.ore.OrePrefix},
         * followed by a {@link gregtech.api.unification.material.Material}.
         * <p>
         * The method must also supply {@link ProcessingHandler#prefixes()} to define the input ore prefixes.
         */
        ORE_PREFIX,
        /**
         * A processing handler which accepts an {@link gregtech.api.unification.material.properties.IMaterialProperty} (of the appropriate type),
         * followed by a {@link gregtech.api.unification.material.Material}
         */
        PROPERTY,
        /**
         * A processing handler which accepts an {@link gregtech.api.unification.material.properties.IMaterialProperty} (of the appropriate type),
         * then an {@link gregtech.api.unification.ore.OrePrefix},
         * followed by a {@link gregtech.api.unification.material.Material}
         * <p>
         * The method must also supply {@link ProcessingHandler#prefixes()} to define the input ore prefixes.
         */
        PROPERTY_PREFIX
    }
}
