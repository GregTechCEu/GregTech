package gregtech.api.recipes.properties.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.properties.RecipeProperty;

import mezz.jei.gui.TooltipRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class FissionCoolantProperty extends RecipeProperty<FissionCoolantProperty.FissionCoolantValues> {

    public static final String KEY = "coolant";

    private static FissionCoolantProperty INSTANCE;

    private FissionCoolantProperty() {
        super(KEY, FissionCoolantValues.class);
    }

    public static FissionCoolantProperty getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FissionCoolantProperty();
            GregTechAPI.RECIPE_PROPERTIES.register(KEY, INSTANCE);
        }
        return INSTANCE;
    }

    @Override
    public @NotNull NBTBase serialize(@NotNull Object value) {
        NBTTagCompound tag = new NBTTagCompound();
        FissionCoolantValues values = castValue(value);
        tag.setLong("heatEquivalent", values.getHeatEquivalentPerOperation());
        tag.setInteger("minTemp", values.getMinimumTemperature());
        tag.setInteger("cutoffTemp", values.getCutoffTemperature());
        return tag;
    }

    @Override
    public @NotNull Object deserialize(@NotNull NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        return new FissionCoolantValues(tag.getLong("heatEquivalent"), tag.getInteger("minTemp"), tag.getInteger("cutoffTemp"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value, int mouseX, int mouseY) {
        FissionCoolantValues values = castValue(value);
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.heat",
                values.getHeatEquivalentPerOperation()), x, y - 20, color);
        minecraft.fontRenderer.drawString(I18n.format("gregtech.recipe.minimum_temperature",
                values.getMinimumTemperature()), x, y - 10, color);
        String str = I18n.format("gregtech.recipe.cutoff_temperature", values.getCutoffTemperature());
        int width = minecraft.fontRenderer.getStringWidth(str);
        minecraft.fontRenderer.drawString(str, x, y, color);
        if (mouseX > x && mouseX < (x + width) && mouseY > y && mouseY < y + 10) {
            TooltipRenderer.drawHoveringText(minecraft, I18n.format("gregtech.recipe.cutoff_temperature.tooltip"), mouseX, mouseY);
        }
    }

    @Override
    public void drawInfo(Minecraft minecraft, int x, int y, int color, Object value) {}

    @Override
    public int getInfoHeight(@NotNull Object value) {
        return 30;
    }

    public static final class FissionCoolantValues {

        public static final FissionCoolantValues EMPTY = new FissionCoolantValues();
        private long heatEquivalentPerOperation;
        private int minimumTemperature;
        private int cutoffTemperature;

        public FissionCoolantValues(long heatEquivalentPerOperation, int minimumTemperature) {
            this(heatEquivalentPerOperation, minimumTemperature, minimumTemperature + 500);
        }

        public FissionCoolantValues(long heatEquivalentPerOperation, int minimumTemperature, int cutoffTemperature) {
            this.heatEquivalentPerOperation = heatEquivalentPerOperation;
            this.minimumTemperature = minimumTemperature;
            this.cutoffTemperature = cutoffTemperature;
        }

        public FissionCoolantValues() {
            this(0, 300);
        }

        public long getHeatEquivalentPerOperation() {
            return heatEquivalentPerOperation;
        }

        public int getMinimumTemperature() {
            return minimumTemperature;
        }

        public int getCutoffTemperature() {
            return cutoffTemperature;
        }

        public void setHeatEquivalentPerOperation(long heatEquivalentPerOperation) {
            this.heatEquivalentPerOperation = heatEquivalentPerOperation;
        }

        public void setMinimumTemperature(int minimumTemperature) {
            this.minimumTemperature = minimumTemperature;
        }

        public void setCutoffTemperature(int cutoffTemperature) {
            this.cutoffTemperature = cutoffTemperature;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (FissionCoolantValues) obj;
            return this.heatEquivalentPerOperation == that.heatEquivalentPerOperation &&
                    this.minimumTemperature == that.minimumTemperature &&
                    this.cutoffTemperature == that.cutoffTemperature;
        }

        @Override
        public int hashCode() {
            return Objects.hash(heatEquivalentPerOperation, minimumTemperature, cutoffTemperature);
        }

        @Override
        public String toString() {
            return "FissionCoolantValues[" +
                    "heatEquivalentPerTick=" + heatEquivalentPerOperation + ", " +
                    "optimalTemperature=" + minimumTemperature + ", " +
                    "speedMultiplierPerKelvin=" + cutoffTemperature + ']';
        }

    }
}
