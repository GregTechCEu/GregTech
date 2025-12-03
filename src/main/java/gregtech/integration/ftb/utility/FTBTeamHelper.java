package gregtech.integration.ftb.utility;

import gregtech.api.util.Mods;

import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;

import java.util.UUID;

public class FTBTeamHelper {

    public static boolean isSameTeam(UUID first, UUID second) {
        return Mods.FTB_UTILITIES.isModLoaded() && FTBLibAPI.arePlayersInSameTeam(first, second);
    }
}
