package gregtech.api.block;

import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

public class UnlistedIntegerProperty implements IUnlistedProperty<Integer> {

    private final String name;

    public UnlistedIntegerProperty(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(Integer integer) {
        return true;
    }

    @NotNull
    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public String valueToString(@NotNull Integer integer) {
        return integer.toString();
    }
}
