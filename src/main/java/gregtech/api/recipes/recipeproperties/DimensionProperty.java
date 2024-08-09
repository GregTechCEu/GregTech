package gregtech.api.recipes.recipeproperties;

import gregtech.api.worldgen.config.WorldGenRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class DimensionProperty extends RecipeProperty<DimensionProperty.DimensionPropertyList> {

    public static final String KEY = "dimension";

    private static DimensionProperty INSTANCE;

    private DimensionProperty() {
        super(KEY, DimensionPropertyList.class);
    }

    public static DimensionProperty getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DimensionProperty();
        return INSTANCE;
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

        public IntList whiteListDimensions = new IntArrayList();
        public IntList blackListDimensions = new IntArrayList();

        public void add(int key, boolean toBlacklist) {
            if (toBlacklist) {
                blackListDimensions.add(key);
                whiteListDimensions.rem(key);
            } else {
                whiteListDimensions.add(key);
                blackListDimensions.rem(key);
            }
        }

        public void merge(DimensionPropertyList list) {
            this.whiteListDimensions.addAll(list.whiteListDimensions);
            this.blackListDimensions.addAll(list.blackListDimensions);
        }

        public boolean checkDimension(int dim) {
            boolean valid = true;
            if (this.blackListDimensions.size() > 0) valid = !this.blackListDimensions.contains(dim);
            if (this.whiteListDimensions.size() > 0) valid = this.whiteListDimensions.contains(dim);
            return valid;
        }
    }
}
