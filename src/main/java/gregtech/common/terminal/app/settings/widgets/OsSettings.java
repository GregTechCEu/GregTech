package gregtech.common.terminal.app.settings.widgets;

import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.GTLog;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.File;
import java.io.IOException;

public class OsSettings extends AbstractWidgetGroup {
    public static boolean DOUBLE_CHECK;
    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            NBTTagCompound nbt = null;
            try {
                nbt = CompressedStreamTools.read(new File(TerminalRegistry.TERMINAL_PATH, "config/os_settings.nbt"));
            } catch (IOException e) {
                GTLog.logger.error("error while loading local nbt for the os settings", e);
            }
            if (nbt == null) {
                DOUBLE_CHECK = true;
            } else {
                DOUBLE_CHECK = nbt.getBoolean("double_check");
            }
        }
    }
    final TerminalOSWidget os;

    public void saveConfig() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("double_check", DOUBLE_CHECK);
            try {
                if (!nbt.isEmpty()) {
                    CompressedStreamTools.safeWrite(nbt, new File(TerminalRegistry.TERMINAL_PATH, "config/os_settings.nbt"));
                }
            } catch (IOException e) {
                GTLog.logger.error("error while saving local nbt for the os settings", e);
            }
        }
    }

    public OsSettings(TerminalOSWidget os) {
        super(Position.ORIGIN, new Size(323, 212));
        this.os = os;
        this.addWidget(new LabelWidget(25, 15, "terminal.settings.os.double_check", -1).setYCentered(true));
        this.addWidget(new RectButtonWidget(10, 10, 10, 10, 2)
                .setToggleButton(new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor()), (c, p)->{
                    DOUBLE_CHECK=!p;
                    saveConfig();
                })
                .setValueSupplier(true, ()->!DOUBLE_CHECK)
                .setColors(TerminalTheme.COLOR_B_3.getColor(),
                        TerminalTheme.COLOR_1.getColor(),
                        TerminalTheme.COLOR_B_3.getColor())
                .setIcon(new ColorRectTexture(TerminalTheme.COLOR_7.getColor()))
                .setHoverText("terminal.settings.os.double_check.desc"));
    }
}
