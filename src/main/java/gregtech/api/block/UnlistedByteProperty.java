package gregtech.api.block;

import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

public class UnlistedByteProperty implements IUnlistedProperty<Byte> {

    private final String name;

    public UnlistedByteProperty(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(Byte value) {
        return true;
    }

    @NotNull
    @Override
    public Class<Byte> getType() {
        return Byte.class;
    }

    @Override
    public String valueToString(@NotNull Byte value) {
        return value.toString();
    }
}
