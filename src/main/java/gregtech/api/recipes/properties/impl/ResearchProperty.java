package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

public final class ResearchProperty extends RecipeProperty<ResearchPropertyData> {

    public static final String KEY = "research";

    private static ResearchProperty INSTANCE;

    private ResearchProperty() {
        super(KEY, ResearchPropertyData.class);
    }

    @NotNull
    public static ResearchProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ResearchProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        NBTTagList list = new NBTTagList();
        for (var entry : castValue(value)) {
            list.appendTag(entry.serializeNBT());
        }
        return list;
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        NBTTagList list = (NBTTagList) nbt;
        ResearchPropertyData data = new ResearchPropertyData();
        for (int i = 0; i < list.tagCount(); i++) {
            data.add(ResearchPropertyData.ResearchEntry.deserializeFromNBT(list.getCompoundTagAt(i)));
        }

        return data;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(@NotNull Minecraft minecraft, int x, int y, int color, Object value) {
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.research"), x, y, color);
    }
}
