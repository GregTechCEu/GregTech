package gregtech.integration.crafttweaker.block;

import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.unification.material.Material;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.block.IBlockState;
import crafttweaker.api.minecraft.CraftTweakerMC;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.gregtech.blocks.HeatingCoils")
@ZenRegister
@SuppressWarnings("unused")
public class CTHeatingCoilBlockStats implements IHeatingCoilBlockStats {

    private final String name;
    private final int coilTemperature;
    private final int level;
    private final int energyDiscount;
    private final int tier;
    private final Material material;

    /**
     * @param name            the unique name of the Heating Coil
     * @param coilTemperature the temperature of the Heating Coil
     * @param level           the level of the Heating Coil - used for Multismelter parallel amount
     * @param energyDiscount  the energy discount of the Heating Coil
     * @param tier            the tier of the Heating Coil - used for cracker pyrolyse discounts
     * @param material        the {@link Material} of the Heating Coil, use null for no specific material
     */
    public CTHeatingCoilBlockStats(String name, int coilTemperature, int level, int energyDiscount, int tier,
                                   @Nullable Material material) {
        this.name = name;
        this.coilTemperature = coilTemperature;
        this.level = level;
        this.energyDiscount = energyDiscount;
        this.tier = tier;
        this.material = material;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getCoilTemperature() {
        return this.coilTemperature;
    }

    @Override
    public int getLevel() {
        return this.level;
    }

    @Override
    public int getEnergyDiscount() {
        return this.energyDiscount;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    @Nullable
    @Override
    public Material getMaterial() {
        return this.material;
    }

    @ZenMethod
    public static void add(@NotNull IBlockState state, @NotNull String name, int coilTemperature, int level,
                           int energyDiscount, int tier, @Nullable Material material) {
        GregTechAPI.HEATING_COILS.put(CraftTweakerMC.getBlockState(state),
                new CTHeatingCoilBlockStats(name, coilTemperature, level, energyDiscount, tier, material));
    }

    @ZenMethod
    public static void remove(@NotNull IBlockState state) {
        GregTechAPI.HEATING_COILS.remove(CraftTweakerMC.getBlockState(state));
    }
}
