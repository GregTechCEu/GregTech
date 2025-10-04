package gregtech.api.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;

public class GregFakePlayer extends EntityPlayer {

    private static final String FAKE_NAME = "[GregTech]";
    private static final UUID FAKE_UUID = UUID.fromString("518FDF18-EC2A-4322-832A-58ED1721309B");
    private static final GameProfile FAKE_PROFILE = new GameProfile(FAKE_UUID, FAKE_NAME);
    private static final Map<UUID, GameProfile> PROFILE_CACHE = new Object2ObjectOpenHashMap<>();
    private static final Map<UUID, WeakReference<FakePlayer>> GREGTECH_PLAYERS = new Object2ObjectOpenHashMap<>();

    public static @NotNull FakePlayer get(@NotNull WorldServer world) {
        return get(world, FAKE_PROFILE);
    }

    public static @NotNull FakePlayer get(@NotNull WorldServer world, @Nullable UUID fakePlayerUUID) {
        return get(world, fakePlayerUUID == null ? FAKE_PROFILE :
                PROFILE_CACHE.computeIfAbsent(fakePlayerUUID, id -> new GameProfile(id, FAKE_NAME)));
    }

    public static @NotNull FakePlayer get(@NotNull WorldServer world, @NotNull GameProfile fakePlayerProfile) {
        UUID id = fakePlayerProfile.getId();
        if (GREGTECH_PLAYERS.containsKey(id)) {
            FakePlayer fakePlayer = GREGTECH_PLAYERS.get(id).get();
            if (fakePlayer != null) {
                return fakePlayer;
            }
        }

        FakePlayer fakePlayer = new FakePlayer(world, fakePlayerProfile);
        GREGTECH_PLAYERS.put(id, new WeakReference<>(fakePlayer));
        return fakePlayer;
    }

    public GregFakePlayer(@NotNull World worldIn) {
        super(worldIn, FAKE_PROFILE);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public Vec3d getPositionVector() {
        return new Vec3d(0, 0, 0);
    }

    @Override
    public boolean canUseCommand(int i, String s) {
        return false;
    }

    @Override
    public void sendStatusMessage(ITextComponent chatComponent, boolean actionBar) {}

    @Override
    public void sendMessage(ITextComponent component) {}

    @Override
    public void addStat(StatBase par1StatBase, int par2) {}

    @Override
    public void openGui(Object mod, int modGuiId, World world, int x, int y, int z) {}

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return true;
    }

    @Override
    public boolean canAttackPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void onDeath(DamageSource source) {/**/}

    @Override
    public void onUpdate() {/**/}

    @Override
    public Entity changeDimension(int dim, ITeleporter teleporter) {
        return this;
    }

    @Override
    public MinecraftServer getServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance();
    }

    @Override
    protected void playEquipSound(ItemStack stack) {}
}
