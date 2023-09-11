package gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.terminal.gui.widgets.SelectorWidget;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MetaTileEntityAlarm extends TieredMetaTileEntity {
    private SoundEvent selectedSound;
    private boolean isActive;
    public static final int BASE_EU_CONSUMPTION = 4;

    public MetaTileEntityAlarm(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
        selectedSound = GTSoundEvents.DEFAULT_ALARM;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAlarm(metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.alarm.tooltip"));
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick", BASE_EU_CONSUMPTION));
        tooltip.add(I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.ALARM_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 240, 50)
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(new SelectorWidget(10, 20, 220, 20,
                        getSounds().stream().map((event) -> event.getSoundName().toString()).collect(Collectors.toList()),
                        0x555555, () -> this.selectedSound.getSoundName().toString(), true).setOnChanged((v) -> {
                    GregTechAPI.soundManager.stopTileSound(getPos());
                    this.selectedSound = SoundEvent.REGISTRY.getObject(new ResourceLocation(v));
                    this.writeCustomData(GregtechDataCodes.UPDATE_SOUND, (writer) -> writer.writeResourceLocation(this.selectedSound.getSoundName()));
                }))
                .build(this.getHolder(), entityPlayer);
    }

    protected List<SoundEvent> getSounds() {
        if (GTValues.FOOLS.get()) {
            return Arrays.asList(GTSoundEvents.DEFAULT_ALARM, GTSoundEvents.ARC, SoundEvents.ENTITY_WOLF_HOWL,
                    SoundEvents.ENTITY_ENDERMEN_DEATH, GTSoundEvents.SUS_RECORD);
        }
        return Arrays.asList(GTSoundEvents.DEFAULT_ALARM, GTSoundEvents.ARC, SoundEvents.ENTITY_WOLF_HOWL,
                SoundEvents.ENTITY_ENDERMEN_DEATH);
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
        return this.isBlockRedstonePowered() && this.energyContainer.changeEnergy(-BASE_EU_CONSUMPTION) == -BASE_EU_CONSUMPTION;
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
        } else if (dataId == GregtechDataCodes.UPDATE_SOUND) {
            this.selectedSound = SoundEvent.REGISTRY.getObject(buf.readResourceLocation());
            GregTechAPI.soundManager.stopTileSound(getPos());
        }
    }

    @Override
    public float getVolume() {
        return 4.0F;
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
        this.selectedSound = SoundEvent.REGISTRY.getObject(buf.readResourceLocation());
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isActive);
        buf.writeResourceLocation(this.selectedSound.getSoundName());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setString("selectedSound", this.selectedSound.getSoundName().toString());
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.isActive = data.getBoolean("isActive");
        this.selectedSound = SoundEvent.REGISTRY.getObject(new ResourceLocation(data.getString("selectedSound")));
        super.readFromNBT(data);
    }
}
