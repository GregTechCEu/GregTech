package gregtech.api.util.color.containers;

import gregtech.api.util.color.ColoredBlockContainer;

import net.minecraft.item.EnumDyeColor;

import org.jetbrains.annotations.Nullable;

public class NullContainer extends ColoredBlockContainer {

    public static final NullContainer NULL_CONTAINER = new NullContainer();

    @Override
    public boolean setColor(@Nullable EnumDyeColor newColor) {
        return false;
    }

    @Override
    public boolean removeColor() {
        return false;
    }

    @Override
    public @Nullable EnumDyeColor getColor() {
        return null;
    }

    @Override
    public boolean isValid() {
        return false;
    }
}
