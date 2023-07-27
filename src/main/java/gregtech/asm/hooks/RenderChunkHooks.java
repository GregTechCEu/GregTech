package gregtech.asm.hooks;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class RenderChunkHooks {

    public static <T extends TileEntity> @Nullable TileEntitySpecialRenderer<T> getRenderer(TileEntityRendererDispatcher renderer, @Nullable TileEntity tileEntityIn) {
        if (tileEntityIn instanceof MetaTileEntity mte && !mte.hasTESR()) {
            return null;
        }
        return renderer.getRenderer(tileEntityIn);
    }
}
