package gregtech.integration.ftb.utility;

import gregtech.api.util.Mods;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class FTBChunksHelper {

    private FTBChunksHelper() {}

    /**
     * Check if a {@link BlockPos} is modifiable by a player abiding by FTB chunk rules, if this block is in a claimed
     * chunk. <br/>
     * Will always return {@code true} if
     * <a href="https://www.curseforge.com/minecraft/mc-mods/ftb-utilities-forge">FTBUtilities</a> isn't present or the
     * {@link BlockPos} isn't in a claimed chunk.
     */
    public static boolean isBlockModifiableByPlayer(@NotNull World world, @NotNull BlockPos blockPos,
                                                    @NotNull EntityPlayer player) {
        return isBlockModifiableByPlayer(world, blockPos, player.getUniqueID());
    }

    /**
     * Check if a {@link BlockPos} is modifiable by a player abiding by FTB chunk rules, if this block is in a claimed
     * chunk. <br/>
     * Will always return {@code true} if
     * <a href="https://www.curseforge.com/minecraft/mc-mods/ftb-utilities-forge">FTBUtilities</a> isn't present or the
     * {@link BlockPos} isn't in a claimed chunk.
     */
    public static boolean isBlockModifiableByPlayer(@NotNull World world, @NotNull BlockPos blockPos,
                                                    @NotNull UUID playerID) {
        if (!Mods.FTB_UTILITIES.isModLoaded()) return true;

        ClaimedChunks instance = ClaimedChunks.instance;
        ClaimedChunk claimedChunk = instance.getChunk(new ChunkDimPos(blockPos, world.provider.getDimension()));
        if (claimedChunk != null) {
            ForgePlayer forgePlayer = instance.universe.getPlayer(playerID);
            EnumTeamStatus status = claimedChunk.getData().getEditBlocksStatus();
            return !claimedChunk.getTeam().hasStatus(forgePlayer, status);
        }

        return true;
    }
}
