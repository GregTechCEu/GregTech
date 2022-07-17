package gregtech.api.metatileentity.multiblock;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class CleanroomType {

    private static final Map<String, CleanroomType> CLEANROOM_TYPES = new Object2ObjectOpenHashMap<>();

    public static final CleanroomType CLEANROOM = new CleanroomType("cleanroom", "gregtech.recipe.cleanroom.display_name");
    public static final CleanroomType STERILE_CLEANROOM = new CleanroomType("sterile_cleanroom", "gregtech.recipe.cleanroom_sterile.display_name");


    private final String name;
    private final String translationKey;

    public CleanroomType(@Nonnull String name, @Nonnull String translationKey) {
        if (CLEANROOM_TYPES.get(name) != null)
            throw new IllegalArgumentException(String.format("CleanroomType with name %s is already registered!", name));

        this.name = name;
        this.translationKey = translationKey;
        CLEANROOM_TYPES.put(name, this);
    }

    @Nonnull
    public String getName() {
        return this.name;
    }

    @Nonnull
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Nullable
    public static CleanroomType getByName(@Nonnull String name) {
        return CLEANROOM_TYPES.get(name);
    }
}
