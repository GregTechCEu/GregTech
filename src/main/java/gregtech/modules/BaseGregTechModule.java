package gregtech.modules;

import gregtech.api.modules.IGregTechModule;
import gregtech.api.util.GTUtility;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public abstract class BaseGregTechModule implements IGregTechModule {

    @Nonnull
    @Override
    public Set<ResourceLocation> getDependencyUids() {
        return Collections.singleton(GTUtility.gregtechId(GregTechModules.MODULE_CORE));
    }
}
