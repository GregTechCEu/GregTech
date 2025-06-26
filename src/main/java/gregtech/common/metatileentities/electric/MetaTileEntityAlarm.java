package gregtech.common.metatileentities.electric;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.client.renderer.texture.Textures;
import gregtech.core.sound.GTSoundEvents;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.IntValue;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityAlarm extends TieredMetaTileEntity {

    public static final int BASE_EU_CONSUMPTION = 4;
    private boolean isActive;
    private int radius = 64;

    @NotNull
    private SoundEvent selectedSound;
    private static final List<SoundEvent> sounds = new ArrayList<>(5);

    public MetaTileEntityAlarm(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 1);
        selectedSound = GTSoundEvents.DEFAULT_ALARM;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAlarm(metaTileEntityId);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.universal.tooltip.uses_per_tick", BASE_EU_CONSUMPTION));
        tooltip.add(
                I18n.format("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (this.isActive) {
            Textures.ALARM_OVERLAY_ACTIVE.renderSided(getFrontFacing(), renderState, translation, pipeline);
        } else {
            Textures.ALARM_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public boolean usesMui2() {
        return true;
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager panelSyncManager) {
        AlarmSyncHandler alarmSyncHandler = new AlarmSyncHandler();
        panelSyncManager.syncValue("alarm_data", 0, alarmSyncHandler);
        IPanelHandler soundSelector = panelSyncManager.panel("sound_selector_popup",
                createSoundsPopup(alarmSyncHandler), true);

        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 200, 55)
                .child(Flow.column()
                        .marginLeft(5)
                        .marginRight(5)
                        .top(5)
                        .coverChildrenHeight()
                        .child(IKey.lang("gregtech.gui.alarm.radius")
                                .asWidget()
                                .alignX(0.0f))
                        .child(new TextFieldWidget()
                                .widthRel(1.0f)
                                .marginTop(1)
                                .height(16)
                                .setMaxLength(10)
                                .setNumbers(0, 128)
                                .value(new IntValue.Dynamic(() -> radius, alarmSyncHandler::setRadius))
                                .background(GTGuiTextures.DISPLAY))
                        .child(Flow.row()
                                .widthRel(1.0f)
                                .marginTop(1)
                                .coverChildrenHeight()
                                .child(new ButtonWidget<>()
                                        .marginRight(4)
                                        .size(16)
                                        .onMousePressed(mouse -> {
                                            if (soundSelector.isPanelOpen()) {
                                                soundSelector.closePanel();
                                            } else {
                                                soundSelector.openPanel();
                                            }

                                            return true;
                                        })// TODO: 🎵 icon overlay on the button or smth
                                        .addTooltipLine(IKey.lang("gregtech.gui.alarm.sounds_popup_button")))
                                .child(IKey.dynamic(() -> getSoundName(selectedSound))
                                        .asWidget()
                                        .alignY(0.5f)
                                        .expanded()
                                        .addTooltipLine(IKey.lang("gregtech.gui.alarm.selected_sound")))));
    }

    protected PanelSyncHandler.IPanelBuilder createSoundsPopup(@NotNull AlarmSyncHandler alarmSyncHandler) {
        return (syncManager, syncHandler) -> {
            List<IWidget> soundList = new ArrayList<>(sounds.size());

            for (SoundEvent sound : sounds) {
                ResourceLocation name = getSoundResourceLocation(sound);
                soundList.add(Flow.row()
                        .widthRel(1.0f)
                        .coverChildrenHeight()
                        .margin(4, 1)
                        .child(new ButtonWidget<>()
                                .widthRel(1.0f)
                                .onMousePressed(mouse -> {
                                    alarmSyncHandler.setSound(name);
                                    syncHandler.closePanel();
                                    return true;
                                })
                                .addTooltipLine(IKey.lang("gregtech.gui.alarm.set_sound"))
                                .overlay(IKey.str(name.toString()))));
            }

            return GTGuis.createPopupPanel("sound_selector", 200, 100)
                    .child(Flow.column()
                            .margin(5)
                            .child(IKey.lang("gregtech.gui.alarm.sounds")
                                    .asWidget())
                            .child(new ListWidget<>()
                                    .left(2)
                                    .right(2)
                                    .marginTop(6)
                                    .marginBottom(4)
                                    .expanded()
                                    .children(soundList)
                                    .background(GTGuiTextures.DISPLAY.asIcon()
                                            .margin(0, -2))));
        };
    }

    @Override
    public @Nullable SoundEvent getSound() {
        return selectedSound;
    }

    @Override
    public boolean isActive() {
        if (this.getWorld().isRemote) {
            return isActive;
        }
        return this.isBlockRedstonePowered() &&
                this.energyContainer.changeEnergy(-BASE_EU_CONSUMPTION) == -BASE_EU_CONSUMPTION;
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
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_ACTIVE) {
            isActive = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.UPDATE_RADIUS) {
            radius = buf.readVarInt();
            GregTechAPI.soundManager.stopTileSound(getPos());
        } else if (dataId == GregtechDataCodes.UPDATE_SOUND) {
            selectedSound = getSound(buf.readResourceLocation());
            GregTechAPI.soundManager.stopTileSound(getPos());
        }
    }

    @Override
    public float getVolume() {
        return radius / 16f;
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
        buf.writeResourceLocation(getSoundResourceLocation(selectedSound));
        buf.writeVarInt(radius);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        isActive = buf.readBoolean();
        selectedSound = getSound(buf.readResourceLocation());
        radius = buf.readVarInt();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("isActive", this.isActive);
        data.setString("selectedSound", getSoundName(selectedSound));
        data.setInteger("radius", this.radius);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.isActive = data.getBoolean("isActive");
        this.selectedSound = getSound(data.getString("selectedSound"));
        this.radius = data.getInteger("radius");
        super.readFromNBT(data);
    }

    public static void addSound(@NotNull SoundEvent soundEvent) {
        sounds.add(soundEvent);
    }

    public static String getSoundName(@NotNull SoundEvent sound) {
        return getSoundResourceLocation(sound).toString();
    }

    public static ResourceLocation getSoundResourceLocation(@NotNull SoundEvent sound) {
        return SoundEvent.REGISTRY.getNameForObject(sound);
    }

    public static @NotNull SoundEvent getSound(@NotNull String name) {
        return getSound(new ResourceLocation(name));
    }

    public static @NotNull SoundEvent getSound(@NotNull ResourceLocation name) {
        SoundEvent sound = SoundEvent.REGISTRY.getObject(name);
        return sound == null ? GTSoundEvents.DEFAULT_ALARM : sound;
    }

    /**
     * Exists so that when in multiplayer, changed values get synced to other clients
     */
    protected class AlarmSyncHandler extends SyncHandler {

        public AlarmSyncHandler() {}

        @Override
        public void readOnClient(int id, PacketBuffer buf) {}

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == 0) {
                radius = buf.readVarInt();
                writeCustomData(GregtechDataCodes.UPDATE_RADIUS, toClients -> toClients.writeVarInt(radius));
                markDirty();
            } else if (id == 1) {
                selectedSound = getSound(buf.readResourceLocation());
                writeCustomData(GregtechDataCodes.UPDATE_SOUND,
                        toClients -> toClients.writeResourceLocation(getSoundResourceLocation(selectedSound)));
                markDirty();
            }
        }

        @SideOnly(Side.CLIENT)
        public void setRadius(int newRadius) {
            if (newRadius == radius) return;
            radius = newRadius;
            syncToServer(0, buf -> buf.writeVarInt(radius));
        }

        @SideOnly(Side.CLIENT)
        public void setSound(@NotNull ResourceLocation name) {
            if (getSoundResourceLocation(selectedSound).equals(name)) return;
            selectedSound = getSound(name);
            syncToServer(1, buf -> buf.writeResourceLocation(name));
        }
    }
}
