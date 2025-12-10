package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.mui.widget.FlappyGreg;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityMaintenanceHatch extends MetaTileEntityMultiblockPart
                                            implements IMultiblockAbilityPart<IMaintenanceHatch>, IMaintenanceHatch,
                                            IMetaTileEntityGuiHolder {

    private final boolean isConfigurable;
    private TapeStackHandler tapeHandler;
    private boolean isTaped;

    // Used to store state temporarily if the Controller is broken
    private byte maintenanceProblems = -1;
    private int timeActive = -1;

    // Some stats used for the Configurable Maintenance Hatch
    private double durationMultiplier = 1.0f;
    private static final double MIN_DURATION_MULTIPLIER = 0.9d;
    private static final double MAX_DURATION_MULTIPLIER = 1.1d;
    private static final DoubleUnaryOperator TIME_ACTION = t -> {
        if (t < 1.0f) {
            return -20.0f * t + 21.0f;
        } else {
            return -8.0f * t + 9.0f;
        }
    };

    private static final DoubleList SLIDER_STOPPER_STOPS = DoubleLists.unmodifiable(
            new DoubleArrayList(new double[] { 0.9d, 0.91d, 0.92d, 0.93d, 0.94d, 0.95d, 0.96d, 0.97d, 0.98d, 0.99d,
                    1.0d, 1.01d, 1.02d, 1.03d, 1.04d, 1.05d, 1.06d, 1.07d, 1.08d, 1.09d, 1.1d }));

    public MetaTileEntityMaintenanceHatch(ResourceLocation metaTileEntityId, boolean isConfigurable) {
        super(metaTileEntityId, isConfigurable ? 3 : 1);
        this.isConfigurable = isConfigurable;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity metaTileEntityHolder) {
        return new MetaTileEntityMaintenanceHatch(metaTileEntityId, isConfigurable);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        if (shouldRenderOverlay()) {
            (isConfigurable ? Textures.MAINTENANCE_OVERLAY_CONFIGURABLE :
                    isTaped ? Textures.MAINTENANCE_OVERLAY_TAPED : Textures.MAINTENANCE_OVERLAY)
                            .renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.tapeHandler = new TapeStackHandler(this);
        this.itemInventory = tapeHandler;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        clearInventory(itemBuffer, tapeHandler);
    }

    /**
     * Sets this Maintenance Hatch as being duct taped
     * 
     * @param isTaped is the state of the hatch being taped or not
     */
    @Override
    public void setTaped(boolean isTaped) {
        this.isTaped = isTaped;
        if (!getWorld().isRemote) {
            writeCustomData(IS_TAPED, buf -> buf.writeBoolean(isTaped));
            markDirty();
        }
    }

    /**
     * Stores maintenance data to this MetaTileEntity
     * 
     * @param maintenanceProblems is the byte value representing the problems
     * @param timeActive          is the int value representing the total time the parent multiblock has been active
     */
    @Override
    public void storeMaintenanceData(byte maintenanceProblems, int timeActive) {
        this.maintenanceProblems = maintenanceProblems;
        this.timeActive = timeActive;
        if (!getWorld().isRemote) {
            writeCustomData(STORE_MAINTENANCE, buf -> {
                buf.writeByte(maintenanceProblems);
                buf.writeInt(timeActive);
            });
        }
    }

    /**
     *
     * @return whether this maintenance hatch has maintenance data
     */
    @Override
    public boolean hasMaintenanceData() {
        return this.maintenanceProblems != -1;
    }

    /**
     * reads this MetaTileEntity's maintenance data
     * 
     * @return Tuple of Byte, Integer corresponding to the maintenance problems, and total time active
     */
    @Override
    public Tuple<Byte, Integer> readMaintenanceData() {
        Tuple<Byte, Integer> data = new Tuple<>(this.maintenanceProblems, this.timeActive);
        storeMaintenanceData((byte) -1, -1);
        return data;
    }

    @Override
    public boolean startWithoutProblems() {
        return isConfigurable;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && getOffsetTimer() % 20 == 0) {
            if (getController() instanceof IMaintenance iMaintenance &&
                    iMaintenance.hasMaintenanceProblems() && tapeHandler.tryConsumeTape()) {
                iMaintenance.fixAllMaintenance();
                setTaped(true);
            }
        }
    }

    protected void fixAllProblems() {
        if (getController() instanceof IMaintenance iMaintenance) {
            iMaintenance.fixAllMaintenance();
        }
    }

    /**
     * Fixes the maintenance problems of this hatch's Multiblock Controller
     * 
     * @param player the player performing the fixing
     */
    private void fixMaintenanceProblems(@NotNull EntityPlayer player) {
        if (!(this.getController() instanceof IMaintenance controller)) {
            return;
        } else if (!controller.hasMaintenanceProblems()) {
            return;
        } else if (player.capabilities.isCreativeMode) {
            controller.fixAllMaintenance();
            return;
        }

        List<ItemStack> playerItems = new ObjectArrayList<>(player.inventory.mainInventory);
        playerItems.removeIf(ItemStack::isEmpty);

        // Try to tape this hatch from the player's inventory first
        Iterator<ItemStack> tapeIterator = playerItems.iterator();
        while (tapeIterator.hasNext()) {
            ItemStack stack = tapeIterator.next();
            if (consumeDuctTape(stack, true)) {
                tapeIterator.remove();
                controller.fixAllMaintenance();
                setTaped(true);
                return;
            }
        }

        ItemStack cursorStack = player.inventory.getItemStack();
        if (!cursorStack.isEmpty()) {
            // If player clicked "slot" with item, only attempt fixing with that
            playerItems.clear();
            playerItems.add(cursorStack);
        }

        fixMaintenanceProblemsWithTools(player, playerItems);
    }

    /**
     * Fix maintenance issues on the multiblock this maintenance hatch is attached to.
     *
     * @param player the player doing the fixing
     * @param stacks a list of item stacks to attempt fixing the problems with. <b>The list will be mutated when this
     *               filters out non-tools!</b>
     */
    public void fixMaintenanceProblemsWithTools(@NotNull EntityPlayer player, @NotNull List<ItemStack> stacks) {
        if (!(getController() instanceof IMaintenance controller) || !controller.hasMaintenanceProblems()) return;

        // Reduce items and unwrap toolbelts into usable tools
        int index = 0;
        while (index < stacks.size()) {
            ItemStack stack = stacks.get(index);
            Item item = stack.getItem();

            if (item instanceof ItemGTToolbelt toolbelt) {
                stacks.remove(index);
                toolbelt.iterateSlots(stack, stacks::add);
                continue;
            }

            if (item.getToolClasses(stack).isEmpty()) {
                stacks.remove(index);
                continue;
            }

            index++;
        }

        Set<Int2ObjectMap.Entry<String>> toolEntries = controller.getToolsForMaintenance();
        for (ItemStack toolStack : stacks) {
            if (toolEntries.isEmpty()) return;

            Int2ObjectMap.Entry<String> entry = findMatchingClass(toolEntries,
                    toolStack.getItem().getToolClasses(toolStack));
            if (entry != null) {
                ToolHelper.damageItemWhenCrafting(toolStack, player);
                controller.setMaintenanceFixed(entry.getIntKey());
                toolEntries.remove(entry);
                setTaped(false);
            }
        }
    }

    @Nullable
    private static Int2ObjectMap.Entry<String> findMatchingClass(@NotNull Set<Int2ObjectMap.Entry<String>> toolEntries,
                                                                 @NotNull Set<String> findIn) {
        for (Int2ObjectMap.Entry<String> entry : toolEntries) {
            if (findIn.contains(entry.getValue())) {
                return entry;
            }
        }

        return null;
    }

    private static boolean consumeDuctTape(@NotNull ItemStack itemStack, boolean consumeTape) {
        if (!itemStack.isEmpty() && TapeStackHandler.isStackTape(itemStack)) {
            if (consumeTape) {
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isFullAuto() {
        return false;
    }

    @Override
    public double getDurationMultiplier() {
        return durationMultiplier;
    }

    protected void setDurationMultiplier(double multiplier) {
        this.durationMultiplier = multiplier;
    }

    @Override
    public double getTimeMultiplier() {
        return TIME_ACTION.applyAsDouble(durationMultiplier);
    }

    @Override
    public void onRemoval() {
        if (getController() instanceof IMaintenance iMaintenance) {
            if (!getWorld().isRemote) {
                iMaintenance.storeTaped(isTaped);
            }
        }

        super.onRemoval();
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (getController() instanceof IMaintenance iMaintenance && iMaintenance.hasMaintenanceProblems()) {
            if (consumeDuctTape(playerIn.getHeldItem(hand), !playerIn.capabilities.isCreativeMode)) {
                iMaintenance.fixAllMaintenance();
                setTaped(true);
                return true;
            }
        }

        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void writeExtraGuiData(@NotNull PacketBuffer buffer) {
        buffer.writeBoolean(GTUtility.isAprilFools());
    }

    @Override
    public @NotNull ModularPanel buildUI(MetaTileEntityGuiData guiData, PanelSyncManager panelSyncManager,
                                         UISettings settings) {
        panelSyncManager.registerServerSyncedAction("game_finish",
                packet -> fixMaintenanceProblems(guiData.getPlayer()));
        InteractionSyncHandler maintenanceClickSync = new InteractionSyncHandler()
                .setOnMousePressed(mouse -> {
                    if (panelSyncManager.isClient()) return;
                    fixMaintenanceProblems(guiData.getPlayer());
                });
        DoubleSyncValue multiplierSync = SyncHandlers.doubleNumber(this::getDurationMultiplier,
                this::setDurationMultiplier);
        panelSyncManager.syncValue("multiplierSync", 0, multiplierSync);
        panelSyncManager.registerSlotGroup("tape_slot", 1);

        boolean aprilFools = guiData.getBuffer().readBoolean();
        return GTGuis.createPanel(this, 176, aprilFools ? 202 : 152)
                .child(IKey.lang(getMetaFullName())
                        .asWidget()
                        .pos(5, 5))
                .childIf(!isConfigurable && aprilFools, () -> new FlappyGreg()
                        .alignX(0.5f)
                        .top(5 + 9 + 7)
                        .size(150, 45 + 25 + 25)
                        // TODO: MUI 3.0.6 remove empty packet consumer
                        .onFinish(() -> panelSyncManager.callSyncedAction("game_finish", buf -> {})))
                .childIf(!aprilFools, () -> Flow.column()
                        .top(17)
                        .widthRel(1.0f)
                        .coverChildrenHeight()
                        .child(new ItemSlot()
                                .slot(SyncHandlers.itemSlot(tapeHandler, 0)
                                        .slotGroup("tape_slot"))
                                .background(GTGuiTextures.SLOT, GTGuiTextures.DUCT_TAPE_OVERLAY)
                                .addTooltipLine(IKey.lang("gregtech.machine.maintenance_hatch_tape_slot.tooltip")))
                        .child(new ButtonWidget<>()
                                .marginTop(4)
                                .size(20)
                                .syncHandler(maintenanceClickSync)
                                .overlay(GTGuiTextures.MAINTENANCE_ICON)
                                .addTooltipLine(IKey.lang("gregtech.machine.maintenance_hatch_tool_slot.tooltip"))))
                .childIf(isConfigurable, () -> {
                    Widget<?> durationText = IKey.lang("gregtech.maintenance.configurable_duration",
                            () -> new Object[] { String.format("%.2f", multiplierSync.getDoubleValue()) })
                            .asWidget()
                            .tooltipBuilder(tooltip -> {
                                double multiplier = multiplierSync.getDoubleValue();
                                if (multiplier == 1.0f) {
                                    tooltip.addLine(IKey
                                            .lang("gregtech.maintenance.configurable_duration.unchanged_description"));
                                } else {
                                    tooltip.addLine(
                                            IKey.lang("gregtech.maintenance.configurable_duration.changed_description",
                                                    String.format("%.2f", multiplier)));
                                }
                            });
                    Widget<?> timeText = IKey.lang("gregtech.maintenance.configurable_time",
                            () -> new Object[] {
                                    String.format("%.2f", TIME_ACTION.applyAsDouble(multiplierSync.getDoubleValue())) })
                            .asWidget()
                            .tooltipBuilder(tooltip -> {
                                double multiplier = TIME_ACTION.applyAsDouble(multiplierSync.getDoubleValue());
                                if (multiplier == 1.0f) {
                                    tooltip.addLine(
                                            IKey.lang("gregtech.maintenance.configurable_time.unchanged_description"));
                                } else {
                                    tooltip.addLine(
                                            IKey.lang("gregtech.maintenance.configurable_time.changed_description",
                                                    String.format("%.2f", multiplier)));
                                }
                            });

                    return new ParentWidget<>()
                            .pos(5, 25)
                            .coverChildren()
                            .child(durationText)
                            .child(timeText.top(14))
                            .child(new SliderWidget()
                                    .width(67 - 8)
                                    .pos(4, 27)
                                    .bounds(MIN_DURATION_MULTIPLIER, MAX_DURATION_MULTIPLIER)
                                    .stopper(SLIDER_STOPPER_STOPS)
                                    .value(new DoubleValue.Dynamic(multiplierSync::getDoubleValue, val -> {
                                        multiplierSync.setDoubleValue(val);
                                        durationText.markTooltipDirty();
                                        timeText.markTooltipDirty();
                                    }))
                                    .background(new Rectangle()
                                            .setColor(Color.BLACK.brighter(2))
                                            .asIcon()
                                            .height(2))
                                    .stopperSize(1, 4)
                                    .stopperTexture(IDrawable.EMPTY)
                                    .sliderHeight(8));
                })
                .child(SlotGroupWidget.playerInventory(false)
                        .left(7)
                        .bottom(7));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("IsTaped", isTaped);
        data.setTag("tapeInventory", tapeHandler.serializeNBT());

        if (isConfigurable) {
            data.setDouble("DurationMultiplier", durationMultiplier);
        }

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isTaped = data.getBoolean("IsTaped");

        if (data.hasKey("tapeInventory", Constants.NBT.TAG_COMPOUND)) {
            this.tapeHandler.deserializeNBT(data.getCompoundTag("tapeInventory"));
        }

        if (isConfigurable) {
            durationMultiplier = data.getDouble("DurationMultiplier");
        }

        // Legacy Inventory Handler Support
        if (data.hasKey("ImportInventory", Constants.NBT.TAG_COMPOUND)) {
            GTUtility.readItems(tapeHandler, "ImportInventory", data);
            data.removeTag("ImportInventory");
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isTaped);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isTaped = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STORE_MAINTENANCE) {
            this.maintenanceProblems = buf.readByte();
            this.timeActive = buf.readInt();
            markDirty();
        } else if (dataId == IS_TAPED) {
            this.isTaped = buf.readBoolean();
            scheduleRenderUpdate();
            markDirty();
        }
    }

    @Override
    public MultiblockAbility<IMaintenanceHatch> getAbility() {
        return MultiblockAbility.MAINTENANCE_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableMaintenance) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.disabled"));
        if (isConfigurable) {
            tooltip.add(I18n.format("gregtech.maintenance.configurable.tooltip_basic"));
            if (!TooltipHelper.isShiftDown()) {
                tooltip.add(I18n.format("gregtech.maintenance.configurable.tooltip_more_info"));
            } else {
                tooltip.add(I18n.format("gregtech.maintenance.configurable.tooltip_pss_header"));
                tooltip.add(I18n.format("gregtech.maintenance.configurable.tooltip_pss_info"));
            }
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.tool_action.tape"));
    }

    private static class TapeStackHandler extends FilteredItemHandler {

        @Nullable
        private static ItemStack tapeReference;

        public TapeStackHandler(MetaTileEntity metaTileEntity) {
            super(metaTileEntity, 1, TapeStackHandler::isStackTape);
        }

        public boolean tryConsumeTape() {
            ItemStack slotStack = getStackInSlot(0);
            // There *should* be no need to check if the stack is tape
            if (slotStack.isEmpty()) return false;
            slotStack.shrink(1);
            return true;
        }

        public static @NotNull ItemStack getTapeReference() {
            if (tapeReference == null) {
                tapeReference = MetaItems.DUCT_TAPE.getStackForm();
            }

            return tapeReference;
        }

        public static boolean isStackTape(@NotNull ItemStack itemStack) {
            return getTapeReference().isItemEqual(itemStack);
        }
    }
}
