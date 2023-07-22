package gregtech.mixins.minecraft;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MusicDiscStats;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class RenderGlobalMixin {

    @Shadow
    private WorldClient world;

    @Inject(method = "playEvent", at = @At("HEAD"), cancellable = true)
    public void playMusicDisc(EntityPlayer player, int type, BlockPos blockPosIn, int data, CallbackInfo ci) {
        if (type == MusicDiscStats.SOUND_TYPE) {
            for (MetaItem<?> metaItem : MetaItem.getMetaItems()) {
                MetaItem<?>.MetaValueItem valueItem = metaItem.getItem((short) data);
                if (valueItem != null) {
                    for (IItemBehaviour behavior : valueItem.getBehaviours()) {
                        if (behavior instanceof MusicDiscStats) {
                            world.playRecord(blockPosIn, ((MusicDiscStats) behavior).getSound());
                            ci.cancel();
                            return;
                        }
                    }

                }
            }
        }
    }
}
