package gregtech.api.block;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ZenClass("mods.gregtech.blocks.HeatingCoils")
@ZenRegister
@SuppressWarnings("unused")
public class CTHeatingCoilBlockStats implements IHeatingCoilBlockStats {

    private final String name;
    private final int coilTemperature;
    private final int level;
    private final int energyDiscount;
    private final Material material;

    /**
     * @param name            the unique name of the Heating Coil
     * @param coilTemperature the temperature of the Heating Coil
     * @param level           the level of the Heating Coil - used for Multismelter parallel amount
     * @param energyDiscount  the energy discount of the Heating Coil
     * @param material        the {@link Material} of the Heating Coil, use null for no specific material
     */
    public CTHeatingCoilBlockStats(String name, int coilTemperature, int level, int energyDiscount, @Nullable Material material) {
        this.name = name;
        this.coilTemperature = coilTemperature;
        this.level = level;
        this.energyDiscount = energyDiscount;
        this.material = material;
    }

    @Nonnull
    @Override
    public String getName() {
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

    @Nullable
    @Override
    public Material getMaterial() {
        return material;
    }

    @ZenMethod()
    public static void add(@Nonnull IBlockState state, @Nonnull String name, int coilTemperature, int level, int energyDiscount, @Nullable Material material) {
        GregTechAPI.HEATING_COILS.put((net.minecraft.block.state.IBlockState) state.getInternal(), new CTHeatingCoilBlockStats(name, coilTemperature, level, energyDiscount, material));
    }

    @ZenMethod
    public static void remove(@Nonnull IBlockState state) {
        GregTechAPI.HEATING_COILS.remove((net.minecraft.block.state.IBlockState) state.getInternal());
    }
}
