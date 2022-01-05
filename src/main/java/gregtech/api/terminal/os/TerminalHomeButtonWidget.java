package gregtech.api.terminal.os;

import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.util.GTLog;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;

public class TerminalHomeButtonWidget extends CircleButtonWidget {
    private final TerminalOSWidget os;
    private int mouseClickTime = -1;
    private final Pair<SystemCall, String>[] actions;

    public TerminalHomeButtonWidget(TerminalOSWidget os) {
        super(351, 115, 11, 2, 18);
        this.os = os;
        this.setColors(0, TerminalTheme.COLOR_F_1.getColor(), 0);
        this.actions = new Pair[8];
        if (FMLCommonHandler.instance().getSide().isClient()) {
            NBTTagCompound nbt = null;
            try {
                nbt = CompressedStreamTools.read(new File(TerminalRegistry.TERMINAL_PATH, "config/home_button.nbt"));
            } catch (IOException e) {
                GTLog.logger.error("error while loading local nbt for the home button", e);
            }
            if (nbt == null) {
                actions[actionMap(false, false, false)] = new MutablePair<>(SystemCall.CALL_MENU, null);
                actions[actionMap(true, false, false)] = new MutablePair<>(SystemCall.MINIMIZE_FOCUS_APP, null);
            } else {
                for (int i = 0; i < actions.length; i++) {
                    if (nbt.hasKey(i + "")) {
                        NBTTagCompound tag = nbt.getCompoundTag(i + "");
                        actions[i] = new MutablePair<>(SystemCall.getFromIndex(tag.getInteger("action")), tag.hasKey("arg") ? tag.getString("arg") : null);
                    }
                }
            }
        }
    }

    public Pair<SystemCall, String>[] getActions() {
        return actions;
    }

    public static int actionMap(boolean doubleClick, boolean isCtrl, boolean isShift) {
        return (doubleClick ? 1 : 0) + (isCtrl ? 2 : 0) + (isShift ? 4 : 0);
    }

    public void saveConfig() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            NBTTagCompound nbt = new NBTTagCompound();
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] != null) {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setInteger("action", actions[i].getKey().index);
                    if (actions[i].getValue() != null) {
                        tag.setString("arg", actions[i].getValue());
                    }
                    nbt.setTag(i + "", tag);
                }
            }
            try {
                if (!nbt.isEmpty()) {
                    CompressedStreamTools.safeWrite(nbt, new File(TerminalRegistry.TERMINAL_PATH, "config/home_button.nbt"));
                }
            } catch (IOException e) {
                GTLog.logger.error("error while saving local nbt for the home button", e);
            }
        }
    }

    private void click(int index, boolean isClient, String... args) {
        SystemCall action = SystemCall.getFromIndex(index);
        if (action != null) {
            action.call(os, isClient, args);
        }
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            int index = buffer.readVarInt();
            int length = buffer.readVarInt();
            String[] args = new String[length];
            for (int i = 0; i < length; i++) {
                args[i] = buffer.readString(32767);
            }
            click(index, false, args);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (mouseClickTime > 3) { // click
            Pair<SystemCall, String> pair = actions[actionMap(false, isCtrlDown(), isShiftDown())];
            sendToServer(pair);
            playButtonClickSound();
            mouseClickTime = -1;
        } else if (mouseClickTime > -1) {
            mouseClickTime++;
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (mouseClickTime == -1) {
                mouseClickTime = 0;
            } else if (mouseClickTime <= 3) { // double click
                Pair<SystemCall, String> pair = actions[actionMap(true, isCtrlDown(), isShiftDown())];
                sendToServer(pair);
                playButtonClickSound();
                mouseClickTime = -1;
            }
            return true;
        }
        return false;
    }

    private void sendToServer(Pair<SystemCall, String> pair) {
        if (pair != null) {
            String[] args = pair.getValue() == null ? new String[0] : pair.getValue().split(" ");
            writeClientAction(1, buffer->{
                buffer.writeVarInt(pair.getKey().index);
                buffer.writeVarInt(args.length);
                for (String arg : args) {
                    buffer.writeString(arg);
                }
            });
            click(pair.getKey().index, true, args);
        }
    }

}
