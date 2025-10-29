package gregtech.common.metatileentities.steam;

import gregtech.api.capability.*;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.capability.impl.miner.MinerLogic;
import gregtech.api.capability.impl.miner.SteamMinerLogic;
import gregtech.api.damagesources.DamageSources;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuiTheme;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.api.util.KeyUtil;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.mui.widget.ScrollableTextWidget;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SteamMiner extends MetaTileEntity implements IMiner, IControllable, IVentable, IDataInfoProvider {

    private boolean needsVenting = false;
    private boolean ventingStuck = false;

    private final int inventorySize;
    private final int energyPerTick;
    private boolean isInventoryFull = false;

    private final MinerLogic minerLogic;

    public SteamMiner(ResourceLocation metaTileEntityId, int speed, int maximumRadius, int fortune) {
        super(metaTileEntityId);
        this.inventorySize = 4;
        this.energyPerTick = 16;
        this.minerLogic = new SteamMinerLogic(this, fortune, speed, maximumRadius, Textures.BRONZE_PLATED_BRICKS);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamMiner(metaTileEntityId, this.minerLogic.getSpeed(), this.minerLogic.getMaximumRadius(),
                this.minerLogic.getFortune());
    }

    @Override
    public FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FilteredFluidHandler(16000).setFilter(CommonFluidFilters.STEAM));
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, 0, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, inventorySize, this, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        ColourMultiplier multiplier = new ColourMultiplier(
                GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
        IVertexOperation[] coloredPipeline = ArrayUtils.add(pipeline, multiplier);
        Textures.STEAM_CASING_BRONZE.render(renderState, translation, coloredPipeline);
        for (EnumFacing renderSide : EnumFacing.HORIZONTALS) {
            if (renderSide == getFrontFacing()) {
                Textures.PIPE_OUT_OVERLAY.renderSided(renderSide, renderState, translation, pipeline);
            } else
                Textures.STEAM_MINER_OVERLAY.renderSided(renderSide, renderState, translation, coloredPipeline);
        }
        Textures.STEAM_VENT_OVERLAY.renderSided(EnumFacing.UP, renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        minerLogic.renderPipe(renderState, translation, pipeline);
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public GTGuiTheme getUITheme() {
        return GTGuiTheme.BRONZE;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        int rowSize = (int) Math.sqrt(inventorySize);
        panelSyncManager.registerSlotGroup("export_items", rowSize);

        IntSyncValue radiusSync = new IntSyncValue(() -> getWorkingArea(minerLogic.getCurrentRadius()));
        BooleanSyncValue isDoneSync = new BooleanSyncValue(minerLogic::isDone);
        BooleanSyncValue isWorkingSync = new BooleanSyncValue(minerLogic::isWorking);
        BooleanSyncValue isWorkingEnabledSync = new BooleanSyncValue(minerLogic::isWorkingEnabled);
        BooleanSyncValue isInventoryFullSync = new BooleanSyncValue(() -> isInventoryFull);
        BooleanSyncValue isVentBlockedSync = new BooleanSyncValue(this::isVentingStuck);
        BooleanSyncValue hasEnoughEnergySync = new BooleanSyncValue(() -> drainEnergy(true));
        panelSyncManager.syncValue("radius", 0, radiusSync);
        panelSyncManager.syncValue("done", 0, isDoneSync);
        panelSyncManager.syncValue("working", 0, isWorkingSync);
        panelSyncManager.syncValue("workingEnabled", 0, isWorkingEnabledSync);
        panelSyncManager.syncValue("inventoryFull", 0, isInventoryFullSync);
        panelSyncManager.syncValue("ventBlocked", 0, isVentBlockedSync);
        panelSyncManager.syncValue("enoughEnergy", 0, hasEnoughEnergySync);

        IntSyncValue xPosSync = new IntSyncValue(() -> minerLogic.getMineX().get());
        IntSyncValue yPosSync = new IntSyncValue(() -> minerLogic.getMineY().get());
        IntSyncValue zPosSync = new IntSyncValue(() -> minerLogic.getMineZ().get());
        panelSyncManager.syncValue("xPos", 0, xPosSync);
        panelSyncManager.syncValue("yPos", 0, yPosSync);
        panelSyncManager.syncValue("zPos", 0, zPosSync);

        return GTGuis.createPanel(this, 175, 176)
                .child(IKey.lang(getMetaFullName())
                        .asWidget()
                        .pos(5, 5))
                .child(new ScrollableTextWidget()
                        .pos(7 + 3, 15 + 3)
                        .size(105 - 3 * 2, 75 - 3 * 2)
                        .autoUpdate(true)
                        .alignment(Alignment.TopLeft)
                        .textBuilder(text -> {
                            text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.mining_at"));
                            text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.mining_pos_x",
                                    xPosSync.getIntValue()));
                            text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.mining_pos_y",
                                    yPosSync.getIntValue()));
                            text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.mining_pos_z",
                                    zPosSync.getIntValue()));

                            text.addLine(KeyUtil.lang(TextFormatting.WHITE, "gregtech.machine.miner.working_area",
                                    radiusSync.getIntValue(), radiusSync.getIntValue()));

                            if (isDoneSync.getBoolValue()) {
                                text.addLine(KeyUtil.lang(TextFormatting.GREEN, "gregtech.machine.miner.done"));
                            } else if (isWorkingSync.getBoolValue()) {
                                text.addLine(KeyUtil.lang(TextFormatting.GOLD, "gregtech.machine.miner.working"));
                            } else if (!isWorkingEnabledSync.getBoolValue()) {
                                text.addLine(KeyUtil.lang("gregtech.multiblock.work_paused"));
                            }

                            if (isInventoryFullSync.getBoolValue()) {
                                text.addLine(KeyUtil.lang(TextFormatting.RED, "gregtech.machine.miner.invfull"));
                            }

                            if (isVentBlockedSync.getBoolValue()) {
                                text.addLine(KeyUtil.lang(TextFormatting.RED, "gregtech.machine.steam_miner.vent"));
                            }

                            if (!hasEnoughEnergySync.getBoolValue()) {
                                text.addLine(KeyUtil.lang(TextFormatting.RED, "gregtech.machine.steam_miner.steam"));
                            }
                        })
                        .background(GTGuiTextures.DISPLAY_BRONZE.asIcon()
                                .margin(-3)))
                .child(new Grid()
                        .pos(151 - 18 * 2, 15)
                        .minElementMargin(0)
                        .minColWidth(18)
                        .minRowHeight(18)
                        .mapTo(rowSize, inventorySize, index -> new ItemSlot()
                                .slot(SyncHandlers.itemSlot(exportItems, index)
                                        .slotGroup("export_items")
                                        .accessibility(false, true))))
                .child(SlotGroupWidget.playerInventory()
                        .left(7)
                        .bottom(7));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick_steam", energyPerTick) + TextFormatting.GRAY +
                ", " + I18n.format("gregtech.machine.miner.per_block", this.minerLogic.getSpeed() / 20));
        int maxArea = getWorkingArea(minerLogic.getMaximumRadius());
        tooltip.add(I18n.format("gregtech.universal.tooltip.working_area", maxArea, maxArea));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    public boolean drainEnergy(boolean simulate) {
        int resultSteam = importFluids.getTankAt(0).getFluidAmount() - energyPerTick;
        if (!ventingStuck && resultSteam >= 0L && resultSteam <= importFluids.getTankAt(0).getCapacity()) {
            if (!simulate)
                importFluids.getTankAt(0).drain(energyPerTick, true);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        super.update();
        this.minerLogic.performMining();
        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 5 == 0)
                pushItemsIntoNearbyHandlers(getFrontFacing());

            if (this.minerLogic.wasActiveAndNeedsUpdate()) {
                this.minerLogic.setWasActiveAndNeedsUpdate(false);
                this.minerLogic.setActive(false);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("needsVenting", this.needsVenting);
        data.setBoolean("ventingStuck", this.ventingStuck);
        return this.minerLogic.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.needsVenting = data.getBoolean("needsVenting");
        this.ventingStuck = data.getBoolean("ventingStuck");
        this.minerLogic.readFromNBT(data);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.needsVenting);
        buf.writeBoolean(this.ventingStuck);
        this.minerLogic.writeInitialSyncData(buf);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.needsVenting = buf.readBoolean();
        this.ventingStuck = buf.readBoolean();
        this.minerLogic.receiveInitialSyncData(buf);
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.NEEDS_VENTING) {
            this.needsVenting = buf.readBoolean();
        } else if (dataId == GregtechDataCodes.VENTING_STUCK) {
            this.ventingStuck = buf.readBoolean();
        }
        this.minerLogic.receiveCustomData(dataId, buf);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.STEAM_CASING_BRONZE.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP),
                getPaintingColorForRendering());
    }

    public void setVentingStuck(boolean ventingStuck) {
        this.ventingStuck = ventingStuck;
        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(GregtechDataCodes.VENTING_STUCK, (buf) -> buf.writeBoolean(ventingStuck));
        }
    }

    @Override
    public void setNeedsVenting(boolean needsVenting) {
        this.needsVenting = needsVenting;
        if (!needsVenting && this.ventingStuck)
            this.setVentingStuck(false);

        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(GregtechDataCodes.NEEDS_VENTING, (buf) -> buf.writeBoolean(needsVenting));
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean isInventoryFull() {
        return isInventoryFull;
    }

    @Override
    public void setInventoryFull(boolean isFull) {
        this.isInventoryFull = isFull;
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.minerLogic.isWorkingEnabled();
    }

    @Override
    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.minerLogic.setWorkingEnabled(isActivationAllowed);
    }

    @Override
    public boolean isNeedsVenting() {
        return this.needsVenting;
    }

    @Override
    public void tryDoVenting() {
        BlockPos machinePos = this.getPos();
        EnumFacing ventingSide = EnumFacing.UP;
        BlockPos ventingBlockPos = machinePos.offset(ventingSide);
        IBlockState blockOnPos = this.getWorld().getBlockState(ventingBlockPos);
        if (blockOnPos.getCollisionBoundingBox(this.getWorld(), ventingBlockPos) == Block.NULL_AABB) {
            this.getWorld()
                    .getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(ventingBlockPos),
                            EntitySelectors.CAN_AI_TARGET)
                    .forEach((entity) -> entity.attackEntityFrom(DamageSources.getHeatDamage(), 6.0F));
            WorldServer world = (WorldServer) this.getWorld();
            double posX = (double) machinePos.getX() + 0.5D + (double) ventingSide.getXOffset() * 0.6D;
            double posY = (double) machinePos.getY() + 0.5D + (double) ventingSide.getYOffset() * 0.6D;
            double posZ = (double) machinePos.getZ() + 0.5D + (double) ventingSide.getZOffset() * 0.6D;
            world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ, 7 + world.rand.nextInt(3),
                    (double) ventingSide.getXOffset() / 2.0D, (double) ventingSide.getYOffset() / 2.0D,
                    (double) ventingSide.getZOffset() / 2.0D, 0.1D);
            if (ConfigHolder.machines.machineSounds && !this.isMuffled()) {
                world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F,
                        1.0F);
            }
            this.setNeedsVenting(false);
        } else if (!this.ventingStuck) {
            this.setVentingStuck(true);
        }
    }

    @Override
    public boolean isVentingStuck() {
        return ventingStuck;
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        int workingArea = getWorkingArea(this.minerLogic.getCurrentRadius());
        return Collections.singletonList(
                new TextComponentTranslation("gregtech.machine.miner.working_area", workingArea, workingArea));
    }
}
