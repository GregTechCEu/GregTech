package gregtech.api.util;

import gregtech.api.GTValues;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class UnlockedCapesRegistry extends WorldSavedData {

    private static final String DATA_ID = GTValues.MODID + ".unlocked_capes";
    private static final Map<UUID, List<ResourceLocation>> unlockedCapes = new HashMap<>();
    private static final Map<Advancement, ResourceLocation> capeAdvancements = new HashMap<>();

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
            for(ResourceLocation cape : entry.getValue()) {
                String capeLocation = cape.getPath();

                NBTTagCompound wrapper = new NBTTagCompound();
                NBTTagCompound tag = new NBTTagCompound();

                tag.setString("Cape", capeLocation);
                tag.setUniqueId("UUID", entry.getKey());
                wrapper.setTag("Entry" + i, tag);

                tagList.appendTag(wrapper);

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
            UUID uuid = tag.getUniqueId("UUID");

            List<ResourceLocation> capes = unlockedCapes.get(uuid);
            if(capes == null) {
                capes = new ArrayList<>();
            }
            capes.add(new ResourceLocation(capeLocation));
            unlockedCapes.put(uuid, capes);
        }
    }

    public static void initializeStorage(World world) {
        MapStorage storage = world.getMapStorage();
        UnlockedCapesRegistry instance = (UnlockedCapesRegistry) storage.getOrLoadData(UnlockedCapesRegistry.class, DATA_ID);

        if (instance == null) {
            instance = new UnlockedCapesRegistry();
            storage.setData(DATA_ID, instance);
        }
    }

    public static List<ResourceLocation> unlockedCapes(UUID uuid) {
        return unlockedCapes.get(uuid);
    }

    public static void linkAdvancement(Advancement advancement, ResourceLocation location) {
        capeAdvancements.put(advancement, location);
    }

    @SideOnly(Side.CLIENT)
    public static void unlockCape(EntityPlayer player, Advancement advancement) {
        if(capeAdvancements.containsKey(advancement)) {
            List<ResourceLocation> capes = unlockedCapes.get(player.getPersistentID());
            if(capes == null) {
                capes = new ArrayList<>();
            }
            capes.add(capeAdvancements.get(advancement));
            unlockedCapes.put(player.getPersistentID(), capes);
            player.sendMessage(new TextComponentTranslation("gregtech.chat.cape"));
        }
    }
}
