package gregtech.api.render;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.IRenderMetaTileEntity;
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
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MetaTileEntityTESR extends TileEntitySpecialRenderer<MetaTileEntityHolder> {

    @Override
    public void render(MetaTileEntityHolder te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        MetaTileEntity metaTileEntity = te.getMetaTileEntity();
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            renderTileEntityFastPart(te, x, y, z, partialTicks, destroyStage);
        }
        if (metaTileEntity instanceof IRenderMetaTileEntity) {
            ((IRenderMetaTileEntity) metaTileEntity).renderMetaTileEntityDynamic(x, y, z, partialTicks);
        }
        if (metaTileEntity != null) {
            List<Tuple<IFastRenderMetaTileEntity, EnumFacing>> coverFast = new ArrayList<>();
            for (EnumFacing side : EnumFacing.VALUES) {
                CoverBehavior cover = metaTileEntity.getCoverAtSide(side);
                if (cover instanceof IFastRenderMetaTileEntity) {
                    coverFast.add(new Tuple<>((IFastRenderMetaTileEntity) cover, side));
                } else if (cover instanceof IRenderMetaTileEntity) {
                    ((IRenderMetaTileEntity) cover).renderMetaTileEntityDynamic(x, y, z, partialTicks);
                }
            }
            if (!coverFast.isEmpty()) {
                renderCoverFast(te, x, y, x, partialTicks, destroyStage, alpha, coverFast);
            }
        }
    }

    /**
     * Used Specifically to render covers that are IFastRenderMetaTileEntity
     * Adapted from renderTileEntityFastPart
     **/
    private void renderCoverFast(MetaTileEntityHolder te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, List<Tuple<IFastRenderMetaTileEntity, EnumFacing>> coverList) {

        Matrix4 translation = (new Matrix4()).translate(x, y, z);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        for (Tuple<IFastRenderMetaTileEntity, EnumFacing> tuple : coverList) {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(buffer);
            renderState.setBrightness(te.getWorld(), te.getPos().offset(tuple.getSecond()));
            tuple.getFirst().renderMetaTileEntityFast(renderState, translation, partialTicks);
        }

        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
    }

    private void renderTileEntityFastPart(MetaTileEntityHolder te, double x, double y, double z, float partialTicks, int destroyStage) {
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

        renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, partialTicks, buffer);
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void renderTileEntityFast(MetaTileEntityHolder te, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer) {
        MetaTileEntity metaTileEntity = te.getMetaTileEntity();
        if (metaTileEntity instanceof IFastRenderMetaTileEntity) {
            CCRenderState renderState = CCRenderState.instance();
            renderState.reset();
            renderState.bind(buffer);
            renderState.setBrightness(te.getWorld(), te.getPos());
            Matrix4 translation = new Matrix4().translate(x, y, z);
            ((IFastRenderMetaTileEntity) metaTileEntity).renderMetaTileEntityFast(renderState, translation, partialTicks);
        }
    }

    @Override
    public boolean isGlobalRenderer(@Nonnull MetaTileEntityHolder te) {
        if (te instanceof IRenderMetaTileEntity) {
            if (((IRenderMetaTileEntity) te).isGlobalRenderer()) {
                return true;
            }
        } else if (te instanceof IFastRenderMetaTileEntity) {
            if (((IFastRenderMetaTileEntity) te).isGlobalRenderer()) {
                return true;
            }
        }
        if (te.getMetaTileEntity() instanceof IRenderMetaTileEntity) {
            if (((IRenderMetaTileEntity) te.getMetaTileEntity()).isGlobalRenderer()) {
                return true;
            }
        } else if (te.getMetaTileEntity() instanceof IFastRenderMetaTileEntity) {
            return ((IFastRenderMetaTileEntity) te.getMetaTileEntity()).isGlobalRenderer();
        }
        return false;
    }
}
