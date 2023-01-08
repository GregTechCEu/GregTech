package gregtech.api.util;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * For when static final fields in Minecraft are clearly named, yet
 * the object does not hold a "name" field in any way...
 */
public class VanillaNameHelper {

    private static final Map<Material, String> MATERIAL_NAMES = new HashMap<>();
    private static final Map<SoundType, String> SOUND_TYPE_NAMES = new HashMap<>();

    static {
        MATERIAL_NAMES.put(Material.AIR, "air");
        MATERIAL_NAMES.put(Material.GRASS, "grass");
        MATERIAL_NAMES.put(Material.GROUND, "ground");
        MATERIAL_NAMES.put(Material.WOOD, "wood");
        MATERIAL_NAMES.put(Material.ROCK, "rock");
        MATERIAL_NAMES.put(Material.IRON, "iron");
        MATERIAL_NAMES.put(Material.ANVIL, "anvil");
        MATERIAL_NAMES.put(Material.WATER, "water");
        MATERIAL_NAMES.put(Material.LAVA, "lava");
        MATERIAL_NAMES.put(Material.LEAVES, "leaves");
        MATERIAL_NAMES.put(Material.PLANTS, "plants");
        MATERIAL_NAMES.put(Material.VINE, "vine");
        MATERIAL_NAMES.put(Material.SPONGE, "sponge");
        MATERIAL_NAMES.put(Material.CLOTH, "cloth");
        MATERIAL_NAMES.put(Material.FIRE, "fire");
        MATERIAL_NAMES.put(Material.SAND, "sand");
        MATERIAL_NAMES.put(Material.CIRCUITS, "circuits");
        MATERIAL_NAMES.put(Material.CARPET, "carpet");
        MATERIAL_NAMES.put(Material.GLASS, "glass");
        MATERIAL_NAMES.put(Material.REDSTONE_LIGHT, "redstone_light");
        MATERIAL_NAMES.put(Material.TNT, "tnt");
        MATERIAL_NAMES.put(Material.CORAL, "coral");
        MATERIAL_NAMES.put(Material.ICE, "ice");
        MATERIAL_NAMES.put(Material.PACKED_ICE, "packed_ice");
        MATERIAL_NAMES.put(Material.SNOW, "snow");
        MATERIAL_NAMES.put(Material.CRAFTED_SNOW, "crafted_snow");
        MATERIAL_NAMES.put(Material.CACTUS, "cactus");
        MATERIAL_NAMES.put(Material.CLAY, "clay");
        MATERIAL_NAMES.put(Material.GOURD, "gourd");
        MATERIAL_NAMES.put(Material.DRAGON_EGG, "dragon_egg");
        MATERIAL_NAMES.put(Material.PORTAL, "portal");
        MATERIAL_NAMES.put(Material.CAKE, "cake");
        MATERIAL_NAMES.put(Material.WEB, "web");
        MATERIAL_NAMES.put(Material.PISTON, "piston");
        MATERIAL_NAMES.put(Material.BARRIER, "barrier");
        MATERIAL_NAMES.put(Material.STRUCTURE_VOID, "structure_void");

        SOUND_TYPE_NAMES.put(SoundType.WOOD, "wood");
        SOUND_TYPE_NAMES.put(SoundType.GROUND, "ground");
        SOUND_TYPE_NAMES.put(SoundType.PLANT, "plant");
        SOUND_TYPE_NAMES.put(SoundType.STONE, "stone");
        SOUND_TYPE_NAMES.put(SoundType.METAL, "metal");
        SOUND_TYPE_NAMES.put(SoundType.GLASS, "glass");
        SOUND_TYPE_NAMES.put(SoundType.CLOTH, "cloth");
        SOUND_TYPE_NAMES.put(SoundType.SAND, "sand");
        SOUND_TYPE_NAMES.put(SoundType.SNOW, "snow");
        SOUND_TYPE_NAMES.put(SoundType.LADDER, "ladder");
        SOUND_TYPE_NAMES.put(SoundType.ANVIL, "anvil");
        SOUND_TYPE_NAMES.put(SoundType.SLIME, "slime");
    }

    public static String getNameForMaterial(Material material) {
        return MATERIAL_NAMES.get(material);
    }

    public static String getNameForSoundType(SoundType soundType) {
        return SOUND_TYPE_NAMES.get(soundType);
    }
}
