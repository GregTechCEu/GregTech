package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.IEnergyContainer;
import gregtech.common.pipelike.laser.tile.LaserContainer;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

public class MultiblockAbility<T> {

    public static final MultiblockAbility<IItemHandlerModifiable> EXPORT_ITEMS = new MultiblockAbility<>();
    public static final MultiblockAbility<IItemHandlerModifiable> IMPORT_ITEMS = new MultiblockAbility<>();

    public static final MultiblockAbility<IFluidTank> EXPORT_FLUIDS = new MultiblockAbility<>();
    public static final MultiblockAbility<IFluidTank> IMPORT_FLUIDS = new MultiblockAbility<>();

    public static final MultiblockAbility<IEnergyContainer> INPUT_ENERGY = new MultiblockAbility<>();
    public static final MultiblockAbility<IEnergyContainer> OUTPUT_ENERGY = new MultiblockAbility<>();

    public static MultiblockAbility<LaserContainer> INPUT_LASER = new MultiblockAbility<>();
    public static MultiblockAbility<LaserContainer> OUTPUT_LASER= new MultiblockAbility<>();
}
