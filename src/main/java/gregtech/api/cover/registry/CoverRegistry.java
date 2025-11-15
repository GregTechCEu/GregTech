package gregtech.api.cover.registry;

import gregtech.api.cover.CoverDefinition;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.util.GTControlledRegistry;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.filter.BaseFilter;
import gregtech.common.covers.filter.IFilter;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.EnumMap;
import java.util.List;

public final class CoverRegistry extends GTControlledRegistry<ResourceLocation, CoverDefinition> {

    private static final EnumMap<IFilter.FilterType, List<ItemStack>> filterCovers = new EnumMap<>(
            IFilter.FilterType.class);

    public CoverRegistry(int maxId) {
        super(maxId);
    }

    @Override
    public void register(int id, @NotNull ResourceLocation key, @NotNull CoverDefinition coverDefinition) {
        super.register(id, key, coverDefinition);

        ItemStack coverStack = coverDefinition.getDropItemStack();
        if (coverStack.getItem() instanceof MetaItem<?>metaItem) {
            MetaItem<?>.MetaValueItem metaValueItem = metaItem.getItem(coverStack);
            if (metaValueItem == null) return;

            IFilter.Factory factory = metaValueItem.getFilterFactory();
            if (factory == null) return;

            BaseFilter filter = factory.create(coverStack);
            IFilter.FilterType filterType = filter.getType();
            if (filterType.isError()) return;

            List<ItemStack> filterList = filterCovers.computeIfAbsent(filterType, $ -> new ObjectArrayList<>());
            filterList.add(coverStack);
        }
    }

    @UnmodifiableView
    public static @NotNull List<ItemStack> getFilterItems(@NotNull IFilter.FilterType filterType) {
        return GTUtility.unmodifiableOrEmpty(filterCovers.get(filterType));
    }
}
