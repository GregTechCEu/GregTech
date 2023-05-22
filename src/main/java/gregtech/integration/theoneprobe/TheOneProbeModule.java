package gregtech.integration.theoneprobe;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.theoneprobe.provider.*;
import gregtech.modules.GregTechModules;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

@GregTechModule(
        moduleID = GregTechModules.MODULE_TOP,
        containerID = GTValues.MODID,
        name = "GregTech TheOneProbe Integration",
        descriptionKey = "gregtech.modules.top_integration.description"
)
public class TheOneProbeModule extends IntegrationSubmodule {

    @Nonnull
    @Override
    public Set<String> getModDependencyIDs() {
        return Collections.singleton(GTValues.MODID_TOP);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        getLogger().info("TheOneProbe found. Enabling integration...");
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new ElectricContainerInfoProvider());
        oneProbe.registerProvider(new FuelableInfoProvider());
        oneProbe.registerProvider(new WorkableInfoProvider());
        oneProbe.registerProvider(new ControllableInfoProvider());
        oneProbe.registerProvider(new DebugPipeNetInfoProvider());
        oneProbe.registerProvider(new TransformerInfoProvider());
        oneProbe.registerProvider(new DiodeInfoProvider());
        oneProbe.registerProvider(new MultiblockInfoProvider());
        oneProbe.registerProvider(new MaintenanceInfoProvider());
        oneProbe.registerProvider(new MultiRecipeMapInfoProvider());
        oneProbe.registerProvider(new ConverterInfoProvider());
        oneProbe.registerProvider(new RecipeLogicInfoProvider());
        oneProbe.registerProvider(new PrimitivePumpInfoProvider());
        oneProbe.registerProvider(new CoverProvider());
        oneProbe.registerProvider(new BlockOreProvider());
        oneProbe.registerProvider(new LampInfoProvider());
    }
}
