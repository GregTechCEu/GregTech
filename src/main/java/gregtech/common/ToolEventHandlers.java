package gregtech.common;

import codechicken.lib.vec.Vector3;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.cover2.CoverDefinition2;
import gregtech.api.cover2.CoverHolder;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TaskScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = GTValues.MODID)
public class ToolEventHandlers {

    /**
     * Handles returning broken stacks for tools
     */
    @SubscribeEvent
    public static void onPlayerDestroyItem(@Nonnull PlayerDestroyItemEvent event) {
        ItemStack original = event.getOriginal();
        Item item = original.getItem();
        if (item instanceof IGTTool) {
            IGTTool def = (IGTTool) item;
            ItemStack brokenStack = def.getToolStats().getBrokenStack();
            // Transfer over remaining charge to power units
            if (brokenStack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null) && def.isElectric()) {
                long remainingCharge = def.getCharge(event.getOriginal());
                IElectricItem electricStack = brokenStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                if (electricStack != null) {
                    // update the max charge of the item, if possible
                    // applies to items like power units, which can have different max charges depending on their recipe
                    if (electricStack instanceof ElectricItem) {
                        ((ElectricItem) electricStack).setMaxChargeOverride(def.getMaxCharge(original));
                    }

                    electricStack.charge(Math.min(remainingCharge, def.getMaxCharge(original)),
                            def.getElectricTier(), true, false);
                }
            }
            if (!brokenStack.isEmpty()) {
                if (event.getHand() == null) {
                    if (!event.getEntityPlayer().addItemStackToInventory(brokenStack)) {
                        event.getEntityPlayer().dropItem(brokenStack, true);
                    }
                } else {
                    event.getEntityPlayer().setHeldItem(event.getHand(), brokenStack);
                }
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerEntityInteract(@Nonnull PlayerInteractEvent.EntityInteract event) {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();

        /*
        Handle item frame power unit duping
         */
        if (item instanceof IGTTool) {
            Entity entity = event.getTarget();
            if (entity instanceof EntityItemFrame) {
                IGTTool def = (IGTTool) item;
                ItemStack brokenStack = def.getToolStats().getBrokenStack();
                if (!brokenStack.isEmpty()) {
                    EntityItemFrame itemFrame = (EntityItemFrame) entity;
                    itemFrame.processInitialInteract(event.getEntityPlayer(), event.getHand());

                    event.setCanceled(true);
                    event.setCancellationResult(EnumActionResult.SUCCESS);
                }
            }
        }
    }

    /**
     * Handles saws harvesting ice without leaving water behind
     * Handles mined blocks teleporting straight into inventory
     * Handles drop conversion when a hammer tool (or tool with hard hammer enchantment) is used
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestDrops(@Nonnull BlockEvent.HarvestDropsEvent event) {
        EntityPlayer player = event.getHarvester();
        if (player != null) {
            ItemStack stack = player.getHeldItemMainhand();
            if (stack.isEmpty() || !stack.hasTagCompound() || !(stack.getItem() instanceof IGTTool)) {
                return;
            }
            if (!event.isSilkTouching()) {
                ToolHelper.applyHammerDropConversion(stack, event.getState(), event.getDrops(), event.getFortuneLevel(), event.getDropChance(), player.getRNG());
            }
            NBTTagCompound behaviorTag = ToolHelper.getBehaviorsTag(stack);
            Block block = event.getState().getBlock();
            if (!event.isSilkTouching() && (block == Blocks.ICE || block == Blocks.PACKED_ICE) && behaviorTag.getBoolean(ToolHelper.HARVEST_ICE_KEY)) {
                Item iceBlock = Item.getItemFromBlock(block);
                if (event.getDrops().stream().noneMatch(drop -> drop.getItem() == iceBlock)) {
                    event.getDrops().add(new ItemStack(iceBlock));
                    final World world = event.getWorld();
                    final BlockPos icePos = event.getPos();
                    TaskScheduler.scheduleTask(world, () -> {
                        IBlockState flowingState = world.getBlockState(icePos);
                        if (flowingState == Blocks.FLOWING_WATER.getDefaultState()) {
                            world.setBlockToAir(icePos);
                        }
                        // only try once, so future water placement does not get eaten too
                        return false;
                    });
                    ((IGTTool) stack.getItem()).playSound(player);
                }
            }
            if (behaviorTag.getBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY)) {

                Iterator<ItemStack> dropItr = event.getDrops().iterator();
                while (dropItr.hasNext()) {
                    ItemStack dropStack = dropItr.next();
                    EntityItem drop = new EntityItem(event.getWorld());
                    drop.setItem(dropStack);

                    if (ForgeEventFactory.onItemPickup(drop, player) == -1 || player.addItemStackToInventory(dropStack)) {
                        dropItr.remove();
                    }
                }
            }
        }
    }

    /**
     * Prevents anvil repairing if tools do not have the same material, or if either are electric.
     * Electric tools can still be repaired with ingots in the anvil, but electric tools cannot
     * be combined with other GT tools, electric or otherwise.
     */
    @SubscribeEvent
    public static void onAnvilUpdateEvent(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft(), right = event.getRight();
        if (left.getItem() instanceof IGTTool leftTool && right.getItem() instanceof IGTTool rightTool) {
            if (leftTool.getToolMaterial(left) != rightTool.getToolMaterial(right)) {
                event.setCanceled(true);
            }
            if (leftTool.isElectric() || rightTool.isElectric()) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Handle client-view of harvestable blocks in AoE (and potentially wrench overlay in the future)
     * Handle machine grid rendering as well
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onDrawHighlightEvent(@Nonnull DrawBlockHighlightEvent event) {
        //noinspection ConstantConditions
        if (event.getTarget().getBlockPos() == null) return;

        EntityPlayer player = event.getPlayer();
        ItemStack stack = player.getHeldItemMainhand();
        BlockPos pos = event.getTarget().getBlockPos();
        IBlockState state = player.world.getBlockState(pos);
        TileEntity tile = player.world.getTileEntity(event.getTarget().getBlockPos());
        boolean sneaking = player.isSneaking();

        // Grid overlays
        if (tile != null && shouldRenderGridOverlays(state, tile, stack, player.getHeldItemOffhand(), sneaking) &&
                renderGridOverlays(player, pos, state, event.getTarget().sideHit, tile, event.getPartialTicks())) {
            event.setCanceled(true);
            return;
        }

        // AoE selection box and block damage overlay
        if (!sneaking && stack.getItem() instanceof IGTTool) {
            Set<BlockPos> validPositions = ToolHelper.getHarvestableBlocks(stack, player.world, player, event.getTarget());
            if (validPositions.isEmpty()) return;

            float partialTicks = event.getPartialTicks();
            for (BlockPos validPosition : validPositions) {
                event.getContext().drawSelectionBox(player, new RayTraceResult(Vec3d.ZERO, player.getHorizontalFacing(), validPosition), 0, partialTicks);
            }

            DestroyBlockProgress progress = event.getContext().damagedBlocks.get(player.getEntityId());
            if (progress != null) {
                int damage = progress.getPartialBlockDamage();
                if (damage > -1) {
                    double relX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
                    double relY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
                    double relZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
                    BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                    Minecraft mc = Minecraft.getMinecraft();
                    BlockRendererDispatcher rendererDispatcher = mc.blockRenderDispatcher;
                    preRenderDamagedBlocks();
                    bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                    bufferBuilder.setTranslation(-relX, -relY, -relZ);
                    bufferBuilder.noColor(); // ?
                    for (BlockPos validPosition : validPositions) {
                        TileEntity tileEntity = mc.world.getTileEntity(validPosition);
                        if (tileEntity == null || tileEntity.canRenderBreaking()) {
                            TextureAtlasSprite sprite = event.getContext().destroyBlockIcons[damage];
                            rendererDispatcher.renderBlockDamage(mc.world.getBlockState(validPosition), validPosition, sprite, mc.world);
                        }
                    }
                    Tessellator.getInstance().draw();
                    bufferBuilder.setTranslation(0.0D, 0.0D, 0.0D);
                    postRenderDamagedBlocks();
                }
            }
        }
    }

    /**
     * Sets up for rendering blocks with break progress
     */
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

    /**
     * Finishes rendering for blocks with break progress
     */
    @SideOnly(Side.CLIENT)
    private static void postRenderDamagedBlocks() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    private static boolean shouldRenderGridOverlays(@Nonnull IBlockState state, TileEntity tile, ItemStack mainHand, ItemStack offHand, boolean isSneaking) {
        if (state.getBlock() instanceof BlockPipe) {
            BlockPipe<?, ?, ?> pipe = (BlockPipe<?, ?, ?>) state.getBlock();
            if (isSneaking && mainHand.getItem().getClass() == Item.getItemFromBlock(pipe).getClass()) {
                return true;
            } else {
                Set<String> mainToolClasses = mainHand.getItem().getToolClasses(mainHand);
                Set<String> offToolClasses = offHand.getItem().getToolClasses(offHand);
                if (mainToolClasses.contains(ToolClasses.SCREWDRIVER) || offToolClasses.contains(ToolClasses.SCREWDRIVER)) return true;
                if (mainToolClasses.stream().anyMatch(s -> pipe.isToolEffective(s, state)) ||
                        offToolClasses.stream().anyMatch(s -> pipe.isToolEffective(s, state))) return true;

                BooleanSupplier hasCover = () -> tile instanceof IPipeTile && ((IPipeTile<?, ?>) tile).getCoverableImplementation().hasAnyCover();
                Predicate<CoverDefinition2> canCover = coverDef -> tile instanceof IPipeTile && ((IPipeTile<?, ?>) tile).getCoverableImplementation().canPlaceCoverOnSide(EnumFacing.DOWN); //TODO find dir
                if (GTUtility.isCoverBehaviorItem(mainHand, hasCover, canCover) || GTUtility.isCoverBehaviorItem(offHand, hasCover, canCover)) {
                    return true;
                }
            }
        }

        if (tile instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) tile).getMetaTileEntity();
            if (mte != null && mte.canRenderMachineGrid(mainHand, offHand)) {
                return true;
            }
        }
        CoverHolder coverHolder = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVER_HOLDER, null);
        return coverHolder != null && GTUtility.isCoverBehaviorItem(mainHand, coverHolder::hasAnyCover, coverDef -> coverHolder.canPlaceCoverOnSide(EnumFacing.DOWN)); //TODO find dir
    }

    private static float rColour;
    private static float gColour;
    private static float bColour;

    @SideOnly(Side.CLIENT)
    private static boolean renderGridOverlays(@Nonnull EntityPlayer player, BlockPos pos, IBlockState state, EnumFacing facing, TileEntity tile, float partialTicks) {
        if (player.world.getWorldBorder().contains(pos)) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
            double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
            double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
            AxisAlignedBB box = state.getSelectedBoundingBox(player.world, pos).grow(0.002D).offset(-d3, -d4, -d5);
            RenderGlobal.drawSelectionBoundingBox(box, 1, 1, 1, 0.4F);

            rColour = gColour = bColour = 0.2F + (float) Math.sin((float) (System.currentTimeMillis() % (Math.PI * 800)) / 800) / 2;

            if (tile instanceof TileEntityPipeBase) {
                TileEntityPipeBase<?, ?> tepb = (TileEntityPipeBase<?, ?>) tile;
                drawGridOverlays(facing, box, face -> tepb.isConnected(face) || tepb.getCoverableImplementation().getCoverAtSide(face) != null);
            } else if (tile instanceof MetaTileEntityHolder) {
                MetaTileEntity mte = ((MetaTileEntityHolder) tile).getMetaTileEntity();
                drawGridOverlays(facing, box, mte::isSideUsed);
            } else {
                drawGridOverlays(box);
            }
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private static void drawGridOverlays(@Nonnull AxisAlignedBB box) {
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

    @SideOnly(Side.CLIENT)
    private static void drawGridOverlays(EnumFacing facing, AxisAlignedBB box, Predicate<EnumFacing> test) {
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
        boolean frontBlocked = test.test(facing);
        boolean backBlocked = test.test(facing.getOpposite());

        switch (facing) {
            case WEST: {
                topRight.rotate(Math.PI / 2, Vector3.down);
                bottomRight.rotate(Math.PI / 2, Vector3.down);
                bottomLeft.rotate(Math.PI / 2, Vector3.down);
                topLeft.rotate(Math.PI / 2, Vector3.down);
                shift.rotate(Math.PI / 2, Vector3.down);
                shiftVert.rotate(Math.PI / 2, Vector3.down);

                leftBlocked = test.test(EnumFacing.NORTH);
                topBlocked = test.test(EnumFacing.UP);
                rightBlocked = test.test(EnumFacing.SOUTH);
                bottomBlocked = test.test(EnumFacing.DOWN);
                break;
            }
            case EAST: {
                topRight.rotate(-Math.PI / 2, Vector3.down);
                bottomRight.rotate(-Math.PI / 2, Vector3.down);
                bottomLeft.rotate(-Math.PI / 2, Vector3.down);
                topLeft.rotate(-Math.PI / 2, Vector3.down);
                shift.rotate(-Math.PI / 2, Vector3.down);
                shiftVert.rotate(-Math.PI / 2, Vector3.down);

                leftBlocked = test.test(EnumFacing.SOUTH);
                topBlocked = test.test(EnumFacing.UP);
                rightBlocked = test.test(EnumFacing.NORTH);
                bottomBlocked = test.test(EnumFacing.DOWN);
                break;
            }
            case NORTH: {
                topRight.rotate(Math.PI, Vector3.down);
                bottomRight.rotate(Math.PI, Vector3.down);
                bottomLeft.rotate(Math.PI, Vector3.down);
                topLeft.rotate(Math.PI, Vector3.down);
                shift.rotate(Math.PI, Vector3.down);
                shiftVert.rotate(Math.PI, Vector3.down);

                leftBlocked = test.test(EnumFacing.EAST);
                topBlocked = test.test(EnumFacing.UP);
                rightBlocked = test.test(EnumFacing.WEST);
                bottomBlocked = test.test(EnumFacing.DOWN);
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

                leftBlocked = test.test(EnumFacing.WEST);
                topBlocked = test.test(EnumFacing.NORTH);
                rightBlocked = test.test(EnumFacing.EAST);
                bottomBlocked = test.test(EnumFacing.SOUTH);
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

                leftBlocked = test.test(EnumFacing.WEST);
                topBlocked = test.test(EnumFacing.SOUTH);
                rightBlocked = test.test(EnumFacing.EAST);
                bottomBlocked = test.test(EnumFacing.NORTH);
                break;
            }
            default: {
                leftBlocked = test.test(EnumFacing.WEST);
                topBlocked = test.test(EnumFacing.UP);
                rightBlocked = test.test(EnumFacing.EAST);
                bottomBlocked = test.test(EnumFacing.DOWN);
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

    @SideOnly(Side.CLIENT)
    private static void startLine(BufferBuilder buffer, Vector3 vec) {
        buffer.pos(vec.x, vec.y, vec.z).color(rColour, gColour, bColour, 0.0F).endVertex();
    }

    @SideOnly(Side.CLIENT)
    private static void endLine(BufferBuilder buffer, Vector3 vec) {
        buffer.pos(vec.x, vec.y, vec.z).color(rColour, gColour, bColour, 1F).endVertex();
    }
}
