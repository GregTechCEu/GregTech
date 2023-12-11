package gregtech.client.renderer.handler;

import gregtech.api.cover.Cover;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class MetaTileEntityTESR extends TileEntitySpecialRenderer<MetaTileEntityHolder> {

    @Override
    public void render(@NotNull MetaTileEntityHolder te, double x, double y, double z, float partialTicks,
                       int destroyStage,
                       float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        MetaTileEntity metaTileEntity = te.getMetaTileEntity();
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(buffer);
            renderState.setBrightness(te.getWorld(), te.getPos());
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntityFast(renderState,
                    new Matrix4().translate(x, y, z), partialTicks);
        }
        if (metaTileEntity != null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                Cover cover = metaTileEntity.getCoverAtSide(side);
                if (cover instanceof IFastRenderMetaTileEntity fastRender) {
                    CCRenderState renderState = CCRenderState.instance();
                    renderState.reset();
                    renderState.bind(buffer);
                    renderState.setBrightness(te.getWorld(), te.getPos().offset(side));
                    fastRender.renderMetaTileEntityFast(renderState, new Matrix4().translate(x, y, z), partialTicks);
                }
            }
        }
        buffer.setTranslation(0, 0, 0);

        tessellator.draw();

        RenderHelper.enableStandardItemLighting();

        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntity(x, y, z, partialTicks);
        }
        if (metaTileEntity != null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                Cover cover = metaTileEntity.getCoverAtSide(side);
                if (cover instanceof IFastRenderMetaTileEntity fastRender) {
                    fastRender.renderMetaTileEntity(x, y, z, partialTicks);
                }
            }
        }
    }

    @Override
    public void renderTileEntityFast(MetaTileEntityHolder te, double x, double y, double z, float partialTicks,
                                     int destroyStage, float alpha, @NotNull BufferBuilder buffer) {
        MetaTileEntity metaTileEntity = te.getMetaTileEntity();
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(buffer);
            renderState.setBrightness(te.getWorld(), te.getPos());
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntityFast(renderState,
                    new Matrix4().translate(x, y, z), partialTicks);
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntity(x, y, z, partialTicks);
        }
        if (metaTileEntity != null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                Cover cover = metaTileEntity.getCoverAtSide(side);
                if (cover instanceof IFastRenderMetaTileEntity fastRender) {
                    CCRenderState renderState = CCRenderState.instance();
                    renderState.reset();
                    renderState.bind(buffer);
                    renderState.setBrightness(te.getWorld(), te.getPos().offset(side));
                    fastRender.renderMetaTileEntityFast(renderState, new Matrix4().translate(x, y, z), partialTicks);
                    fastRender.renderMetaTileEntity(x, y, z, partialTicks);
                }
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(@NotNull MetaTileEntityHolder te) {
        if (te.getMetaTileEntity() instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) te.getMetaTileEntity()).isGlobalRenderer();
        }
        return false;
    }
}
