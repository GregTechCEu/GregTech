package gregtech.common.entities;

import gregtech.api.util.TeleportHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public class PortalEntity extends Entity {

    private double TargetX = 0;
    private double TargetY = 0;
    private double TargetZ = 0;

    private int TargetDim = 0;

    public PortalEntity(World worldIn){
        super(worldIn);
        rideCooldown = -1;
    }

    public PortalEntity(World worldIn, double x, double y, double z){
        super(worldIn);
        this.setPosition(x, y, z);
        rideCooldown = -1;
    }

    @Override
    protected void entityInit(){}

    @Override
    public void readEntityFromNBT(NBTTagCompound nbtTagCompound){
        this.TargetX = nbtTagCompound.getDouble("TargetX");
        this.TargetY = nbtTagCompound.getDouble("TargetY");
        this.TargetZ = nbtTagCompound.getDouble("TargetZ");
        this.TargetDim = nbtTagCompound.getInteger("TargetDim");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbtTagCompound){
        nbtTagCompound.setDouble("TargetX", this.TargetX);
        nbtTagCompound.setDouble("TargetY", this.TargetY);
        nbtTagCompound.setDouble("TargetZ", this.TargetZ);
        nbtTagCompound.setInteger("TargetDim", this.TargetDim);
    }

    @Override
    public void onUpdate() {
        if (!this.world.isRemote) {
            this.setFlag(6, this.isGlowing());
        }

        this.onEntityUpdate();
        List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), null);
        for(Entity entity : list){
            if (!(entity instanceof PortalEntity)) {
                TeleportHandler.teleport(entity, TargetDim, TargetX + entity.getLookVec().x, TargetY, TargetZ + entity.getLookVec().z);
            }
        }
    }

    @Override
    public void setRotation(float yaw, float pitch){
        super.setRotation(yaw, pitch);
    }

    public void setTargetCoordinates(int dimension, double x, double y, double z) {
        this.TargetDim = dimension;
        this.TargetX = x;
        this.TargetY = y;
        this.TargetZ = z;
    }
}
