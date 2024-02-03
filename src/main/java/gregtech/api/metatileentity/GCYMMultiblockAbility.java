package gregtech.api.metatileentity;

import gregtech.api.capability.IParallelHatch;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;

@SuppressWarnings("InstantiationOfUtilityClass")
public final class GCYMMultiblockAbility {

    public static final MultiblockAbility<IParallelHatch> PARALLEL_HATCH = new MultiblockAbility<>("parallel_hatch");

    public static final MultiblockAbility<ITieredMetaTileEntity> TIERED_HATCH = new MultiblockAbility<>("tiered_hatch");

    private GCYMMultiblockAbility() {}
}
