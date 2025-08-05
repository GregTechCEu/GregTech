package gregtech.api.color.containers;

import gregtech.api.color.ColoredBlockContainer;

import net.minecraft.item.EnumDyeColor;

import org.jetbrains.annotations.Nullable;

public class NullColorContainer extends ColoredBlockContainer {

    public static final NullColorContainer NULL_CONTAINER = new NullColorContainer();

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
