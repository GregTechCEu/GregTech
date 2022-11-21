package gregtech.module;

import gregtech.api.module.IGregTechModule;
import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public abstract class BaseGregTechModule implements IGregTechModule {

    @Nonnull
    @Override
    public Set<ResourceLocation> getDependencyUids() {
        return Collections.singleton(new ResourceLocation(GTValues.MODID, GregTechModules.MODULE_CORE));
    }
}
