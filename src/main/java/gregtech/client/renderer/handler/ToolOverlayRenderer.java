package gregtech.client.renderer.handler;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.vec.Vector3;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.ICoverable;
import gregtech.api.cover.ICoverable.PrimaryBoxData;
import gregtech.api.items.toolitem.IAOEItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
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
import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class ToolOverlayRenderer {

    private static float rColor;
    private static float gColor;
    private static float bColor;

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

        if (tileEntity instanceof IGregTechTileEntity) {
            if (((IGregTechTileEntity) tileEntity).getMetaTileEntity() instanceof MetaTileEntityMonitorScreen) {
                event.setCanceled(true);
                return;
            }
        }

        if (tileEntity != null && (shouldDrawOverlayForItem(blockState, tileEntity, heldItem) || shouldDrawOverlayForItem(blockState, tileEntity, player.getHeldItem(EnumHand.OFF_HAND)))) {
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
                RenderGlobal.drawSelectionBoundingBox(box, 1, 1, 1, 0.4F);
                rColor = gColor = bColor = 0.2f + (float) Math.sin((float) (System.currentTimeMillis() % (Math.PI * 800)) / 800) / 2;

                if (tileEntity instanceof TileEntityPipeBase)
                    drawOverlayLines(facing, box, facing1 -> ((TileEntityPipeBase<?, ?>) tileEntity).isConnected(facing1) ||
                            ((TileEntityPipeBase<?, ?>) tileEntity).getCoverableImplementation().getCoverAtSide(facing1) != null);
                else if (tileEntity instanceof IGregTechTileEntity)
                    drawOverlayLines(facing, box, face -> ((IGregTechTileEntity) tileEntity).getMetaTileEntity().isSideUsed(face));
                else
                    drawOverlayLines(facing, box, ignored -> false);
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

    public static boolean shouldDrawOverlayForItem(IBlockState state, TileEntity tileEntity, ItemStack itemStack) {
        // MetaTileEntity
        if (tileEntity instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
            if (mte == null || !mte.canRenderMachineGrid())
                return false;
            if (itemStack.hasCapability(GregtechCapabilities.CAPABILITY_WRENCH, null))
                return true;
        }

        // Pipes
        if (state.getBlock() instanceof BlockPipe) {
            return ((BlockPipe<?, ?, ?>) state.getBlock()).hasPipeCollisionChangingItem(tileEntity.getWorld(), tileEntity.getPos(), itemStack);
        }

        // ICoverable
        ICoverable coverable;
        if ((coverable = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null)) != null) {
            return itemStack.hasCapability(GregtechCapabilities.CAPABILITY_SCREWDRIVER, null) ||
                    GTUtility.isCoverBehaviorItem(itemStack, coverable::hasAnyCover, cover -> ICoverable.canPlaceCover(cover, coverable));
        }

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

    private static void drawOverlayLines(EnumFacing facing, AxisAlignedBB box, Function<EnumFacing, Boolean> test) {
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

        boolean leftBlocked;
        boolean topBlocked;
        boolean rightBlocked;
        boolean bottomBlocked;
        boolean frontBlocked = test.apply(facing);
        boolean backBlocked = test.apply(facing.getOpposite());

        switch (facing) {
            case WEST: {
                topRight.rotate(Math.PI / 2, Vector3.down);
                bottomRight.rotate(Math.PI / 2, Vector3.down);
                bottomLeft.rotate(Math.PI / 2, Vector3.down);
                topLeft.rotate(Math.PI / 2, Vector3.down);
                shift.rotate(Math.PI / 2, Vector3.down);
                shiftVert.rotate(Math.PI / 2, Vector3.down);

                leftBlocked = test.apply(EnumFacing.NORTH);
                topBlocked = test.apply(EnumFacing.UP);
                rightBlocked = test.apply(EnumFacing.SOUTH);
                bottomBlocked = test.apply(EnumFacing.DOWN);
                break;
            }
            case EAST: {
                topRight.rotate(-Math.PI / 2, Vector3.down);
                bottomRight.rotate(-Math.PI / 2, Vector3.down);
                bottomLeft.rotate(-Math.PI / 2, Vector3.down);
                topLeft.rotate(-Math.PI / 2, Vector3.down);
                shift.rotate(-Math.PI / 2, Vector3.down);
                shiftVert.rotate(-Math.PI / 2, Vector3.down);

                leftBlocked = test.apply(EnumFacing.SOUTH);
                topBlocked = test.apply(EnumFacing.UP);
                rightBlocked = test.apply(EnumFacing.NORTH);
                bottomBlocked = test.apply(EnumFacing.DOWN);
                break;
            }
            case NORTH: {
                topRight.rotate(Math.PI, Vector3.down);
                bottomRight.rotate(Math.PI, Vector3.down);
                bottomLeft.rotate(Math.PI, Vector3.down);
                topLeft.rotate(Math.PI, Vector3.down);
                shift.rotate(Math.PI, Vector3.down);
                shiftVert.rotate(Math.PI, Vector3.down);

                leftBlocked = test.apply(EnumFacing.EAST);
                topBlocked = test.apply(EnumFacing.UP);
                rightBlocked = test.apply(EnumFacing.WEST);
                bottomBlocked = test.apply(EnumFacing.DOWN);
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

                leftBlocked = test.apply(EnumFacing.WEST);
                topBlocked = test.apply(EnumFacing.NORTH);
                rightBlocked = test.apply(EnumFacing.EAST);
                bottomBlocked = test.apply(EnumFacing.SOUTH);
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

                leftBlocked = test.apply(EnumFacing.WEST);
                topBlocked = test.apply(EnumFacing.SOUTH);
                rightBlocked = test.apply(EnumFacing.EAST);
                bottomBlocked = test.apply(EnumFacing.NORTH);
                break;
            }
            default: {
                leftBlocked = test.apply(EnumFacing.WEST);
                topBlocked = test.apply(EnumFacing.UP);
                rightBlocked = test.apply(EnumFacing.EAST);
                bottomBlocked = test.apply(EnumFacing.DOWN);
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

        if (leftBlocked) {
            startLine(buffer, topLeft.copy().add(shiftVert.copy().negate()));
            endLine(buffer, bottomLeft.copy().add(shiftVert.copy()).add(shift));

            startLine(buffer, topLeft.copy().add(shiftVert.copy().negate()).add(shift));
            endLine(buffer, bottomLeft.copy().add(shiftVert));
        }
        if (topBlocked) {
            startLine(buffer, topLeft.copy().add(shift));
            endLine(buffer, topRight.copy().add(shift.copy().negate()).add(shiftVert.copy().negate()));

            startLine(buffer, topLeft.copy().add(shift).add(shiftVert.copy().negate()));
            endLine(buffer, topRight.copy().add(shift.copy().negate()));
        }
        if (rightBlocked) {
            startLine(buffer, topRight.copy().add(shiftVert.copy().negate()));
            endLine(buffer, bottomRight.copy().add(shiftVert.copy()).add(shift.copy().negate()));

            startLine(buffer, topRight.copy().add(shiftVert.copy().negate()).add(shift.copy().negate()));
            endLine(buffer, bottomRight.copy().add(shiftVert));
        }
        if (bottomBlocked) {
            startLine(buffer, bottomLeft.copy().add(shift));
            endLine(buffer, bottomRight.copy().add(shift.copy().negate()).add(shiftVert));

            startLine(buffer, bottomLeft.copy().add(shift).add(shiftVert));
            endLine(buffer, bottomRight.copy().add(shift.copy().negate()));
        }
        if (frontBlocked) {
            startLine(buffer, topLeft.copy().add(shift).add(shiftVert.copy().negate()));
            endLine(buffer, bottomRight.copy().add(shift.copy().negate()).add(shiftVert));

            startLine(buffer, topRight.copy().add(shift.copy().negate()).add(shiftVert.copy().negate()));
            endLine(buffer, bottomLeft.copy().add(shift).add(shiftVert));
        }
        if (backBlocked) {
            Vector3 localXShift = new Vector3(0, 0, 0); // Set up translations for the current X.
            for (int i = 0; i < 2; i++) {
                Vector3 localXShiftVert = new Vector3(0, 0, 0);
                for (int j = 0; j < 2; j++) {
                    startLine(buffer, topLeft.copy().add(localXShift).add(localXShiftVert));
                    endLine(buffer, topLeft.copy().add(localXShift).add(localXShiftVert).add(shift).subtract(shiftVert));

                    startLine(buffer, topLeft.copy().add(localXShift).add(localXShiftVert).add(shift));
                    endLine(buffer, topLeft.copy().add(localXShift).add(localXShiftVert).subtract(shiftVert));

                    localXShiftVert.add(bottomLeft.copy().subtract(topLeft).add(shiftVert)); // Move by the vector from the top to the bottom, minus the shift from the edge.
                }
                localXShift.add(topRight.copy().subtract(topLeft).subtract(shift)); // Move by the vector from the left to the right, minus the shift from the edge.
            }
        }

        tessellator.draw();
    }

    private static void startLine(BufferBuilder buffer, Vector3 vec) {
        buffer.pos(vec.x, vec.y, vec.z).color(rColor, gColor, bColor, 0.0F).endVertex();
    }

    private static void endLine(BufferBuilder buffer, Vector3 vec) {
        buffer.pos(vec.x, vec.y, vec.z).color(rColor, gColor, bColor, 1F).endVertex();
    }

}
