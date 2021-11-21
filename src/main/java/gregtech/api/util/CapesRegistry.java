package gregtech.api.util;

import gregtech.api.GTValues;
import gregtech.api.render.Textures;
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

import java.lang.reflect.Field;
import java.util.*;

public class CapesRegistry extends WorldSavedData {

    private static final String DATA_ID = GTValues.MODID + ".capes";
    private static final Map<UUID, List<ResourceLocation>> unlockedCapes = new HashMap<>();
    public static final Map<UUID, ResourceLocation> wornCapes = new HashMap<>();

    private static final Map<Advancement, ResourceLocation> capeAdvancements = new HashMap<>();
    private static CapesRegistry instance;

    public CapesRegistry() {
        super(DATA_ID);
    }

    @SuppressWarnings("unused")
    public CapesRegistry(String name) {
        super(name);
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

            List<ResourceLocation> devReward = new ArrayList<>();
            devReward.add(Textures.GREGTECH_CAPE_TEXTURE);
            unlockedCapes.put(UUID.fromString("2fa297a6-7803-4629-8360-7059155cf43e"), devReward); // KilaBash
            unlockedCapes.put(UUID.fromString("a82fb558-64f9-4dd6-a87d-84040e84bb43"), devReward); // Dan
            unlockedCapes.put(UUID.fromString("5c2933b3-5340-4356-81e7-783c53bd7845"), devReward); // Tech22
            unlockedCapes.put(UUID.fromString("56bd41d0-06ef-4ed7-ab48-926ce45651f9"), devReward); // Zalgo239
            unlockedCapes.put(UUID.fromString("aaf70ec1-ac70-494f-9966-ea5933712750"), devReward); // Bruberu
            unlockedCapes.put(UUID.fromString("a24a9108-23d2-43fc-8db7-43f809d017db"), devReward); // ALongString


            storage.setData(DATA_ID, instance);
        }
    }

    public boolean isDirty() {
        return true; // Doesn't work too well otherwise.
    }

    public static List<ResourceLocation> unlockedCapes(UUID uuid) {
        return unlockedCapes.get(uuid);
    }

    public static void linkAdvancement(ResourceLocation advancement, ResourceLocation location, World world) {
        if (world instanceof WorldServer) {
            try {
                Field advManager = World.class.getDeclaredField("advancementManager");
                advManager.setAccessible(true);
                Advancement advObject = ((AdvancementManager) advManager.get(world)).getAdvancement(advancement);
                capeAdvancements.put(advObject, location);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static void unlockCape(EntityPlayer player, Advancement advancement) {
        if (capeAdvancements.containsKey(advancement)) {
            List<ResourceLocation> capes = unlockedCapes.get(player.getPersistentID());
            if (capes == null) {
                capes = new ArrayList<>();
            } else if (capes.contains(capeAdvancements.get(advancement)))
                return;
            capes.add(capeAdvancements.get(advancement));
            unlockedCapes.put(player.getPersistentID(), capes);
            instance.markDirty();
            player.sendMessage(new TextComponentTranslation("gregtech.chat.cape"));
        }
    }

    public static void clearMaps() {
        unlockedCapes.clear();
    }

}
