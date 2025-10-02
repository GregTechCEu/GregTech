package gregtech.api.block.coil;

import gregtech.api.unification.material.Material;
import gregtech.api.util.GTUtility;
import gregtech.api.util.function.QuadConsumer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

public class CoilStatBuilder {

    private final CustomCoilStats stats;
    private ResourceLocation textureLocation;
    private final String modid;

    CoilStatBuilder(String modid) {
        this.modid = modid;
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

    public CoilStatBuilder texture(String location) {
        this.textureLocation = new ResourceLocation(this.modid, location);
        return this;
    }

    public CoilStatBuilder texture(String location, boolean generic) {
        return texture(location).generic(generic);
    }

    public CoilStatBuilder generic(boolean generic) {
        this.stats.isGeneric = generic;
        return this;
    }

    public CoilStatBuilder generic() {
        return generic(true);
    }

    public CoilStatBuilder tooltip(QuadConsumer<ItemStack, World, List<String>, Boolean> additionalTooltips) {
        this.stats.additionalTooltips = additionalTooltips;
        return this;
    }

    CustomCoilStats build() {
        if (this.textureLocation == null) {
            this.textureLocation = GTUtility.gregtechId("wire_coil");
        }
        String variant;
        if (this.stats.isGeneric) {
            variant = "%s";
            this.stats.inactive = new ModelResourceLocation(this.textureLocation, String.format(variant, "normal"));
            this.stats.active = new ModelResourceLocation(this.textureLocation, String.format(variant, "active"));
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
