package gregtech.api.block;

import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

public class UnlistedStringProperty implements IUnlistedProperty<String> {

    private final String name;

    public UnlistedStringProperty(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isValid(String s) {
        return true;
    }

    @NotNull
    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public String valueToString(String s) {
        return s;
    }
}
