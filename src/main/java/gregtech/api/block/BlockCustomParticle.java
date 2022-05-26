package gregtech.api.block;

import codechicken.lib.vec.Vector3;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.NetworkUtils;
import gregtech.api.net.packets.SPacketBlockParticle;
import gregtech.api.util.ParticleHandlerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public abstract class BlockCustomParticle extends Block implements ICustomParticleBlock {

    public BlockCustomParticle(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockCustomParticle(Material materialIn) {
        super(materialIn);
    }

    @SideOnly(Side.CLIENT)
    protected abstract Pair<TextureAtlasSprite, Integer> getParticleTexture(World world, BlockPos blockPos);

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(@Nonnull IBlockState state, @Nonnull World worldObj, RayTraceResult target, @Nonnull ParticleManager manager) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(worldObj, target.getBlockPos());
        ParticleHandlerUtil.addHitEffects(state, worldObj, target, atlasSprite.getLeft(), atlasSprite.getRight(), manager);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull ParticleManager manager) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(world, pos);
        ParticleHandlerUtil.addBlockDestroyEffects(world.getBlockState(pos), world, pos, atlasSprite.getLeft(), atlasSprite.getRight(), manager);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleCustomParticle(World worldObj, BlockPos blockPos, ParticleManager particleManager, Vector3 entityPos, int numberOfParticles) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(worldObj, blockPos);
        ParticleHandlerUtil.addBlockLandingEffects(worldObj, entityPos, atlasSprite.getLeft(), atlasSprite.getRight(), particleManager, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull Entity entity) {
        if (world.isRemote) {
            Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(world, pos);
            ParticleHandlerUtil.addBlockRunningEffects(world, entity, atlasSprite.getLeft(), atlasSprite.getRight());
        }
        return true;
    }

    @Override
    public boolean addLandingEffects(@Nonnull IBlockState state, @Nonnull WorldServer worldObj, @Nonnull BlockPos blockPosition, @Nonnull IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
        SPacketBlockParticle
                packet = new SPacketBlockParticle(blockPosition, new Vector3(entity.posX, entity.posY, entity.posZ), numberOfParticles);
        NetworkHandler.channel.sendToAllTracking(packet.toFMLPacket(), NetworkUtils.blockPoint(worldObj, blockPosition));
        return true;
    }
}
