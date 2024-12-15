package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public final class TotalComputationProperty extends RecipeProperty<Integer> {

    public static final String KEY = "total_computation";

    private static TotalComputationProperty INSTANCE;

    private TotalComputationProperty() {
        super(KEY, Integer.class);
    }

    public static TotalComputationProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TotalComputationProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return new NBTTagInt(castValue(value));
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return ((NBTTagInt) nbt).getInt();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.total_computation", castValue(value)), x, y,
                color);
    }

    @Override
    public boolean hideDuration() {
        return true;
    }
}
