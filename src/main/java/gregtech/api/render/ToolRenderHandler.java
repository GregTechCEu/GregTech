package gregtech.api.render;

import codechicken.lib.raytracer.RayTracer;
import gregtech.api.items.toolitem.IAOEItem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ToolRenderHandler implements IResourceManagerReloadListener {

    public static final ToolRenderHandler INSTANCE = new ToolRenderHandler();

    private ToolRenderHandler() {
    }

    private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        //render block damage progress for all breakers
        for (int breakerEntityId : event.getContext().damagedBlocks.keySet()) {
            Entity breakerEntity = mc.world.getEntityByID(breakerEntityId);
            if (!(breakerEntity instanceof EntityPlayer)) {
                continue;
            }
            EntityPlayer entityPlayer = (EntityPlayer) breakerEntity;
            ItemStack itemStack = entityPlayer.getHeldItem(EnumHand.MAIN_HAND);
            if (!(itemStack.getItem() instanceof IAOEItem)) {
                continue;
            }
            DestroyBlockProgress progress = event.getContext().damagedBlocks.get(breakerEntityId);
            RayTraceResult rayTraceResult = RayTracer.retraceBlock(mc.world, entityPlayer, progress.getPosition());
            if (rayTraceResult == null || rayTraceResult.typeOfHit != Type.BLOCK) {
                continue;
            }
            List<BlockPos> aoeBlocksToRender = ((IAOEItem) itemStack.getItem()).getAOEBlocks(itemStack, entityPlayer, rayTraceResult);
            int breakProgress = progress.getPartialBlockDamage();
            preRenderDamagedBlocks();
            drawBlockDamageTexture(mc, Tessellator.getInstance(), mc.getRenderViewEntity(), event.getPartialTicks(), aoeBlocksToRender, breakProgress);
            postRenderDamagedBlocks();
        }

        //render block selection only for current player
        PlayerControllerMP playerController = mc.playerController;
        if (!playerController.getIsHittingBlock()) {
            ItemStack itemStack = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if (itemStack.getItem() instanceof IAOEItem) {
                RayTraceResult rayTraceResult = RayTracer.retrace(mc.player);
                if (rayTraceResult != null && rayTraceResult.typeOfHit == Type.BLOCK) {
                    List<BlockPos> aoeBlocksToRender = ((IAOEItem) itemStack.getItem()).getAOEBlocks(itemStack, mc.player, rayTraceResult);
                    preRenderSelectionOutline();
                    drawSelectionOutlines(mc, aoeBlocksToRender, mc.getRenderViewEntity(), event.getPartialTicks());
                    postRenderSelectionOutline();
                }
            }
        }
    }

    private void preRenderSelectionOutline() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
    }

    private void postRenderSelectionOutline() {
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    //functions below are copied from RenderGlobal
    private void preRenderDamagedBlocks() {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
    }

    private void postRenderDamagedBlocks() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    public void drawSelectionOutlines(Minecraft mc, List<BlockPos> blocksToRender, Entity entityIn, float partialTicks) {
        double d3 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
        double d4 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
        double d5 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;

        for (BlockPos blockPos : blocksToRender) {
            IBlockState blockState = mc.world.getBlockState(blockPos);
            AxisAlignedBB box = blockState.getSelectedBoundingBox(mc.world, blockPos).grow(0.002D).offset(-d3, -d4, -d5);
            RenderGlobal.drawSelectionBoundingBox(box, 0.0F, 0.0F, 0.0F, 0.4F);
        }
    }

    public void drawBlockDamageTexture(Minecraft mc, Tessellator tessellator, Entity viewEntity, float partialTicks, List<BlockPos> blocksToRender, int partialBlockDamage) {
        double d3 = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double d4 = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double d5 = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        BlockRendererDispatcher rendererDispatcher = mc.getBlockRendererDispatcher();

        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        preRenderDamagedBlocks();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        bufferBuilder.setTranslation(-d3, -d4, -d5);
        bufferBuilder.noColor();

        for (BlockPos blockPos : blocksToRender) {
            IBlockState blockState = mc.world.getBlockState(blockPos);
            TileEntity tileEntity = mc.world.getTileEntity(blockPos);
            boolean hasBreak = tileEntity != null && tileEntity.canRenderBreaking();
            if (!hasBreak && blockState.getMaterial() != Material.AIR) {
                TextureAtlasSprite textureAtlasSprite = this.destroyBlockIcons[partialBlockDamage];
                rendererDispatcher.renderBlockDamage(blockState, blockPos, textureAtlasSprite, mc.world);
            }
        }

        tessellator.draw();
        bufferBuilder.setTranslation(0.0D, 0.0D, 0.0D);
        postRenderDamagedBlocks();
    }

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {
        this.updateDestroyBlockIcons();
    }

    private void updateDestroyBlockIcons() {
        Minecraft mc = Minecraft.getMinecraft();
        TextureMap texturemap = mc.getTextureMapBlocks();

        for (int i = 0; i < destroyBlockIcons.length; ++i) {
            this.destroyBlockIcons[i] = texturemap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
        }
    }
}
