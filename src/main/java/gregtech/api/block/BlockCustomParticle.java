package gregtech.api.block;

import gregtech.api.GregTechAPI;
import gregtech.api.util.ParticleHandlerUtil;
import gregtech.core.network.NetworkUtils;
import gregtech.core.network.packets.PacketBlockParticle;

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

import codechicken.lib.vec.Vector3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

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
    public boolean addHitEffects(@NotNull IBlockState state, @NotNull World worldObj, RayTraceResult target,
                                 @NotNull ParticleManager manager) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(worldObj, target.getBlockPos());
        ParticleHandlerUtil.addHitEffects(state, worldObj, target, atlasSprite.getLeft(), atlasSprite.getRight(),
                manager);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(@NotNull World world, @NotNull BlockPos pos, @NotNull ParticleManager manager) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(world, pos);
        ParticleHandlerUtil.addBlockDestroyEffects(world.getBlockState(pos), world, pos, atlasSprite.getLeft(),
                atlasSprite.getRight(), manager);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleCustomParticle(World worldObj, BlockPos blockPos, ParticleManager particleManager,
                                     Vector3 entityPos, int numberOfParticles) {
        Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(worldObj, blockPos);
        ParticleHandlerUtil.addBlockLandingEffects(worldObj, entityPos, atlasSprite.getLeft(), atlasSprite.getRight(),
                particleManager, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(@NotNull IBlockState state, World world, @NotNull BlockPos pos,
                                     @NotNull Entity entity) {
        if (world.isRemote) {
            Pair<TextureAtlasSprite, Integer> atlasSprite = getParticleTexture(world, pos);
            ParticleHandlerUtil.addBlockRunningEffects(world, entity, atlasSprite.getLeft(), atlasSprite.getRight());
        }
        return true;
    }

    @Override
    public boolean addLandingEffects(@NotNull IBlockState state, @NotNull WorldServer worldObj,
                                     @NotNull BlockPos blockPosition, @NotNull IBlockState iblockstate,
                                     EntityLivingBase entity, int numberOfParticles) {
        PacketBlockParticle packet = new PacketBlockParticle(blockPosition,
                new Vector3(entity.posX, entity.posY, entity.posZ), numberOfParticles);
        GregTechAPI.networkHandler.sendToAllTracking(packet, NetworkUtils.blockPoint(worldObj, blockPosition));
        return true;
    }
}
