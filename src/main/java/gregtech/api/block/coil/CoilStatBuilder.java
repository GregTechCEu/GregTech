package gregtech.api.block.coil;

import gregtech.api.unification.material.Material;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

public class CoilStatBuilder {

    private final CustomCoilStats stats;
    private ResourceLocation textureLocation;

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
        this.textureLocation = Objects.requireNonNull(location);
        this.stats.isGeneric = generic;
        return this;
    }

    CustomCoilStats build() {
        String variant;
        if (this.stats.isGeneric) {
            variant = "%s";
            this.stats.inactive = new ModelResourceLocation(this.textureLocation, String.format(variant, false));
            this.stats.active = new ModelResourceLocation(this.textureLocation, String.format(variant, true));
        } else {
            variant = "active=%s,variant=%s";
            this.stats.inactive = new ModelResourceLocation(this.textureLocation,
                    String.format(variant, false, stats.name));
            this.stats.active = new ModelResourceLocation(this.textureLocation,
                    String.format(variant, true, stats.name));
        }
        return this.stats;
    }
}
