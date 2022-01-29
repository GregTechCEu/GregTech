package gregtech.api.metatileentity.multiblock;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CleanroomType {

    private static final Map<String, CleanroomType> CLEANROOM_TYPES = new Object2ObjectOpenHashMap<>();

    public static final CleanroomType CLEANROOM = new CleanroomType("cleanroom");


    private final String name;

    public CleanroomType(@Nonnull String name) {
        if (CLEANROOM_TYPES.get(name) != null)
            throw new IllegalArgumentException(String.format("CleanroomType with name %s is already registered!", name));

        this.name = name;
        CLEANROOM_TYPES.put(name, this);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nullable
    public static CleanroomType getByName(String name) {
        return CLEANROOM_TYPES.get(name);
    }
}
