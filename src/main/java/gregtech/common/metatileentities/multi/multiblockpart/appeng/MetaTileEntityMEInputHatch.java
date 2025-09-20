package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.sync.appeng.AEFluidSyncHandler;
import gregtech.api.mui.widget.appeng.fluid.AEFluidConfigSlot;
import gregtech.api.mui.widget.appeng.fluid.AEFluidDisplaySlot;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEFluidSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedFluidStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.IFluidTank;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class MetaTileEntityMEInputHatch extends MetaTileEntityAEHostableChannelPart<IAEFluidStack>
                                        implements IMultiblockAbilityPart<IFluidTank>, IDataStickIntractable {

    public static final String FLUID_BUFFER_TAG = "FluidTanks";
    public static final String WORKING_TAG = "WorkingEnabled";
    public static final String SYNC_HANDLER_NAME = "aeSync";

    public final static int CONFIG_SIZE = 16;
    protected ExportOnlyAEFluidList aeFluidHandler;

    private boolean workingEnabled = true;

    public MetaTileEntityMEInputHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false, IFluidStorageChannel.class);
    }

    protected ExportOnlyAEFluidList getAEFluidHandler() {
        if (aeFluidHandler == null) {
            aeFluidHandler = new ExportOnlyAEFluidList(this, CONFIG_SIZE, this.getController());
        }
        return aeFluidHandler;
    }

    public boolean isAutoPull() {
        return getAEFluidHandler().isAutoPull();
    }

    @Override
    protected void initializeInventory() {
        getAEFluidHandler(); // initialize it
        super.initializeInventory();
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false, getAEFluidHandler().getInventory());
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && updateMEStatus() && shouldSyncME()) {
            operateOnME();
        }
    }

    protected void operateOnME() {
        syncME();
    }

    protected void syncME() {
        IMEMonitor<IAEFluidStack> monitor = getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEFluidSlot aeTank : this.getAEFluidHandler().getInventory()) {
            // Try to clear the wrong fluid
            IAEFluidStack exceedFluid = aeTank.exceedStack();
            if (exceedFluid != null) {
                long total = exceedFluid.getStackSize();
                IAEFluidStack notInserted = monitor.injectItems(exceedFluid, Actionable.MODULATE,
                        this.getActionSource());
                if (notInserted != null && notInserted.getStackSize() > 0) {
                    aeTank.drain((int) (total - notInserted.getStackSize()), true);
                    continue;
                } else {
                    aeTank.drain((int) total, true);
                }
            }
            // Fill it
            IAEFluidStack reqFluid = aeTank.requestStack();
            if (reqFluid != null) {
                IAEFluidStack extracted = monitor.extractItems(reqFluid, Actionable.MODULATE, this.getActionSource());
                if (extracted != null) {
                    aeTank.addStack(extracted);
                }
            }
        }
    }

    @Override
    public void onRemoval() {
        flushInventory();
        super.onRemoval();
    }

    protected void flushInventory() {
        IMEMonitor<IAEFluidStack> monitor = getMonitor();
        if (monitor == null) return;

        for (ExportOnlyAEFluidSlot aeTank : this.getAEFluidHandler().getInventory()) {
            IAEFluidStack stock = aeTank.getStock();
            if (stock instanceof WrappedFluidStack wrappedFluidStack) {
                stock = wrappedFluidStack.getAEStack();
            }
            if (stock != null) {
                monitor.injectItems(stock, Actionable.MODULATE, this.getActionSource());
            }
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEInputHatch(this.metaTileEntityId, getTier());
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        ModularPanel mainPanel = GTGuis.createPanel(this, 176, 18 + 18 * 4 + 94);

        final boolean isStocking = getAEFluidHandler().isStocking();

        AEFluidSyncHandler syncHandler = new AEFluidSyncHandler(getAEFluidHandler(), this::markDirty);
        guiSyncManager.syncValue(SYNC_HANDLER_NAME, 0, syncHandler);

        Grid configGrid = new Grid()
                .pos(7, 25)
                .size(18 * 4)
                .minElementMargin(0, 0)
                .minColWidth(18)
                .minRowHeight(18)
                .matrix(Grid.mapToMatrix((int) Math.sqrt(CONFIG_SIZE), CONFIG_SIZE,
                        index -> new AEFluidConfigSlot(isStocking, index, this::isAutoPull)
                                .syncHandler(SYNC_HANDLER_NAME, 0)
                                .debugName("Index " + index)));

        for (IWidget aeWidget : configGrid.getChildren()) {
            ((AEFluidConfigSlot) aeWidget).onSelect(() -> {
                for (IWidget widget : configGrid.getChildren()) {
                    ((AEFluidConfigSlot) widget).deselect();
                }
            });
        }

        return mainPanel.child(IKey.lang(getMetaFullName()).asWidget().pos(5, 5))
                .child(SlotGroupWidget.playerInventory().left(7).bottom(7))
                .child(IKey.lang(() -> isOnline() ? "gregtech.gui.me_network.online" :
                        "gregtech.gui.me_network.offline")
                        .asWidget()
                        .marginLeft(5)
                        .widthRel(1.0f)
                        .top(15))
                .child(configGrid)
                .child(new Grid()
                        .pos(7 + 18 * 5, 25)
                        .size(18 * 4)
                        .minElementMargin(0, 0)
                        .minColWidth(18)
                        .minRowHeight(18)
                        .matrix(Grid.mapToMatrix((int) Math.sqrt(CONFIG_SIZE), CONFIG_SIZE,
                                index -> new AEFluidDisplaySlot(index)
                                        .background(GTGuiTextures.SLOT_DARK)
                                        .syncHandler(SYNC_HANDLER_NAME, 0)
                                        .debugName("Index " + index))))
                .child(Flow.column()
                        .pos(7 + 18 * 4, 25)
                        .size(18, 18 * 4)
                        .child(getExtraButton())
                        .child(GTGuiTextures.ARROW_DOUBLE.asWidget())
                        .child(new Widget<>()
                                .size(18))
                        .child(GTGuiTextures.getLogo(getUITheme()).asWidget()
                                .size(17)))
                .child(Flow.row()
                        .width(isStocking ? 18 : 18 * 2)
                        .height(18)
                        .top(5)
                        .right(7)
                        .childIf(!isStocking, getMultiplierWidget(guiSyncManager))
                        .child(getSettingWidget(guiSyncManager)));
    }

    protected Widget<?> getSettingWidget(PanelSyncManager guiSyncManager) {
        IPanelHandler settingPopup = guiSyncManager.panel("settings_panel", this::buildSettingsPopup, true);

        return new ButtonWidget<>()
                .onMousePressed(mouse -> {
                    if (settingPopup.isPanelOpen()) {
                        settingPopup.closePanel();
                    } else {
                        settingPopup.openPanel();
                    }

                    return true;
                })
                .addTooltipLine(IKey.lang("gregtech.machine.me.settings.button"))
                .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY);
    }

    protected ModularPanel buildSettingsPopup(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        IntSyncValue refreshRateSync = new IntSyncValue(this::getRefreshRate, this::setRefreshRate);
        ItemDrawable meControllerDrawable = new ItemDrawable(getStackForm());

        final int width = 110;
        return GTGuis.createPopupPanel("settings", width, getSettingsPopupHeight())
                .child(Flow.row()
                        .pos(4, 4)
                        .height(16)
                        .child(meControllerDrawable.asWidget()
                                .size(16)
                                .marginRight(4))
                        .child(IKey.lang("gregtech.machine.me.settings.button")
                                .asWidget()
                                .heightRel(1.0f)))
                .child(IKey.lang("gregtech.machine.me.settings.refresh_rate")
                        .asWidget()
                        .left(5)
                        .top(5 + 18))
                .child(new TextFieldWidget()
                        .left(5)
                        .top(15 + 18)
                        .size(width - 10, 10)
                        .setNumbers(1, Integer.MAX_VALUE)
                        .setDefaultNumber(ConfigHolder.compat.ae2.updateIntervals)
                        .value(refreshRateSync));
    }

    protected int getSettingsPopupHeight() {
        return 33 + 14 + 5;
    }

    protected Widget<?> getMultiplierWidget(PanelSyncManager syncManager) {
        IPanelHandler multiplierPopup = syncManager.panel("multiplier_panel", this::buildMultiplierPopup, true);

        return new ButtonWidget<>()
                .onMousePressed(mouse -> {
                    if (multiplierPopup.isPanelOpen()) {
                        multiplierPopup.closePanel();
                    } else {
                        multiplierPopup.openPanel();
                    }

                    return true;
                })
                .addTooltipLine(IKey.lang("gregtech.machine.me.multiplier.button"))
                .overlay(GTGuiTextures.ARROW_OPPOSITE);
    }

    protected ModularPanel buildMultiplierPopup(PanelSyncManager syncManager, IPanelHandler syncHandler) {
        AEFluidSyncHandler aeSyncHandler = (AEFluidSyncHandler) ((PanelSyncHandler) syncHandler).getSyncManager()
                .getSyncHandler(PanelSyncManager.makeSyncKey(SYNC_HANDLER_NAME, 0));
        IntValue multiplier = new IntValue(2);

        return GTGuis.createPopupPanel("multiplier", 100, 35)
                .child(new ButtonWidget<>()
                        .onMousePressed(mouse -> {
                            aeSyncHandler.modifyConfigAmounts(
                                    (index, amount) -> Math.max(1, amount / multiplier.getIntValue()));
                            return true;
                        })
                        .left(5)
                        .top(7)
                        .overlay(IKey.str("/")))
                .child(new TextFieldWidget()
                        .alignX(0.5f)
                        .top(5)
                        .widthRel(0.5f)
                        .height(20)
                        .setNumbers(2, Integer.MAX_VALUE)
                        .setDefaultNumber(2)
                        .value(multiplier))
                .child(new ButtonWidget<>()
                        .onMousePressed(mouse -> {
                            aeSyncHandler.modifyConfigAmounts((index, amount) -> GTUtility
                                    .safeIntegerMultiplication(amount, multiplier.getIntValue()));
                            return true;
                        })
                        .right(5)
                        .top(7)
                        .overlay(IKey.str("x")));
    }

    protected Widget<?> getExtraButton() {
        return new Widget<>()
                .size(18);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        World world = this.getWorld();
        if (world != null && !world.isRemote) {
            writeCustomData(GregtechDataCodes.WORKING_ENABLED, buf -> buf.writeBoolean(workingEnabled));
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
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.workingEnabled);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.workingEnabled = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        data.setBoolean(WORKING_TAG, this.workingEnabled);

        NBTTagList tanks = new NBTTagList();
        for (int i = 0; i < CONFIG_SIZE; i++) {
            ExportOnlyAEFluidSlot tank = this.getAEFluidHandler().getInventory()[i];
            NBTTagCompound tankTag = new NBTTagCompound();
            tankTag.setInteger("slot", i);
            tankTag.setTag("tank", tank.serializeNBT());
            tanks.appendTag(tankTag);
        }
        data.setTag(FLUID_BUFFER_TAG, tanks);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }

        if (data.hasKey(FLUID_BUFFER_TAG, 9)) {
            NBTTagList tanks = (NBTTagList) data.getTag(FLUID_BUFFER_TAG);
            for (NBTBase nbtBase : tanks) {
                NBTTagCompound tankTag = (NBTTagCompound) nbtBase;
                ExportOnlyAEFluidSlot tank = this.getAEFluidHandler().getInventory()[tankTag.getInteger("slot")];
                tank.deserializeNBT(tankTag.getCompoundTag("tank"));
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            if (isOnline()) {
                Textures.ME_INPUT_HATCH_ACTIVE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.ME_INPUT_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.fluid_hatch.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.fluid_import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me_import_fluid_hatch.configs.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.copy_paste.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.extra_connections.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IFluidTank> getAbility() {
        return MultiblockAbility.IMPORT_FLUIDS;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.addAll(Arrays.asList(this.getAEFluidHandler().getInventory()));
    }

    @Override
    public final void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("MEInputHatch", writeConfigToTag());
        dataStick.setTagCompound(tag);
        dataStick.setTranslatableName("gregtech.machine.me.fluid_import.data_stick.name");
        player.sendStatusMessage(new TextComponentTranslation("gregtech.machine.me.import_copy_settings"), true);
    }

    protected NBTTagCompound writeConfigToTag() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound configStacks = new NBTTagCompound();
        tag.setTag("ConfigStacks", configStacks);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            var slot = this.aeFluidHandler.getInventory()[i];
            IAEFluidStack config = slot.getConfig();
            if (config == null) {
                continue;
            }
            NBTTagCompound stackNbt = new NBTTagCompound();
            config.writeToNBT(stackNbt);
            configStacks.setTag(Integer.toString(i), stackNbt);
        }

        tag.setInteger(REFRESH_RATE_TAG, this.refreshRate);

        return tag;
    }

    @Override
    public final boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = dataStick.getTagCompound();
        if (tag == null || !tag.hasKey("MEInputHatch")) {
            return false;
        }
        readConfigFromTag(tag.getCompoundTag("MEInputHatch"));
        syncME();
        player.sendStatusMessage(new TextComponentTranslation("gregtech.machine.me.import_paste_settings"), true);
        return true;
    }

    protected void readConfigFromTag(NBTTagCompound tag) {
        if (tag.hasKey("ConfigStacks")) {
            NBTTagCompound configStacks = tag.getCompoundTag("ConfigStacks");
            for (int i = 0; i < CONFIG_SIZE; i++) {
                String key = Integer.toString(i);
                if (configStacks.hasKey(key)) {
                    NBTTagCompound configTag = configStacks.getCompoundTag(key);
                    this.aeFluidHandler.getInventory()[i].setConfig(WrappedFluidStack.fromNBT(configTag));
                } else {
                    this.aeFluidHandler.getInventory()[i].setConfig(null);
                }
            }
        }

        if (tag.hasKey(REFRESH_RATE_TAG)) {
            this.refreshRate = tag.getInteger(REFRESH_RATE_TAG);
        }
    }
}
