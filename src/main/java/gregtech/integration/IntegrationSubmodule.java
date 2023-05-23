package gregtech.integration;

import gregtech.api.GTValues;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

/**
 * Abstract class meant to be used by mod-specific compatibility modules.
 * Implements some shared skeleton code that should be shared by other modules.
 */
public abstract class IntegrationSubmodule extends BaseGregTechModule {

    private static final Set<ResourceLocation> DEPENDENCY_UID = Collections.singleton(
            new ResourceLocation(GTValues.MODID, GregTechModules.MODULE_INTEGRATION));

    @Nonnull
    @Override
    public Logger getLogger() {
        return IntegrationModule.logger;
    }

    @Nonnull
    @Override
    public Set<ResourceLocation> getDependencyUids() {
        return DEPENDENCY_UID;
    }
}
