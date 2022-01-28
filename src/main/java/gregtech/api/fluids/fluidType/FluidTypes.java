package gregtech.api.fluids.fluidType;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenProperty;

@ZenClass("mods.gregtech.material.FluidTypes")
@ZenRegister
public class FluidTypes {

    @ZenProperty
    public static final FluidType LIQUID = new FluidTypeLiquid("liquid", "", "", "gregtech.fluid.generic");

    @ZenProperty
    public static final FluidType ACID = new FluidTypeAcid("acid", "", "", "gregtech.fluid.generic");

    @ZenProperty
    public static final FluidType GAS = new FluidTypeGas("gas", "", "", "gregtech.fluid.generic");

    @ZenProperty
    public static final FluidType PLASMA = new FluidTypePlasma("plasma", "plasma", "", "gregtech.fluid.plasma");
}
