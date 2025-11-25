package gregtech.client.renderer.scene;

import gregtech.api.util.Mods;
import gregtech.client.utils.OptiFineHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.ShadersRender;

import org.lwjgl.opengl.GL11;

import java.util.Collection;

@SideOnly(Side.CLIENT)
public class VBOWorldSceneRenderer extends ImmediateWorldSceneRenderer {

    protected static final VertexBuffer[] VBOS = new VertexBuffer[BlockRenderLayer.values().length];
    protected boolean isDirty = true;

    public VBOWorldSceneRenderer(World world) {
        super(world);
    }

    private void uploadVBO() {
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();

        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {

                OptiFineHelper.preRenderChunkLayer(layer);

                renderBlockLayer(layer);

                // Get the buffer again
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.finishDrawing();
                buffer.reset();

                int i = layer.ordinal();
                var vbo = VBOS[i];
                if (vbo == null) vbo = VBOS[i] = new VertexBuffer(DefaultVertexFormats.BLOCK);
                vbo.bufferData(buffer.getByteBuffer());

                OptiFineHelper.postRenderChunkLayer(layer);
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
        if (beforeRender != null) {
            beforeRender.accept(this);
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

            OptiFineHelper.preRenderChunkLayer(layer);

            GlStateManager.pushMatrix();
            {
                int i = layer.ordinal();
                var vbo = VBOS[i];
                vbo.bindBuffer();
                enableClientStates();
                setupArrayPointers();
                vbo.drawArrays(GL11.GL_QUADS);
                disableClientStates();
                vbo.unbindBuffer();
            }
            GlStateManager.popMatrix();

            OptiFineHelper.postRenderChunkLayer(layer);
        }
        ForgeHooksClient.setRenderLayer(oldRenderLayer);

        renderTileEntities(); // Handle TileEntities

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        if (afterRender != null) {
            afterRender.accept(this);
        }
    }

    @Override
    public WorldSceneRenderer addRenderedBlocks(Collection<BlockPos> blocks) {
        this.isDirty = true;
        return super.addRenderedBlocks(blocks);
    }

    protected void enableClientStates() {
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);
    }

    protected void disableClientStates() {
        for (VertexFormatElement element : DefaultVertexFormats.BLOCK.getElements()) {
            switch (element.getUsage()) {
                case POSITION -> GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                case COLOR -> GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);
                case UV -> {
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + element.getIndex());
                    GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                }
                default -> {}
            }
        }
    }

    protected void setupArrayPointers() {
        if (Mods.ShadersMod.isModLoaded()) {
            ShadersRender.setupArrayPointersVbo();
        } else {
            // 28 == DefaultVertexFormats.BLOCK.getSize();
            GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
            GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, 12);
            GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        }
    }
}
