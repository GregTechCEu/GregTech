package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.cover.CoverableView;
import gregtech.api.graphnet.pipenet.WorldPipeNetNode;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public interface IWorldPipeNetTile {

    // universal (mostly for active nodes)

    EnumMap<EnumFacing, TileEntity> getTargetsWithCapabilities(WorldPipeNetNode destination);

    CoverableView getCoverHolder();

    // fluid piping

    void spawnParticles(EnumFacing direction, EnumParticleTypes particleType, int particleCount);

    void dealAreaDamage(int size, Consumer<EntityLivingBase> damageFunction);

    void playLossSound();

    void visuallyExplode();

    void setNeighborsToFire();
}
