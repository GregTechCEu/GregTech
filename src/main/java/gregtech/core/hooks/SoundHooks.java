package gregtech.core.hooks;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MusicDiscStats;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class SoundHooks {

    public static void playRecord(RenderGlobal renderGlobal, int type, BlockPos pos, int data) {
        if (type == 1010) {
            WorldClient world = ObfuscationReflectionHelper.getPrivateValue(RenderGlobal.class, renderGlobal, "field_72769_h");

            // Check if it is a normal Item first, to avoid going to the main method
            if (Item.getItemById(data) instanceof ItemRecord) {
                world.playRecord(pos, ((ItemRecord)Item.getItemById(data)).getSound());
                return;
            }

            // Then check if it is a MetaItem
            for (MetaItem<?> metaItem : MetaItem.getMetaItems()) {
                MetaItem<?>.MetaValueItem valueItem = metaItem.getItem((short) data);
                if (valueItem != null) {
                    for (IItemBehaviour behavior : valueItem.getBehaviours()) {
                        if (behavior instanceof MusicDiscStats) {
                            world.playRecord(pos, ((MusicDiscStats) behavior).getSound());
                            return;
                        }
                    }
                }
            }
            // In this case, an Event with the correct code was fired, but is an invalid record to play.
            world.playRecord(pos, null);
        }
    }
}
