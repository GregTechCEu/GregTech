package gregtech.common.entities;

import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Section of the mining pipe. The pipe should be divided into multiple segments due to naive algorithm used in AABB
 * checks making exceptionally large bounding boxes not work properly
 */
public class MiningPipeEntity extends Entity {

    private static final DataParameter<Integer> Y = EntityDataManager.createKey(MiningPipeEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LENGTH = EntityDataManager.createKey(MiningPipeEntity.class, DataSerializers.VARINT);

    @Nullable
    private MetaTileEntity mte;

    private int prevLength = -1;

    public MiningPipeEntity(@Nonnull World world) {
        super(world);
        this.setSize(.5f, 0);
        this.setNoGravity(true);
        this.noClip = true;
        this.preventEntitySpawning = true;
        this.setEntityInvulnerable(true);
    }

    public MiningPipeEntity(@Nonnull MetaTileEntity mte, BlockPos origin) {
        this(mte.getWorld());
        this.mte = mte;
        this.setPosition(origin.getX() + .5, origin.getY(), origin.getZ() + .5);
    }

    public void setLength(int y, int length) {
        this.dataManager.set(Y, y);
        this.dataManager.set(LENGTH, length);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(Y, 0);
        this.dataManager.register(LENGTH, 0);
    }

    @Override
    public void onUpdate() {
        if (!this.world.isRemote) {
            if (this.mte == null || !this.mte.isValid()) {
                setDead();
                return;
            }
        }

        int length = this.dataManager.get(LENGTH);
        if (length != this.prevLength) {
            this.prevLength = length;
            if (!this.world.isRemote) {
                setPosition(this.posX, this.dataManager.get(Y) - length, this.posZ);
            }
            setSize(.5f, length);
        }

        this.firstUpdate = false;
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Nonnull
    @Override
    public EnumPushReaction getPushReaction() {
        return EnumPushReaction.IGNORE;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(@Nonnull Entity entity) {
        return entity.getEntityBoundingBox(); // TODO maybe avoiding pushable check on the entity would allow pipes to stop stuffs like arrow?
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return this.getEntityBoundingBox();
    }

    @Override
    public Entity changeDimension(int dim, @Nonnull ITeleporter teleporter) {
        return this;
    }

    @Override
    protected void readEntityFromNBT(@Nonnull NBTTagCompound tag) {}

    @Override
    protected void writeEntityToNBT(@Nonnull NBTTagCompound tag) {}

    // @Override
    // public boolean shouldRenderInPass(int pass) {
    //     return false;
    // }
}
