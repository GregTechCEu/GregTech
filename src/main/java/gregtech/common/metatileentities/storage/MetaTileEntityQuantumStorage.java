package gregtech.common.metatileentities.storage;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.IActiveOutputSide;
import gregtech.api.capability.IQuantumController;
import gregtech.api.capability.IQuantumStorage;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.handler.BlockPosHighlightRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.client.utils.RenderUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.DynamicDrawable;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ScrollingTextWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static gregtech.api.capability.GregtechDataCodes.*;

public abstract class MetaTileEntityQuantumStorage<T> extends MetaTileEntity implements IQuantumStorage<T>,
                                                  IActiveOutputSide {

    /** not synced, server only. lazily initialized from pos */
    private WeakReference<IQuantumController> controller = new WeakReference<>(null);

    /** synced, server and client */
    private BlockPos controllerPos;
    protected static final String IS_VOIDING = "IsVoiding";
    protected static final String INPUT_FROM_OUTPUT = "AllowInputFromOutputSide";
    protected static final String INPUT_FROM_OUTPUT_FLUID = "AllowInputFromOutputSideF";
    protected static final String OUTPUT_FACING = "OutputFacing";
    protected static final String AUTO_OUTPUT_ITEMS = "AutoOutputItems";
    protected static final String IS_LOCKED = "IsLocked";
    protected static final String HAS_CONTROLLER = "HasController";
    protected static final String CONTROLLER_POS = "ControllerPos";

    protected EnumFacing outputFacing;
    protected boolean voiding = false;
    protected boolean autoOutput;
    protected boolean allowInputFromOutputSide = false;
    protected boolean locked = false;

    public MetaTileEntityQuantumStorage(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @SuppressWarnings("DataFlowIssue")
    @SideOnly(Side.CLIENT)
    protected void renderIndicatorOverlay(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        SimpleOverlayRenderer texture;
        if (isConnected()) {
            texture = getQuantumController().isPowered() ?
                    Textures.QUANTUM_INDICATOR_POWERED :
                    Textures.QUANTUM_INDICATOR_CONNECTED;
        } else {
            texture = Textures.QUANTUM_INDICATOR;
        }
        texture.renderSided(getFrontFacing(), renderState, RenderUtil.adjustTrans(translation, getFrontFacing(), 1),
                pipeline);
    }

    @Override
    public void setConnected(IQuantumController controller) {
        if (getWorld().isRemote) return;

        if (!controller.getPos().equals(controllerPos)) {
            this.controller = new WeakReference<>(controller);
            this.controllerPos = controller.getPos();
            writeCustomData(GregtechDataCodes.UPDATE_CONTROLLER_POS, buf -> buf.writeBlockPos(controllerPos));
            markDirty();
        }
    }

    @Override
    public void setDisconnected() {
        if (getWorld().isRemote) return;

        controller.clear();
        controllerPos = null;
        writeCustomData(GregtechDataCodes.REMOVE_CONTROLLER, buf -> {});
        markDirty();
    }

    // use this to make sure controller is properly initialized
    @Override
    public final IQuantumController getQuantumController() {
        if (isConnected()) {
            if (controller.get() != null) return controller.get();
            MetaTileEntity mte = GTUtility.getMetaTileEntity(getWorld(), controllerPos);
            if (mte instanceof IQuantumController quantumController) {
                controller = new WeakReference<>(quantumController);
                return quantumController;
            } else {
                // controller is no longer there for some reason, need to disconnect
                setDisconnected();
                tryFindNetwork();
            }
        }
        return null;
    }

    @Override
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    @Override
    public void onRemoval() {
        if (!getWorld().isRemote && isConnected()) {
            IQuantumController controller = getQuantumController();
            if (controller != null) controller.rebuildNetwork();
        }
    }

    @Override
    public void onPlacement(@Nullable EntityLivingBase placer) {
        super.onPlacement(placer);
        if (getWorld() == null || getWorld().isRemote)
            return;

        // add to the network if an adjacent block is part of a network
        // use whatever we find first, merging networks is not supported
        tryFindNetwork();
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        var panel = GTGuis.createPanel(this, 176, 166);
        createWidgets(panel, guiSyncManager);
        return panel.padding(4)
                .child(IKey.lang(getMetaFullName()).asWidget())
                .child(createQuantumIO(importItems, exportItems))
                .child(createQuantumButtonRow())
                .child(createConnectionButton()
                        .right(9)
                        .top(18 + 45))
                .child(SlotGroupWidget.playerInventory().left(7));
    }

    protected void createWidgets(ModularPanel mainPanel, PanelSyncManager syncManager) {}

    public Flow createQuantumDisplay(String lang,
                                     Supplier<String> name, Predicate<TextWidget> condition,
                                     Supplier<String> count) {
        return Flow.column()
                .background(GTGuiTextures.DISPLAY)
                .padding(4)
                .height(46)
                .top(16)
                .child(IKey.comp(IKey.lang(lang), IKey.SPACE, IKey.dynamic(count))
                        .alignment(Alignment.TopLeft)
                        .color(Color.WHITE.main)
                        .asWidget()
                        .widthRel(1f)
                        .left(4)
                        .marginBottom(2))
                .child(new ScrollingTextWidget(IKey.dynamic(name))
                        .alignment(Alignment.CenterLeft)
                        .color(Color.WHITE.main)
                        .setEnabledIf(condition)
                        .widthRel(0.75f)
                        .left(4)
                        .height(20)
                        .marginBottom(2));
    }

    public ParentWidget<?> createQuantumIO(IItemHandlerModifiable importHandler,
                                           IItemHandlerModifiable exportHandler) {
        return Flow.row()
                .pos(79, 18 + 45)
                .coverChildren()
                .child(new ItemSlot()
                        .background(GTGuiTextures.SLOT, GTGuiTextures.IN_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(importHandler, 0)
                                .accessibility(true, true)
                                .singletonSlotGroup(200))
                        .marginRight(18))
                .child(new ItemSlot()
                        .background(GTGuiTextures.SLOT, GTGuiTextures.OUT_SLOT_OVERLAY)
                        .slot(SyncHandlers.itemSlot(exportHandler, 0)
                                .accessibility(false, true)));
    }

    public Flow createQuantumButtonRow() {
        boolean isFluid = getType() == Type.FLUID;

        return Flow.row()
                .coverChildren()
                .pos(7, 63)
                // fluid
                .childIf(isFluid, () -> new ToggleButton()
                        .overlay(GTGuiTextures.BUTTON_FLUID_OUTPUT)
                        .addTooltip(true, IKey.lang("gregtech.gui.fluid_auto_output.tooltip.enabled"))
                        .addTooltip(false, IKey.lang("gregtech.gui.fluid_auto_output.tooltip.disabled"))
                        .value(new BooleanSyncValue(this::isAutoOutputFluids, this::setAutoOutput)))
                .childIf(isFluid, () -> new ToggleButton()
                        .overlay(GTGuiTextures.FLUID_LOCK_OVERLAY)
                        .addTooltip(true, IKey.lang("gregtech.gui.fluid_lock.tooltip.enabled"))
                        .addTooltip(false, IKey.lang("gregtech.gui.fluid_lock.tooltip.disabled"))
                        .value(new BooleanSyncValue(this::isLocked, this::setLocked)))
                .childIf(isFluid, () -> new ToggleButton()
                        .addTooltip(true, IKey.lang("gregtech.gui.fluid_voiding.tooltip.enabled"))
                        .addTooltip(false, IKey.lang("gregtech.gui.fluid_voiding.tooltip.disabled"))
                        .overlay(isFluid ? GTGuiTextures.FLUID_VOID_OVERLAY : GTGuiTextures.ITEM_VOID_OVERLAY)
                        .value(new BooleanSyncValue(this::isVoiding, this::setVoiding)))
                // item
                .childIf(!isFluid, () -> new ToggleButton()
                        .overlay(GTGuiTextures.BUTTON_ITEM_OUTPUT)
                        .addTooltip(true, IKey.lang("gregtech.gui.item_auto_output.tooltip.enabled"))
                        .addTooltip(false, IKey.lang("gregtech.gui.item_auto_output.tooltip.disabled"))
                        .value(new BooleanSyncValue(this::isAutoOutputItems, this::setAutoOutput)))
                .childIf(!isFluid, () -> new ToggleButton()
                        .overlay(GTGuiTextures.FLUID_LOCK_OVERLAY)
                        .addTooltip(true, IKey.lang("gregtech.gui.item_lock.tooltip.enabled"))
                        .addTooltip(false, IKey.lang("gregtech.gui.item_lock.tooltip.disabled"))
                        .value(new BooleanSyncValue(this::isLocked, this::setLocked)))
                .childIf(!isFluid, () -> new ToggleButton()
                        .addTooltip(true, IKey.lang("gregtech.gui.item_voiding.tooltip.enabled"))
                        .addTooltip(false, IKey.lang("gregtech.gui.item_voiding.tooltip.disabled"))
                        .overlay(isFluid ? GTGuiTextures.FLUID_VOID_OVERLAY : GTGuiTextures.ITEM_VOID_OVERLAY)
                        .value(new BooleanSyncValue(this::isVoiding, this::setVoiding)));
    }

    protected ModularPanel appendCreativeUI(ModularPanel panel, boolean isTank,
                                            BoolValue.Dynamic isActive,
                                            IntSyncValue amountPerCycle,
                                            IntSyncValue ticksPerCycle) {
        return panel.height(209)
                .bindPlayerInventory()
                .child(Flow.column()
                        .pos(7, 28)
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .coverChildren()
                        .child(IKey.lang("gregtech.creative." +
                                (isTank ? "tank.mbpc" : "chest.ipc"))
                                .asWidget()
                                .marginBottom(2))
                        .child(new TextFieldWidget()
                                .left(2)
                                .marginBottom(15)
                                .size(154, 14)
                                .keepScrollBarInArea(true)
                                .setNumbers(1, Integer.MAX_VALUE)
                                .setMaxLength(11)
                                .value(amountPerCycle))
                        .child(IKey.lang("gregtech.creative.tank.tpc").asWidget()
                                .marginBottom(2))
                        .child(new TextFieldWidget()
                                .left(2)
                                .size(154, 14)
                                .keepScrollBarInArea(true)
                                .setNumbers(1, Integer.MAX_VALUE)
                                .setMaxLength(11)
                                .value(ticksPerCycle)))
                .child(new ToggleButton()
                        .disableHoverBackground()
                        .pos(7, 101)
                        .size(162, 20)
                        .overlay(IKey.lang(() -> String.format("gregtech.creative.activity.%s",
                                isActive.getBoolValue() ? "on" : "off"))
                                .color(Color.WHITE.main))
                        .value(new BooleanSyncValue(isActive::getBoolValue, value -> {
                            isActive.setBoolValue(value);
                            scheduleRenderUpdate();
                            var c = getQuantumController();
                            if (c != null) c.onHandlerUpdate();
                        })))
                .child(createConnectionButton()
                        .right(9)
                        .top(7));
    }

    protected boolean isVoiding() {
        return this.voiding;
    }

    protected void setVoiding(boolean isVoiding) {
        this.voiding = isVoiding;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_IS_VOIDING, buf -> buf.writeBoolean(this.voiding));
            markDirty();
        }
    }

    protected boolean isLocked() {
        return this.locked;
    }

    protected void setLocked(boolean locked) {
        if (this.locked == locked) return;
        this.locked = locked;
        if (!getWorld().isRemote) {
            markDirty();
            writeCustomData(LOCK_FILL, buf -> buf.writeBoolean(this.locked));
        }
    }

    public EnumFacing getOutputFacing() {
        return outputFacing == null ? frontFacing.getOpposite() : outputFacing;
    }

    public void setOutputFacing(EnumFacing outputFacing) {
        this.outputFacing = outputFacing;
        if (!getWorld().isRemote) {
            notifyBlockUpdate();
            writeCustomData(UPDATE_OUTPUT_FACING, buf -> buf.writeByte(outputFacing.getIndex()));
            markDirty();
        }
    }

    public boolean isAutoOutputItems() {
        return getType() == Type.ITEM && autoOutput;
    }

    public boolean isAutoOutputFluids() {
        return getType() == Type.FLUID && autoOutput;
    }

    public void setAutoOutput(boolean autoOutput) {
        this.autoOutput = autoOutput;
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_AUTO_OUTPUT_ITEMS, buf -> buf.writeBoolean(autoOutput));
            markDirty();
        }
    }

    @Override
    public boolean isAllowInputFromOutputSideItems() {
        return getType() == Type.ITEM && allowInputFromOutputSide;
    }

    @Override
    public boolean isAllowInputFromOutputSideFluids() {
        return getType() == Type.FLUID && allowInputFromOutputSide;
    }

    public void setAllowInputFromOutputSide(boolean allowInputFromOutputSide) {
        this.allowInputFromOutputSide = allowInputFromOutputSide;
        if (!getWorld().isRemote) {
            markDirty();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(controllerPos != null);
        if (controllerPos != null) {
            buf.writeBlockPos(controllerPos);
        }
        buf.writeByte(getOutputFacing().getIndex());
        buf.writeBoolean(autoOutput);
        buf.writeBoolean(voiding);
        buf.writeBoolean(locked);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        if (buf.readBoolean()) {
            controllerPos = buf.readBlockPos();
            scheduleRenderUpdate();
        }
        this.outputFacing = EnumFacing.VALUES[buf.readByte()];
        this.autoOutput = buf.readBoolean();
        this.voiding = buf.readBoolean();
        this.locked = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_CONTROLLER_POS) {
            this.controllerPos = buf.readBlockPos();
            this.controller.clear();

            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.REMOVE_CONTROLLER) {
            this.controllerPos = null;
            this.controller.clear();

            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.LOCATE_CONTROLLER) {
            // tell controller to highlight
            BlockPosHighlightRenderer.renderBlockBoxHighLight(getControllerPos(), 6000, 1500);
            Minecraft.getMinecraft().player.closeScreen();
        } else if (dataId == UPDATE_OUTPUT_FACING) {
            this.outputFacing = EnumFacing.VALUES[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_AUTO_OUTPUT_FLUIDS || dataId == UPDATE_AUTO_OUTPUT_ITEMS) {
            this.autoOutput = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_IS_VOIDING) {
            setVoiding(buf.readBoolean());
        } else if (dataId == LOCK_FILL) {
            setLocked(buf.readBoolean());
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagCompound tagCompound = super.writeToNBT(data);
        tagCompound.setBoolean(HAS_CONTROLLER, controllerPos != null);
        if (controllerPos != null) {
            tagCompound.setLong(CONTROLLER_POS, controllerPos.toLong());
        }
        if (getType() == Type.ITEM || getType() == Type.FLUID) {
            data.setInteger(OUTPUT_FACING, getOutputFacing().getIndex());
            data.setBoolean(AUTO_OUTPUT_ITEMS, autoOutput);
            data.setBoolean(INPUT_FROM_OUTPUT, this.allowInputFromOutputSide);
            data.setBoolean(IS_VOIDING, isVoiding());
            data.setBoolean(IS_LOCKED, locked);
        }
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.getBoolean(HAS_CONTROLLER)) {
            this.controllerPos = BlockPos.fromLong(data.getLong(CONTROLLER_POS));
        }
        if (getType() == Type.ITEM || getType() == Type.FLUID) {
            this.outputFacing = EnumFacing.VALUES[data.getInteger(OUTPUT_FACING)];
            this.autoOutput = data.getBoolean(AUTO_OUTPUT_ITEMS);

            if (data.hasKey(INPUT_FROM_OUTPUT))
                this.allowInputFromOutputSide = data.getBoolean(INPUT_FROM_OUTPUT);
            else if (data.hasKey(INPUT_FROM_OUTPUT_FLUID))
                this.allowInputFromOutputSide = data.getBoolean(INPUT_FROM_OUTPUT_FLUID);

            // todo remove legacy save support "IsPartiallyVoiding" post 2.9
            this.voiding = data.getBoolean(IS_VOIDING) || data.getBoolean("IsPartiallyVoiding");

            this.locked = data.getBoolean(IS_LOCKED);
        }
    }

    protected ButtonWidget<?> createConnectionButton() {
        return new ButtonWidget<>()
                .disableHoverBackground()
                .onMousePressed(mouseButton -> {
                    // tell controller to highlight
                    if (!isConnected()) return false;
                    BlockPosHighlightRenderer.renderBlockBoxHighLight(getControllerPos(), 6000, 1500);
                    Minecraft.getMinecraft().player.closeScreen();
                    return true;
                })
                .tooltip(tooltip -> tooltip.setAutoUpdate(true))
                .tooltipBuilder(tooltip -> {
                    if (isConnected()) {
                        var pos = getControllerPos();
                        tooltip.addLine(IKey.lang("gregtech.machine.quantum_storage.connected", pos.getX(), pos.getY(),
                                pos.getZ()));
                    } else {
                        tooltip.addLine(IKey.lang("gregtech.machine.quantum_storage.disconnected"));
                    }
                })
                .background(new DynamicDrawable(
                        () -> isConnected() ? GTGuiTextures.GREGTECH_LOGO : GTGuiTextures.GREGTECH_LOGO_DARK));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.quantum_chest.tooltip"));
    }
}
