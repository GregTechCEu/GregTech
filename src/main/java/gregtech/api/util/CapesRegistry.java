package gregtech.api.util;

import crafttweaker.annotations.ZenRegister;
import gregtech.api.GTValues;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.packets.SPacketNotifyCapeChange;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.io.File;
import java.io.IOException;
import java.util.*;

@ZenClass("mods.gregtech.util.CapesRegistry")
@ZenRegister
public class CapesRegistry {

    private static final Map<UUID, List<ResourceLocation>> UNLOCKED_CAPES = new HashMap<>();
    private static final Map<UUID, ResourceLocation> WORN_CAPES = new HashMap<>();
    private static final Map<Advancement, ResourceLocation> CAPE_ADVANCEMENTS = new HashMap<>();

    public static void registerDevCapes() {
        unlockCape(UUID.fromString("2fa297a6-7803-4629-8360-7059155cf43e"), Textures.GREGTECH_CAPE_TEXTURE); // KilaBash
        unlockCape(UUID.fromString("a82fb558-64f9-4dd6-a87d-84040e84bb43"), Textures.GREGTECH_CAPE_TEXTURE); // Dan
        unlockCape(UUID.fromString("5c2933b3-5340-4356-81e7-783c53bd7845"), Textures.GREGTECH_CAPE_TEXTURE); // Tech22
        unlockCape(UUID.fromString("56bd41d0-06ef-4ed7-ab48-926ce45651f9"), Textures.GREGTECH_CAPE_TEXTURE); // Zalgo239
        unlockCape(UUID.fromString("aaf70ec1-ac70-494f-9966-ea5933712750"), Textures.GREGTECH_CAPE_TEXTURE); // Bruberu
        unlockCape(UUID.fromString("a24a9108-23d2-43fc-8db7-43f809d017db"), Textures.GREGTECH_CAPE_TEXTURE); // ALongString
        unlockCape(UUID.fromString("77e2129d-8f68-4025-9394-df946f1f3aee"), Textures.GREGTECH_CAPE_TEXTURE); // Brachy84
        save();
    }

    public static ResourceLocation getPlayerCape(UUID uuid) {
        return WORN_CAPES.get(uuid);
    }

    public static void save() {
        NBTTagCompound comp = new NBTTagCompound();
        NBTTagList unlockedCapesTag = new NBTTagList();
        for (Map.Entry<UUID, List<ResourceLocation>> entry : UNLOCKED_CAPES.entrySet()) {
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
        for (Map.Entry<UUID, ResourceLocation> entry : WORN_CAPES.entrySet()) {
            if (entry.getValue() == null)
                continue;
            String capeLocation = entry.getValue().toString();

            NBTTagCompound tag = new NBTTagCompound();

            tag.setString("Cape", capeLocation);
            tag.setUniqueId("UUID", entry.getKey());

            wornCapesTag.appendTag(tag);
        }
        comp.setTag("WornCapesValList", wornCapesTag);
        try {
            CompressedStreamTools.safeWrite(comp, new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "gregtech_cape.dat"));
        } catch (IOException exception) {
            GTLog.logger.error(exception);
        }
    }

    public static void load() {
        NBTTagCompound comp = null;
        try {
            comp = CompressedStreamTools.read(new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "gregtech_cape.dat"));
        } catch (IOException exception) {
            GTLog.logger.error(exception);
        }
        clearMaps();
        if (comp == null) {
            registerDevCapes();
            return;
        }
        NBTTagList unlockedCapesTag = comp.getTagList("UnlockedCapesValList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < unlockedCapesTag.tagCount(); i++) {
            NBTTagCompound tag = unlockedCapesTag.getCompoundTagAt(i);
            String capeLocation = tag.getString("Cape");
            if (capeLocation.isEmpty())
                continue;
            UUID uuid = tag.getUniqueId("UUID");

            List<ResourceLocation> capes = UNLOCKED_CAPES.get(uuid);
            if (capes == null) {
                capes = new ArrayList<>();
            }
            capes.add(new ResourceLocation(capeLocation));
            UNLOCKED_CAPES.put(uuid, capes);
        }

        NBTTagList wornCapesTag = comp.getTagList("WornCapesValList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < wornCapesTag.tagCount(); i++) {
            NBTTagCompound tag = wornCapesTag.getCompoundTagAt(i);
            String capeLocation = tag.getString("Cape");
            if (capeLocation.isEmpty())
                continue;
            UUID uuid = tag.getUniqueId("UUID");
            WORN_CAPES.put(uuid, new ResourceLocation(capeLocation));
        }
        registerDevCapes();
    }

    public static void checkAdvancements(World world) {
        registerCape(new ResourceLocation(GTValues.MODID, "ultimate_voltage/74_wetware_mainframe"), Textures.GREGTECH_CAPE_TEXTURE, world);
        registerCape(new ResourceLocation(GTValues.MODID, "steam/12_electronic_circuit"), Textures.RED_CAPE_TEXTURE, world);
        registerCape(new ResourceLocation(GTValues.MODID, "high_voltage/82_large_chemical_reactor"), Textures.YELLOW_CAPE_TEXTURE, world);
        registerCape(new ResourceLocation(GTValues.MODID, "ludicrous_voltage/60_fusion"), Textures.GREEN_CAPE_TEXTURE, world);
        for (Tuple<ResourceLocation, ResourceLocation> tuple : ctRegisterCapes) {
            registerCape(tuple.getFirst(), tuple.getSecond(), world);
        }
    }

    /**
     * Allows one to check what capes a specific player has unlocked through CapesRegistry.
     * @param uuid The player data used to get what capes the player has through internal maps.
     * @return A list of ResourceLocations containing the cape textures that the player has unlocked.
     */
    public static List<ResourceLocation> getUnlockedCapes(UUID uuid) {
        return UNLOCKED_CAPES.getOrDefault(uuid, Collections.emptyList());
    }


    /**
     * Links an advancement with a cape, which allows a player to unlock it when they receive the advancement.
     * This should only be called on world load, since advancements are only accessible then.
     * @param advancement A ResourceLocation pointing to the advancement that is to be used for getting a cape.
     * @param cape        The ResourceLocation that points to the cape that can be unlocked through the advancement.
     * @param world       The world that may contain the advancement used for getting a cape.
     */
    public static void registerCape(ResourceLocation advancement, ResourceLocation cape, World world) {
        if (!world.isRemote) {
            AdvancementManager advManager = ObfuscationReflectionHelper.getPrivateValue(World.class, world, "field_191951_C");
            Advancement advObject = advManager.getAdvancement(advancement);
            if (advObject != null) {
                CAPE_ADVANCEMENTS.put(advObject, cape);
            }
        }
    }

    private static List<Tuple<ResourceLocation, ResourceLocation>> ctRegisterCapes = new ArrayList<>();

    @Optional.Method(modid = GTValues.MODID_CT)
    @ZenMethod
    public static void registerCape(String advancement, String cape) {
        ctRegisterCapes.add(new Tuple<>(new ResourceLocation(advancement), new ResourceLocation(cape)));
    }

    /**
     * Automatically gives a cape to a player, which may be used for a reward for something other than an advancement
     * DOES NOT SAVE AUTOMATICALLY; PLEASE CALL SAVE AFTER THIS FUNCTION IS USED IF THIS DATA IS MEANT TO PERSIST.
     * @param uuid The UUID of the player to be given the cape.
     * @param cape The ResourceLocation that holds the cape used here.
     */
    public static void unlockCape(UUID uuid, ResourceLocation cape) {
        List<ResourceLocation> capes = UNLOCKED_CAPES.get(uuid);
        if (capes == null) {
            capes = new ArrayList<>();
        } else if (capes.contains(cape))
            return;
        capes.add(cape);
        UNLOCKED_CAPES.put(uuid, capes);
    }

    public static void unlockCapeOnAdvancement(EntityPlayer player, Advancement advancement) {
        if (CAPE_ADVANCEMENTS.containsKey(advancement)) {
            unlockCape(player.getPersistentID(), CAPE_ADVANCEMENTS.get(advancement));
            player.sendMessage(new TextComponentTranslation("gregtech.chat.cape"));
            save();
        }
    }

    public static void clearMaps() {
        UNLOCKED_CAPES.clear();
        WORN_CAPES.clear();
    }

    @SideOnly(Side.CLIENT)
    public static void giveRawCape(UUID uuid, ResourceLocation cape) {
        WORN_CAPES.put(uuid, cape);
    }

    public static void giveCape(UUID uuid, ResourceLocation cape) {
        WORN_CAPES.put(uuid, cape);
        NetworkHandler.channel.sendToAll(new SPacketNotifyCapeChange(uuid, cape).toFMLPacket());
        save();
    }

    // For loading capes when the player logs in, so that it's synced to the clients.
    public static void loadWornCapeOnLogin(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            UUID uuid = player.getPersistentID();
            NetworkHandler.channel.sendToAll(new SPacketNotifyCapeChange(uuid, WORN_CAPES.get(uuid)).toFMLPacket()); // sync to others
            for (EntityPlayerMP otherPlayer : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) { // sync to login
                uuid = otherPlayer.getPersistentID();
                NetworkHandler.channel.sendTo(new SPacketNotifyCapeChange(uuid, WORN_CAPES.get(uuid)).toFMLPacket(), (EntityPlayerMP) player);
            }
        }
    }

    // Runs on login, and looks for any advancements that give the player a cape that the player doesn't already have.
    public static void detectNewCapes(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            for (Map.Entry<Advancement, ResourceLocation> capeEntry : CAPE_ADVANCEMENTS.entrySet()) {
                if ((UNLOCKED_CAPES.get(player.getPersistentID()) == null || !UNLOCKED_CAPES.get(player.getPersistentID()).contains(capeEntry.getValue())) &&
                        ((EntityPlayerMP) player).getAdvancements().getProgress(capeEntry.getKey()).isDone()) {
                    unlockCapeOnAdvancement(player, capeEntry.getKey());
                }
            }
        }
    }

}
