package gregtech.client.renderer.item;

import codechicken.lib.colour.Colour;
import codechicken.lib.model.ItemQuadBakery;
import codechicken.lib.model.bakedmodels.ModelProperties;
import codechicken.lib.model.bakedmodels.PerspectiveAwareBakedModel;
import codechicken.lib.util.TransformUtils;
import com.google.common.collect.ImmutableList;
import morph.avaritia.api.ICosmicRenderItem;
import morph.avaritia.api.IHaloRenderItem;
import morph.avaritia.client.render.item.WrappedItemRenderer;
import morph.avaritia.client.render.shader.CosmicShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.model.IModelState;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class CosmicItemRenderer extends WrappedItemRenderer {
    private static final HashMap<TextureAtlasSprite, IBakedModel> spriteQuadCache = new HashMap();
    private Random randy = new Random();

    public CosmicItemRenderer(IModelState state, IBakedModel model) {
        super(state, model);
    }

    public CosmicItemRenderer(IModelState state, WrappedItemRenderer.IWrappedModelGetter getter) {
        super(state, getter);
    }

    public void renderItem(ItemStack stack, ItemCameraTransforms.TransformType transformType) {
        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();
        this.processLightLevel(transformType);
        if (stack.getItem() instanceof IHaloRenderItem && transformType == ItemCameraTransforms.TransformType.GUI) {
            IHaloRenderItem hri = (IHaloRenderItem)stack.getItem();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            if (hri.shouldDrawHalo(stack)) {
                Colour.glColourARGB(hri.getHaloColour(stack));
                TextureAtlasSprite sprite = hri.getHaloTexture(stack);
                double spread = (double)hri.getHaloSize(stack) / 16.0;
                double min = 0.0 - spread;
                double max = 1.0 + spread;
                float minU = sprite.getMinU();
                float maxU = sprite.getMaxU();
                float minV = sprite.getMinV();
                float maxV = sprite.getMaxV();
                buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                buffer.pos(max, max, 0.0).tex((double)maxU, (double)minV).endVertex();
                buffer.pos(min, max, 0.0).tex((double)minU, (double)minV).endVertex();
                buffer.pos(min, min, 0.0).tex((double)minU, (double)maxV).endVertex();
                buffer.pos(max, min, 0.0).tex((double)maxU, (double)maxV).endVertex();
                tess.draw();
            }

            if (hri.shouldDrawPulse(stack)) {
                GlStateManager.pushMatrix();
                double scale = this.randy.nextDouble() * 0.15 + 0.95;
                double trans = (1.0 - scale) / 2.0;
                GlStateManager.translate(trans, trans, 0.0);
                GlStateManager.scale(scale, scale, 1.0001);
                renderModel(this.wrapped, stack, 0.6F);
                GlStateManager.popMatrix();
            }

            renderModel(this.wrapped, stack);
            GlStateManager.enableAlpha();
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        } else {
            renderModel(this.wrapped, stack);

        }
        if (transformType == ItemCameraTransforms.TransformType.GUI) {
            this.renderInventory(stack, this.renderEntity);
        } else {
            this.renderSimple(stack, this.renderEntity);
        }

    }
    protected void renderSimple(ItemStack stack, EntityLivingBase player) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        World world = player != null ? player.world : null;
        IBakedModel model = this.wrapped.getOverrides().handleItemState(this.wrapped, stack, world, player);
        renderModel(model, stack);
        if (stack.getItem() instanceof ICosmicRenderItem) {
            ICosmicRenderItem cri = (ICosmicRenderItem)stack.getItem();
            GlStateManager.disableAlpha();
            GlStateManager.depthFunc(514);
            TextureAtlasSprite cosmicSprite = cri.getMaskTexture(stack, player);
            IBakedModel cosmicModel = (IBakedModel)spriteQuadCache.computeIfAbsent(cosmicSprite, CosmicItemRenderer::computeModel);
            CosmicShaderHelper.cosmicOpacity = cri.getMaskOpacity(stack, player);
            CosmicShaderHelper.useShader();
            renderModel(cosmicModel, stack);
            CosmicShaderHelper.releaseShader();
            GlStateManager.depthFunc(515);
            GlStateManager.enableAlpha();
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    private static IBakedModel computeModel(TextureAtlasSprite sprite) {
        List<BakedQuad> quads = ItemQuadBakery.bakeItem(ImmutableList.of(sprite));
        return new PerspectiveAwareBakedModel(quads, TransformUtils.DEFAULT_ITEM, new ModelProperties(true, false));
    }

    protected void renderInventory(ItemStack stack, EntityLivingBase player) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        World world = player != null ? player.world : null;
        IBakedModel model = this.wrapped.getOverrides().handleItemState(this.wrapped, stack, world, player);
        renderModel(model, stack);
        if (stack.getItem() instanceof ICosmicRenderItem) {
            ICosmicRenderItem cri = (ICosmicRenderItem)stack.getItem();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.disableDepth();
            TextureAtlasSprite sprite = cri.getMaskTexture(stack, player);
            IBakedModel cosmicModel = (IBakedModel)spriteQuadCache.computeIfAbsent(sprite, CosmicItemRenderer::computeModel);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            CosmicShaderHelper.cosmicOpacity = cri.getMaskOpacity(stack, player);
            CosmicShaderHelper.inventoryRender = true;
            CosmicShaderHelper.useShader();
            renderModel(cosmicModel, stack);
            CosmicShaderHelper.releaseShader();
            CosmicShaderHelper.inventoryRender = false;
            GlStateManager.popMatrix();
        }

        GlStateManager.enableAlpha();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    protected void processLightLevel(ItemCameraTransforms.TransformType transformType) {
        switch (transformType) {
            case GROUND:
                if (this.entityPos != null) {
                    CosmicShaderHelper.setLightFromLocation(this.world, this.entityPos);
                    return;
                }
                break;
            case THIRD_PERSON_LEFT_HAND:
            case THIRD_PERSON_RIGHT_HAND:
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
            case HEAD:
                if (this.renderEntity != null) {
                    CosmicShaderHelper.setLightFromLocation(this.world, this.entityPos);
                    return;
                }
                break;
            case GUI:
                CosmicShaderHelper.setLightLevel(1.2F);
                return;
        }

        CosmicShaderHelper.setLightLevel(1.0F);
    }
}
