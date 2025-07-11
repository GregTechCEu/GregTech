package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.IMaintenanceHatch;
import gregtech.api.capability.impl.FilteredItemHandler;
import gregtech.api.items.toolitem.ItemGTToolbelt;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.util.GTUtility;
import gregtech.api.util.function.FloatUnaryOperator;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
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
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

import static gregtech.api.capability.GregtechDataCodes.*;

public class MetaTileEntityMaintenanceHatch extends MetaTileEntityMultiblockPart
                                            implements IMultiblockAbilityPart<IMaintenanceHatch>, IMaintenanceHatch {

    private final boolean isConfigurable;
    private TapeStackHandler tapeHandler;
    private boolean isTaped;

    // Used to store state temporarily if the Controller is broken
    private byte maintenanceProblems = -1;
    private int timeActive = -1;

    // Some stats used for the Configurable Maintenance Hatch
    private float durationMultiplier = 1.0f;
    private static final float MAX_DURATION_MULTIPLIER = 1.1f;
    private static final float MIN_DURATION_MULTIPLIER = 0.9f;
    private static final float DURATION_ACTION_AMOUNT = 0.01f;
    private static final FloatUnaryOperator TIME_ACTION = t -> {
        if (t < 1.0f) {
            return -20.0f * t + 21.0f;
        } else {
            return -8.0f * t + 9.0f;
        }
    };

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
            MultiblockControllerBase controller = getController();
            if (controller instanceof IMaintenance iMaintenance) {
                if (iMaintenance.hasMaintenanceProblems()) {
                    if (tapeHandler.tryConsumeTape()) {
                        iMaintenance.fixAllMaintenance();
                        setTaped(true);
                    }
                }
            }
        }
    }

    /**
     * Fixes the maintenance problems of this hatch's Multiblock Controller
     * 
     * @param player the player performing the fixing
     */
    private void fixMaintenanceProblems(@NotNull EntityPlayer player) {
        if (!(this.getController() instanceof IMaintenance iMaintenance)) {
            return;
        }

        if (!iMaintenance.hasMaintenanceProblems()) {
            return;
        }

        // Fix automatically on slot click by player in Creative Mode
        if (player.capabilities.isCreativeMode) {
            iMaintenance.fixAllMaintenance();
            return;
        }

        // Then for every slot in the player's main inventory, try to duct tape fix
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            if (consumeDuctTape(player.inventory.mainInventory.get(i), true)) {
                iMaintenance.fixAllMaintenance();
                setTaped(true);
                return;
            }
        }

        // Lastly for each problem the multi has, try to fix with tools
        String[] toolsToMatch = new String[6];
        if (!IMaintenance.getToolsForMaintenance(iMaintenance.getMaintenanceProblems(), toolsToMatch)) {
            return;
        }

        ItemStack heldStack = player.inventory.getItemStack();
        if (!heldStack.isEmpty()) {
            handleItemFix(heldStack, toolsToMatch, player, iMaintenance);
        }

        for (ItemStack itemStack : player.inventory.mainInventory) {
            if (itemStack.isEmpty()) continue;
            handleItemFix(heldStack, toolsToMatch, player, iMaintenance);
        }
    }

    private void handleItemFix(@NotNull ItemStack itemStack,
                               @Nullable String @NotNull [] toolsToMatch, @NotNull EntityPlayer player,
                               @NotNull IMaintenance iMaintenance) {
        if (itemStack.getItem() instanceof ItemGTToolbelt toolbelt) {
            for (int index = 0; index < toolsToMatch.length; index++) {
                String toolToMatch = toolsToMatch[index];
                if (toolToMatch == null) continue;
                if (toolbelt.damageAgainstMaintenanceProblem(itemStack, player, toolToMatch)) {
                    iMaintenance.setMaintenanceFixed(index);
                    toolsToMatch[index] = null;
                    setTaped(false);
                }
            }
        } else {
            for (int index = 0; index < toolsToMatch.length; index++) {
                String toolToMatch = toolsToMatch[index];
                if (toolToMatch == null) continue;
                if (ToolHelper.isTool(itemStack, toolToMatch)) {
                    iMaintenance.setMaintenanceFixed(index);
                    ToolHelper.damageItemWhenCrafting(itemStack, player);
                    toolsToMatch[index] = null;
                    setTaped(false);
                }
            }
        }
    }

    @ApiStatus.Internal
    public void fixMaintenanceProblemsWithToolbelt(@NotNull Predicate<String> toolCheck) {
        String[] toolsToMatch = new String[6];
        IMaintenance iMaintenance = (IMaintenance) getController();
        if (!IMaintenance.getToolsForMaintenance(iMaintenance.getMaintenanceProblems(), toolsToMatch)) {
            return;
        }

        for (int index = 0; index < toolsToMatch.length; index++) {
            String toolToMatch = toolsToMatch[index];
            if (toolToMatch == null) continue;
            if (toolCheck.test(toolToMatch)) {
                iMaintenance.setMaintenanceFixed(index);
                toolsToMatch[index] = null;
                setTaped(false);
            }
        }
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
    public float getDurationMultiplier() {
        return durationMultiplier;
    }

    @Override
    public float getTimeMultiplier() {
        return TIME_ACTION.apply(durationMultiplier);
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
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        BooleanSyncValue wiringMinigameSync = new BooleanSyncValue(GTValues.FOOLS);
        panelSyncManager.syncValue("wiringMinigame", 0, wiringMinigameSync);
        InteractionSyncHandler maintenanceClickSync = new InteractionSyncHandler()
                .setOnMousePressed(mouse -> {
                    if (panelSyncManager.isClient()) return;
                    fixMaintenanceProblems(guiData.getPlayer());
                });
        panelSyncManager.registerSlotGroup("tape_slot", 1);

        return GTGuis.createPanel(this, 176, 152)
                .child(IKey.lang(getMetaFullName())
                        .asWidget()
                        .pos(5, 5))
                // TODO: amongus
                // .childIf(!isConfigurable && wiringMinigame.getBoolValue(), () -> new FixWiringTaskWidgetButMUI2())
                .childIf(!wiringMinigameSync.getBoolValue(), () -> Flow.column()
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
                .childIf(isConfigurable, () -> new ParentWidget<>()
                        .pos(5, 25)
                        .coverChildren())
                .child(SlotGroupWidget.playerInventory()
                        .left(7)
                        .bottom(7));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("IsTaped", isTaped);
        data.setTag("tapeInventory", tapeHandler.serializeNBT());

        if (isConfigurable) {
            data.setFloat("DurationMultiplier", durationMultiplier);
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
            if (data.hasKey("DurationMultiplier", Constants.NBT.TAG_DOUBLE)) {
                durationMultiplier = (float) data.getDouble("DurationMultiplier");
            } else {
                durationMultiplier = data.getFloat("DurationMultiplier");
            }
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
