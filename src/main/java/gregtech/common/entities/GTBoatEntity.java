package gregtech.common.entities;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.wood.BlockGregPlanks;
import gregtech.common.items.MetaItems;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class GTBoatEntity extends EntityBoat {

    private static final DataParameter<Integer> GT_BOAT_TYPE = EntityDataManager.createKey(GTBoatEntity.class,
            DataSerializers.VARINT);

    public GTBoatEntity(World world) {
        super(world);
    }

    public GTBoatEntity(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(GT_BOAT_TYPE, 0);
    }

    protected void dropBoatItems(boolean destroyed) {
        switch (this.getGTBoatType()) {
            case RUBBER_WOOD_BOAT:
                if (destroyed) {
                    this.entityDropItem(MetaBlocks.PLANKS.getItemVariant(BlockGregPlanks.BlockType.RUBBER_PLANK, 3), 0);
                    this.entityDropItem(new ItemStack(Items.STICK, 2), 0);
                } else {
                    this.entityDropItem(MetaItems.RUBBER_WOOD_BOAT.getStackForm(), 0);
                }
                break;
            case TREATED_WOOD_BOAT:
                this.entityDropItem(MetaItems.TREATED_WOOD_BOAT.getStackForm(), 0);
                break;
        }
    }

    // override to change boat drop
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (!this.world.isRemote && !this.isDead) {
            if (source instanceof EntityDamageSourceIndirect && source.getTrueSource() != null &&
                    this.isPassenger(source.getTrueSource())) {
                return false;
            }
            this.setForwardDirection(-this.getForwardDirection());
            this.setTimeSinceHit(10);
            this.setDamageTaken(this.getDamageTaken() + amount * 10);
            this.markVelocityChanged();
            boolean flag = source.getTrueSource() instanceof EntityPlayer &&
                    ((EntityPlayer) source.getTrueSource()).capabilities.isCreativeMode;

            if (flag || this.getDamageTaken() > 40) {
                if (!flag && this.world.getGameRules().getBoolean("doEntityDrops")) {
                    dropBoatItems(false);
                }

                this.setDead();
            }

        }
        return true;
    }

    // override to change boat drop
    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.lastYd = this.motionY;

        if (!this.isRiding()) {
            if (onGroundIn) {
                if (this.fallDistance > 3) {
                    if (this.status != EntityBoat.Status.ON_LAND) {
                        this.fallDistance = 0;
                        return;
                    }

                    this.fall(this.fallDistance, 1);

                    if (!this.world.isRemote && !this.isDead) {
                        this.setDead();
                        if (this.world.getGameRules().getBoolean("doEntityDrops")) {
                            dropBoatItems(true);
                        }
                    }
                }

                this.fallDistance = 0;
            } else if (this.world.getBlockState((new BlockPos(this)).down()).getMaterial() != Material.WATER && y < 0) {
                this.fallDistance = (float) ((double) this.fallDistance - y);
            }
        }
    }

    @Override
    public ItemStack getPickedResult(RayTraceResult ray) {
        switch (getGTBoatType()) {
            case RUBBER_WOOD_BOAT:
                return MetaItems.RUBBER_WOOD_BOAT.getStackForm();
            case TREATED_WOOD_BOAT:
                return MetaItems.TREATED_WOOD_BOAT.getStackForm();
            default:
                return ItemStack.EMPTY;
        }
    }

    /**
     * @deprecated Vanilla boat types do not affect GTBoat instances; use
     *             {@link GTBoatEntity#setGTBoatType(GTBoatType)}.
     */
    @Deprecated
    @Override
    public void setBoatType(Type type) {
        super.setBoatType(type);
    }

    /**
     * @deprecated Vanilla boat types do not affect GTBoat instances; use {@link GTBoatEntity#getGTBoatType()}.
     */
    @Deprecated
    @Override
    public Type getBoatType() {
        return super.getBoatType();
    }

    public void setGTBoatType(GTBoatType type) {
        this.dataManager.set(GT_BOAT_TYPE, type.ordinal());
    }

    public GTBoatType getGTBoatType() {
        switch (this.dataManager.get(GT_BOAT_TYPE)) {
            case 1:
                return GTBoatType.TREATED_WOOD_BOAT;
            case 0:
            default:
                return GTBoatType.RUBBER_WOOD_BOAT;
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setString("GTType", this.getGTBoatType().toString());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        String type = tag.getString("GTType");
        if (type.equalsIgnoreCase("TREATED_WOOD_BOAT")) {
            this.setGTBoatType(GTBoatType.TREATED_WOOD_BOAT);
        } else {
            this.setGTBoatType(GTBoatType.RUBBER_WOOD_BOAT);
        }
    }

    public enum GTBoatType {
        RUBBER_WOOD_BOAT,
        TREATED_WOOD_BOAT
    }
}
