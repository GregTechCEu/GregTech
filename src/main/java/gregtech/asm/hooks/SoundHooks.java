package gregtech.asm.hooks;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MusicDiscStats;
import gregtech.api.items.metaitem.stats.IItemBehaviour;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@SuppressWarnings("unused")
public class SoundHooks {

    public static void playRecord(IWorldEventListener listener, EntityPlayer player, int type, BlockPos pos, int data) {
        if (type == MusicDiscStats.SOUND_TYPE && FMLCommonHandler.instance().getSide().isClient() &&
                listener instanceof RenderGlobal) {
            for (MetaItem<?> metaItem : MetaItem.getMetaItems()) {
                MetaItem<?>.MetaValueItem valueItem = metaItem.getItem((short) data);
                if (valueItem != null) {
                    for (IItemBehaviour behavior : valueItem.getBehaviours()) {
                        if (behavior instanceof MusicDiscStats) {
                            WorldClient world = ObfuscationReflectionHelper.getPrivateValue(RenderGlobal.class,
                                    (RenderGlobal) listener, "field_72769_h");
                            world.playRecord(pos, ((MusicDiscStats) behavior).getSound());
                            return;
                        }
                    }
                }
            }
        }
        listener.playEvent(player, type, pos, data);
    }
}
