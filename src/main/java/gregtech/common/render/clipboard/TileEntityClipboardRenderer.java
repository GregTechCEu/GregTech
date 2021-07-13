package gregtech.common.render.clipboard;
/*
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.MetaTileEntityRenderer;
import gregtech.common.blocks.clipboard.MetaTileEntityClipboard;
import gregtech.common.tileentities.GTNativeTileEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;

public class TileEntityClipboardRenderer extends MetaTileEntityRenderer {
    private double textSpacing = -0.0658D;

    @Override
    public void render(MetaTileEntity tileEntity, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat) {
        MetaTileEntityClipboard tile = (MetaTileEntityClipboard) tileEntity;
        if (tile != null) {
            renderText(tile.titletext, 0.037D, 0.825D, 0.27D);
            renderText(tile.button0text, 0.037D, 0.76D, 0.222D);
            renderText(tile.button1text, 0.037D, 0.76D + 1.0D * this.textSpacing, 0.222D);
            renderText(tile.button2text, 0.037D, 0.76D + 2.0D * this.textSpacing, 0.222D);
            renderText(tile.button3text, 0.037D, 0.76D + 3.0D * this.textSpacing, 0.222D);
            renderText(tile.button4text, 0.037D, 0.76D + 4.0D * this.textSpacing, 0.222D);
            renderText(tile.button5text, 0.037D, 0.76D + 5.0D * this.textSpacing, 0.222D);
            renderText(tile.button6text, 0.037D, 0.76D + 6.0D * this.textSpacing, 0.222D);
            renderText(tile.button7text, 0.037D, 0.76D + 7.0D * this.textSpacing, 0.222D);
            String pageNum = "" + tile.currentPage;
            if (tile.currentPage > 9) {
                renderText(pageNum, 0.037D, 0.17D, 0.03D);
            } else {
                renderText(pageNum, 0.037D, 0.17D, 0.02D);
            }
        }
    }

    public void renderText(String text, double xAdjust, double yAdjust, double zAdjust) {
        FontRenderer fontRender = this.getFontRenderer();
        float offsetx = 0.0F;
        float offsetz = 0.0F;
        switch(this.getAngle()) {
            case SOUTH:
                offsetx = -0.0116F;
                break;
            case WEST:
                offsetz = -0.0116F;
                break;
            case NORTH:
                offsetx = 0.0116F;
                break;
            case EAST:
                offsetz = 0.0116F;
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.globalX + 0.5D + offsetx, this.globalY, this.globalZ + 0.5D + offsetz);
        switch (getAngle()) {
            case SOUTH:
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case WEST:
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            case NORTH:
            default:
                break;
            case EAST:
                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        }
        GlStateManager.translate(-0.5D + xAdjust, yAdjust, zAdjust);
        GlStateManager.depthMask(false);
        GlStateManager.scale(0.0045F, 0.0045F, 0.0045F);
        GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        switch (this.shift) {
            case HALF_SHIFT:
                GlStateManager.translate(0.0D, 0.0D, -95.0D);
                break;
            case FULL_SHIFT:
                GlStateManager.translate(0.0D, 0.0D, -205.0D);
                break;
        }
        additionalGLStuffForText();
        GlStateManager.glNormal3f(0.0F, 0.0F, -0.010416667F);
        fontRender.drawString(text, 0, 0, 0);
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

}
*/
