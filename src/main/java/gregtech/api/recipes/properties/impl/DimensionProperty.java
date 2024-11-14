package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.lookup.property.DimensionInhabitedProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.worldgen.config.WorldGenRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;

public final class DimensionProperty extends RecipePropertyWithFilter<DimensionProperty.DimensionPropertyList> {

    public static final String KEY = "dimension";

    private static DimensionProperty INSTANCE;

    private DimensionProperty() {
        super(KEY, DimensionPropertyList.class);
    }

    public static DimensionProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DimensionProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        DimensionPropertyList list = castValue(value);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("whiteListDimensions", list.whiteListDimensions.toArray(new int[0]));
        tag.setIntArray("blackListDimensions", list.blackListDimensions.toArray(new int[0]));
        return tag;
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        DimensionPropertyList list = new DimensionPropertyList();
        for (int i : tag.getIntArray("whiteListDimensions")) {
            list.add(i, false);
        }

        for (int i : tag.getIntArray("blackListDimensions")) {
            list.add(i, true);
        }
        return tag;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        DimensionPropertyList list = castValue(value);

        if (list.whiteListDimensions.size() > 0)
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.dimensions",
                    getDimensionsForRecipe(castValue(value).whiteListDimensions)), x, y, color);
        if (list.blackListDimensions.size() > 0)
            minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.dimensions_blocked",
                    getDimensionsForRecipe(castValue(value).blackListDimensions)), x, y, color);
    }

    private static String getDimensionsForRecipe(IntList value) {
        Int2ObjectMap<String> dimNames = WorldGenRegistry.getNamedDimensions();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            builder.append(dimNames.getOrDefault(value.getInt(i), String.valueOf(value.getInt(i))));
            if (i != value.size() - 1)
                builder.append(", ");
        }
        String str = builder.toString();

        if (str.length() >= 13) {
            str = str.substring(0, 10) + "..";
        }
        return str;
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof DimensionProperty;
    }

    @Override
    public int filterHash() {
        return 5;
    }

    @Override
    public @NotNull Filter<DimensionPropertyList> getNewFilter() {
        return new DimensionFilter();
    }

    // It would've been better to have one list and swap between blacklist and whitelist, but that would've been
    // a bit awkward to apply to the property in practice.
    public static class DimensionPropertyList {

        public static DimensionPropertyList EMPTY_LIST = new DimensionPropertyList();

        public final IntList whiteListDimensions = new IntArrayList();
        public final IntList blackListDimensions = new IntArrayList();

        public void add(int key, boolean toBlacklist) {
            if (toBlacklist) {
                blackListDimensions.add(key);
                whiteListDimensions.rem(key);
            } else {
                whiteListDimensions.add(key);
                blackListDimensions.rem(key);
            }
        }

        public void merge(@NotNull DimensionPropertyList list) {
            this.whiteListDimensions.addAll(list.whiteListDimensions);
            this.blackListDimensions.addAll(list.blackListDimensions);
        }

        public boolean checkDimension(int dim) {
            return !blackListDimensions.contains(dim) && whiteListDimensions.contains(dim);
        }
    }

    @Override
    public boolean matches(PropertySet properties, DimensionPropertyList value) {
        DimensionInhabitedProperty inhabited = properties.getNullable(DimensionFilter.MATCHER);
        if (inhabited == null) return value.whiteListDimensions.isEmpty();
        int dim = inhabited.dimension();
        return value.checkDimension(dim);
    }

    private static class DimensionFilter implements Filter<DimensionPropertyList> {

        private static final DimensionInhabitedProperty MATCHER = new DimensionInhabitedProperty(0);

        Int2ObjectOpenHashMap<BitSet> whiteList = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<BitSet> blackList = new Int2ObjectOpenHashMap<>();

        @Override
        public void accumulate(short recipeID, @NotNull DimensionPropertyList filterInformation) {
            for (int i : filterInformation.whiteListDimensions) {
                whiteList.computeIfAbsent(i, v -> new BitSet()).set(recipeID);
            }
            for (int i : filterInformation.blackListDimensions) {
                blackList.computeIfAbsent(i, v -> new BitSet()).set(recipeID);
            }
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            DimensionInhabitedProperty inhabited = properties.getNullable(MATCHER);
            int dimension = inhabited == null ? 0 : inhabited.dimension();
            for (var entry : whiteList.int2ObjectEntrySet()) {
                if (inhabited == null || entry.getIntKey() != dimension) {
                    recipeMask.or(entry.getValue());
                }
            }
            if (inhabited == null) return;
            BitSet fetch = blackList.get(dimension);
            if (fetch != null) recipeMask.or(fetch);
        }
    }
}
