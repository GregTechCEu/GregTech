package gregtech.common.render;


import com.google.common.collect.ImmutableMap;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.common.items.behaviors.ClipboardBehaviour;
import gregtech.common.tileentities.EnumShiftPosition;
import gregtech.common.tileentities.EnumVertPosition;
import gregtech.common.tileentities.GTNativeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.common.model.IModelState;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.function.Function;

import static gregtech.common.tileentities.EnumShiftPosition.FULL_SHIFT;
import static gregtech.common.tileentities.EnumShiftPosition.HALF_SHIFT;

public abstract class GTNativeTileEntityRenderer extends TileEntitySpecialRenderer {
    private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");

    Minecraft mc = Minecraft.getMinecraft();

    private EnumFacing angle = EnumFacing.NORTH;

    private EnumVertPosition vert = EnumVertPosition.FLOOR;

    private EnumShiftPosition shift = EnumShiftPosition.NO_SHIFT;

    public float xshift;

    public float zshift;

    public int degreeAngle;

    public double globalX;

    public double globalY;

    public double globalZ;

    public Tessellator tessellator;

    public BufferBuilder worldRenderer;

    private RenderItem itemRenderer;

    private RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

    public void render(TileEntity tileEntity, double x, double y, double z, float tick, int destroyStage, float what) {
        this.globalX = x;
        this.globalY = y;
        this.globalZ = z;
        GTNativeTileEntity tile = (GTNativeTileEntity)tileEntity;
        if (tile != null) {
            this.angle = tile.getAngle();
            this.vert = tile.getVertPosition();
            this.shift = tile.getShiftPosition();
        }

        this.xshift = 0.0F;
        this.zshift = 0.0F;
        if (this.itemRenderer == null || this.tessellator == null || this.worldRenderer == null) {
            this.itemRenderer = Minecraft.getMinecraft().getRenderItem();
            this.tessellator = Tessellator.getInstance();
            this.worldRenderer = this.tessellator.getBuffer();
        }

        float halfShift = 0.25F;
        float fullShift = 0.5F;
        switch(this.angle) {
            case SOUTH:
                this.degreeAngle = 270;
                this.xshift = 1.0F;
                this.zshift = 0.0F;
                if (this.shift == EnumShiftPosition.FULL_SHIFT) {
                    this.xshift += -fullShift;
                    this.zshift += 0.0F;
                }

                if (this.shift == EnumShiftPosition.HALF_SHIFT) {
                    this.xshift += -halfShift;
                    this.zshift += 0.0F;
                }
                break;
            case WEST:
                this.degreeAngle = 180;
                this.xshift = 1.0F;
                this.zshift = 1.0F;
                if (this.shift == EnumShiftPosition.FULL_SHIFT) {
                    this.xshift += 0.0F;
                    this.zshift += -fullShift;
                }

                if (this.shift == EnumShiftPosition.HALF_SHIFT) {
                    this.xshift += 0.0F;
                    this.zshift += -halfShift;
                }
                break;
            case NORTH:
                this.degreeAngle = 90;
                this.xshift = 0.0F;
                this.zshift = 1.0F;
                if (this.shift == EnumShiftPosition.FULL_SHIFT) {
                    this.xshift += fullShift;
                    this.zshift += 0.0F;
                }

                if (this.shift == EnumShiftPosition.HALF_SHIFT) {
                    this.xshift += halfShift;
                    this.zshift += 0.0F;
                }
                break;
            case EAST:
                this.degreeAngle = 0;
                this.xshift = 0.0F;
                this.zshift = 0.0F;
                if (this.shift == EnumShiftPosition.FULL_SHIFT) {
                    this.xshift += 0.0F;
                    this.zshift += fullShift;
                }

                if (this.shift == EnumShiftPosition.HALF_SHIFT) {
                    this.xshift += 0.0F;
                    this.zshift += halfShift;
                }
        }

        render(tile, x, y, z, tick);
    }

    public abstract void render(GTNativeTileEntity paramBiblioTileEntity, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat);

    public EnumFacing getAngle() {
        return this.angle;
    }

    public EnumVertPosition getVertPosition() {
        return this.vert;
    }

    public EnumShiftPosition getShiftPosition() {
        return this.shift;
    }

    public void renderSlotItem(ItemStack stack, double x, double y, double z, float scale) {
        if (stack != null && stack != ItemStack.EMPTY) {
            double tx;
            switch(this.angle) {
                case SOUTH:
                    tx = x;
                    x = -z;
                    z = tx;
                    break;
                case WEST:
                    x *= -1.0D;
                    z *= -1.0D;
                    break;
                case NORTH:
                    tx = x;
                    x = z;
                    z = -tx;
                case EAST:
            }

            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            GlStateManager.translate(this.globalX + x + (double)this.xshift, this.globalY + y + 0.05D, this.globalZ + z + (double)this.zshift);
            GlStateManager.rotate((float)this.degreeAngle + 180.0F, 0.0F, 1.0F, 0.0F);
            this.additionalGLStuffForItemStack();
            Block testBlock = Block.getBlockFromItem(stack.getItem());

            if (stack.getItem() instanceof MetaItem &&
                    ((MetaItem)stack.getItem()).getItem((short) stack.getItemDamage()).getBehaviours()
                            .stream()
                            .filter(behaviour -> behaviour instanceof ClipboardBehaviour)
                            .count() == 1 ) {
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.4D, 0.05D, 0.0D);
            }

            if (testBlock == null) {
                scale *= 0.7F;
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            }

            GlStateManager.scale(scale, scale, scale);
            this.itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            GlStateManager.popMatrix();
        }
    }

    public void additionalGLStuffForItemStack() {}

    public void renderItemMap(ItemStack stack, float x, float y, float z, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.globalX + x, this.globalY + y + 1.0199999809265137D, this.globalZ + z);
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        scale = (float)(scale * 0.0063D);
        GlStateManager.scale(scale, scale, scale);
        this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        MapData mapdata = Items.FILLED_MAP.getMapData(stack, getWorld());
        if (mapdata != null)
            this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
        GlStateManager.popMatrix();
    }

    public IBakedModel initModel(List<String> parts, ResourceLocation modelResource) {
        IModel model = null;
        try {
            model = ModelLoaderRegistry.getModel(modelResource);
            model = model.process(ImmutableMap.of("flip-v", "true"));
        } catch (Exception e) {
            model = ModelLoaderRegistry.getMissingModel();
        }
        OBJModel.OBJState state = new OBJModel.OBJState(parts, true);
        return model.bake(state, Attributes.DEFAULT_BAKED_FORMAT, this.getModelTexture);
    }

    protected Function<ResourceLocation, TextureAtlasSprite> getModelTexture = new Function<ResourceLocation, TextureAtlasSprite>() {
        public TextureAtlasSprite apply(ResourceLocation location) {
            return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(GTNativeTileEntityRenderer.this.getTextureString(location));
        }
    };

    public String getTextureString(ResourceLocation location) {
        return location.toString();
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

    public void additionalGLStuffForText() {}
}
