package gregtech.common.items.behaviors.monitorplugin;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.CycleButtonWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.resources.picturetexture.PictureTexture;
import gregtech.api.gui.resources.utils.DownloadThread;
import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.api.newgui.GTGuis;
import gregtech.api.newgui.GuiTextures;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class OnlinePicPluginBehavior extends MonitorPluginBaseBehavior {

    public String url;
    public float scaleX;
    public float scaleY;
    public float rotation;
    public boolean flippedX;
    public boolean flippedY;

    // run-time
    private String tmpUrl;

    @SideOnly(Side.CLIENT)
    private DownloadThread downloader;
    @SideOnly(Side.CLIENT)
    public PictureTexture texture;
    @SideOnly(Side.CLIENT)
    public boolean failed;
    @SideOnly(Side.CLIENT)
    public String error;

    public void setConfig(String url, float rotation, float scaleX, float scaleY, boolean flippedX, boolean flippedY) {
        if (url.length() > 200 || (this.url.equals(url) && this.rotation == rotation && this.scaleX == scaleX && this.scaleY == scaleY && this.flippedX == flippedX && this.flippedY == flippedY))
            return;
        this.url = url;
        this.rotation = rotation;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.flippedX = flippedX;
        this.flippedY = flippedY;
        writePluginData(GregtechDataCodes.UPDATE_PLUGIN_CONFIG, packetBuffer -> {
            packetBuffer.writeString(url);
            packetBuffer.writeFloat(rotation);
            packetBuffer.writeFloat(scaleX);
            packetBuffer.writeFloat(scaleY);
            packetBuffer.writeBoolean(flippedX);
            packetBuffer.writeBoolean(flippedY);
        });
        markAsDirty();
    }

    @Override
    public void readPluginData(int id, PacketBuffer buf) {
        if (id == GregtechDataCodes.UPDATE_PLUGIN_CONFIG) {
            String _url = buf.readString(200);
            if (!this.url.equals(_url)) {
                this.url = _url;
                this.texture = null;
                this.failed = false;
                this.error = null;
            }
            this.rotation = buf.readFloat();
            this.scaleX = buf.readFloat();
            this.scaleY = buf.readFloat();
            this.flippedX = buf.readBoolean();
            this.flippedY = buf.readBoolean();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setString("url", url);
        data.setFloat("rotation", rotation);
        data.setFloat("scaleX", scaleX);
        data.setFloat("scaleY", scaleY);
        data.setBoolean("flippedX", flippedX);
        data.setBoolean("flippedY", flippedY);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.url = data.hasKey("url") ? data.getString("url") : "";
        this.rotation = data.hasKey("rotation") ? data.getFloat("rotation") : 0;
        this.scaleX = data.hasKey("scaleX") ? data.getFloat("scaleX") : 1;
        this.scaleY = data.hasKey("scaleY") ? data.getFloat("scaleY") : 1;
        this.flippedX = data.hasKey("flippedX") && data.getBoolean("flippedX");
        this.flippedY = data.hasKey("flippedY") && data.getBoolean("flippedY");
    }

    @Override
    public MonitorPluginBaseBehavior createPlugin() {
        return new OnlinePicPluginBehavior();
    }

    @Override
    public ModularPanel createPluginConfigUI(GuiSyncManager syncManager, @Nullable MetaTileEntityMonitorScreen screen, @Nullable GuiCreationContext context) {
        ModularPanel panel = GTGuis.createPanel("cm_plugin_online_pic", 150, 122);
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        panel.child(IKey.str("Plugin Config").asWidget().pos(5, 5))
                .child(new Column()
                        .top(18).left(7).right(7).bottom(7)
                        .child(new TextFieldWidget()
                                .value(new StringSyncValue(() -> this.url, val -> {
                                    this.url = val;
                                    markAsDirty();
                                }))
                                .setMaxLength(200)
                                .widthRel(1f)
                                .height(16)
                                .marginBottom(3))
                        .child(IKey.dynamic(() -> "Rotation: " + decimalFormat.format(this.rotation) + "°").asWidget().widthRel(1f))
                        .child(new SliderWidget()
                                .widthRel(1f)
                                .height(10)
                                .bounds(-180.0, 180.0)
                                .stopper(1.0)
                                .stopperTexture(null)
                                .background(SLIDER_BACKGROUND)
                                .value(new DoubleSyncValue(() -> this.rotation, val -> {
                                    this.rotation = (float) val;
                                    markAsDirty();
                                }))
                                .marginBottom(3))
                        .child(IKey.dynamic(() -> "Scale X: " + decimalFormat.format(this.scaleX)).asWidget().widthRel(1f))
                        .child(new SliderWidget()
                                .widthRel(1f)
                                .height(10)
                                .bounds(0.0, 1.0)
                                .stopper(0.05)
                                .stopperTexture(null)
                                .background(SLIDER_BACKGROUND)
                                .value(new DoubleSyncValue(() -> this.scaleX, val -> {
                                    this.scaleX = (float) val;
                                    markAsDirty();
                                }))
                                .marginBottom(3))
                        .child(IKey.dynamic(() -> "Scale Y: " + decimalFormat.format(this.scaleY)).asWidget().widthRel(1f))
                        .child(new SliderWidget()
                                .widthRel(1f)
                                .height(10)
                                .bounds(0.0, 1.0)
                                .stopper(0.05)
                                .stopperTexture(null)
                                .background(SLIDER_BACKGROUND)
                                .value(new DoubleSyncValue(() -> this.scaleY, val -> {
                                    this.scaleY = (float) val;
                                    markAsDirty();
                                }))
                                .marginBottom(3))
                        .child(new Row()
                                .widthRel(1f)
                                .height(15)
                                .child(IKey.str("Flipped X: ").asWidget().height(15))
                                .child(new CycleButtonWidget()
                                        .value(new BooleanSyncValue(() -> this.flippedX, val -> {
                                            this.flippedX = val;
                                            markAsDirty();
                                        }))
                                        .size(15)
                                        .texture(GuiTextures.CLIPBOARD_CHECK_BOX)
                                        .marginRight(6))
                                .child(IKey.str("Flipped Y: ").asWidget().height(15))
                                .child(new CycleButtonWidget()
                                        .value(new BooleanSyncValue(() -> this.flippedY, val -> {
                                            this.flippedY = val;
                                            markAsDirty();
                                        }))
                                        .size(15)
                                        .texture(GuiTextures.CLIPBOARD_CHECK_BOX))));

        return panel;
    }

    @Override
    public void update() {
        if (this.screen != null && this.screen.getWorld().isRemote) {
            if (this.texture != null) {
                texture.tick(); // gif update
            }
        }
    }

    @Override
    public void renderPlugin(float partialTicks, RayTraceResult rayTraceResult) {
        if (!this.url.isEmpty()) {
            if (texture != null && texture.hasTexture()) {
                texture.render(-0.5f, -0.5f, 1, 1, this.rotation, this.scaleX, this.scaleY, this.flippedX, this.flippedY);
            } else
                this.loadTexture();
        }
    }

    @SideOnly(Side.CLIENT)
    public void loadTexture() {
        if (texture == null && !failed) {
            if (downloader == null && DownloadThread.activeDownloads < DownloadThread.MAXIMUM_ACTIVE_DOWNLOADS) {
                PictureTexture loadedTexture = DownloadThread.loadedImages.get(url);

                if (loadedTexture == null) {
                    synchronized (DownloadThread.LOCK) {
                        if (!DownloadThread.loadingImages.contains(url)) {
                            downloader = new DownloadThread(url);
                            return;
                        }
                    }
                } else {
                    texture = loadedTexture;
                }
            }
            if (downloader != null && downloader.hasFinished()) {
                if (downloader.hasFailed()) {
                    failed = true;
                    error = downloader.getError();
                    DownloadThread.LOGGER.error("Could not load image of " + (this.screen != null ? this.screen.getPos().toString() : "") + " " + error);
                } else {
                    texture = DownloadThread.loadImage(downloader);
                }
                downloader = null;
            }
        }
    }
}
