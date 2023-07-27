package gregtech.client.renderer.handler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
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
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class MetaTileEntityTESR extends TileEntitySpecialRenderer<MetaTileEntity> {

    @Override
    public void render(MetaTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled())
        {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        }
        else
        {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        MetaTileEntity metaTileEntity = te.getMetaTileEntity();
        if (metaTileEntity instanceof IFastRenderMetaTileEntity fastRender) {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(buffer);
            renderState.setBrightness(te.getWorld(), te.getPos());
            fastRender.renderMetaTileEntityFast(renderState, new Matrix4().translate(x, y, z), partialTicks);
        }
        if (metaTileEntity != null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
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
                CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
                if (cover instanceof IFastRenderMetaTileEntity fastRender) {
                    fastRender.renderMetaTileEntity(x, y, z, partialTicks);
                }
            }
        }
    }

    @Override
    public void renderTileEntityFast(MetaTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, @Nonnull BufferBuilder buffer) {
        if (te instanceof IFastRenderMetaTileEntity fastRender) {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(buffer);
            renderState.setBrightness(te.getWorld(), te.getPos());
            fastRender.renderMetaTileEntityFast(renderState, new Matrix4().translate(x, y, z), partialTicks);
            fastRender.renderMetaTileEntity(x, y, z, partialTicks);
        }
        if (te != null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                CoverBehavior cover = te.getCoverAtSide(side);
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
    public boolean isGlobalRenderer(@Nonnull MetaTileEntity te) {
        if (te.getMetaTileEntity() instanceof IFastRenderMetaTileEntity fastRender) {
            return fastRender.isGlobalRenderer();
        }
        return false;
    }
}
