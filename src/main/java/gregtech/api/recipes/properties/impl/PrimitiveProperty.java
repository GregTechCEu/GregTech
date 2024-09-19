package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

/**
 * Simple Marker Property to tell JEI to not display Total EU and EU/t.
 */
public final class PrimitiveProperty extends RecipeProperty<Boolean> {

    public static final String KEY = "primitive_property";
    private static PrimitiveProperty INSTANCE;

    private PrimitiveProperty() {
        super(KEY, Boolean.class);
    }

    public static PrimitiveProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PrimitiveProperty();
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
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {}

    @Override
    public int getInfoHeight(@NotNull Object value) {
        return 0;
    }

    @Override
    public boolean hideTotalEU() {
        return true;
    }

    @Override
    public boolean hideEUt() {
        return true;
    }
}
