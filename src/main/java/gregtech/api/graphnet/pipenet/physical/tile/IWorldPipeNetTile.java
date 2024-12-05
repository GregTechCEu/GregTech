package gregtech.api.graphnet.pipenet.physical.tile;

import gregtech.api.cover.CoverableView;
import gregtech.api.graphnet.pipenet.WorldPipeNode;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;

import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.function.Consumer;

public interface IWorldPipeNetTile extends ICapabilityProvider {

    @NotNull
    EnumMap<EnumFacing, TileEntity> getTargetsWithCapabilities(WorldPipeNode destination);

    @Nullable
    TileEntity getTargetWithCapabilities(WorldPipeNode destination, EnumFacing facing);

    PipeCapabilityWrapper getWrapperForNode(WorldPipeNode node);

    @NotNull
    CoverableView getCoverHolder();
}
