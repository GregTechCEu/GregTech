package gregtech.client.renderer.scene;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Collection;

@SideOnly(Side.CLIENT)
public class VBOWorldSceneRenderer extends ImmediateWorldSceneRenderer {

    protected final VertexBuffer[] vbos = new VertexBuffer[BlockRenderLayer.values().length];
    protected boolean isDirty = true;

    public VBOWorldSceneRenderer(World world) {
        super(world);
    }

    private void uploadVBO() {
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();

        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {

                var vbo = this.vbos[layer.ordinal()] = new VertexBuffer(DefaultVertexFormats.BLOCK);

                renderBlockLayer(layer);

                // Get the buffer again
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.finishDrawing();
                buffer.reset();

                ByteBuffer data = buffer.getByteBuffer();
                vbo.bufferData(data);
            }
        } finally {
            ForgeHooksClient.setRenderLayer(oldRenderLayer);
        }
        this.isDirty = false;
    }

    @Override
    protected void drawWorld() {
        if (this.isDirty) {
            uploadVBO();
        }

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();

        var oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        for (var layer : BlockRenderLayer.values()) {

            ForgeHooksClient.setRenderLayer(layer);

            int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
            setDefaultPassRenderState(pass);

            GlStateManager.pushMatrix();
            {
                var vbo = this.vbos[layer.ordinal()];
                vbo.bindBuffer();
                setupClientStates();
                setupArrayPointers();
                vbo.drawArrays(GL11.GL_QUADS);
                resetClientStates();
                vbo.unbindBuffer();
            }
            GlStateManager.popMatrix();
        }
        ForgeHooksClient.setRenderLayer(oldRenderLayer);

        renderTESR(); // Handles TileEntities

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    @Override
    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks) {
        this.isDirty = true;
        return super.addRenderedBlocks(blocks);
    }

    protected void setupClientStates() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
    }

    protected void resetClientStates() {
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
    }

    protected void setupArrayPointers() {
        // 28 == DefaultVertexFormats.BLOCK.getSize();
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
        GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12);
        GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
