package gregtech.api.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class GregFakePlayer {

    private static final GameProfile GREGTECH = new GameProfile(UUID.fromString("518FDF18-EC2A-4322-832A-58ED1721309B"), "[GregTech]");
    private static WeakReference<FakePlayer> GREGTECH_PLAYER = null;

    public static FakePlayer get(WorldServer world) {
        FakePlayer ret = GREGTECH_PLAYER != null ? GREGTECH_PLAYER.get() : null;
        if (ret == null) {
            ret = FakePlayerFactory.get(world, GREGTECH);
            GREGTECH_PLAYER = new WeakReference<>(ret);
        }
        return ret;
    }

}
