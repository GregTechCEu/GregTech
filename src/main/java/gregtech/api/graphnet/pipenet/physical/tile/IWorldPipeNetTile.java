package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.cover.CoverableView;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.function.Consumer;

public interface IWorldPipeNetTile {

    // universal (mostly for active nodes)

    @NotNull
    EnumMap<EnumFacing, TileEntity> getTargetsWithCapabilities(WorldPipeNetNode destination);

    @Nullable
    TileEntity getTargetWithCapabilities(WorldPipeNetNode destination, EnumFacing facing);

    PipeCapabilityWrapper getWrapperForNode(WorldPipeNetNode node);

    @NotNull
    CoverableView getCoverHolder();

    // fluid piping

    void spawnParticles(EnumFacing direction, EnumParticleTypes particleType, int particleCount);

    void dealAreaDamage(int size, Consumer<EntityLivingBase> damageFunction);

    void playLossSound();

    void visuallyExplode();

    void setNeighborsToFire();
}
