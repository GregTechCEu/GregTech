package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public final class ScanProperty extends RecipeProperty<Boolean> {

    public static final String KEY = "scan";

    private static ScanProperty INSTANCE;

    private ScanProperty() {
        super(KEY, Boolean.class);
    }

    @NotNull
    public static ScanProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScanProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        return new NBTTagByte((byte) (castValue(value) ? 1 : 0));
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        return ((NBTTagByte) nbt).getByte() == 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.scan_for_research"), x, y, color);
    }
}
