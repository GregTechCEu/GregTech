package gregtech.api.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class GTSoundManager {

    private static final Map<BlockPos, ISound> soundMap = new HashMap<>();

    public static ISound startTileSound(ResourceLocation soundName, float volume, BlockPos pos) {
        ISound s = soundMap.get(pos);
        if (s == null || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(s)) {
            s = new PositionedSoundRecord(soundName, SoundCategory.BLOCKS, volume, 1.0F,
                    true, 0, ISound.AttenuationType.LINEAR, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F) {
                @Override
                public float getVolume() {
                    if (this.sound == null) {
                        this.createAccessor(Minecraft.getMinecraft().getSoundHandler());
                    }
                    return super.getVolume();
                }
            };

            soundMap.put(pos, s);
            Minecraft.getMinecraft().getSoundHandler().playSound(s);
        }
        return s;
    }

    public static void stopTileSound(BlockPos pos) {
        ISound s = soundMap.get(pos);
        if (s != null) {
            Minecraft.getMinecraft().getSoundHandler().stopSound(s);
            soundMap.remove(pos);
        }
    }
}
