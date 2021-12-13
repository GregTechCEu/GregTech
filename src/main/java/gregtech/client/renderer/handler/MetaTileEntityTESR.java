package gregtech.client.renderer.handler;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
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
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class MetaTileEntityTESR extends TileEntitySpecialRenderer<MetaTileEntityHolder> {

    @Override
    public void render(MetaTileEntityHolder te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(buffer);
            renderState.setBrightness(te.getWorld(), te.getPos());
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntityFast(renderState, new Matrix4().translate(x, y, z), partialTicks);
        }
        if (metaTileEntity != null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
                if (cover instanceof IFastRenderMetaTileEntity) {
                    CCRenderState renderState = CCRenderState.instance();
                    renderState.reset();
                    renderState.bind(buffer);
                    renderState.setBrightness(te.getWorld(), te.getPos().offset(side));
                    ((IFastRenderMetaTileEntity) cover).renderMetaTileEntityFast(renderState, new Matrix4().translate(x, y, z), partialTicks);
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
                if (cover instanceof IFastRenderMetaTileEntity) {
                    ((IFastRenderMetaTileEntity) cover).renderMetaTileEntity(x, y, z, partialTicks);
                }
            }
        }
    }

    @Override
    public void renderTileEntityFast(MetaTileEntityHolder te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, @Nonnull BufferBuilder buffer) {
        MetaTileEntity metaTileEntity = te.getMetaTileEntity();
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(buffer);
            renderState.setBrightness(te.getWorld(), te.getPos());
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntityFast(renderState, new Matrix4().translate(x, y, z), partialTicks);
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntity(x, y, z, partialTicks);
        }
        if (metaTileEntity != null) {
            for (EnumFacing side : EnumFacing.VALUES) {
                CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
                if (cover instanceof IFastRenderMetaTileEntity) {
                    CCRenderState renderState = CCRenderState.instance();
                    renderState.reset();
                    renderState.bind(buffer);
                    renderState.setBrightness(te.getWorld(), te.getPos().offset(side));
                    ((IFastRenderMetaTileEntity) cover).renderMetaTileEntityFast(renderState, new Matrix4().translate(x, y, z), partialTicks);
                    ((IFastRenderMetaTileEntity) cover).renderMetaTileEntity(x, y, z, partialTicks);
                }
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(@Nonnull MetaTileEntityHolder te) {
        if (te.getMetaTileEntity() instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) te.getMetaTileEntity()).isGlobalRenderer();
        }
        return false;
    }
}
