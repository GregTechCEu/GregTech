package gregtech.common.metatileentities.electric;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class MetaTileEntityAlarm extends TieredMetaTileEntity {
    private SoundEvent selectedSound;
    private boolean isActive;

    public MetaTileEntityAlarm(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
        selectedSound = GTSoundEvents.DEFAULT_ALARM;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAlarm(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public SoundEvent getSound() {
        return selectedSound;
    }

    @Override
    public boolean isActive() {
        if (this.getWorld().isRemote) {
            return isActive;
        }
        return this.isBlockRedstonePowered();
    }

    @Override
    public void update() {
        super.update();
        if (!this.getWorld().isRemote) {
            if (this.isActive != this.isActive()) {
                this.writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, (writer) -> writer.writeBoolean(this.isActive()));
                this.isActive = this.isActive();
            }
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_ACTIVE) {
            this.isActive = buf.readBoolean();
        }
    }

    @Override
    public float getVolume() {
        return 4.0F;
    }
}
