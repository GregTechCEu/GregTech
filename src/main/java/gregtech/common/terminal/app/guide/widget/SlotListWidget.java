package gregtech.common.terminal.app.guide.widget;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.terminal.gui.widgets.DraggableScrollableWidgetGroup;
import gregtech.common.terminal.app.guideeditor.widget.configurator.ItemStackConfigurator;
import gregtech.api.util.Size;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SlotListWidget extends GuideWidgetGroup {
    public final static String NAME = "slots";

    // config
    public List<ItemStackInfo> item_list;

    @Override
    public Widget initFixed() {
        this.clearAllWidgets();
        ItemStackHandler itemStackHandler = new ItemStackHandler(item_list.size());
        IGuiTexture background = new ColorRectTexture(0x4f000000);
        int size = item_list.size();
        int maxXSize = getSize().width / 18;
        int xPos;
        if (maxXSize < 1) {
            maxXSize = 1;
            xPos = 0;
        } else {
            xPos = (getSize().width - (Math.min(size, maxXSize)) * 18) / 2;
        }
        int maxYSize = size / maxXSize + ((size % maxXSize) == 0 ? 0 : 1);
        for (int y = 0; y <= size / maxXSize; y++) {
            for (int x = 0; x < maxXSize; x++) {
                int i = x + y * maxXSize;
                if (i < size) {
                    itemStackHandler.setStackInSlot(i, item_list.get(i).getInstance());
                    SlotWidget widget = new SlotWidget(itemStackHandler, i, xPos + x * 18, y * 18, false, false);
                    widget.setBackgroundTexture(background);
                    this.addWidget(widget);
                }
            }
        }
        setSize(new Size(getSize().width / 18 > 0 ? getSize().width : 18, maxYSize * 18));
        return this;
    }

    @Override
    public String getRegistryName() {
        return NAME;
    }

    @Override
    public JsonObject getTemplate(boolean isFixed) {
        JsonObject template = super.getTemplate(isFixed);
        template.add("item_list", new Gson().toJsonTree(Collections.singletonList(new ItemStackInfo("minecraft:ender_pearl", 0, 1))));
        return template;
    }

    @Override
    public void loadConfigurator(DraggableScrollableWidgetGroup group, JsonObject config, boolean isFixed, Consumer<String> needUpdate) {
        super.loadConfigurator(group, config, isFixed, needUpdate);
        group.addWidget(new ItemStackConfigurator(group, config, "item_list").setOnUpdated(needUpdate));
    }

    public static class ItemStackInfo {
        // config
        public String id;
        public int damage;
        public int count = 1;

        private transient ItemStack itemStack;

        public ItemStackInfo() {

        }

        public void update(ItemStack itemStack) {
            ResourceLocation resourceLocation = itemStack.getItem().getRegistryName();
            id = resourceLocation == null ? "minecraft:air" : resourceLocation.toString();
            damage = itemStack.getItemDamage();
            count = itemStack.getCount();
        }

        public ItemStackInfo(String id, int damage, int count) {
            this.id = id;
            this.damage = damage;
            this.count = count;
        }

        public ItemStack getInstance() {
            if (itemStack == null && id != null) {
                Item item = Item.getByNameOrId(id);
                if (item == null) {
                    itemStack = ItemStack.EMPTY;
                    return itemStack;
                }
                itemStack = item.getDefaultInstance();
                itemStack.setCount(count);
                itemStack.setItemDamage(damage);
            }
            return itemStack == null ? ItemStack.EMPTY : itemStack;
        }
    }
}
