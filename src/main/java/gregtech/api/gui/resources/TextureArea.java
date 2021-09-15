package gregtech.api.gui.resources;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import gregtech.api.GTValues;
import gregtech.api.util.Position;
import gregtech.api.util.PositionedRect;
import gregtech.api.util.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Represents a texture area of image
 * This representation doesn't take image size in account, so all image variables are
 * 0.0 - 1.0 bounds
 */
public class TextureArea implements IGuiTexture {

    public final ResourceLocation imageLocation;

    public final double offsetX;
    public final double offsetY;

    public final double imageWidth;
    public final double imageHeight;

    public TextureArea(ResourceLocation imageLocation, double offsetX, double offsetY, double width, double height) {
        this.imageLocation = imageLocation;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.imageWidth = width;
        this.imageHeight = height;
    }

    public static TextureArea fullImage(String imageLocation) {
        return new TextureArea(new ResourceLocation(GTValues.MODID, imageLocation), 0.0, 0.0, 1.0, 1.0);
    }

    public static TextureArea areaOfImage(String imageLocation, int imageSizeX, int imageSizeY, int u, int v, int width, int height) {
        return new TextureArea(new ResourceLocation(imageLocation),
                u / (imageSizeX * 1.0),
                v / (imageSizeY * 1.0),
                (u + width) / (imageSizeX * 1.0),
                (v + height) / (imageSizeY * 1.0));
    }

    public TextureArea getSubArea(double offsetX, double offsetY, double width, double height) {
        return new TextureArea(imageLocation,
                this.offsetX + (imageWidth * offsetX),
                this.offsetY + (imageHeight * offsetY),
                this.imageWidth * width,
                this.imageHeight * height);
    }

    @SideOnly(Side.CLIENT)
    public void drawRotated(int x, int y, Size areaSize, PositionedRect positionedRect, int orientation) {
        Transformation transformation = createOrientation(areaSize, orientation);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0f);
        transformation.glApply();
        draw(positionedRect.position.x, positionedRect.position.y, positionedRect.size.width, positionedRect.size.height);
        GlStateManager.popMatrix();
    }

    public static Transformation createOrientation(Size areaSize, int orientation) {
        Transformation transformation = new Rotation(Math.toRadians(orientation * 90.0), 0.0, 0.0, 1.0)
                .at(new Vector3(areaSize.width / 2.0, areaSize.height / 2.0, 0.0));
        Size orientedSize = transformSize(transformation, areaSize);
        double offsetX = (areaSize.width - orientedSize.width) / 2.0;
        double offsetY = (areaSize.height - orientedSize.height) / 2.0;
        return transformation.with(new Translation(-offsetX, -offsetY, 0.0));
    }

    public static Size transformSize(Transformation transformation, Size position) {
        Vector3 sizeVector = new Vector3(position.width, position.height, 0.0);
        Vector3 zeroVector = new Vector3(0.0, 0.0, 0.0);
        transformation.apply(zeroVector);
        transformation.apply(sizeVector);
        sizeVector.subtract(zeroVector);
        return new Size((int) Math.abs(sizeVector.x), (int) Math.abs(sizeVector.y));
    }

    public static PositionedRect transformRect(Transformation transformation, PositionedRect positionedRect) {
        Position pos1 = transformPos(transformation, positionedRect.position);
        Position pos2 = transformPos(transformation, positionedRect.position.add(positionedRect.size));
        return new PositionedRect(pos1, pos2);
    }

    public static Position transformPos(Transformation transformation, Position position) {
        Vector3 vector = new Vector3(position.x, position.y, 0.0);
        transformation.apply(vector);
        return new Position((int) vector.x, (int) vector.y);
    }

    @SideOnly(Side.CLIENT)
    public void draw(double x, double y, int width, int height) {
        drawSubArea(x, y, width, height, 0.0, 0.0, 1.0, 1.0);
    }

    @SideOnly(Side.CLIENT)
    public void drawSubArea(double x, double y, int width, int height, double drawnU, double drawnV, double drawnWidth, double drawnHeight) {
        //sub area is just different width and height
        double imageU = this.offsetX + (this.imageWidth * drawnU);
        double imageV = this.offsetY + (this.imageHeight * drawnV);
        double imageWidth = this.imageWidth * drawnWidth;
        double imageHeight = this.imageHeight * drawnHeight;
        Minecraft.getMinecraft().renderEngine.bindTexture(imageLocation);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0D).tex(imageU, imageV + imageHeight).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(imageU + imageWidth, imageV + imageHeight).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(imageU + imageWidth, imageV).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(imageU, imageV).endVertex();
        tessellator.draw();
    }
}
