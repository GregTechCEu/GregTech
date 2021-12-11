package gregtech.common.terminal.app.guide;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.common.items.MetaItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemGuideApp extends GuideApp<ItemGuideApp.GuideItem> {

    public ItemGuideApp() {
        super("items", new ItemStackTexture(MetaItems.PROSPECTOR_LV.getStackForm()));
    }

    @Override
    protected String itemName(GuideItem item) {
        return item.stack.getDisplayName();
    }

    @Override
    protected String rawItemName(GuideItem item) {
        if (item.stack.getItem() instanceof MetaItem) {
            return ((MetaItem<?>) item.stack.getItem()).getItem((short) item.stack.getMetadata()).unlocalizedName;
        }
        return item.stack.getTranslationKey();
    }

    @Override
    protected IGuiTexture itemIcon(GuideItem item) {
        return new ItemStackTexture(item.stack);
    }

    @Override
    public GuideItem ofJson(JsonObject json) {
        return GuideItem.ofJson(json);
    }

    public static class GuideItem {
        public final ItemStack stack;
        public final String name;

        public GuideItem(ItemStack stack, String name) {
            this.stack = stack;
            this.name = name;
        }

        public GuideItem(ItemStack stack) {
            this(stack, stack.getItem().getRegistryName().toString() + ":" + stack.getMetadata());
        }

        public GuideItem(MetaItem<?>.MetaValueItem item) {
            this(item.getStackForm(), item.unlocalizedName);
        }

        public static GuideItem ofJson(JsonObject json) {
            if (json.has("item")) {
                JsonElement element = json.get("item");
                if (element.isJsonPrimitive()) {
                    String[] s = element.getAsString().split(":");
                    if (s.length < 2) return null;
                    Item item = Item.getByNameOrId(s[0] + ":" + s[1]);
                    if (item == null) return null;
                    int meta = 0;
                    if (s.length > 2)
                        meta = Integer.parseInt(s[2]);
                    return new GuideItem(new ItemStack(item, 1, meta));
                }
            }
            if (json.has("metaitem")) {
                String metaItemId = json.get("metaitem").getAsString();
                for (MetaItem<?> metaItem : MetaItem.getMetaItems()) {
                    MetaItem<?>.MetaValueItem metaValueItem = metaItem.getAllItems().stream().filter(m -> m.unlocalizedName.equals(metaItemId)).findFirst().orElse(null);
                    if (metaValueItem != null) return new GuideItem(metaValueItem);
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GuideItem guideItem = (GuideItem) o;
            return Objects.equals(stack, guideItem.stack) && Objects.equals(name, guideItem.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack, name);
        }
    }
}
