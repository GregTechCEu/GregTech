package gregtech.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyLog;
import gregtech.api.recipes.RecipeBuilder;

public class GroovyRecipeBuilderExpansion {

    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> property(RecipeBuilder<R> builder, String key, Object value) {
        if (!builder.applyProperty(key, value)) {
            GroovyLog.get().error("Failed to add property '{}' with '{}' to recipe", key, value);
        }
        return builder;
    }
}
