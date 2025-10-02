package gregtech.api.block.coil;

import gregtech.api.GTValues;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
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

    String name;

    // electric blast furnace properties
    int coilTemperature = -1;

    // multi smelter properties
    int level = -1;
    int energyDiscount = 0;

    // voltage tier
    int tier = GTValues.ULV;

    Material material = Materials.Iron;
    ModelResourceLocation active, inactive;
    boolean isGeneric;
    QuadConsumer<ItemStack, World, List<String>, ITooltipFlag> additionalTooltips;

    CustomCoilStats() {}

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
            this.additionalTooltips.accept(itemStack, worldIn, lines, tooltipFlag);
        }
    }
}
