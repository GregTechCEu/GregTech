package gregtech.client.renderer.handler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.vec.Vector3;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.ICoverable.PrimaryBoxData;
import gregtech.api.items.toolitem.IAOEItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ToolOverlayRenderer {

    public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        EntityPlayer player = event.getPlayer();
        World world = player.world;
        RayTraceResult target = event.getTarget();

        // Draw damaged blocks
        ItemStack stack = player.getHeldItemMainhand();
        List<BlockPos> blocksToRender;
        if (stack.getItem() instanceof IAOEItem) {
            blocksToRender = ((IAOEItem) stack.getItem()).getAOEBlocks(stack, player, event.getTarget());
            for (BlockPos pos : blocksToRender) {
                // Facing here doesn't matter, but it likes to complain anyway :P
                //noinspection ConstantConditions
                event.getContext().drawSelectionBox(player, new RayTraceResult(Vec3d.ZERO, null, pos), 0, event.getPartialTicks());
            }

            DestroyBlockProgress progress = event.getContext().damagedBlocks.get(player.getEntityId());
            if (progress != null) {
                preRenderDamagedBlocks();
                drawBlockDamageTexture(event, blocksToRender, progress.getPartialBlockDamage());
                postRenderDamagedBlocks();
            }
        }

        // Draw Block Highlight
        if (target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return; //magically, draw block highlight is called not only for blocks (see forge issues)
        }

        BlockPos pos = target.getBlockPos();
        IBlockState blockState = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);

        if (tileEntity instanceof MetaTileEntityHolder) {
            if (((MetaTileEntityHolder) tileEntity).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                event.setCanceled(true);
                return;
            }
        }

        if (tileEntity != null && shouldDrawOverlayForItem(heldItem, tileEntity)) {
            EnumFacing facing = target.sideHit;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);

            if (world.getWorldBorder().contains(pos)) {
                double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
                double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
                double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();
                AxisAlignedBB box = blockState.getSelectedBoundingBox(world, pos).grow(0.002D).offset(-d3, -d4, -d5);
                RenderGlobal.drawSelectionBoundingBox(box, 0.0F, 0.0F, 0.0F, 0.4F);
                drawOverlayLines(facing, box);
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();

            event.setCanceled(true);
        }
    }

    public static boolean useGridForRayTraceResult(RayTraceResult result) {
        if (result instanceof CuboidRayTraceResult) {
            CuboidRayTraceResult traceResult = (CuboidRayTraceResult) result;
            //use grid only for center hit or for primary box with placement grid enabled
            if (traceResult.cuboid6.data == null) {
                return true; //default is true
            } else if (traceResult.cuboid6.data instanceof PrimaryBoxData) {
                PrimaryBoxData primaryBoxData = (PrimaryBoxData) traceResult.cuboid6.data;
                return primaryBoxData.usePlacementGrid;
            } else return false; //otherwise, do not use grid
        }
        //also use it for default collision blocks
        return true;
    }

    public static boolean shouldDrawOverlayForItem(ItemStack itemStack, TileEntity tileEntity) {
        if (tileEntity instanceof MetaTileEntityHolder) {
            MetaTileEntity mte = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
            if (mte == null || !mte.canRenderMachineGrid()) return false;
        }

        if (tileEntity instanceof TileEntityPipeBase) {
            TileEntityPipeBase<?, ?> pipeTE = (TileEntityPipeBase<?, ?>) tileEntity;
            Class<?> pipeClass = pipeTE.getPipeBlock().getPipeTypeClass();

            // Cables/wires. Add screwdriver here if cover for wires that can use screwdriver is added.
            if (pipeClass == Insulation.class) {
                return itemStack.hasCapability(GregtechCapabilities.CAPABILITY_CUTTER, null) || GTUtility.isCoverBehaviorItem(itemStack);
            }

            // Pipes
            if (pipeClass == FluidPipeType.class || pipeClass == ItemPipeType.class) {
                return itemStack.hasCapability(GregtechCapabilities.CAPABILITY_WRENCH, null) ||
                        itemStack.hasCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null)
                        || GTUtility.isCoverBehaviorItem(itemStack);
            }
        }

        // MetaTileEntities
        if (tileEntity instanceof MetaTileEntityHolder &&
                itemStack.hasCapability(GregtechCapabilities.CAPABILITY_WRENCH, null))
            return true;

        // ICoverable
        if (tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null))
            return itemStack.hasCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null) || GTUtility.isCoverBehaviorItem(itemStack);

        return false;
    }

    // Copied from RenderGlobal
    private static void preRenderDamagedBlocks() {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
    }

    // Copied from RenderGlobal
    private static void postRenderDamagedBlocks() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    // Copied from RenderGlobal (mostly)
    private static void drawBlockDamageTexture(DrawBlockHighlightEvent event, List<BlockPos> blocksToRender, int partialBlockDamage) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = event.getPlayer();
        float partialTicks = event.getPartialTicks();
        double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
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
            if (!hasBreak) {
                TextureAtlasSprite sprite = event.getContext().destroyBlockIcons[partialBlockDamage];
                rendererDispatcher.renderBlockDamage(blockState, blockPos, sprite, mc.world);
            }
        }

        Tessellator.getInstance().draw();
        bufferBuilder.setTranslation(0.0D, 0.0D, 0.0D);
        postRenderDamagedBlocks();
    }

    private static void drawOverlayLines(EnumFacing facing, AxisAlignedBB box) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

        Vector3 topRight = new Vector3(box.maxX, box.maxY, box.maxZ);
        Vector3 bottomRight = new Vector3(box.maxX, box.minY, box.maxZ);
        Vector3 bottomLeft = new Vector3(box.minX, box.minY, box.maxZ);
        Vector3 topLeft = new Vector3(box.minX, box.maxY, box.maxZ);
        Vector3 shift = new Vector3(0.25, 0, 0);
        Vector3 shiftVert = new Vector3(0, 0.25, 0);

        Vector3 cubeCenter = new Vector3(box.getCenter());


        topRight.subtract(cubeCenter);
        bottomRight.subtract(cubeCenter);
        bottomLeft.subtract(cubeCenter);
        topLeft.subtract(cubeCenter);

        switch (facing) {
            case WEST: {
                topRight.rotate(Math.PI / 2, Vector3.down);
                bottomRight.rotate(Math.PI / 2, Vector3.down);
                bottomLeft.rotate(Math.PI / 2, Vector3.down);
                topLeft.rotate(Math.PI / 2, Vector3.down);
                shift.rotate(Math.PI / 2, Vector3.down);
                shiftVert.rotate(Math.PI / 2, Vector3.down);
                break;
            }
            case EAST: {
                topRight.rotate(-Math.PI / 2, Vector3.down);
                bottomRight.rotate(-Math.PI / 2, Vector3.down);
                bottomLeft.rotate(-Math.PI / 2, Vector3.down);
                topLeft.rotate(-Math.PI / 2, Vector3.down);
                shift.rotate(-Math.PI / 2, Vector3.down);
                shiftVert.rotate(-Math.PI / 2, Vector3.down);
                break;
            }
            case NORTH: {
                topRight.rotate(Math.PI, Vector3.down);
                bottomRight.rotate(Math.PI, Vector3.down);
                bottomLeft.rotate(Math.PI, Vector3.down);
                topLeft.rotate(Math.PI, Vector3.down);
                shift.rotate(Math.PI, Vector3.down);
                shiftVert.rotate(Math.PI, Vector3.down);
                break;
            }
            case UP: {
                Vector3 side = new Vector3(1, 0, 0);
                topRight.rotate(-Math.PI / 2, side);
                bottomRight.rotate(-Math.PI / 2, side);
                bottomLeft.rotate(-Math.PI / 2, side);
                topLeft.rotate(-Math.PI / 2, side);
                shift.rotate(-Math.PI / 2, side);
                shiftVert.rotate(-Math.PI / 2, side);
                break;
            }
            case DOWN: {
                Vector3 side = new Vector3(1, 0, 0);
                topRight.rotate(Math.PI / 2, side);
                bottomRight.rotate(Math.PI / 2, side);
                bottomLeft.rotate(Math.PI / 2, side);
                topLeft.rotate(Math.PI / 2, side);
                shift.rotate(Math.PI / 2, side);
                shiftVert.rotate(Math.PI / 2, side);
                break;
            }
        }

        topRight.add(cubeCenter);
        bottomRight.add(cubeCenter);
        bottomLeft.add(cubeCenter);
        topLeft.add(cubeCenter);

        // straight top bottom lines
        startLine(buffer, topRight.copy().add(shift.copy().negate()));
        endLine(buffer, bottomRight.copy().add(shift.copy().negate()));

        startLine(buffer, bottomLeft.copy().add(shift));
        endLine(buffer, topLeft.copy().add(shift));

        // straight side to side lines
        startLine(buffer, topLeft.copy().add(shiftVert.copy().negate()));
        endLine(buffer, topRight.copy().add(shiftVert.copy().negate()));

        startLine(buffer, bottomLeft.copy().add(shiftVert));
        endLine(buffer, bottomRight.copy().add(shiftVert));

        tessellator.draw();
    }

    private static void startLine(BufferBuilder buffer, Vector3 vec) {
        buffer.pos(vec.x, vec.y, vec.z).color(0, 0, 0, 0.0F).endVertex();
    }

    private static void endLine(BufferBuilder buffer, Vector3 vec) {
        buffer.pos(vec.x, vec.y, vec.z).color(0, 0, 0, 0.5F).endVertex();
    }

}
