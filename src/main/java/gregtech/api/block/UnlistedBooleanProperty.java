package gregtech.api.block;

import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;

public class UnlistedBooleanProperty implements IUnlistedProperty<Boolean> {
    private final String name;

    public UnlistedBooleanProperty(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(Boolean value) {
        return true;
    }

    @Nonnull
    @Override
    public Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    public String valueToString(@Nonnull Boolean value) {
        return value.toString();
    }
}
