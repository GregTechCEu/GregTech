package gregtech.common.metatileentities.electric;

import gregtech.api.GTValues;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.BlockUtility;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GregFakePlayer;
import gregtech.api.util.Mods;
import gregtech.api.util.function.TriPredicate;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.RenderUtil;
import gregtech.integration.ftb.utility.FTBChunksHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_OUTPUT_FACING;

public class MetaTileEntityBlockBreaker extends TieredMetaTileEntity {

    private EnumFacing outputFacing;
    private int breakProgressTicksLeft;
    private float currentBlockHardness;
    private static final List<@NotNull TriPredicate<@NotNull World, @NotNull BlockPos, @NotNull FakePlayer>> PREDICATE_LIST = new ObjectArrayList<>(
            3);

    static {
        PREDICATE_LIST.add((world, blockPos, fakePlayer) -> !world.isAirBlock(blockPos));
        PREDICATE_LIST.add(
                (world, blockPos, fakePlayer) -> !(world.getBlockState(blockPos).getBlock() instanceof BlockLiquid));
        PREDICATE_LIST.add((world, blockPos, fakePlayer) -> world.isBlockModifiable(fakePlayer, blockPos));
        if (Mods.FTB_UTILITIES.isModLoaded()) {
            PREDICATE_LIST.add(FTBChunksHelper::isBlockModifiableByPlayer);
        }
    }

    /**
     * Add a predicate check to the block breaker. Intended to be used to prevent a certain block from being broken.
     * <br/>
     * <b>Warning!</b> the {@link FakePlayer} passed to the predicate is not a real player, but a fake one with
     * the same UUID as the real player who placed the block breaker. <br/>
     * Return {@code false} to cancel a break attempt on this block. <br/>
     * Return {@code true} to move onto the next predicate, and eventually break the block only if all other predicates
     * returned {@code false}.
     */
    @SuppressWarnings("unused")
    public static void registerBlockBreakerPredicate(@NotNull TriPredicate<@NotNull World, @NotNull BlockPos, @NotNull FakePlayer> predicate) {
        PREDICATE_LIST.add(predicate);
    }

    public MetaTileEntityBlockBreaker(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBlockBreaker(metaTileEntityId, getTier());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ROCK_BREAKER_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), false,
                false);
        Textures.PIPE_OUT_OVERLAY.renderSided(getOutputFacing(), renderState,
                RenderUtil.adjustTrans(translation, getOutputFacing(), 2), pipeline);
    }

    @Override
    public void update() {
        super.update();

        World world = getWorld();
        if (!world.isRemote) {
            tryBreakBlock(world);

            if (getOffsetTimer() % 5 == 0) {
                pushItemsIntoNearbyHandlers(getOutputFacing());
            }
        }
    }

    protected void tryBreakBlock(@NotNull World world) {
        BlockPos selfPos = getPos();
        BlockPos lookingAtPos = selfPos.offset(getFrontFacing());
        FakePlayer fakePlayer = GregFakePlayer.get((WorldServer) world, getOwner());

        for (TriPredicate<World, BlockPos, FakePlayer> predicate : PREDICATE_LIST) {
            if (!predicate.test(world, lookingAtPos, fakePlayer)) {
                return;
            }
        }

        IBlockState blockState = world.getBlockState(lookingAtPos);
        if (breakProgressTicksLeft > 0 && --breakProgressTicksLeft == 0 &&
                energyContainer.getEnergyStored() >= getEnergyPerBlockBreak()) {
            float hardness = blockState.getBlockHardness(world, lookingAtPos);
            if (hardness >= 0.0f && Math.abs(hardness - currentBlockHardness) < 0.5f) {
                List<ItemStack> drops = attemptBreakBlockAndObtainDrops(world, lookingAtPos, blockState, fakePlayer);
                addToInventoryOrDropItems(drops, world, getFrontFacing());
            }

            currentBlockHardness = 0.0f;
            energyContainer.removeEnergy(getEnergyPerBlockBreak());
        }

        if (breakProgressTicksLeft == 0 && isBlockRedstonePowered()) {
            float hardness = blockState.getBlockHardness(world, lookingAtPos);
            if (hardness >= 0.0f) {
                breakProgressTicksLeft = getTicksPerBlockBreak(hardness);
                currentBlockHardness = hardness;
            }
        }
    }

    protected void addToInventoryOrDropItems(@NotNull List<ItemStack> drops, @NotNull World world,
                                             @NotNull EnumFacing frontFacing) {
        double itemSpawnX = getPos().getX() + 0.5 + frontFacing.getXOffset();
        double itemSpawnY = getPos().getY() + 0.5 + frontFacing.getYOffset();
        double itemSpawnZ = getPos().getZ() + 0.5 + frontFacing.getZOffset();
        for (ItemStack itemStack : drops) {
            ItemStack remainStack = GTTransferUtils.insertItem(exportItems, itemStack, false);
            if (!remainStack.isEmpty()) {
                EntityItem entityitem = new EntityItem(world, itemSpawnX, itemSpawnY, itemSpawnZ, remainStack);
                entityitem.setDefaultPickupDelay();
                world.spawnEntity(entityitem);
            }
        }
    }

    protected @NotNull List<ItemStack> attemptBreakBlockAndObtainDrops(@NotNull World world,
                                                                       @NotNull BlockPos lookingAtPos,
                                                                       @NotNull IBlockState blockState,
                                                                       @NotNull EntityPlayer entityPlayer) {
        TileEntity tileEntity = world.getTileEntity(lookingAtPos);
        Block block = blockState.getBlock();
        if (block instanceof BlockSkull) {
            BlockUtility.startCaptureDrops();
            boolean result = block.removedByPlayer(blockState, world, lookingAtPos, entityPlayer, true);
            List<ItemStack> drops = BlockUtility.stopCaptureDrops();

            if (result) {
                world.playEvent(null, 2001, lookingAtPos, Block.getStateId(blockState));
                block.onPlayerDestroy(world, lookingAtPos, blockState);

                block.harvestBlock(world, entityPlayer, lookingAtPos, blockState, tileEntity, ItemStack.EMPTY);
                return drops;
            }
        } else {
            if (block.removedByPlayer(blockState, world, lookingAtPos, entityPlayer, true)) {
                world.playEvent(null, 2001, lookingAtPos, Block.getStateId(blockState));
                block.onPlayerDestroy(world, lookingAtPos, blockState);

                BlockUtility.startCaptureDrops();
                block.harvestBlock(world, entityPlayer, lookingAtPos, blockState, tileEntity, ItemStack.EMPTY);
                return BlockUtility.stopCaptureDrops();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public boolean onWrenchClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                 CuboidRayTraceResult hitResult) {
        if (!playerIn.isSneaking()) {
            EnumFacing currentOutputSide = getOutputFacing();
            if (currentOutputSide == facing || getFrontFacing() == facing) return false;
            setOutputFacing(facing);
            return true;
        }
        return super.onWrenchClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("OutputFacing", getOutputFacing().getIndex());
        data.setInteger("BlockBreakProgress", breakProgressTicksLeft);
        data.setFloat("BlockHardness", currentBlockHardness);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.outputFacing = EnumFacing.VALUES[data.getInteger("OutputFacing")];
        this.breakProgressTicksLeft = data.getInteger("BlockBreakProgress");
        this.currentBlockHardness = data.getFloat("BlockHardness");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(getOutputFacing().getIndex());
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        }
    }

    @Override
    public boolean isValidFrontFacing(EnumFacing facing) {
        // use direct outputFacing field instead of getter method because otherwise
        // it will just return SOUTH for null output facing
        return facing != outputFacing;
    }

    @NotNull
    public EnumFacing getOutputFacing() {
        return outputFacing == null ? EnumFacing.SOUTH : outputFacing;
    }

    public void setOutputFacing(@NotNull EnumFacing outputFacing) {
        this.outputFacing = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> buf.writeByte(outputFacing.getIndex()));
            markDirty();
        }
    }

    @Override
    public void setFrontFacing(@NotNull EnumFacing frontFacing) {
        super.setFrontFacing(frontFacing);
        // Set initial output facing as opposite to front
        setOutputFacing(frontFacing.getOpposite());
    }

    public int getEnergyPerBlockBreak() {
        return (int) GTValues.V[getTier()];
    }

    protected int getInventorySize() {
        int sizeRoot = (1 + getTier());
        return sizeRoot * sizeRoot;
    }

    public int getTicksPerBlockBreak(float blockHardness) {
        int ticksPerOneDurability = 5;
        int totalTicksPerBlock = (int) Math.ceil(ticksPerOneDurability * blockHardness);
        float efficiencyMultiplier = 1.0f - getEfficiencyMultiplier();
        return (int) Math.ceil(totalTicksPerBlock * efficiencyMultiplier);
    }

    public float getEfficiencyMultiplier() {
        return 1.0f - MathHelper.clamp(1.0f - 0.2f * (getTier() - 1.0f), 0.1f, 1.0f);
    }

    public int getBreakProgressTicksLeft() {
        return breakProgressTicksLeft;
    }

    public float getCurrentBlockHardness() {
        return currentBlockHardness;
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, getInventorySize());
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        int rowSize = (int) Math.sqrt(getInventorySize());
        guiSyncManager.registerSlotGroup("item_inv", rowSize);

        List<List<IWidget>> widgets = new ArrayList<>();
        for (int i = 0; i < rowSize; i++) {
            widgets.add(new ArrayList<>());
            for (int j = 0; j < rowSize; j++) {
                int index = i * rowSize + j;
                widgets.get(i).add(new ItemSlot().slot(SyncHandlers.itemSlot(exportItems, index)
                        .slotGroup("item_inv").accessibility(false, true)));
            }
        }

        return GTGuis.createPanel(this, 176, 18 + 18 * rowSize + 94)
                .child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(new Grid()
                        .top(18).height(rowSize * 18)
                        .minElementMargin(0, 0)
                        .minColWidth(18).minRowHeight(18)
                        .alignX(0.5f)
                        .matrix(widgets));
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.block_breaker.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_op", getEnergyPerBlockBreak()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(),
                GTValues.VNF[getTier()]));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", getInventorySize()));
        tooltip.add(I18n.format("gregtech.machine.block_breaker.speed_bonus", (int) (getEfficiencyMultiplier() * 100)));
        tooltip.add(I18n.format("gregtech.universal.tooltip.requires_redstone"));
    }

    @Override
    public boolean needsSneakToRotate() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}
