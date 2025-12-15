package gregtech.common.metatileentities.electric;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
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
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
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

public class MetaTileEntityAlarm extends TieredMetaTileEntity implements IMetaTileEntityGuiHolder {

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
    public @NotNull ModularPanel buildUI(MetaTileEntityGuiData guiData, PanelSyncManager panelSyncManager,
                                         UISettings settings) {
        IntSyncValue radiusSync = new IntSyncValue(this::getRadius, this::setRadius);
        SoundEventSyncHandler soundEventSyncHandler = new SoundEventSyncHandler();
        panelSyncManager.syncValue("alarm_data", 0, soundEventSyncHandler);
        IPanelHandler soundSelector = panelSyncManager.syncedPanel("sound_selector_popup", true,
                createSoundsPopup(soundEventSyncHandler));

        // TODO: Change the position of the name when it's standardized.
        return GTGuis.createPanel(this, 200, 58)
                .child(Flow.column()
                        .margin(5)
                        .coverChildrenHeight()
                        .crossAxisAlignment(Alignment.CrossAxis.START)
                        .child(IKey.lang("gregtech.gui.alarm.radius")
                                .asWidget())
                        .child(new TextFieldWidget()
                                .widthRel(1.0f)
                                .margin(0, 2)
                                .height(16)
                                .setMaxLength(10)
                                .setNumbers(0, 128)
                                .value(radiusSync)
                                .background(GTGuiTextures.DISPLAY))
                        .child(Flow.row()
                                .widthRel(1.0f)
                                .coverChildrenHeight()
                                .mainAxisAlignment(Alignment.MainAxis.START)
                                .childPadding(2)
                                .child(new ButtonWidget<>()
                                        .size(18)
                                        .onMousePressed(mouse -> {
                                            guiData.getPlayer().playSound(selectedSound, 1.0f, 1.0f);
                                            // returns false so the default click sound isn't played
                                            return false;
                                        })
                                        .overlay(GTGuiTextures.SPEAKER_ICON.asIcon()
                                                .size(18))
                                        .addTooltipLine(IKey.lang("gregtech.gui.alarm.sounds_preview_button")))
                                .child(new ButtonWidget<>()
                                        .size(18)
                                        .onMousePressed(mouse -> {
                                            if (soundSelector.isPanelOpen()) {
                                                soundSelector.closePanel();
                                            } else {
                                                soundSelector.openPanel();
                                            }

                                            return true;
                                        })
                                        .overlay(GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon()
                                                .size(16))
                                        .addTooltipLine(IKey.lang("gregtech.gui.alarm.sounds_popup_button")))
                                .child(IKey.dynamic(() -> getSoundName(selectedSound))
                                        .asWidget()
                                        .alignY(0.5f)
                                        .expanded()
                                        .addTooltipLine(IKey.lang("gregtech.gui.alarm.selected_sound")))));
    }

    protected PanelSyncHandler.IPanelBuilder createSoundsPopup(@NotNull MetaTileEntityAlarm.SoundEventSyncHandler soundEventSyncHandler) {
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
                                    soundEventSyncHandler.setSound(name);
                                    syncHandler.closePanel();
                                    return true;
                                })
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        GregTechAPI.soundManager.stopTileSound(getPos());
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

    public class SoundEventSyncHandler extends SyncHandler {

        public SoundEventSyncHandler() {}

        @Override
        public void readOnClient(int id, PacketBuffer buf) {}

        @Override
        public void readOnServer(int id, PacketBuffer buf) {
            if (id == 0) {
                selectedSound = getSound(buf.readResourceLocation());
                writeCustomData(GregtechDataCodes.UPDATE_SOUND,
                        toClients -> toClients.writeResourceLocation(getSoundResourceLocation(selectedSound)));
                markDirty();
            }
        }

        @SideOnly(Side.CLIENT)
        public void setSound(@NotNull ResourceLocation name) {
            if (getSoundResourceLocation(selectedSound).equals(name)) return;
            selectedSound = getSound(name);
            syncToServer(0, buf -> buf.writeResourceLocation(name));
        }
    }
}
