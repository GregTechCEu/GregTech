package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public final class ImplosionExplosiveProperty extends RecipeProperty<ItemStack> {

    public static final String KEY = "explosives";

    private static ImplosionExplosiveProperty INSTANCE;

    private ImplosionExplosiveProperty() {
        super(KEY, ItemStack.class);
    }

    public static ImplosionExplosiveProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ImplosionExplosiveProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }

        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return castValue(value).serializeNBT();
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return new ItemStack((NBTTagCompound) nbt);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.explosive",
                castValue(value).getDisplayName()), x, y, color);
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
