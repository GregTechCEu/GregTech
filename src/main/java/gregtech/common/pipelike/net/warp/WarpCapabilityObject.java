package gregtech.common.pipelike.net.warp;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEntityRelay;
import gregtech.api.graphnet.GraphNetUtility;
import gregtech.api.graphnet.net.NetNode;
import gregtech.api.graphnet.pipenet.WorldPipeNode;
import gregtech.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import gregtech.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.traverse.EdgeDirection;
import gregtech.api.graphnet.traverse.EdgeSelector;
import gregtech.api.graphnet.traverse.NetClosestIterator;
import gregtech.common.pipelike.block.warp.WarpDuctTileEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpCapabilityObject implements IPipeCapabilityObject, IEntityRelay {

    public static final int ACTIVE_KEY = 543;

    private final WorldPipeNode node;

    private @Nullable PipeTileEntity tile;

    private boolean transferring = false;

    public WarpCapabilityObject(WorldPipeNode node) {
        this.node = node;
    }

    @Override
    public void init(@NotNull PipeTileEntity tile, @NotNull PipeCapabilityWrapper wrapper) {
        this.tile = tile;
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
        if (capability == GregtechTileCapabilities.CAPABILITY_ENTITY_TRANSFER)
            return GregtechTileCapabilities.CAPABILITY_ENTITY_TRANSFER.cast(this);
        return null;
    }

    @Override
    public boolean receiveEntity(@NotNull Entity entity, @Nullable BlockPos acceptorPos,
                                 @Nullable EnumFacing acceptorFacing) {
        transferring = true;
        EntityTestObject testObject = new EntityTestObject(entity);
        NetClosestIterator iter = new NetClosestIterator(node,
                EdgeSelector.filtered(EdgeDirection.OUTGOING, GraphNetUtility.standardEdgeBlacklist(testObject)));
        while (iter.hasNext()) {
            NetNode next = iter.next();
            if (next instanceof WorldPipeNode destination) {
                IWorldPipeNetTile tile = destination.getTileEntity();
                // first, look for tile destinations
                for (EnumFacing facing : EnumFacing.VALUES) {
                    TileEntity t = tile.getTargetWithCapabilities(destination, facing);
                    if (t != null && !(t instanceof PipeTileEntity)) {
                        IEntityRelay relay = t.getCapability(GregtechTileCapabilities.CAPABILITY_ENTITY_TRANSFER,
                                facing.getOpposite());
                        if (relay != null) {
                            if (relay.receiveEntity(entity, acceptorPos, acceptorFacing)) {
                                transferring = false;
                                return true;
                            }
                        }
                    }
                }
                // second, see if we can teleport the entity
                if (tile instanceof WarpDuctTileEntity warpDuct) {
                    for (EnumFacing facing : warpDuct.validDestinationSides) {
                        if (facing == acceptorFacing && warpDuct.getPos().equals(acceptorPos)) continue;
                        teleportEntity(entity, acceptorPos, acceptorFacing, warpDuct.getPos(), facing);
                        return true;
                    }
                }

            }
        }
        transferring = false;
        return false;
    }

    protected void teleportEntity(@NotNull Entity entity, @Nullable BlockPos acceptorPos,
                                  @Nullable EnumFacing acceptorFacing, @NotNull BlockPos destPos,
                                  @NotNull EnumFacing destFacing) {
        boolean verticalRotation = acceptorFacing == null ||
                acceptorFacing.getAxis().isHorizontal() ^ destFacing.getAxis().isHorizontal();
        // handle velocity and look vector
        // we can't do anything if we have no acceptor facing
        if (acceptorFacing != null) {
            if (acceptorFacing == destFacing) {
                // simple reflection of the velocity component aligned with the facing
                // the look vector can also be reflected, though this is somewhat more complex

                // a rotationYaw of 0 is due south, a rotationYaw of 90 is due west
                if (destFacing.getAxis() == EnumFacing.Axis.X) {
                    entity.motionX *= -1;
                    entity.rotationYaw *= -1;
                }
                if (destFacing.getAxis() == EnumFacing.Axis.Y) {
                    entity.motionY *= -1;
                    // don't invert vertical angle for players, it's very disorienting.
                    if (!(entity instanceof EntityPlayer)) entity.rotationPitch *= -1;
                }
                if (destFacing.getAxis() == EnumFacing.Axis.Z) {
                    entity.motionZ *= -1;
                    entity.rotationYaw = (-(entity.rotationYaw + 90) - 90) % 360;
                }
                // we do nothing to the velocity or look vector if the facings are opposite to each other
            } else if (acceptorFacing.getOpposite() != destFacing) {
                if (!verticalRotation) {
                    // if we are here, we know it is a 90 degree rotation in the horizontal plane.
                    // the x and z components are swapped, and possibly inverted.
                    // the look vector should be rotated as well.
                    double x = entity.motionX;
                    double z = entity.motionZ;
                    int mult = acceptorFacing.getAxis() == EnumFacing.Axis.Z ? 1 : -1;
                    int axi = destFacing.getAxisDirection().getOffset() *
                            acceptorFacing.getAxisDirection().getOffset();
                    entity.motionX = mult * -axi * z;
                    entity.motionZ = mult * axi * x;
                    entity.rotationYaw = (entity.rotationYaw + 90 * axi * mult) % 360;
                } else {
                    // if we are here, it is a transformation from horizontal to vertical or vice versa.
                    // the y component will be swapped with one of the horizontal components, and they may be inverted.
                    // do not bother with look vector rotation.
                    double y = entity.motionY;
                    int axi = acceptorFacing.getAxisDirection().getOffset() * destFacing.getAxisDirection().getOffset();
                    if (acceptorFacing.getAxis() == EnumFacing.Axis.Y) {
                        // vertical to horizontal
                        if (destFacing.getAxis() == EnumFacing.Axis.X) {
                            entity.motionY = entity.motionX * axi;
                            entity.motionX = y * -axi;
                        } else {
                            entity.motionY = entity.motionZ * axi;
                            entity.motionZ = y * -axi;
                        }
                    } else {
                        // horizontal to vertical
                        if (acceptorFacing.getAxis() == EnumFacing.Axis.X) {
                            entity.motionY = entity.motionX * -axi;
                            entity.motionX = y * axi;

                        } else {
                            entity.motionY = entity.motionZ * -axi;
                            entity.motionZ = y * axi;
                        }
                    }
                }
            }
        }
        // handle position
        AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        Vec3d entityPosition = entity.getPositionVector();
        switch (destFacing) {
            case DOWN -> entity.setPositionAndUpdate(destPos.getX() + 0.5,
                    destPos.getY() + entityPosition.y - entityBox.maxY, destPos.getZ() + 0.5);
            case UP -> entity.setPositionAndUpdate(destPos.getX() + 0.5,
                    destPos.getY() + 1 + entityPosition.y - entityBox.minY, destPos.getZ() + 0.5);
            case NORTH -> entity.setPositionAndUpdate(destPos.getX() + 0.5,
                    destPos.getY() + 0.5, destPos.getZ() + entityPosition.z - entityBox.maxZ);
            case SOUTH -> entity.setPositionAndUpdate(destPos.getX() + 0.5,
                    destPos.getY() + 0.5, destPos.getZ() + 1 + entityPosition.z - entityBox.minZ);
            case WEST -> entity.setPositionAndUpdate(destPos.getX() + entityPosition.x - entityBox.maxX,
                    destPos.getY() + 0.5, destPos.getZ() + 0.5);
            case EAST -> entity.setPositionAndUpdate(destPos.getX() + 1 + entityPosition.x - entityBox.minX,
                    destPos.getY() + 0.5, destPos.getZ() + 0.5);
        }
        entity.getEntityWorld().playSound(null, entity.posX, entity.posY, entity.posZ,
                SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (entity.getEntityWorld() instanceof WorldServer server) {
            for (int j = 0; j < 16; ++j) {
                double particleX = entity.posX + (GTValues.RNG.nextDouble() - 0.5) * entity.width * 2.0;
                double particleY = entity.posY + GTValues.RNG.nextDouble() * entity.height;
                double particleZ = entity.posZ + (GTValues.RNG.nextDouble() - 0.5) * entity.width * 2.0;
                server.spawnParticle(EnumParticleTypes.PORTAL, particleX, particleY, particleZ, 0,
                        destPos.getX() + 0.5 + destFacing.getXOffset() * 0.5 - particleX,
                        destPos.getY() + destFacing.getYOffset() * 0.5 - particleY,
                        destPos.getZ() + 0.5 + destFacing.getZOffset() * 0.5 - particleZ, 1);
            }
        }
    }
}
