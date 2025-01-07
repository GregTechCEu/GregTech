package gregtech.common.pipelike.block.warp;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEntityRelay;
import gregtech.api.graphnet.pipenet.physical.tile.PipeCapabilityWrapper;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class WarpDuctTileEntity extends PipeTileEntity {

    public final EnumSet<EnumFacing> validDestinationSides = EnumSet.noneOf(EnumFacing.class);

    public void handleEntityCollision(Entity entity) {
        if (entity.timeUntilPortal > 0) return;
        AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        for (int i = 0; i < 10; i++) {
            float offset = (1 - getStructure().getRenderThickness()) / 2;
            BlockPos pos = getPos();
            for (EnumFacing facing : EnumFacing.VALUES) {
                IEntityRelay relay = getCapability(GregtechTileCapabilities.CAPABILITY_ENTITY_TRANSFER, facing);
                if (relay != null && isConnected(facing) && !isBlocked(facing)) {
                    BlockPos testPos = getPos().offset(facing);
                    if (getWorld().getBlockState(testPos).getCollisionBoundingBox(getWorld(), testPos) ==
                            Block.NULL_AABB) {
                        boolean xAxis = facing.getAxis() == EnumFacing.Axis.X;
                        boolean yAxis = facing.getAxis() == EnumFacing.Axis.Y;
                        boolean zAxis = facing.getAxis() == EnumFacing.Axis.Z;
                        int axisOffset = facing.getAxisDirection().getOffset();
                        if (entityBox.intersects(pos.getX() + (xAxis ? axisOffset * 0.5 + 0.5 : offset),
                                pos.getY() + (yAxis ? axisOffset * 0.5 + 0.5 : offset),
                                pos.getZ() + (zAxis ? axisOffset * 0.5 + 0.5 : offset),
                                pos.getX() + (xAxis ? axisOffset * 0.5 + 0.5 : 1 - offset),
                                pos.getY() + (yAxis ? axisOffset * 0.5 + 0.5 : 1 - offset),
                                pos.getZ() + (zAxis ? axisOffset * 0.5 + 0.5 : 1 - offset))) {
                            double oldPosX = entity.posX;
                            double oldPosY = entity.posY;
                            double oldPosZ = entity.posZ;

                            double oldX = entity.motionX;
                            double oldY = entity.motionY;
                            double oldZ = entity.motionZ;
                            // approximate the velocity before collision by using prev position
                            entity.motionX = entity.posX - entity.prevPosX;
                            entity.motionY = entity.posY - entity.prevPosY;
                            entity.motionZ = entity.posZ - entity.prevPosZ;
                            if (relay.receiveEntity(entity, getPos(), facing)) {
                                entity.timeUntilPortal = Math.max(entity.getPortalCooldown(), 30);
                                if (entity instanceof EntityPlayerMP) {
                                    ((EntityPlayerMP) entity).connection.sendPacket(new SPacketEntityVelocity(entity));
                                }
                                this.world.playSound(null, entity.prevPosX, entity.prevPosY, entity.prevPosZ,
                                        SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                                if (world instanceof WorldServer server) {
                                    for (int j = 0; j < 16; ++j) {
                                        double particleX = oldPosX +
                                                (GTValues.RNG.nextDouble() - 0.5) * entity.width * 2.0;
                                        double particleY = oldPosY + GTValues.RNG.nextDouble() * entity.height;
                                        double particleZ = oldPosZ +
                                                (GTValues.RNG.nextDouble() - 0.5) * entity.width * 2.0;
                                        server.spawnParticle(EnumParticleTypes.PORTAL, particleX, particleY, particleZ,
                                                0,
                                                pos.getX() + 0.5 + facing.getXOffset() * 0.5 - particleX,
                                                pos.getY() + facing.getYOffset() * 0.5 - particleY,
                                                pos.getZ() + 0.5 + facing.getZOffset() * 0.5 - particleZ, -1);
                                    }
                                }
                            } else {
                                entity.motionX = oldX;
                                entity.motionY = oldY;
                                entity.motionZ = oldZ;
                            }
                            return;
                        }
                    }
                }
            }
            // successively grow the entity's box until we determine the hit side, or grow it 9 times.
            entityBox = entityBox.grow(0.05);
        }
    }

    @Override
    public void setBlocked(EnumFacing facing) {
        super.setBlocked(facing);
        updateWarpStatus(facing);
    }

    @Override
    public void setUnblocked(EnumFacing facing) {
        super.setUnblocked(facing);
        updateWarpStatus(facing);
    }

    @Override
    public void updateActiveStatus(@Nullable EnumFacing facing, boolean canOpenConnection) {
        super.updateActiveStatus(facing, canOpenConnection);
        if (facing != null) updateWarpStatus(facing);
    }

    protected void updateWarpStatus(@NotNull EnumFacing facing) {
        // two tasks: update entity detection for submitting to warpnet, and update destination status.
        if (isConnectedCoverAdjusted(facing)) {
            BlockPos pos = getPos().offset(facing);
            if (getWorld().getBlockState(pos).getCollisionBoundingBox(getWorld(), pos) == Block.NULL_AABB) {
                validDestinationSides.add(facing);
                for (PipeCapabilityWrapper cap : netCapabilities.values()) {
                    if (cap.matchesCapability(GregtechTileCapabilities.CAPABILITY_ENTITY_TRANSFER)) {
                        cap.setActive(facing);
                    }
                }
                return;
            }
        }
        validDestinationSides.remove(facing);
        for (PipeCapabilityWrapper cap : netCapabilities.values()) {
            if (cap.matchesCapability(GregtechTileCapabilities.CAPABILITY_ENTITY_TRANSFER)) {
                cap.setIdle(facing);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRenderFace(EnumFacing facing) {
        return this.getBlockType().getDefaultState().shouldSideBeRendered(getWorld(), this.getPos(), facing);
    }
}
