package gregtech.common.entities;

import gregtech.api.sound.GTSounds;
import gregtech.api.util.GTTeleporter;
import gregtech.api.util.TeleportHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

import static gregtech.api.metatileentity.IFastRenderMetaTileEntity.RENDER_PASS_TRANSLUCENT;

public class PortalEntity extends Entity {

    private double targetX = 0;
    private double targetY = 0;
    private double targetZ = 0;

    private int targetDim = 0;

    private int timeToDespawn = 200;

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
        this.targetX = nbtTagCompound.getDouble("targetX");
        this.targetY = nbtTagCompound.getDouble("targetY");
        this.targetZ = nbtTagCompound.getDouble("targetZ");
        this.targetDim = nbtTagCompound.getInteger("targetDim");
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbtTagCompound){
        nbtTagCompound.setDouble("targetX", this.targetX);
        nbtTagCompound.setDouble("targetY", this.targetY);
        nbtTagCompound.setDouble("targetZ", this.targetZ);
        nbtTagCompound.setInteger("targetDim", this.targetDim);
    }

    @Override
    public void onUpdate() {
        if(timeToDespawn <= 0){
            this.setDead();
        }
        if (!this.world.isRemote) {
            this.setFlag(6, this.isGlowing());
        }

        if(timeToDespawn == 200){
            this.playSound(GTSounds.PORTAL_OPENING, 0.7F, 1.F);
        }else if(timeToDespawn == 10){
            this.playSound(GTSounds.PORTAL_CLOSING, 0.7F, 1.F);
        }

        this.onEntityUpdate();

        if(!this.world.isRemote) {
            List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), null);
            for (Entity entity : list) {
                if (!(entity instanceof PortalEntity)) {
                    GTTeleporter teleporter = new GTTeleporter(TeleportHandler.getWorldByDimensionID(targetDim), targetX, targetY, targetZ);
                    TeleportHandler.teleport(entity, targetDim, teleporter, targetX + entity.getLookVec().x, targetY, targetZ + entity.getLookVec().z);
                }
            }
        }
        --timeToDespawn;
    }

    @Override
    public void setRotation(float yaw, float pitch){
        super.setRotation(yaw, pitch);
    }

    public void setTargetCoordinates(int dimension, double x, double y, double z) {
        this.targetDim = dimension;
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == RENDER_PASS_TRANSLUCENT;
    }

    public boolean isOpening(){
        return timeToDespawn >= 190;
    }

    public boolean isClosing(){
        return timeToDespawn <= 10;
    }

    public int getTimeToDespawn() {
        return timeToDespawn;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getBrightnessForRender(){
        return 15728880;
    }
}
