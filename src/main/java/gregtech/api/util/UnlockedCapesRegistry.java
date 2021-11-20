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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.util.*;

public class UnlockedCapesRegistry extends WorldSavedData {

    private static final String DATA_ID = GTValues.MODID + ".unlocked_capes";
    private static final Map<UUID, List<ResourceLocation>> unlockedCapes = new HashMap<>();
    private static final Map<Advancement, ResourceLocation> capeAdvancements = new HashMap<>();
    private static UnlockedCapesRegistry instance;

    public UnlockedCapesRegistry() {
        super(DATA_ID);
    }

    @SuppressWarnings("unused")
    public UnlockedCapesRegistry(String name) {
        super(name);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound comp) {
        // Add resource locations
        NBTTagList tagList = new NBTTagList();
        int i = 0;
        for (Map.Entry<UUID, List<ResourceLocation>> entry : unlockedCapes.entrySet()) {
            for (ResourceLocation cape : entry.getValue()) {
                String capeLocation = cape.toString();

                NBTTagCompound tag = new NBTTagCompound();

                tag.setString("Cape", capeLocation);
                tag.setUniqueId("UUID", entry.getKey());

                tagList.appendTag(tag);

                i++;
            }
        }
        comp.setTag("CapeValList", tagList);

        return comp;
    }

    public void readFromNBT(NBTTagCompound comp) {
        unlockedCapes.clear();
        NBTTagList tagList = comp.getTagList("CapeValList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            String capeLocation = tag.getString("Cape");
            if(capeLocation.isEmpty())
                continue;
            UUID uuid = tag.getUniqueId("UUID");

            List<ResourceLocation> capes = unlockedCapes.get(uuid);
            if (capes == null) {
                capes = new ArrayList<>();
            }
            capes.add(new ResourceLocation(capeLocation));
            unlockedCapes.put(uuid, capes);
        }
    }

    public static void initializeStorage(World world) {
        MapStorage storage = world.getMapStorage();
        instance = (UnlockedCapesRegistry) storage.getOrLoadData(UnlockedCapesRegistry.class, DATA_ID);

        if (instance == null) {
            instance = new UnlockedCapesRegistry();
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
