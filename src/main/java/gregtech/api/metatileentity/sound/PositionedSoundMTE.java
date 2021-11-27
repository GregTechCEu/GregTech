package gregtech.api.metatileentity.sound;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PositionedSoundMTE extends PositionedSound implements ITickableSound {

    protected ISoundCreator mte;

    public PositionedSoundMTE(ResourceLocation soundId, SoundCategory categoryIn, ISoundCreator mteIn, BlockPos pos) {
        super(soundId, categoryIn);
        this.repeat = true;
        this.repeatDelay = 0;
        this.mte = mteIn;
        this.xPosF = pos.getX();
        this.yPosF = pos.getY();
        this.zPosF = pos.getZ();
    }

    @Override
    public void update() {
        volume = mte.canCreateSound() && !mte.isMuffled() ? 1 : 0;
    }

    @Override
    public boolean isDonePlaying() {
        return !this.mte.isValid();
    }
}
