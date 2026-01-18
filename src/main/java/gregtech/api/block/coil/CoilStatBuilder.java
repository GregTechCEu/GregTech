package gregtech.api.block.coil;

import gregtech.api.GTValues;
import gregtech.api.recipes.properties.impl.TemperatureProperty;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.GTUtility;
import gregtech.api.util.function.QuadConsumer;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

public class CoilStatBuilder {

    private final String modid;

    private String name;

    // electric blast furnace properties
    private int coilTemperature = -1;

    // multi smelter properties
    private int level = -1;
    private int energyDiscount = 0;

    // voltage tier
    private int tier = GTValues.ULV;

    private Material material = Materials.Iron;
    private ResourceLocation textureLocation;
    private boolean isGeneric;
    private QuadConsumer<ItemStack, World, List<String>, Boolean> additionalTooltips;

    CoilStatBuilder(String modid) {
        this.modid = modid;
    }

    /**
     * @param material Material that this coil should be based off of. Used for
     *                 {@link TemperatureProperty#registerCoilType(int, Material, String)}.
     * @return this
     */
    public CoilStatBuilder material(Material material) {
        return material(material, material.getName());
    }

    /**
     * @param material Material that this coil should be based off of. Used for
     *                 {@link TemperatureProperty#registerCoilType(int, Material, String)}.
     * @param name     Name of the variant to look for in the model json (typically the name of the material).
     * @return this
     */
    public CoilStatBuilder material(Material material, String name) {
        this.material = material;
        this.name = name;
        return this;
    }

    public CoilStatBuilder coilTemp(int coilTemperature) {
        this.coilTemperature = coilTemperature;
        return this;
    }

    /**
     * @param tier The voltage tier of this coil variant, used for the energy discount in the cracking unit and pyrolyse
     *             oven
     * @return this
     */
    public CoilStatBuilder tier(int tier) {
        this.tier = Math.max(0, tier);
        return this;
    }

    /**
     * @param level          This is used for the amount of parallel recipes in the multi smelter. Multiplied by 32.
     * @param energyDiscount This is used for the energy discount in the multi smelter
     * @return this
     */
    public CoilStatBuilder multiSmelter(int level, int energyDiscount) {
        this.level = level;
        this.energyDiscount = energyDiscount;
        return this;
    }

    /**
     * @param location Location of the block model json
     * @return this
     */
    public CoilStatBuilder texture(String location) {
        this.textureLocation = new ResourceLocation(this.modid, location);
        return this;
    }

    /**
     * @param location Location of the block model json
     * @param generic  If true, the coil will use a grayscale texture to tint based off of the materials color.
     *                 Otherwise, it will look for a texture specifically for this variant.
     * @return this
     */
    public CoilStatBuilder texture(String location, boolean generic) {
        return texture(location).generic(generic);
    }

    /**
     * @param generic If true, the coil will use a grayscale texture to tint based off of the materials color.
     *                Otherwise, it will look for a texture specifically for this variant.
     * @return this
     */
    public CoilStatBuilder generic(boolean generic) {
        this.isGeneric = generic;
        return this;
    }

    /**
     * Marks this variant as generic, it will look for a grayscale texture and tint based on material color.
     * 
     * @return this
     */
    public CoilStatBuilder generic() {
        return generic(true);
    }

    /**
     * @param additionalTooltips Used for adding additional tooltips for this variant
     * @return this
     */
    public CoilStatBuilder tooltip(QuadConsumer<ItemStack, World, List<String>, Boolean> additionalTooltips) {
        this.additionalTooltips = additionalTooltips;
        return this;
    }

    CustomCoilStats build() {
        if (this.textureLocation == null) {
            this.textureLocation = GTUtility.gregtechId("wire_coil");
        }

        String variant;
        ModelResourceLocation inactive;
        ModelResourceLocation active;
        if (this.isGeneric) {
            variant = "%s";
            inactive = new ModelResourceLocation(this.textureLocation, String.format(variant, "normal"));
            active = new ModelResourceLocation(this.textureLocation, String.format(variant, "active"));
        } else {
            variant = "active=%s,variant=%s";
            inactive = new ModelResourceLocation(this.textureLocation,
                    String.format(variant, false, this.name));
            active = new ModelResourceLocation(this.textureLocation,
                    String.format(variant, true, this.name));
        }
        return new CustomCoilStats(
                this.name,
                this.coilTemperature,
                this.level,
                this.energyDiscount,
                this.tier,
                this.material,
                active,
                inactive,
                this.isGeneric,
                this.additionalTooltips);
    }
}
