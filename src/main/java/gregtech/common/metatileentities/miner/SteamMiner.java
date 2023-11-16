package gregtech.common.metatileentities.miner;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IVentable;
import gregtech.api.capability.impl.CommonFluidFilters;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTTransferUtils;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.ConfigHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class SteamMiner extends MetaTileEntity implements Miner, IControllable, IVentable, IDataInfoProvider, IFastRenderMetaTileEntity {

    private boolean needsVenting = false;
    private boolean ventingStuck = false;

    private final int inventorySize;
    private final int energyPerTick;

    private final MinerLogic<SteamMiner> minerLogic;

    public SteamMiner(ResourceLocation metaTileEntityId, int workFrequency, int maximumDiameter) {
        super(metaTileEntityId);
        this.inventorySize = 4;
        this.energyPerTick = 16;
        this.minerLogic = new MinerLogic<>(this, workFrequency, maximumDiameter);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamMiner(metaTileEntityId, this.minerLogic.getWorkFrequency(), this.minerLogic.getMaximumDiameter());
    }

    @Override
    public FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, new FilteredFluidHandler(16000).setFilter(CommonFluidFilters.STEAM));
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(0, this, false);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(inventorySize, this, true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.STEAM_CASING_BRONZE.render(renderState, translation, colouredPipeline);
        // TODO replace top with vent, in standalone mod
        Textures.MINER_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), minerLogic.isActive(), minerLogic.isWorkingEnabled());
    }

    @Override
    public void renderMetaTileEntity(double x, double y, double z, float partialTicks) {
        MiningArea previewArea = this.minerLogic.getPreviewArea();
        if (previewArea != null) previewArea.renderMetaTileEntity(this, x, y, z, partialTicks);
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        MiningArea previewArea = this.minerLogic.getPreviewArea();
        if (previewArea != null) previewArea.renderMetaTileEntityFast(this, renderState, translation, partialTicks);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        MiningArea previewArea = this.minerLogic.getPreviewArea();
        return previewArea != null ? previewArea.getRenderBoundingBox() : MinerUtil.EMPTY_AABB;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        MiningArea previewArea = this.minerLogic.getPreviewArea();
        return previewArea != null && previewArea.shouldRenderInPass(pass);
    }

    @Override
    public boolean isGlobalRenderer() {
        return true;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int columns = 2;
        int xStart = (176 - (18 * columns)) / 2;
        int yStart = 25;
        int sideWidgetY = yStart + (columns * 18 - 18) / 2;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND_STEAM.get(false), 176, 166)
                .label(6, 6, getMetaFullName())
                .shouldColor(false)
                .widget(new ToggleButtonWidget(152, 25, 18, 18,
                        GuiTextures.BUTTON_MINER_AREA_PREVIEW,
                        this.minerLogic::isPreviewEnabled, this.minerLogic::setPreviewEnabled));

        IItemHandlerModifiable exportItems = this.getExportItems();
        for (int i = 0, slots = exportItems.getSlots(); i < slots; i++) {
            builder.slot(exportItems, i, xStart + 18 * (i % columns), yStart + 18 * (i / columns),
                    true, false, GuiTextures.SLOT_STEAM.get(false));
        }

        builder.widget(
                new ProgressWidget(this.minerLogic::getWorkProgress, xStart - 4 - 21, sideWidgetY, 21, 18,
                        GuiTextures.PROGRESS_BAR_MACERATE_STEAM.get(false), ProgressWidget.MoveType.HORIZONTAL)
        ).widget(
                new ImageWidget(xStart - 4 - 20, sideWidgetY + 18, 18, 18,
                        GuiTextures.INDICATOR_NO_STEAM.get(false))
                        .setPredicate(minerLogic::hasNotEnoughEnergy)
        ).bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT_STEAM.get(false), 0);

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick_steam", energyPerTick)
                + TextFormatting.GRAY + ", " +
                I18n.format("gregtech.machine.miner.per_block", this.minerLogic.getWorkFrequency() / 20));
        int maxDiameter = minerLogic.getMaximumDiameter();
        tooltip.add(I18n.format("gregtech.universal.tooltip.working_area", maxDiameter, maxDiameter));
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        tooltip.add(I18n.format("gregtech.tool_action.soft_mallet.reset"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean collectBlockDrops(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
        NonNullList<ItemStack> drops = NonNullList.create();
        IItemHandlerModifiable inventory = getExportItems();
        state.getBlock().getDrops(drops, world, pos, state, 0);
        if (GTTransferUtils.addItemsToItemHandler(inventory, true, drops)) {
            GTTransferUtils.addItemsToItemHandler(inventory, false, drops);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean drainMiningResources(@Nonnull MinedBlockType minedBlockType, boolean pipeExtended, boolean simulate) {
        if (minedBlockType == MinedBlockType.NOTHING) return true;
        if (this.ventingStuck) return false;
        FluidStack drained = this.importFluids.drain(energyPerTick, simulate);
        return drained != null && drained.amount >= energyPerTick;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public MiningPipeModel getMiningPipeModel() {
        return MiningPipeModels.BRONZE;
    }

    @Override
    public boolean canOperate() {
        if (!isNeedsVenting()) return true;
        tryDoVenting();
        return !isVentingStuck();
    }

    @Override
    public void onMineOperation(@Nonnull BlockPos pos, boolean isOre, boolean isOrigin) {
        setNeedsVenting(true);
    }

    @Override
    public void update() {
        super.update();
        this.minerLogic.update();
        if (!getWorld().isRemote) {
            if (getOffsetTimer() % 5 == 0)
                pushItemsIntoNearbyHandlers(getFrontFacing());
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
        return Pair.of(Textures.STEAM_CASING_BRONZE.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.TOP), getPaintingColorForRendering());
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
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

    public void setVentingStuck(boolean ventingStuck) {
        if (this.ventingStuck == ventingStuck) return;
        this.ventingStuck = ventingStuck;
        if (!this.getWorld().isRemote) {
            this.markDirty();
            this.writeCustomData(GregtechDataCodes.VENTING_STUCK, (buf) -> buf.writeBoolean(ventingStuck));
        }
    }

    @Override
    public void tryDoVenting() {
        if (GTUtility.tryVenting(getWorld(), getPos(), EnumFacing.UP,
                6, true,
                ConfigHolder.machines.machineSounds && !this.isMuffled())) {
            setNeedsVenting(false);
        } else {
            setVentingStuck(true);
        }
    }

    @Override
    public boolean isVentingStuck() {
        return ventingStuck;
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

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        int diameter = this.minerLogic.getCurrentDiameter();
        return Collections.singletonList(new TextComponentTranslation("gregtech.machine.miner.working_area", diameter, diameter));
    }
}
