package gregtech.api.recipes.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class RecipeRunRegistry {

    private final static Object2ObjectOpenHashMap<String, Function<NBTTagCompound, RecipeRun>> REGISTRY = fireEvent();

    @Nullable
    public static RecipeRun deserialize(@NotNull String type, NBTTagCompound tag) {
        if (type.equals("Legacy")) return new LegacyRecipeRun(tag);
        if (!REGISTRY.containsKey(type)) return null;
        return REGISTRY.get(type).apply(tag);
    }

    private RecipeRunRegistry() {}

    private static Object2ObjectOpenHashMap<String, Function<NBTTagCompound, RecipeRun>> fireEvent() {
        RecipeRunRegistrationEvent event = new RecipeRunRegistrationEvent();
        MinecraftForge.EVENT_BUS.post(event);
        return event.getBuilding();
    }
}
