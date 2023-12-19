package gregtech.api.worldgen.config;

import gregtech.api.util.GTUtility;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class WorldConfigUtils {

    public static Predicate<WorldProvider> createWorldPredicate(JsonElement element) {
        Preconditions.checkArgument(element.isJsonArray(), "World filter should be array!");
        JsonArray predicateArray = element.getAsJsonArray();
        ArrayList<Predicate<WorldProvider>> allPredicates = new ArrayList<>();

        for (JsonElement worldPredicate : predicateArray) {
            String stringValue = worldPredicate.getAsString();
            if (stringValue.equals("is_surface_world")) {
                allPredicates.add(WorldProvider::isSurfaceWorld);
                continue;
            } else if (stringValue.equals("is_nether")) {
                allPredicates.add(wp -> wp.isNether() || wp.getDimensionType() == DimensionType.NETHER);
                continue;
            }
            Function<WorldProvider, String> stringSupplier = null;
            if (stringValue.startsWith("dimension_id:")) {
                String filterValue = stringValue.substring(13);
                if (filterValue.indexOf(':') == -1) {
                    int dimensionId = Integer.parseInt(filterValue);
                    allPredicates.add(provider -> provider.getDimension() == dimensionId);
                } else {
                    int indexOf = filterValue.indexOf(':');
                    int indexOfExclusive = indexOf + 1;
                    int minDimensionId = indexOf == 0 ? -Integer.MAX_VALUE :
                            Integer.parseInt(filterValue.substring(0, indexOf));
                    int maxDimensionId = indexOfExclusive == filterValue.length() ? Integer.MAX_VALUE :
                            Integer.parseInt(filterValue.substring(indexOfExclusive));
                    allPredicates.add(provider -> provider.getDimension() >= minDimensionId &&
                            provider.getDimension() <= maxDimensionId);
                }
            } else if (stringValue.startsWith("name:")) {
                stringSupplier = provider -> provider.getDimensionType().getName();
                stringValue = stringValue.substring(5);
            } else if (stringValue.startsWith("provider_class:")) {
                stringSupplier = provider -> provider.getClass().getSimpleName();
                stringValue = stringValue.substring(15);
            } else throw new IllegalArgumentException("Unknown world predicate: " + stringValue);
            if (stringSupplier != null) {
                if (stringValue.startsWith("*")) {
                    Pattern pattern = Pattern.compile(stringValue.substring(1));
                    Function<WorldProvider, String> finalStringSupplier = stringSupplier;
                    allPredicates.add(provider -> pattern.matcher(finalStringSupplier.apply(provider)).matches());
                } else {
                    String finalStringValue = stringValue;
                    Function<WorldProvider, String> finalStringSupplier1 = stringSupplier;
                    allPredicates
                            .add(provider -> finalStringValue.equalsIgnoreCase(finalStringSupplier1.apply(provider)));
                }
            }
        }

        return provider -> allPredicates.stream().anyMatch(p -> p.test(provider));
    }

    public static Function<Biome, Integer> createBiomeWeightModifier(JsonElement element) {
        if (!element.isJsonObject())
            throw new IllegalArgumentException("Biome weight modifier should be object!");
        JsonObject object = element.getAsJsonObject();
        String influenceType = object.get("type").getAsString();

        switch (influenceType) {
            case "biome_map": {
                HashMap<Biome, Integer> backedMap = new HashMap<>();
                for (Entry<String, JsonElement> elementEntry : object.entrySet()) {
                    if (elementEntry.getKey().equals("type")) continue; // skip type
                    ResourceLocation biomeName = new ResourceLocation(elementEntry.getKey());
                    Biome biome = GameRegistry.findRegistry(Biome.class).getValue(biomeName);
                    if (biome == null)
                        throw new IllegalArgumentException("Couldn't find biome with name " + biomeName);
                    backedMap.put(biome, elementEntry.getValue().getAsInt());
                }
                return biome -> backedMap.getOrDefault(biome, 0);
            }
            case "biome_dictionary": {
                HashMap<Type, Integer> backedMap = new HashMap<>();
                for (Entry<String, JsonElement> elementEntry : object.entrySet()) {
                    if (elementEntry.getKey().equals("type")) continue; // skip type
                    String tagName = elementEntry.getKey().toUpperCase();
                    Type type = GTUtility.getBiomeTypeTagByName(tagName);
                    if (type == null)
                        throw new IllegalArgumentException("Couldn't find biome dictionary tag " + tagName);
                    backedMap.put(type, elementEntry.getValue().getAsInt());
                }
                return biome -> {
                    int totalModifier = 0;
                    for (Entry<Type, Integer> entry : backedMap.entrySet()) {
                        if (BiomeDictionary.hasType(biome, entry.getKey())) {
                            totalModifier += entry.getValue();
                        }
                    }
                    return totalModifier;
                };
            }
            default:
                throw new IllegalArgumentException("Unknown biome influence type: " + influenceType);
        }
    }
}
