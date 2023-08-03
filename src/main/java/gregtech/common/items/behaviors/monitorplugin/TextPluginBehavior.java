package gregtech.common.items.behaviors.monitorplugin;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.manager.GuiCreationContext;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.GuiSyncManager;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ColorPickerDialog;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.gui.IUIHolder;
import gregtech.api.gui.widgets.TextFieldWidget;
import gregtech.api.items.behavior.MonitorPluginBaseBehavior;
import gregtech.api.newgui.GTGuis;
import gregtech.client.utils.RenderUtil;
import gregtech.common.gui.widget.WidgetARGB;
import gregtech.common.gui.widget.monitor.WidgetPluginConfig;
import gregtech.common.metatileentities.multi.electric.centralmonitor.MetaTileEntityMonitorScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TextPluginBehavior extends MonitorPluginBaseBehavior {

    public String[] texts;
    public int[] colors;

    public void setText(int line, String text, int color) {
        if (line < 0 || line > texts.length || (texts[line].equals(text) && colors[line] == color)) return;
        this.texts[line] = text;
        this.colors[line] = color;
        writePluginData(GregtechDataCodes.UPDATE_PLUGIN_CONFIG, packetBuffer -> {
            packetBuffer.writeInt(texts.length);
            for (int i = 0; i < texts.length; i++) {
                packetBuffer.writeString(texts[i]);
                packetBuffer.writeInt(colors[i]);
            }
        });
        markAsDirty();
    }

    private void setText(int line, String text) {
        this.texts[line] = text;
        markAsDirty();
    }

    private void setColor(int line, int color) {
        this.colors[line] = color;
        markAsDirty();
    }

    @Override
    public void readPluginData(int id, PacketBuffer buf) {
        if (id == GregtechDataCodes.UPDATE_PLUGIN_CONFIG) {
            texts = new String[buf.readInt()];
            colors = new int[texts.length];
            for (int i = 0; i < texts.length; i++) {
                texts[i] = buf.readString(100);
                colors[i] = buf.readInt();
            }
        }
    }

    @Override
    public MonitorPluginBaseBehavior createPlugin() {
        TextPluginBehavior plugin = new TextPluginBehavior();
        plugin.texts = new String[16];
        plugin.colors = new int[16];
        return plugin;
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        for (int i = 0; i < texts.length; i++) {
            data.setString("t" + i, texts[i]);
        }
        data.setIntArray("color", colors);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        for (int i = 0; i < texts.length; i++) {
            texts[i] = data.hasKey("t" + i) ? data.getString("t" + i) : "";
        }
        if (data.hasKey("color")) {
            colors = data.getIntArray("color");
        } else {
            Arrays.fill(colors, 0XFFFFFFFF);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderPlugin(float partialTicks, RayTraceResult rayTraceResult) {
        for (int i = 0; i < texts.length; i++) {
            RenderUtil.renderText(-0.5f, -0.4625f + i / 16f, 0.002f, 1 / 128f, colors[i], texts[i], false);
        }
    }

    @Override
    public boolean hasUI() {
        return true;
    }

    @Override
    public WidgetPluginConfig customUI(WidgetPluginConfig widgets, IUIHolder holder, EntityPlayer entityPlayer) {
        widgets.setSize(260, 210);
        for (int i = 0; i < texts.length; i++) {
            int finalI = i;
            widgets.addWidget(new TextFieldWidget(25, 25 + i * 10, 100, 10, true, () -> this.texts[finalI], (text) -> setText(finalI, text, this.colors[finalI])).setValidator((data) -> true));
            widgets.addWidget(new WidgetARGB(135, 25 + i * 10, 10, colors[i], color -> setText(finalI, this.texts[finalI], color)));
        }
        return widgets;
    }

    @Override
    public boolean useMui2() {
        return true;
    }

    @Override
    public ModularPanel createPluginConfigUI(GuiSyncManager syncManager, @Nullable MetaTileEntityMonitorScreen screen, @Nullable GuiCreationContext context) {
        ModularPanel panel = GTGuis.createPanel("cm_plugin_text", 134, 150);
        panel.child(IKey.str("Plugin Config").asWidget().pos(5, 5));
        ListWidget<?, ?, ?> list = new ListWidget<>()
                .top(20).left(7).right(7).bottom(7);
        panel.child(list);
        for (int i = 0; i < texts.length; i++) {
            int finalI = i;
            IntSyncValue colorValue = new IntSyncValue(() -> this.colors[finalI], val -> setColor(finalI, val));
            syncManager.syncValue("color", finalI, colorValue);
            list.child(new Row()
                    .widthRel(1f).height(12)
                    .child(new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget()
                            .size(100, 12)
                            .value(new StringSyncValue(() -> this.texts[finalI], val -> setText(finalI, val))))
                    .child(new ButtonWidget<>()
                            .size(12)
                            .marginLeft(2)
                            .marginRight(2)
                            .background((context1, x, y, width, height) -> {
                                GuiDraw.drawRect(x, y, width, height, this.colors[finalI]);
                                GuiDraw.drawBorder(x + 1, y + 1, width - 2, height - 2, Color.BLACK.normal, 1);
                            })
                            .onMousePressed(mouseButton -> {
                                panel.getScreen().openPanel(new ColorPickerDialog(val -> colorValue.setIntValue(val, true, true), this.colors[finalI], true));
                                return true;
                            })));
        }

        return panel;
    }
}
