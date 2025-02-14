package gregtech.api.capability.data.query;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public final class DataQueryFormat implements IStringSerializable {

    public static final DataQueryFormat RECIPE = create("gregtech.data_format.query.recipe");
    public static final DataQueryFormat COMPUTATION = create("gregtech.data_format.query.computation");

    public static DataQueryFormat create(@NotNull String name) {
        return new DataQueryFormat(name);
    }

    private final @NotNull String name;

    private DataQueryFormat(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }
}
