package gregtech.api.block;

import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;

public class UnlistedStringProperty implements IUnlistedProperty<String> {

    private final String name;

    public UnlistedStringProperty(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(String s) {
        return true;
    }

    @Nonnull
    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String valueToString(String s) {
        return s;
    }
}
