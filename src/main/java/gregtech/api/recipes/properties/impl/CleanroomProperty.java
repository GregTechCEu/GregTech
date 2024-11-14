package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.multiblock.CleanroomType;
import gregtech.api.recipes.lookup.property.CleanroomFulfilmentProperty;
import gregtech.api.recipes.lookup.property.PropertySet;
import gregtech.api.recipes.lookup.property.filter.IPropertyFilter;
import gregtech.api.recipes.lookup.property.filter.RecipePropertyWithFilter;
import gregtech.api.recipes.properties.RecipeProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.Objects;

public final class CleanroomProperty extends RecipePropertyWithFilter<CleanroomType> {

    public static final String KEY = "cleanroom";

    private static CleanroomProperty INSTANCE;

    private CleanroomProperty() {
        super(KEY, CleanroomType.class);
    }

    public static CleanroomProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CleanroomProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return new NBTTagString(castValue(value).getName());
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return Objects.requireNonNull(CleanroomType.getByName(((NBTTagString) nbt).getString()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        CleanroomType type = castValue(value);

        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.cleanroom", getName(type)), x, y, color);
    }

    @NotNull
    private static String getName(@NotNull CleanroomType value) {
        String name = I18n.format(value.getTranslationKey());
        if (name.length() >= 20) return name.substring(0, 20) + "..";
        return name;
    }

    @Override
    public boolean filterEquals(@Nullable IPropertyFilter<?> other) {
        return other instanceof CleanroomProperty;
    }

    @Override
    public int filterHash() {
        return 2;
    }

    @Override
    public @NotNull Filter<CleanroomType> getNewFilter() {
        return new CleanroomFilter();
    }

    @Override
    public boolean matches(PropertySet properties, CleanroomType value) {
        return properties.getDefaultable(CleanroomFulfilmentProperty.EMPTY).isFulfilled(value);
    }

    private static final class CleanroomFilter extends Object2ObjectArrayMap<CleanroomType, BitSet>
                                               implements IPropertyFilter.Filter<CleanroomType> {

        private final BitSet zeroReference = new BitSet();

        @Override
        public void accumulate(short recipeID, @NotNull CleanroomType filterInformation) {
            this.computeIfAbsent(filterInformation, k -> new BitSet()).set(recipeID);
            zeroReference.set(recipeID);
        }

        @Override
        public void filter(@NotNull BitSet recipeMask, @NotNull PropertySet properties) {
            CleanroomFulfilmentProperty c = properties.getNullable(CleanroomFulfilmentProperty.EMPTY);
            if (c == null) {
                recipeMask.or(zeroReference);
                return;
            }
            for (var entry : this.entrySet()) {
                if (!c.isFulfilled(entry.getKey())) {
                    recipeMask.or(entry.getValue());
                }
            }
        }
    }
}
