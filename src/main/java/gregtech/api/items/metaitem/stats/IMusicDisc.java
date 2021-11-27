package gregtech.api.items.metaitem.stats;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMusicDisc extends IItemComponent {

    @SideOnly(Side.CLIENT)
    SoundEvent getSound();
}
