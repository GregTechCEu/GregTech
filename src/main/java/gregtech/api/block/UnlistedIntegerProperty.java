package gregtech.api.block;

import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;

public class UnlistedIntegerProperty implements IUnlistedProperty<Integer> {

    private final String name;

    public UnlistedIntegerProperty(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(Integer integer) {
        return true;
    }

    @Nonnull
    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public String valueToString(@Nonnull Integer integer) {
        return integer.toString();
    }
}
