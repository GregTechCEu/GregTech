package gregtech.api.block.coil;

import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

public class CoilStatBuilder {

    private final CustomCoilStats stats;

    CoilStatBuilder() {
        this.stats = new CustomCoilStats();
    }

    public CoilStatBuilder material(Material material) {
        stats.material = material;
        stats.name = material.getResourceLocation().getPath();
        return this;
    }

    public CoilStatBuilder coilTemp(int coilTemperature) {
        stats.coilTemperature = coilTemperature;
        return this;
    }

    public CoilStatBuilder tier(int tier) {
        stats.tier = Math.max(0, tier);
        return this;
    }

    public CoilStatBuilder multiSmelter(int level, int energyDiscount) {
        stats.level = level;
        stats.energyDiscount = energyDiscount;
        return this;
    }

    public CoilStatBuilder texture(ResourceLocation location, boolean generic) {
        ResourceLocation loc = Objects.requireNonNull(location);
        String variant = generic ? "%s" : "active=%s,variant=%s";
        stats.inactive = new ModelResourceLocation(loc,
                generic ? String.format(variant, false) : String.format(variant, false, stats.name));
        stats.active = new ModelResourceLocation(loc,
                generic ? String.format(variant, true) : String.format(variant, true, stats.name));
        return this;
    }

    CustomCoilStats build() {
        if (stats.inactive == null) {
            stats.inactive = new ModelResourceLocation(GTUtility.gregtechId("wire_coil"), "normal");
        }
        if (stats.active == null) {
            stats.active = stats.inactive;
        }
        return this.stats;
    }
}
