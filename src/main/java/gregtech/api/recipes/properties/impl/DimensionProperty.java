package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;
import gregtech.api.worldgen.config.WorldGenRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;

public final class DimensionProperty extends RecipeProperty<DimensionProperty.DimensionPropertyList> {

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
}
