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
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLCommonHandler;

import com.mojang.authlib.GameProfile;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class GregFakePlayer extends EntityPlayer {

    private static final GameProfile GREGTECH = new GameProfile(UUID.fromString("518FDF18-EC2A-4322-832A-58ED1721309B"),
            "[GregTech]");
    private static WeakReference<FakePlayer> GREGTECH_PLAYER = null;

    public static FakePlayer get(WorldServer world) {
        FakePlayer ret = GREGTECH_PLAYER != null ? GREGTECH_PLAYER.get() : null;
        if (ret == null) {
            ret = FakePlayerFactory.get(world, GREGTECH);
            GREGTECH_PLAYER = new WeakReference<>(ret);
        }
        return ret;
    }

    public GregFakePlayer(World worldIn) {
        super(worldIn, GREGTECH);
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
