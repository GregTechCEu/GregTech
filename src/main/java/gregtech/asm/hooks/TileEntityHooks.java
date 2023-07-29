package gregtech.asm.hooks;

import gregtech.api.GregTechAPI;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public class TileEntityHooks {

    public static TileEntity createMTE(String id) {
        ResourceLocation location = new ResourceLocation(id);
        if (GregTechAPI.MTE_REGISTRY.isMetaTileEntity(location)) {
            return GregTechAPI.MTE_REGISTRY.loadMetaTileEntity(location);
        }
        return null;
    }
}
