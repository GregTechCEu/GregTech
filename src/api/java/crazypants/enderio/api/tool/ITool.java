package crazypants.enderio.api.tool;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;

public interface ITool extends IHideFacades {

    boolean canUse(@Nonnull EnumHand stack, @Nonnull EntityPlayer player, @Nonnull BlockPos pos);

    void used(@Nonnull EnumHand stack, @Nonnull EntityPlayer player, @Nonnull BlockPos pos);
}
