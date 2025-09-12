package gregtech.api.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import org.jetbrains.annotations.NotNull;

public class FTBChunksUtil {

    public static boolean isBlockModifiableByPlayer(@NotNull World world, @NotNull BlockPos blockPos,
                                                    @NotNull EntityPlayer player) {
        if (!Mods.FTB_UTILITIES.isModLoaded()) return true;

        ClaimedChunks instance = ClaimedChunks.instance;
        ClaimedChunk claimedChunk = instance.getChunk(new ChunkDimPos(blockPos, world.provider.getDimension()));
        if (claimedChunk != null) {
            ForgePlayer forgePlayer = instance.universe.getPlayer(player.getUniqueID());
            EnumTeamStatus status = claimedChunk.getData().getEditBlocksStatus();
            return !claimedChunk.getTeam().hasStatus(forgePlayer, status);
        }

        return true;
    }
}
