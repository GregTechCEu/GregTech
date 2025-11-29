package gregtech.integration.groovy;

import gregtech.api.fluids.FluidBuilder;
import gregtech.api.fluids.attribute.FluidAttributes;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.unification.Element;
import gregtech.api.unification.Elements;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.properties.ExtraToolProperty;
import gregtech.api.unification.material.properties.MaterialToolProperty;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.api.GroovyLog;

public class GroovyExpansions {

    public static <R extends RecipeBuilder<R>> RecipeBuilder<R> property(RecipeBuilder<R> builder, String key,
                                                                         Object value) {
        if (!builder.applyPropertyCT(key, value)) {
            GroovyLog.get().error("Failed to add property '{}' with '{}' to recipe", key, value);
        }
        return builder;
    }

    public static Material.Builder materialBuilder(MaterialEvent event, int id, ResourceLocation resourceLocation) {
        return Material.builder(id, resourceLocation);
    }

    public static Material.Builder materialBuilder(MaterialEvent event, int id, String domain, String path) {
        return materialBuilder(event, id, new ResourceLocation(domain, path));
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

    public static MaterialToolProperty.Builder toolBuilder(MaterialEvent event, float harvestSpeed, float attackDamage,
                                                           int durability, int harvestLevel) {
        return MaterialToolProperty.Builder.of(harvestSpeed, attackDamage, durability, harvestLevel);
    }

    public static MaterialToolProperty.Builder toolBuilder(MaterialEvent event) {
        return toolBuilder(event, 1.0F, 1.0F, 100, 2);
    }

    public static ExtraToolProperty.Builder overrideToolBuilder(MaterialEvent event) {
        return ExtraToolProperty.Builder.of();
    }

    public static ExtraToolProperty.Builder overrideToolBuilder(MaterialEvent event, float harvestSpeed,
                                                                float attackDamage,
                                                                int durability, int harvestLevel) {
        return ExtraToolProperty.Builder.of(harvestSpeed, attackDamage, durability, harvestLevel);
    }

    public static FluidBuilder fluidBuilder(MaterialEvent event) {
        return new FluidBuilder();
    }

    public static Element addElement(MaterialEvent event, long protons, long neutrons, long halfLifeSeconds,
                                     String decayTo, String name, String symbol, boolean isIsotope) {
        return Elements.add(protons, neutrons, halfLifeSeconds, decayTo, name, symbol, isIsotope);
    }

    public static Element addElement(MaterialEvent event, long protons, long neutrons, String name, String symbol,
                                     boolean isIsotope) {
        return Elements.add(protons, neutrons, name, symbol, isIsotope);
    }

    public static Element addElement(MaterialEvent event, long protons, long neutrons, String name, String symbol) {
        return Elements.add(protons, neutrons, name, symbol);
    }

    public static FluidBuilder acidic(FluidBuilder builder) {
        return builder.attributes(FluidAttributes.ACID);
    }
}
