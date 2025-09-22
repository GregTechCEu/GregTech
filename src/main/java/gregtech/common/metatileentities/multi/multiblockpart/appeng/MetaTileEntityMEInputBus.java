package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import gregtech.api.capability.IDataStickIntractable;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.widget.appeng.item.AEItemConfigSlot;
import gregtech.api.mui.widget.appeng.item.AEItemDisplaySlot;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemList;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.slot.ExportOnlyAEItemSlot;
import gregtech.common.metatileentities.multi.multiblockpart.appeng.stack.WrappedItemStack;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.layout.Grid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Arrays;
import java.util.List;

public class MetaTileEntityMEInputBus extends MetaTileEntityMEInputBase<IAEItemStack>
                                      implements IMultiblockAbilityPart<IItemHandlerModifiable>, IDataStickIntractable {

    public static final String ITEM_BUFFER_TAG = "ItemSlots";

    protected ExportOnlyAEItemList aeItemHandler;
    protected NotifiableItemStackHandler extraSlotInventory;
    private ItemHandlerList actualImportItems;

    public MetaTileEntityMEInputBus(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false, IItemStorageChannel.class);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEInputBus(metaTileEntityId, getTier());
    }

    public @NotNull ExportOnlyAEItemList getAEHandler() {
        if (aeItemHandler == null) {
            aeItemHandler = new ExportOnlyAEItemList(this, CONFIG_SIZE, this.getController());
        }

        return aeItemHandler;
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.aeItemHandler = getAEHandler();
        this.extraSlotInventory = new NotifiableItemStackHandler(this, 1, this, false);
        this.extraSlotInventory.addNotifiableMetaTileEntity(this);
        this.actualImportItems = new ItemHandlerList(
                Arrays.asList(this.aeItemHandler, this.circuitInventory, this.extraSlotInventory));
        this.importItems = this.actualImportItems;
    }

    public IItemHandlerModifiable getImportItems() {
        return this.actualImportItems;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        ItemStack extraSlotStack = extraSlotInventory.getStackInSlot(0);
        if (!extraSlotStack.isEmpty()) {
            itemBuffer.add(extraSlotStack);
        }
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        super.addToMultiBlock(controllerBase);
        for (IItemHandler handler : this.actualImportItems.getBackingHandlers()) {
            if (handler instanceof INotifiableHandler notifiable) {
                notifiable.addNotifiableMetaTileEntity(controllerBase);
                notifiable.addToNotifiedList(this, handler, false);
            }
        }
    }

    @Override
    public void removeFromMultiBlock(MultiblockControllerBase controllerBase) {
        super.removeFromMultiBlock(controllerBase);
        for (IItemHandler handler : this.actualImportItems.getBackingHandlers()) {
            if (handler instanceof INotifiableHandler notifiable) {
                notifiable.removeNotifiableMetaTileEntity(controllerBase);
            }
        }
    }

    @Override
    protected @NotNull Widget<?> createMainColumnWidget(@Range(from = 0, to = 3) int index, @NotNull PosGuiData guiData,
                                                        @NotNull PanelSyncManager panelSyncManager) {
        if (index == 3) {
            panelSyncManager.registerSlotGroup("extra_slot", 1);
            return new ItemSlot()
                    .slot(SyncHandlers.itemSlot(extraSlotInventory, 0)
                            .slotGroup("extra_slot"))
                    .addTooltipLine(IKey.lang("gregtech.gui.me_bus.extra_slot"));
        }

        return super.createMainColumnWidget(index, guiData, panelSyncManager);
    }

    @Override
    protected @NotNull Widget<?> createConfigGrid(@NotNull PosGuiData guiData,
                                                  @NotNull PanelSyncManager panelSyncManager) {
        Grid grid = new Grid()
                .pos(7, 25)
                .size(18 * 4)
                .minElementMargin(0, 0)
                .minColWidth(18)
                .minRowHeight(18)
                .matrix(Grid.mapToMatrix((int) Math.sqrt(CONFIG_SIZE), CONFIG_SIZE,
                        index -> new AEItemConfigSlot(isStocking(), index, this::isAutoPull)
                                .syncHandler(SYNC_HANDLER_NAME, 0)
                                .debugName("Index " + index)));

        for (IWidget slotUpper : grid.getChildren()) {
            ((AEItemConfigSlot) slotUpper).onSelect(() -> {
                for (IWidget slotLower : grid.getChildren()) {
                    ((AEItemConfigSlot) slotLower).deselect();
                }
            });
        }

        return grid;
    }

    @Override
    protected @NotNull Widget<?> createDisplayGrid(@NotNull PosGuiData guiData,
                                                   @NotNull PanelSyncManager panelSyncManager) {
        return new Grid()
                .pos(7 + 18 * 5, 25)
                .size(18 * 4)
                .minElementMargin(0, 0)
                .minColWidth(18)
                .minRowHeight(18)
                .matrix(Grid.mapToMatrix((int) Math.sqrt(CONFIG_SIZE), CONFIG_SIZE,
                        index -> new AEItemDisplaySlot(index)
                                .background(GTGuiTextures.SLOT_DARK)
                                .syncHandler(SYNC_HANDLER_NAME, 0)
                                .debugName("Index " + index)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);

        NBTTagList slots = new NBTTagList();
        for (int i = 0; i < CONFIG_SIZE; i++) {
            ExportOnlyAEItemSlot slot = this.getAEHandler().getInventory()[i];
            NBTTagCompound slotTag = new NBTTagCompound();
            slotTag.setInteger("slot", i);
            slotTag.setTag("stack", slot.serializeNBT());
            slots.appendTag(slotTag);
        }
        data.setTag(ITEM_BUFFER_TAG, slots);

        this.circuitInventory.write(data);
        GTUtility.writeItems(this.extraSlotInventory, "ExtraInventory", data);

        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);

        if (data.hasKey(ITEM_BUFFER_TAG, 9)) {
            NBTTagList slots = (NBTTagList) data.getTag(ITEM_BUFFER_TAG);
            for (NBTBase nbtBase : slots) {
                NBTTagCompound slotTag = (NBTTagCompound) nbtBase;
                ExportOnlyAEItemSlot slot = this.getAEHandler().getInventory()[slotTag.getInteger("slot")];
                slot.deserializeNBT(slotTag.getCompoundTag("stack"));
            }
        }

        this.circuitInventory.read(data);
        GTUtility.readItems(this.extraSlotInventory, "ExtraInventory", data);
        this.importItems = createImportItemHandler();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            if (isOnline()) {
                Textures.ME_INPUT_BUS_ACTIVE.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.ME_INPUT_BUS.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.item_import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me_import_item_hatch.configs.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.copy_paste.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.extra_connections.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this.actualImportItems);
    }

    @Override
    public final void onDataStickLeftClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("MEInputBus", writeConfigToTag());
        dataStick.setTagCompound(tag);
        dataStick.setTranslatableName("gregtech.machine.me.item_import.data_stick.name");
        player.sendStatusMessage(new TextComponentTranslation("gregtech.machine.me.import_copy_settings"), true);
    }

    protected NBTTagCompound writeConfigToTag() {
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagCompound configStacks = new NBTTagCompound();
        tag.setTag("ConfigStacks", configStacks);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            var slot = this.aeItemHandler.getInventory()[i];
            IAEItemStack config = slot.getConfig();
            if (config == null) {
                continue;
            }
            NBTTagCompound stackNbt = new NBTTagCompound();
            config.getDefinition().writeToNBT(stackNbt);
            configStacks.setTag(Integer.toString(i), stackNbt);
        }

        tag.setByte("GhostCircuit", (byte) this.circuitInventory.getCircuitValue());

        tag.setInteger(REFRESH_RATE_TAG, this.refreshRate);

        return tag;
    }

    @Override
    public final boolean onDataStickRightClick(EntityPlayer player, ItemStack dataStick) {
        NBTTagCompound tag = dataStick.getTagCompound();
        if (tag == null || !tag.hasKey("MEInputBus")) {
            return false;
        }
        readConfigFromTag(tag.getCompoundTag("MEInputBus"));
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
                    this.aeItemHandler.getInventory()[i].setConfig(WrappedItemStack.fromNBT(configTag));
                } else {
                    this.aeItemHandler.getInventory()[i].setConfig(null);
                }
            }
        }

        if (tag.hasKey("GhostCircuit")) {
            this.setGhostCircuitConfig(tag.getByte("GhostCircuit"));
        }

        if (tag.hasKey(REFRESH_RATE_TAG)) {
            this.refreshRate = tag.getInteger(REFRESH_RATE_TAG);
        }
    }
}
