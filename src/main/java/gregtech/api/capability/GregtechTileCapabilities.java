package gregtech.api.capability;

import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.cover.CoverHolder;
import gregtech.api.metatileentity.multiblock.IMaintenance;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class GregtechTileCapabilities {

    @CapabilityInject(IWorkable.class)
    public static Capability<IWorkable> CAPABILITY_WORKABLE = null;

    @CapabilityInject(CoverHolder.class)
    public static Capability<CoverHolder> CAPABILITY_COVER_HOLDER = null;

    @CapabilityInject(IControllable.class)
    public static Capability<IControllable> CAPABILITY_CONTROLLABLE = null;

    @CapabilityInject(IActiveOutputSide.class)
    public static Capability<IActiveOutputSide> CAPABILITY_ACTIVE_OUTPUT_SIDE = null;

    @CapabilityInject(AbstractRecipeLogic.class)
    public static Capability<AbstractRecipeLogic> CAPABILITY_RECIPE_LOGIC = null;

    @CapabilityInject(IMultipleRecipeMaps.class)
    public static Capability<IMultipleRecipeMaps> CAPABILITY_MULTIPLE_RECIPEMAPS = null;

    @CapabilityInject(IMaintenance.class)
    public static Capability<IMaintenance> CAPABILITY_MAINTENANCE = null;

    @CapabilityInject(IDataAccessHatch.class)
    public static Capability<IDataAccessHatch> CAPABILITY_DATA_ACCESS = null;

    @CapabilityInject(ILaserContainer.class)
    public static Capability<ILaserContainer> CAPABILITY_LASER = null;

    @CapabilityInject(IOpticalComputationProvider.class)
    public static Capability<IOpticalComputationProvider> CABABILITY_COMPUTATION_PROVIDER = null;
}
