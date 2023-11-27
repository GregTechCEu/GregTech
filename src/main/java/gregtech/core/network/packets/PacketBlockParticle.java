package gregtech.core.network.packets;

import gregtech.api.block.ICustomParticleBlock;
import gregtech.api.network.IClientExecutor;
import gregtech.api.network.IPacket;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.vec.Vector3;

public class PacketBlockParticle implements IPacket, IClientExecutor {

    private BlockPos blockPos;
    private Vector3 entityPos;
    private int particlesAmount;

    @SuppressWarnings("unused")
    public PacketBlockParticle() {}

    public PacketBlockParticle(BlockPos blockPos, Vector3 entityPos, int particlesAmount) {
        this.blockPos = blockPos;
        this.entityPos = entityPos;
        this.particlesAmount = particlesAmount;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBlockPos(blockPos);
        buf.writeFloat((float) entityPos.x);
        buf.writeFloat((float) entityPos.y);
        buf.writeFloat((float) entityPos.z);
        buf.writeVarInt(particlesAmount);
    }

    @Override
    public void decode(PacketBuffer buf) {
        this.blockPos = buf.readBlockPos();
        this.entityPos = new Vector3(buf.readFloat(), buf.readFloat(), buf.readFloat());
        this.particlesAmount = buf.readVarInt();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void executeClient(NetHandlerPlayClient handler) {
        World world = Minecraft.getMinecraft().world;
        IBlockState blockState = world.getBlockState(blockPos);
        ParticleManager particleManager = Minecraft.getMinecraft().effectRenderer;
        ((ICustomParticleBlock) blockState.getBlock()).handleCustomParticle(world, blockPos, particleManager, entityPos,
                particlesAmount);
    }
}
