package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.capability.IMufflerHatch;
import gregtech.common.metatileentities.electric.multiblockpart.MetaTileEntityRotorHolder;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

@SuppressWarnings("InstantiationOfUtilityClass")
public class MultiblockAbility<T> {

    public static final MultiblockAbility<IItemHandlerModifiable> EXPORT_ITEMS = new MultiblockAbility<>();
    public static final MultiblockAbility<IItemHandlerModifiable> IMPORT_ITEMS = new MultiblockAbility<>();

    public static final MultiblockAbility<IFluidTank> EXPORT_FLUIDS = new MultiblockAbility<>();
    public static final MultiblockAbility<IFluidTank> IMPORT_FLUIDS = new MultiblockAbility<>();

    public static final MultiblockAbility<IEnergyContainer> INPUT_ENERGY = new MultiblockAbility<>();
    public static final MultiblockAbility<IEnergyContainer> OUTPUT_ENERGY = new MultiblockAbility<>();

    public static final MultiblockAbility<MetaTileEntityRotorHolder> ABILITY_ROTOR_HOLDER = new MultiblockAbility<>();

    public static final MultiblockAbility<IFluidTank> PUMP_FLUID_HATCH = new MultiblockAbility<>();

    public static final MultiblockAbility<IFluidTank> STEAM = new MultiblockAbility<>();
    public static final MultiblockAbility<IItemHandlerModifiable> STEAM_IMPORT_ITEMS = new MultiblockAbility<>();
    public static final MultiblockAbility<IItemHandlerModifiable> STEAM_EXPORT_ITEMS = new MultiblockAbility<>();

    public static final MultiblockAbility<IMaintenanceHatch> MAINTENANCE_HATCH = new MultiblockAbility<>();
    public static final MultiblockAbility<IMufflerHatch> MUFFLER_HATCH = new MultiblockAbility<>();
}
