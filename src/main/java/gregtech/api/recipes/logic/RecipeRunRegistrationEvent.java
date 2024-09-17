package gregtech.api.recipes.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.function.Function;

public final class RecipeRunRegistrationEvent extends Event {

    Object2ObjectOpenHashMap<String, Function<NBTTagCompound, RecipeRun>> building;

    public void register(String name, Function<NBTTagCompound, RecipeRun> deserializer) {
        building.put(name, deserializer);
    }

    Object2ObjectOpenHashMap<String, Function<NBTTagCompound, RecipeRun>> getBuilding() {
        return building;
    }
}
