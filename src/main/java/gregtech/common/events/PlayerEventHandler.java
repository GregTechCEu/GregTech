package gregtech.common.events;

import gregtech.api.GTValues;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.items.ItemHandlerHelper;

@Mod.EventBusSubscriber
public class PlayerEventHandler {
    private static final String HAS_TERMINAL = GTValues.MODID + ".terminal";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ConfigHolder.U.spawnTerminal) {
            NBTTagCompound playerData = event.player.getEntityData();
            NBTTagCompound data = playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG) ? playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG) : new NBTTagCompound();

            if (!data.getBoolean(HAS_TERMINAL)) {
                ItemHandlerHelper.giveItemToPlayer(event.player, MetaItems.TERMINAL.getStackForm());
                data.setBoolean(HAS_TERMINAL, true);
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            }
        }
    }
}
