package gregtech.common.covers.newFilter;

import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.util.GTLog;
import gregtech.common.covers.newFilter.fluid.SimpleFluidFilter;
import gregtech.common.covers.newFilter.item.OreDictFilter;
import gregtech.common.covers.newFilter.item.SimpleItemFilter;
import gregtech.common.covers.newFilter.item.SmartFilter;
import gregtech.common.items.MetaItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class FilterRegistry {

    public static void init() {
        register(MetaItems.ITEM_FILTER.getStackForm(), SimpleItemFilter::new);
        register(MetaItems.ORE_DICTIONARY_FILTER.getStackForm(), OreDictFilter::new);
        register(MetaItems.SMART_FILTER.getStackForm(), SmartFilter::new);
        register(MetaItems.FLUID_FILTER.getStackForm(), SimpleFluidFilter::new);
    }

    private static final Map<ItemKey, Supplier<Filter<?>>> REGISTRY = new HashMap<>();

    public static void register(ItemStack item, Supplier<Filter<?>> filterCreator) {
        ItemKey key = new ItemKey(item);
        GTLog.logger.info("Registering filter for {}", item);
        REGISTRY.put(key, filterCreator);
    }

    @Nullable
    public static Filter<?> createFilter(ItemStack item) {
        ItemKey key = new ItemKey(item);
        Supplier<Filter<?>> filterCreator = REGISTRY.get(key);
        if (filterCreator == null) {
            return null;
        }
        return filterCreator.get();
    }

    public static class ItemKey {
        public final Item item;
        public final int meta;
        @Nullable
        public final NBTTagCompound nbt;
        public final int hash;

        public ItemKey(ItemStack item) {
            this(item.getItem(), item.getMetadata(), item.getTagCompound());
        }

        public ItemKey(ItemAndMetadata itemAndMetadata) {
            this(itemAndMetadata.item, itemAndMetadata.itemDamage, null);
        }

        public ItemKey(Item item, int meta, @Nullable NBTTagCompound nbt) {
            this.item = item;
            this.meta = meta;
            this.nbt = nbt;
            this.hash = Objects.hash(item, meta, nbt);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemKey itemKey = (ItemKey) o;
            return meta == itemKey.meta && item == itemKey.item && Objects.equals(nbt, itemKey.nbt);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
