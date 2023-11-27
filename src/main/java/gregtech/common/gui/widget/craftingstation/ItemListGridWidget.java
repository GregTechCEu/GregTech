package gregtech.common.gui.widget.craftingstation;

import gregtech.api.gui.INativeWidget;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ScrollableListWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.common.inventory.IItemInfo;
import gregtech.common.inventory.IItemList;
import gregtech.common.inventory.IItemList.InsertMode;
import gregtech.common.inventory.SimpleItemInfo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class ItemListGridWidget extends ScrollableListWidget {

    private static final Comparator<IItemInfo> COMPARATOR = Comparator.comparing(
            IItemInfo::getItemStack,
            Comparator.<ItemStack>comparingInt(it -> Item.REGISTRY.getIDForObject(it.getItem()))
                    .thenComparing(ItemStack::getItemDamage)
                    .thenComparing(ItemStack::hasTagCompound)
                    .thenComparing(it -> -Objects.hashCode(it.getTagCompound()))
                    .thenComparing(it -> -it.getCount()));

    @Nullable
    private final IItemList itemList;
    private final int slotAmountX;
    private final int slotAmountY;
    private int slotRowsAmount = 0;
    private final Map<ItemStack, SimpleItemInfo> cachedItemList = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());
    private final List<SimpleItemInfo> itemsChanged = new ArrayList<>();
    private final List<ItemStack> itemsRemoved = new ArrayList<>();

    private final List<SimpleItemInfo> displayItemList = new ArrayList<>();

    public ItemListGridWidget(int x, int y, int slotsX, int slotsY, @Nullable IItemList itemList) {
        super(x, y, slotsX * 18 + 10, slotsY * 18);
        this.itemList = itemList;
        this.slotAmountX = slotsX;
        this.slotAmountY = slotsY;
    }

    @Nullable
    public IItemList getItemList() {
        return itemList;
    }

    @Nullable
    public IItemInfo getItemInfoAt(int index) {
        return displayItemList.size() > index ? displayItemList.get(index) : null;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        if (!result && isShiftDown()) {
            INativeWidget hoveredSlot = findHoveredSlot(mouseX, mouseY);
            if (hoveredSlot != null) {
                dispatchOtherSlotShiftClick(hoveredSlot);
                return true;
            }
        }
        return result;
    }

    private void dispatchOtherSlotShiftClick(INativeWidget clickedSlot) {
        ItemStack stackInSlot = clickedSlot.getHandle().getStack();
        if (!stackInSlot.isEmpty()) {
            writeClientAction(4, buf -> buf.writeVarInt(clickedSlot.getHandle().slotNumber));
        }
    }

    private void handleSlotShiftClick(INativeWidget clickedSlot) {
        ItemStack itemStack = clickedSlot.getHandle().getStack();
        if (clickedSlot.getHandle().canTakeStack(gui.entityPlayer) && !itemStack.isEmpty()) {
            itemStack = clickedSlot.onItemTake(gui.entityPlayer, itemStack, true);
            int amountInserted = getItemList().insertItem(itemStack, itemStack.getCount(), false,
                    InsertMode.LOWEST_PRIORITY);
            if (amountInserted > 0) {
                clickedSlot.onItemTake(gui.entityPlayer, itemStack, false);
                itemStack.shrink(amountInserted);
                if (!clickedSlot.canMergeSlot(itemStack)) {
                    gui.entityPlayer.dropItem(itemStack.copy(), false, false);
                    itemStack.setCount(0);
                }
                clickedSlot.getHandle().onSlotChanged();
                uiAccess.sendSlotUpdate(clickedSlot);
                gui.entityPlayer.openContainer.detectAndSendChanges();
            }
        }
    }

    private void addSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            int widgetAmount = widgets.size();
            WidgetGroup widgetGroup = new WidgetGroup();
            for (int j = 0; j < slotAmountX; j++) {
                Widget widget = new ItemListSlotWidget(j * 18, 0, this, widgetAmount * slotAmountX + j);
                widgetGroup.addWidget(widget);
            }
            addWidget(widgetGroup);
        }
    }

    private void removeSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            Widget slotWidget = widgets.remove(widgets.size() - 1);
            removeWidget(slotWidget);
        }
    }

    private void modifySlotRows(int delta) {
        if (delta > 0) {
            addSlotRows(delta);
        } else {
            removeSlotRows(delta);
        }
    }

    private void checkItemListForChanges() {
        Iterator<ItemStack> iterator = cachedItemList.keySet().iterator();
        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next();
            if (!itemList.hasItemStored(itemStack)) {
                iterator.remove();
                itemsRemoved.add(itemStack);
            }
        }
        for (ItemStack itemStack : itemList.getStoredItems()) {
            IItemInfo itemInfo = itemList.getItemInfo(itemStack);
            if (itemInfo == null)
                continue;

            if (!cachedItemList.containsKey(itemStack)) {
                SimpleItemInfo lookupInfo = new SimpleItemInfo(itemStack);
                int totalAmount = itemInfo.getTotalItemAmount();

                if (totalAmount == 0) {
                    itemsRemoved.add(itemStack);
                } else {
                    lookupInfo.setTotalItemAmount(totalAmount);
                    cachedItemList.put(itemStack, lookupInfo);
                    itemsChanged.add(lookupInfo);
                }
            } else {
                SimpleItemInfo cachedItemInfo = cachedItemList.get(itemStack);
                if (cachedItemInfo.getTotalItemAmount() != itemInfo.getTotalItemAmount()) {
                    cachedItemInfo.setTotalItemAmount(itemInfo.getTotalItemAmount());
                    itemsChanged.add(cachedItemInfo);
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (itemList == null) return;
        int amountOfItemTypes = itemList.getStoredItems().size();
        int slotRowsRequired = Math.max(slotAmountY, (int) Math.ceil(amountOfItemTypes / (slotAmountX * 1.0)));
        if (slotRowsAmount != slotRowsRequired) {
            int slotsToAdd = slotRowsRequired - slotRowsAmount;
            this.slotRowsAmount = slotRowsRequired;
            writeUpdateInfo(2, buf -> buf.writeVarInt(slotsToAdd));
            modifySlotRows(slotsToAdd);
        }

        this.itemsChanged.clear();
        this.itemsRemoved.clear();
        checkItemListForChanges();
        if (!itemsChanged.isEmpty() || !itemsRemoved.isEmpty()) {
            writeUpdateInfo(3, buf -> {
                buf.writeVarInt(itemsRemoved.size());
                for (ItemStack stack : itemsRemoved) {
                    buf.writeItemStack(stack);
                }
                buf.writeVarInt(itemsChanged.size());
                for (SimpleItemInfo itemInfo : itemsChanged) {
                    buf.writeItemStack(itemInfo.getItemStack());
                    buf.writeVarInt(itemInfo.getTotalItemAmount());
                }
            });
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            int slotsToAdd = buffer.readVarInt();
            modifySlotRows(slotsToAdd);
        }
        if (id == 3) {
            try {
                int itemsRemoved = buffer.readVarInt();
                for (int i = 0; i < itemsRemoved; i++) {
                    ItemStack itemStack = buffer.readItemStack();
                    this.displayItemList.removeIf(
                            it -> ItemStackHashStrategy.comparingAllButCount().equals(it.getItemStack(), itemStack));
                }
                int itemsChanged = buffer.readVarInt();
                for (int i = 0; i < itemsChanged; i++) {
                    ItemStack itemStack = buffer.readItemStack();
                    int newTotalAmount = buffer.readVarInt();
                    SimpleItemInfo itemInfo = displayItemList.stream()
                            .filter(it -> ItemStackHashStrategy.comparingAllButCount().equals(it.getItemStack(),
                                    itemStack))
                            .findAny()
                            .orElse(null);
                    if (itemInfo == null) {
                        itemInfo = new SimpleItemInfo(itemStack);
                        this.displayItemList.add(itemInfo);
                    }
                    itemInfo.setTotalItemAmount(newTotalAmount);
                }
                this.displayItemList.sort(COMPARATOR);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 4) {
            INativeWidget clickedSlot = findSlotByNumber(buffer.readVarInt());
            if (clickedSlot != null) {
                handleSlotShiftClick(clickedSlot);
            }
        }
    }

    @Nullable
    private INativeWidget findHoveredSlot(int mouseX, int mouseY) {
        return gui.guiWidgets.values().stream()
                .flatMap(it -> it.getNativeWidgets().stream())
                .filter(it -> it.getHandle().isEnabled())
                .filter(it -> it.getHandle().canTakeStack(gui.entityPlayer))
                .filter(it -> ((Widget) it).isMouseOverElement(mouseX, mouseY))
                .findFirst().orElse(null);
    }

    @Nullable
    private INativeWidget findSlotByNumber(int slotNumber) {
        return gui.guiWidgets.values().stream()
                .flatMap(it -> it.getNativeWidgets().stream())
                .filter(it -> it.getHandle().slotNumber == slotNumber)
                .findFirst().orElse(null);
    }
}
