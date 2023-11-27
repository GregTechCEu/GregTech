package gregtech.api.block;

import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

public class UnlistedBooleanProperty implements IUnlistedProperty<Boolean> {

    private final String name;

    public UnlistedBooleanProperty(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(Boolean value) {
        return true;
    }

    @NotNull
    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public String valueToString(@NotNull Boolean value) {
        return value.toString();
    }
}
