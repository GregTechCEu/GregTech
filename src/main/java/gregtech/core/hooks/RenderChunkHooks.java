package gregtech.core.hooks;

import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class RenderChunkHooks {
    public static <T extends TileEntity> TileEntitySpecialRenderer<T> getRenderer(TileEntityRendererDispatcher renderer, @Nullable TileEntity tileEntityIn) {
        // TODO
        if (tileEntityIn instanceof MetaTileEntityHolder && !((MetaTileEntityHolder) tileEntityIn).hasTESR()) {
            return null;
        }
        return renderer.getRenderer(tileEntityIn);
    }
}
