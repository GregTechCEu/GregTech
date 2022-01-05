package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.CPacketClientCapeChange;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.*;

public class CapesRegistry extends WorldSavedData {

    private static final String DATA_ID = GTValues.MODID + ".capes";
    private static final Map<UUID, List<ResourceLocation>> unlockedCapes = new HashMap<>();
    public static final Map<UUID, ResourceLocation> wornCapes = new HashMap<>();
    private static final Map<UUID, List<ResourceLocation>> givenCapes = new HashMap<>();

    private static final Map<Advancement, ResourceLocation> capeAdvancements = new HashMap<>();
    private static CapesRegistry instance;

    public CapesRegistry() {
        super(DATA_ID);
    }

    @SuppressWarnings("unused")
    public CapesRegistry(String name) {
        super(name);
    }

    public static void handleCapeChange(UUID uuid, ResourceLocation cape) {
        wornCapes.put(uuid, cape);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound comp) {
        // Add resource locations
        NBTTagList unlockedCapesTag = new NBTTagList();
        for (Map.Entry<UUID, List<ResourceLocation>> entry : unlockedCapes.entrySet()) {
            for (ResourceLocation cape : entry.getValue()) {
                String capeLocation = cape.toString();

                NBTTagCompound tag = new NBTTagCompound();

                tag.setString("Cape", capeLocation);
                tag.setUniqueId("UUID", entry.getKey());

                unlockedCapesTag.appendTag(tag);

            }
        }
        comp.setTag("UnlockedCapesValList", unlockedCapesTag);

        NBTTagList wornCapesTag = new NBTTagList();
        for (Map.Entry<UUID, ResourceLocation> entry : wornCapes.entrySet()) {
            if(entry.getValue() == null)
                continue;
            String capeLocation = entry.getValue().toString();

            NBTTagCompound tag = new NBTTagCompound();

            tag.setString("Cape", capeLocation);
            tag.setUniqueId("UUID", entry.getKey());

            wornCapesTag.appendTag(tag);
        }
        comp.setTag("WornCapesValList", wornCapesTag);
        return comp;
    }

    public void readFromNBT(NBTTagCompound comp) {
        unlockedCapes.clear();
        NBTTagList unlockedCapesTag = comp.getTagList("UnlockedCapesValList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < unlockedCapesTag.tagCount(); i++) {
            NBTTagCompound tag = unlockedCapesTag.getCompoundTagAt(i);
            String capeLocation = tag.getString("Cape");
            if (capeLocation.isEmpty())
                continue;
            UUID uuid = tag.getUniqueId("UUID");

            List<ResourceLocation> capes = unlockedCapes.get(uuid);
            if (capes == null) {
                capes = new ArrayList<>();
            }
            capes.add(new ResourceLocation(capeLocation));
            unlockedCapes.put(uuid, capes);
        }

        wornCapes.clear();
        NBTTagList wornCapesTag = comp.getTagList("WornCapesValList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < wornCapesTag.tagCount(); i++) {
            NBTTagCompound tag = wornCapesTag.getCompoundTagAt(i);
            String capeLocation = tag.getString("Cape");
            if (capeLocation.isEmpty())
                continue;
            UUID uuid = tag.getUniqueId("UUID");
            wornCapes.put(uuid, new ResourceLocation(capeLocation));
        }
    }

    public static void initializeStorage(World world) {
        MapStorage storage = world.getMapStorage();
        instance = (CapesRegistry) storage.getOrLoadData(CapesRegistry.class, DATA_ID);

        if (instance == null) {
            instance = new CapesRegistry();

            for(Map.Entry<UUID, List<ResourceLocation>> givenCapeSet : givenCapes.entrySet()) {
                unlockedCapes.put(givenCapeSet.getKey(), givenCapeSet.getValue());
            }

            List<ResourceLocation> devReward = new ArrayList<>();
            devReward.add(Textures.GREGTECH_CAPE_TEXTURE);


            storage.setData(DATA_ID, instance);
        }
    }

    public boolean isDirty() {
        return true; // Doesn't work too well otherwise.
    }

    /**
     * Allows one to check what capes a specific player has unlocked through CapesRegistry.
     * @param uuid The player data used to get what capes the player has through internal maps.
     * @return A list of ResourceLocations containing the cape textures that the player has unlocked.
     */
    public static List<ResourceLocation> getUnlockedCapes(UUID uuid) {
        return unlockedCapes.get(uuid);
    }


    /**
     * Links an advancement with a cape, which allows a player to unlock it when they receive the advancement.
     * This should only be called on world load, since advancements are only accessible then.
     * @param advancement A ResourceLocation pointing to the advancement that is to be used for getting a cape.
     * @param cape The ResourceLocation that points to the cape that can be unlocked through the advancement.
     * @param world The world that may contain the advancement used for getting a cape.
     */
    public static void registerCape(ResourceLocation advancement, ResourceLocation cape, World world) {
        if (world instanceof WorldServer) {
            AdvancementManager advManager = ObfuscationReflectionHelper.getPrivateValue(World.class, world, "field_191951_C");
            Advancement advObject = advManager.getAdvancement(advancement);
            capeAdvancements.put(advObject, cape);
        }
    }

    /**
     * Automatically gives a cape to a player, which may be used for a reward for something other than an advancement
     * @param uuid The UUID of the player to be given the cape.
     * @param cape The ResourceLocation that holds the cape used here.
     */
    public static void unlockCape(UUID uuid, ResourceLocation cape) {
        List<ResourceLocation> capes = unlockedCapes.get(uuid);
        if (capes == null) {
            capes = new ArrayList<>();
        } else if (capes.contains(cape))
            return;
        capes.add(cape);
        unlockedCapes.put(uuid, capes);
    }

    /**
     * Automatically gives a cape to a player to every world they join. Should be called on game load on server side.
     * @param uuid The UUID of the player to be given the cape.
     * @param cape The ResourceLocation that holds the cape used here.
     */
    public static void unlockCapeEverywhere(UUID uuid, ResourceLocation cape) {
        List<ResourceLocation> capes = unlockedCapes.get(uuid);
        if (capes == null) {
            capes = new ArrayList<>();
        } else if (capes.contains(cape))
            return;
        capes.add(cape);
        unlockedCapes.put(uuid, capes);
    }

    public static void unlockCapeOnAdvancement(EntityPlayer player, Advancement advancement) {
        if (capeAdvancements.containsKey(advancement)) {
            unlockCape(player.getPersistentID(), capeAdvancements.get(advancement));
            player.sendMessage(new TextComponentTranslation("gregtech.chat.cape"));
        }
    }

    public static void clearMaps() {
        unlockedCapes.clear();
    }

    public static void giveCape(UUID uuid, ResourceLocation cape) {
        wornCapes.put(uuid, cape);
        NetworkHandler.channel.sendToServer(new CPacketClientCapeChange(uuid, cape).toFMLPacket());
    }

    // For loading capes when the player logs in, so that it's synced to the clients.
    public static void loadWornCapeOnLogin(UUID uuid) {
        if(wornCapes.get(uuid) != null)
            NetworkHandler.channel.sendToServer(new CPacketClientCapeChange(uuid, wornCapes.get(uuid)).toFMLPacket());
    }

}
