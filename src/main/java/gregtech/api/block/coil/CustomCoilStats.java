package gregtech.api.block.coil;

import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.unification.material.Material;
import gregtech.api.util.function.QuadConsumer;
import gregtech.client.model.ActiveVariantBlockBakedModel;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

public final class CustomCoilStats implements IHeatingCoilBlockStats, Comparable<CustomCoilStats>,
                                   IStringSerializable {

    private final String name;

    // electric blast furnace properties
    private final int coilTemperature;

    // multi smelter properties
    private final int level;
    private final int energyDiscount;

    // voltage tier
    private final int tier;

    private final Material material;
    private final ModelResourceLocation active;
    private final ModelResourceLocation inactive;
    private final boolean isGeneric;
    private final QuadConsumer<ItemStack, World, List<String>, Boolean> additionalTooltips;

    CustomCoilStats(String name,
                    int coilTemperature,
                    int level,
                    int energyDiscount,
                    int tier,
                    Material material,
                    ModelResourceLocation active,
                    ModelResourceLocation inactive,
                    boolean isGeneric,
                    QuadConsumer<ItemStack, World, List<String>, Boolean> additionalTooltips) {
        this.name = name;
        this.coilTemperature = coilTemperature;
        this.level = level;
        this.energyDiscount = energyDiscount;
        this.tier = tier;
        this.material = material;
        this.active = active;
        this.inactive = inactive;
        this.isGeneric = isGeneric;
        this.additionalTooltips = additionalTooltips;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public int getCoilTemperature() {
        return coilTemperature;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnergyDiscount() {
        return energyDiscount;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public @Nullable Material getMaterial() {
        return material;
    }

    @Override
    public ActiveVariantBlockBakedModel createModel(BooleanSupplier bloomConfig) {
        return new ActiveVariantBlockBakedModel(inactive, active, bloomConfig);
    }

    @Override
    public int compareTo(@NotNull CustomCoilStats o) {
        // todo add more comparisons?
        return Integer.compare(o.getTier(), this.getTier());
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack itemStack, @Nullable World worldIn, @NotNull List<String> lines,
                               @NotNull ITooltipFlag tooltipFlag) {
        if (this.additionalTooltips != null) {
            this.additionalTooltips.accept(itemStack, worldIn, lines, tooltipFlag.isAdvanced());
        }
    }

    public boolean isGeneric() {
        return isGeneric;
    }
}
