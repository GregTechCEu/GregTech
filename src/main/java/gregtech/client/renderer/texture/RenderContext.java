package gregtech.client.renderer.texture;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.common.property.IExtendedBlockState;

import org.jetbrains.annotations.Nullable;

public class RenderContext {

    private static final ThreadLocal<RenderContext> contextThreadLocal = ThreadLocal.withInitial(RenderContext::new);

    public IBlockState state;
    public IBlockAccess world;
    public BlockPos pos;
    public Matrix4f translation;

    public static RenderContext getContext() {
        return contextThreadLocal.get();
    }

    @Nullable
    public IExtendedBlockState getExtendedState() {
        if (this.state instanceof IExtendedBlockState extendedState) {
            return (extendedState);
        }
        return null;
    }

    public boolean canRender(EnumFacing side, BlockRenderLayer renderLayer) {
        if (renderLayer == null || renderLayer != MinecraftForgeClient.getRenderLayer()) return false;
        return this.state.shouldSideBeRendered(world, pos, side);
    }

    public void updateLighter(VertexLighterFlat lighter, VertexBufferConsumer consumer) {
        consumer.setOffset(pos);
        lighter.setParent(consumer);
        lighter.setWorld(this.world);
        lighter.setState(this.state);
        lighter.setBlockPos(this.pos);
        lighter.updateBlockInfo();
    }

    public boolean useAo() {
        return Minecraft.isAmbientOcclusionEnabled() && this.state.getLightValue(this.world, this.pos) == 0;
    }
}
