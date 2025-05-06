package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;

import gregtech.api.util.GTUtility;

import gregtech.api.util.TextFormattingUtil;

import mezz.jei.gui.TooltipRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class FissionProperty extends RecipeProperty<FissionProperty.FissionValues> {

    public static final String KEY = "fission";

    private static FissionProperty INSTANCE;

    private FissionProperty() {
        super(KEY, FissionValues.class);
    }

    public static FissionProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FissionProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        NBTTagCompound tag = new NBTTagCompound();
        FissionValues values = castValue(value);
        tag.setLong("heatEquivalent", values.getHeatEquivalentPerTick());
        tag.setInteger("optimalTemp", values.getOptimalTemperature());
        tag.setDouble("penalty", values.getSpeedMultiplierPerKelvin());
        return tag;
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        return new FissionValues(tag.getLong("heatEquivalent"), tag.getInteger("optimalTemp"), tag.getDouble("penalty"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value, int mouseX, int mouseY) {
        FissionValues values = castValue(value);
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.heat",
                values.getHeatEquivalentPerTick()), x, y - 20, color);
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.optimal_temperature",
                values.getOptimalTemperature()), x, y - 10, color);
        String str = I18n.format("gregtech.recipe.heat_penalty", values.getSpeedMultiplierPerKelvin());
        int width = minecraft.fontRenderer.getStringWidth(str);
        minecraft.fontRenderer.drawString(str, x, y, color);
        if (mouseX > x && mouseX < (x + width) && mouseY > y && mouseY < y + 10) {
            TooltipRenderer.drawHoveringText(minecraft, I18n.format("gregtech.recipe.heat_penalty.tooltip"), mouseX, mouseY);
        }
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {}

    @Override
    public int getInfoHeight(@NotNull Object value) {
        return 30;
    }

    public static final class FissionValues {

        public static final FissionValues EMPTY = new FissionValues();
        private long heatEquivalentPerTick;
        private int optimalTemperature;
        private double speedMultiplierPerKelvin;

        public FissionValues(long heatEquivalentPerTick, int optimalTemperature, double speedMultiplierPerKelvin) {
            this.heatEquivalentPerTick = heatEquivalentPerTick;
            this.optimalTemperature = optimalTemperature;
            this.speedMultiplierPerKelvin = speedMultiplierPerKelvin;
        }

        public FissionValues() {
            this(0, 300, 0);
        }

        public long getHeatEquivalentPerTick() {
            return heatEquivalentPerTick;
        }

        public int getOptimalTemperature() {
            return optimalTemperature;
        }

        public double getSpeedMultiplierPerKelvin() {
            return speedMultiplierPerKelvin;
        }

        public void setHeatEquivalentPerTick(long heatEquivalentPerTick) {
            this.heatEquivalentPerTick = heatEquivalentPerTick;
        }

        public void setOptimalTemperature(int optimalTemperature) {
            this.optimalTemperature = optimalTemperature;
        }

        public void setSpeedMultiplierPerKelvin(double speedMultiplierPerKelvin) {
            this.speedMultiplierPerKelvin = speedMultiplierPerKelvin;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (FissionValues) obj;
            return this.heatEquivalentPerTick == that.heatEquivalentPerTick &&
                    this.optimalTemperature == that.optimalTemperature &&
                    Double.doubleToLongBits(this.speedMultiplierPerKelvin) ==
                            Double.doubleToLongBits(that.speedMultiplierPerKelvin);
        }

        @Override
        public int hashCode() {
            return Objects.hash(heatEquivalentPerTick, optimalTemperature, speedMultiplierPerKelvin);
        }

        @Override
        public String toString() {
            return "FissionCoolantValues[" +
                    "heatEquivalentPerTick=" + heatEquivalentPerTick + ", " +
                    "optimalTemperature=" + optimalTemperature + ", " +
                    "speedMultiplierPerKelvin=" + speedMultiplierPerKelvin + ']';
        }

    }
}
