package gregtech.integration.groovy;

import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.event.MaterialEvent;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;

public class GroovyExpansions {

    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> property(RecipeBuilder<R> builder, String key,
                                                                         Object value) {
        if (!builder.applyProperty(key, value)) {
            GroovyLog.get().error("Failed to add property '{}' with '{}' to recipe", key, value);
        }
        return builder;
    }

    public static Material.Builder materialBuilder(MaterialEvent event, int id, ResourceLocation resourceLocation) {
        return new Material.Builder(id, resourceLocation);
    }

    public static Material.Builder materialBuilder(MaterialEvent event, int id, String domain, String path) {
        return materialBuilder(event, id, domain, path);
    }

    public static Material.Builder materialBuilder(MaterialEvent event, int id, String s) {
        String domain, path;
        if (s.contains(":")) {
            String[] parts = s.split(":", 2);
            domain = parts[0];
            path = parts[1];
        } else {
            domain = GroovyScript.getRunConfig().getPackId();
            path = s;
        }
        return materialBuilder(event, id, new ResourceLocation(domain, path));
    }
}
