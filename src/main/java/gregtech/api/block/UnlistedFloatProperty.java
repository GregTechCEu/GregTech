package gregtech.api.block;

import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

public class UnlistedFloatProperty implements IUnlistedProperty<Float> {

    private final String name;

    public UnlistedFloatProperty(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(Float value) {
        return true;
    }

    @NotNull
    @Override
    public Class<Float> getType() {
        return Float.class;
    }

    @Override
    public String valueToString(@NotNull Float value) {
        return value.toString();
    }
}
