package gregtech.common.metatileentities.multi.multiblockpart.fission;

import gregtech.api.GregTechAPI;
import gregtech.api.fission.component.FissionComponentData;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MTEFissionItemComponent<T extends FissionComponentData> extends AbstractMTEFissionComponent<T> {

    private @Nullable ItemStack stored;
    protected int durability;

    public MTEFissionItemComponent(@NotNull ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    /**
     * @return {@code T.class}
     */
    protected abstract @NotNull Class<T> getDataClass();

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        if (this.componentData == null && !locked) {
            for (IItemHandlerModifiable notified : getNotifiedItemInputList()) {
                ItemStack stack = notified.extractItem(0, 1, true);
                if (!stack.isEmpty()) {
                    this.componentData = GregTechAPI.FISSION_COMPONENT_REGISTRY.getData(getDataClass(), stack);

                    if (componentData != null && acceptsComponentData()) {
                        this.stored = notified.extractItem(0, 1, false);
                        NBTTagCompound tag = stored.getTagCompound();
                        if (tag != null && tag.hasKey("durability")) {
                            this.durability = tag.getInteger("durability");
                        } else {
                            this.durability = dataDurability();
                        }
                        onDataSet(tag);
                        break;
                    }
                }
            }
            getNotifiedItemInputList().clear();
        }
    }

    /**
     * @return if the component data can be accepted
     * @throws AssertionError if {@link #componentData} is {@code null}.
     */
    protected boolean acceptsComponentData() {
        return true;
    }

    /**
     * @param tag the tag providing additional data
     * @throws AssertionError if {@link #componentData} is {@code null}.
     */
    protected void onDataSet(@Nullable NBTTagCompound tag) {}

    @Override
    public void reduceDurability(int amount) {
        this.durability -= amount;
        if (durability <= 0) {
            assert stored != null;
            onDurabilityDepleted(stored);

            ItemStack stack = importItems.getStackInSlot(0);
            if (stack.isEmpty()) {
                clearStored();
            } else if (stack.getCount() == 1) {
                importItems.setStackInSlot(0, ItemStack.EMPTY);
                clearStored();
            } else {
                if (componentData != null && extractExtra()) {
                    this.durability += dataDurability();
                }
            }
        }
    }

    /**
     * @return the durability stored in {@link #componentData}.
     * @throws AssertionError if {@link #componentData} is {@code null}.
     */
    protected abstract int dataDurability();

    /**
     * @param stack the stack which had its durability depleted
     * @throws AssertionError if {@link #componentData} is {@code null}.
     */
    protected void onDurabilityDepleted(@NotNull ItemStack stack) {}

    /**
     * Extract an extra item to refill durability
     *
     * @return if extraction was successful
     * @throws AssertionError if {@link #componentData} is {@code null}.
     */
    protected boolean extractExtra() {
        if (!importItems.extractItem(0, 1, true).isEmpty()) {
            importItems.extractItem(0, 1, false);
            return true;
        }
        return false;
    }

    private void clearStored() {
        if (!locked && componentData != null) {
            this.componentData = null;
        }
    }

    @Override
    public int durability() {
        return durability;
    }

    @Override
    public void clearMachineInventory(@NotNull List<@NotNull ItemStack> itemBuffer) {
        super.clearMachineInventory(itemBuffer);
        if (stored != null && componentData != null && durability < dataDurability()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("durability", durability);
            stored.setTagCompound(tag);
            itemBuffer.add(stored);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("durability", durability);
        if (stored != null && !stored.isEmpty()) {
            NBTTagCompound tag = stored.serializeNBT();
            data.setTag("stored", tag);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.durability = data.getInteger("durability");
        if (data.hasKey("stored")) {
            this.stored = new ItemStack(data.getCompoundTag("stored"));
            this.componentData = GregTechAPI.FISSION_COMPONENT_REGISTRY.getData(getDataClass(), stored);
        }
    }
}
