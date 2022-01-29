package gregtech.api.sound;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GTSoundManager {

    private static final Object2ObjectMap<BlockPos, ISound> soundMap = new Object2ObjectOpenHashMap<>();

    public static ISound startTileSound(ResourceLocation soundName, float volume, BlockPos pos) {
        ISound s = soundMap.get(pos);
        if (s == null || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(s)) {
            s = new PositionedSoundRecord(soundName, SoundCategory.BLOCKS, volume, 1.0F,
                    true, 0, ISound.AttenuationType.LINEAR, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);

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
