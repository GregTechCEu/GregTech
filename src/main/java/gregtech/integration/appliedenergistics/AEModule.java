package gregtech.integration.appliedenergistics;

import appeng.api.features.IRegistryContainer;
import appeng.api.movable.IMovableRegistry;
import appeng.core.Api;
import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.modules.GregTechModule;
import gregtech.common.pipelike.cable.tile.TileEntityCable;
import gregtech.common.pipelike.cable.tile.TileEntityCableTickable;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import gregtech.common.pipelike.laser.tile.TileEntityLaserPipe;
import gregtech.common.pipelike.optical.tile.TileEntityOpticalPipe;
import gregtech.integration.IntegrationSubmodule;
import gregtech.modules.GregTechModules;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@GregTechModule(
        moduleID = GregTechModules.MODULE_AE,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_APPENG,
        name = "GregTech Applied Energistics 2 Integration",
        descriptionKey = "gregtech.modules.ae_integration.description"
)
public class AEModule extends IntegrationSubmodule {
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        getLogger().info("AE2 found. Enabling integration...");

        final IRegistryContainer registries = Api.INSTANCE.registries();
        final IMovableRegistry mr = registries.movable();
        getLogger().info("Registering Gregtech Tile Entities to the Spatial IO whitelist...");
        mr.whiteListTileEntity(MetaTileEntityHolder.class);
        mr.whiteListTileEntity(TileEntityCable.class);
        mr.whiteListTileEntity(TileEntityCableTickable.class);
        mr.whiteListTileEntity(TileEntityFluidPipe.class);
        mr.whiteListTileEntity(TileEntityItemPipe.class);
        mr.whiteListTileEntity(TileEntityOpticalPipe.class);
        mr.whiteListTileEntity(TileEntityLaserPipe.class);
        mr.whiteListTileEntity(TileEntityFluidPipeTickable.class);
        mr.whiteListTileEntity(TileEntityItemPipeTickable.class);
    }
}
