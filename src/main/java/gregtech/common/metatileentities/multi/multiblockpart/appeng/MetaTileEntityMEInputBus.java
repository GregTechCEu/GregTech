package gregtech.common.metatileentities.multi.multiblockpart.appeng;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.GridAccessException;
import appeng.util.item.AEItemStack;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.INotifiableHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.gui.widget.appeng.AEItemConfigWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author GlodBlock
 * @Description The Input Bus that can auto fetch item ME storage network.
 * @Date 2023/4/22-13:34
 */
public class MetaTileEntityMEInputBus extends MetaTileEntityAEHostablePart implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    public final static String ITEM_BUFFER_TAG = "ItemSlots";
    public final static String WORKING_TAG = "WorkingEnabled";
    private final static int CONFIG_SIZE = 16;
    private boolean workingEnabled;
    private ExportOnlyAEItem[] aeItemSlots;

    public MetaTileEntityMEInputBus(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.UHV, true);
        this.workingEnabled = true;
    }

    @Override
    protected void initializeInventory() {
        this.aeItemSlots = new ExportOnlyAEItem[CONFIG_SIZE];
        for (int i = 0; i < CONFIG_SIZE; i ++) {
            this.aeItemSlots[i] = new ExportOnlyAEItem(null, null, this);
        }
        super.initializeInventory();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && this.workingEnabled && this.shouldSyncME()) {
            if (this.updateMEStatus()) {
                try {
                    IMEMonitor<IAEItemStack> aeNetwork = this.getProxy().getStorage().getInventory(ITEM_NET);
                    for (ExportOnlyAEItem aeSlot : this.aeItemSlots) {
                        // Try to clear the wrong item
                        IAEItemStack exceedItem = aeSlot.exceedStack();
                        if (exceedItem != null) {
                            long total = exceedItem.getStackSize();
                            IAEItemStack notInserted = aeNetwork.injectItems(exceedItem, Actionable.MODULATE, this.getActionSource());
                            if (notInserted != null && notInserted.getStackSize() > 0) {
                                aeSlot.extractItem(0, (int) (total - notInserted.getStackSize()), false);
                                continue;
                            } else {
                                aeSlot.extractItem(0, (int) total, false);
                            }
                        }
                        // Fill it
                        IAEItemStack reqItem = aeSlot.requestStack();
                        if (reqItem != null) {
                            IAEItemStack extracted = aeNetwork.extractItems(reqItem, Actionable.MODULATE, this.getActionSource());
                            if (extracted != null) {
                                aeSlot.addStack(extracted);
                            }
                        }
                    }
                } catch (GridAccessException ignore) {
                }
            }
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMEInputBus(this.metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI
                .builder(GuiTextures.BACKGROUND, 176, 18 + 18 * 4 + 94)
                .label(10, 5, getMetaFullName());
        // ME Network status
        builder.dynamicLabel(10, 15, () -> this.isOnline ?
                        I18n.format("gregtech.gui.me_network.online") :
                        I18n.format("gregtech.gui.me_network.offline"),
                0xFFFFFFFF);

        // Config slots
        builder.widget(new AEItemConfigWidget(16, 25, this.aeItemSlots));

        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * 4 + 12);
        return builder.build(this.getHolder(), entityPlayer);
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
        buf.writeBoolean(workingEnabled);
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
        NBTTagList slots = new NBTTagList();
        for (int i = 0; i < CONFIG_SIZE; i ++) {
            ExportOnlyAEItem slot = this.aeItemSlots[i];
            NBTTagCompound slotTag = new NBTTagCompound();
            slotTag.setInteger("slot", i);
            slotTag.setTag("stack", slot.serializeNBT());
            slots.appendTag(slotTag);
        }
        data.setTag(ITEM_BUFFER_TAG, slots);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey(WORKING_TAG)) {
            this.workingEnabled = data.getBoolean(WORKING_TAG);
        }
        if (data.hasKey(ITEM_BUFFER_TAG, 9)) {
            NBTTagList slots = (NBTTagList) data.getTag(ITEM_BUFFER_TAG);
            for (NBTBase nbtBase : slots) {
                NBTTagCompound slotTag = (NBTTagCompound) nbtBase;
                ExportOnlyAEItem slot = this.aeItemSlots[slotTag.getInteger("slot")];
                slot.deserializeNBT(slotTag.getCompoundTag("stack"));
            }
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.shouldRenderOverlay()) {
            Textures.ME_INPUT_BUS.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @Nonnull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.item_bus.import.tooltip"));
        tooltip.add(I18n.format("gregtech.machine.me.item_import.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.enabled"));
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.IMPORT_ITEMS;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> list) {
        list.addAll(Arrays.asList(this.aeItemSlots));
    }

    public static class ExportOnlyAEItem extends ExportOnlyAESlot<IAEItemStack> implements IItemHandlerModifiable, INotifiableHandler {
        private final List<MetaTileEntity> notifiableEntities = new ArrayList<>();

        public ExportOnlyAEItem(IAEItemStack config, IAEItemStack stock, MetaTileEntity mte) {
            super(config, stock);
            this.notifiableEntities.add(mte);
        }

        public ExportOnlyAEItem() {
            super();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt.hasKey(CONFIG_TAG)) {
                this.config = AEItemStack.fromNBT(nbt.getCompoundTag(CONFIG_TAG));
            }
            if (nbt.hasKey(STOCK_TAG)) {
                this.stock = AEItemStack.fromNBT(nbt.getCompoundTag(STOCK_TAG));
            }
        }

        @Override
        public void addNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.notifiableEntities.add(metaTileEntity);
        }

        @Override
        public void removeNotifiableMetaTileEntity(MetaTileEntity metaTileEntity) {
            this.notifiableEntities.remove(metaTileEntity);
        }

        private void trigger() {
            for (MetaTileEntity metaTileEntity : this.notifiableEntities) {
                if (metaTileEntity != null && metaTileEntity.isValid()) {
                    this.addToNotifiedList(metaTileEntity, this, true);
                }
            }
        }

        @Override
        public ExportOnlyAEItem copy() {
            return new ExportOnlyAEItem(
                    this.config == null ? null : this.config.copy(),
                    this.stock == null ? null : this.stock.copy(),
                    null
            );
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            // NO-OP
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == 0 && this.stock != null) {
                return this.stock.createItemStack();
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == 0 && this.stock != null) {
                int extracted = (int) Math.min(this.stock.getStackSize(), amount);
                ItemStack result = this.stock.createItemStack();
                result.setCount(extracted);
                if (!simulate) {
                    this.stock.decStackSize(extracted);
                    if (this.stock.getStackSize() == 0) {
                        this.stock = null;
                    }
                    trigger();
                }
                return result;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return Integer.MAX_VALUE;
        }
    }

}
