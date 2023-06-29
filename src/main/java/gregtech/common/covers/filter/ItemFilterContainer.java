package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.util.IDirtyNotifiable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class ItemFilterContainer implements INBTSerializable<NBTTagCompound> {

    private final ItemStackHandler filterInventory;
    private final ItemFilterWrapper filterWrapper;
    private int maxStackSizeLimit = 1;
    private int transferStackSize;

    public ItemFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        this.filterWrapper = new ItemFilterWrapper(dirtyNotifiable);
        this.filterWrapper.setOnFilterInstanceChange(this::onFilterInstanceChange);
        this.filterInventory = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return FilterTypeRegistry.getItemFilterForStack(stack) != null;
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onLoad() {
                onFilterSlotChange(false);
            }

            @Override
            protected void onContentsChanged(int slot) {
                onFilterSlotChange(true);
            }
        };
    }

    public ItemStackHandler getFilterInventory() {
        return filterInventory;
    }

    public ItemFilterWrapper getFilterWrapper() {
        return filterWrapper;
    }

    private void onFilterInstanceChange() {
        this.filterWrapper.setMaxStackSize(getTransferStackSize());
    }

    public int getMaxStackSize() {
        return maxStackSizeLimit;
    }

    public int getTransferStackSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxStackSize();
        }
        return transferStackSize;
    }

    public void setTransferStackSize(int transferStackSize) {
        this.transferStackSize = MathHelper.clamp(transferStackSize, 1, getMaxStackSize());
        this.filterWrapper.setMaxStackSize(getTransferStackSize());
    }

    public void adjustTransferStackSize(int amount) {
        setTransferStackSize(transferStackSize + amount);
    }

    public void initUI(int y, Consumer<Widget> widgetGroup) {
        widgetGroup.accept(new LabelWidget(10, y, "cover.conveyor.item_filter.title"));
        widgetGroup.accept(new SlotWidget(filterInventory, 0, 10, y + 15)
                .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));


        this.filterWrapper.initUI(y + 15, widgetGroup);
        this.filterWrapper.blacklistUI(y + 15, widgetGroup, () -> true);
/*
        ServerWidgetGroup stackSizeGroup = new ServerWidgetGroup(this::showGlobalTransferLimitSlider);
        stackSizeGroup.addWidget(new ImageWidget(139, 70, 35, 20, GuiTextures.DISPLAY));

        stackSizeGroup.addWidget(new IncrementButtonWidget(146, 70, 20, 20, 1, 8, 64, 512, this::adjustTransferStackSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));
        stackSizeGroup.addWidget(new IncrementButtonWidget(91, 70, 20, 20, -1, -8, -64, -512, this::adjustTransferStackSize)
                .setDefaultTooltip()
                .setTextScale(0.7f)
                .setShouldClientCallback(false));

        stackSizeGroup.addWidget(new TextFieldWidget2(113, 75, 31, 20, () -> String.valueOf(transferStackSize), val -> {
                    if (val != null && !val.isEmpty())
                        setTransferStackSize(Integer.parseInt(val));
                })
                        .setAllowedChars(TextFieldWidget2.NATURAL_NUMS)
                        .setMaxLength(4)
                        .setValidator(getTextFieldValidator(() -> Integer.MAX_VALUE))
                        .setScale(0.9f)
        );


        widgetGroup.accept(stackSizeGroup);

        this.filterWrapper.initUI(y + 15, widgetGroup);
*/
    }

    protected void onFilterSlotChange(boolean notify) {
        ItemStack filterStack = filterInventory.getStackInSlot(0);
        ItemFilter newItemFilter = FilterTypeRegistry.getItemFilterForStack(filterStack);
        ItemFilter currentItemFilter = filterWrapper.getItemFilter();
        if (newItemFilter == null) {
            if (currentItemFilter != null) {
                filterWrapper.setItemFilter(null);
                filterWrapper.setBlacklistFilter(false);
                if (notify) filterWrapper.onFilterInstanceChange();
            }
        } else if (currentItemFilter == null ||
                newItemFilter.getClass() != currentItemFilter.getClass()) {
            filterWrapper.setItemFilter(newItemFilter);
            if (notify) filterWrapper.onFilterInstanceChange();
        }
    }

    public void setMaxStackSize(int maxStackSizeLimit) {
        this.maxStackSizeLimit = maxStackSizeLimit;
        setTransferStackSize(transferStackSize);
    }

    public boolean showGlobalTransferLimitSlider() {
        return getMaxStackSize() > 1 && filterWrapper.showGlobalTransferLimitSlider();
    }

    public int getSlotTransferLimit(Object slotIndex) {
        return filterWrapper.getSlotTransferLimit(slotIndex, getTransferStackSize());
    }

    public Object matchItemStack(ItemStack itemStack) {
        return filterWrapper.matchItemStack(itemStack);
    }

    public Object matchItemStack(ItemStack itemStack, boolean whitelist) {
        return filterWrapper.matchItemStack(itemStack, whitelist);
    }

    public boolean testItemStack(ItemStack itemStack) {
        return matchItemStack(itemStack) != null;
    }

    public boolean testItemStack(ItemStack itemStack, boolean whitelist) {
        return matchItemStack(itemStack, whitelist) != null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setTag("FilterInventory", filterInventory.serializeNBT());
        tagCompound.setBoolean("IsBlacklist", filterWrapper.isBlacklistFilter());
        tagCompound.setInteger("MaxStackSize", maxStackSizeLimit);
        tagCompound.setInteger("TransferStackSize", transferStackSize);
        if (filterWrapper.getItemFilter() != null) {
            NBTTagCompound filterInventory = new NBTTagCompound();
            filterWrapper.getItemFilter().writeToNBT(filterInventory);
            tagCompound.setTag("Filter", filterInventory);
        }
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tagCompound) {
        this.filterInventory.deserializeNBT(tagCompound.getCompoundTag("FilterInventory"));
        this.filterWrapper.setBlacklistFilter(tagCompound.getBoolean("IsBlacklist"));
        setMaxStackSize(tagCompound.getInteger("MaxStackSize"));
        setTransferStackSize(tagCompound.getInteger("TransferStackSize"));
        if (filterWrapper.getItemFilter() != null) {
            this.filterWrapper.getItemFilter().readFromNBT(tagCompound.getCompoundTag("Filter"));
        }
    }

}
