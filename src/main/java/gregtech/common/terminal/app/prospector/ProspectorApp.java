package gregtech.common.terminal.app.prospector;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.net.packets.PacketProspecting;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.os.TerminalOSWidget;
import gregtech.api.terminal.os.menu.IMenuComponent;
import gregtech.api.util.GTLog;
import gregtech.common.terminal.app.prospector.widget.WidgetOreList;
import gregtech.common.terminal.app.prospector.widget.WidgetProspectingMap;
import gregtech.common.terminal.component.ClickComponent;
import gregtech.common.terminal.component.SearchComponent;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ProspectorApp extends AbstractApplication implements SearchComponent.IWidgetSearch<String> {
    WidgetOreList widgetOreList;
    WidgetProspectingMap widgetProspectingMap;
    ColorRectTexture background;
    @SideOnly(Side.CLIENT)
    Table<Integer, Integer, PacketProspecting> persist;
    final int mode;

    public ProspectorApp(int mode) {
        super(mode == 0 ? "ore_prospector" : "fluid_prospector");
        this.mode = mode;
    }

    @Override
    public AbstractApplication createAppInstance(TerminalOSWidget os, boolean isClient, NBTTagCompound nbt) {
        ProspectorApp app = new ProspectorApp(mode);
        app.isClient = isClient;
        app.nbt = nbt;
        return app;
    }

    @Override
    public AbstractApplication initApp() {
        int chunkRadius = getAppTier() + 3;
        int offset = (232 - 32 * 7 + 16) / 2;
        background = new ColorRectTexture(0xA0000000);
        this.addWidget(new ImageWidget(0, 0, 333, 232, background));
        if (isClient) {
            this.addWidget(new ImageWidget(0, 0, 333, offset, GuiTextures.UI_FRAME_SIDE_UP));
            this.addWidget(new ImageWidget(0, 232 - offset, 333, offset, GuiTextures.UI_FRAME_SIDE_DOWN));
            this.widgetOreList = new WidgetOreList(32 * chunkRadius - 16, offset, 333 - 32 * chunkRadius + 16, 232 - 2 * offset);
            this.addWidget(this.widgetOreList);
        }
        this.widgetProspectingMap = new WidgetProspectingMap(0, offset + (7 - chunkRadius) * 16, chunkRadius, this.widgetOreList, mode, 1);
        if (isClient) {
            persist = Tables.newCustomTable(Maps.newHashMap(), Maps::newHashMap);
            widgetProspectingMap.setOnPacketReceived(packet -> persist.put(packet.chunkX, packet.chunkZ, packet));
        }

        this.addWidget(1, this.widgetProspectingMap);
        loadLocalConfig(nbt -> {
            this.widgetProspectingMap.setDarkMode(nbt.getBoolean("dark"));
            background.setColor(this.widgetProspectingMap.getDarkMode() ? 0xA0000000 : 0xA0ffffff);
        });
        if (isClient) {
            loadPacketLocalConfig();
        }
        return this;
    }

    @SideOnly(Side.CLIENT)
    protected void loadPacketLocalConfig() {
        new Thread(()-> { // thread for better QoL
            int posX = gui.entityPlayer.getPosition().getX();
            int posZ = gui.entityPlayer.getPosition().getZ();
            int playerChunkX = posX >> 4;
            int playerChunkZ = posZ >> 4;
            int chunkRadius = getAppTier() + 3 - 1;
            for (int i = playerChunkX - chunkRadius; i <= playerChunkX + chunkRadius; i++) {
                for (int j = playerChunkZ - chunkRadius; j <= playerChunkZ + chunkRadius; j++) {
                    NBTTagCompound nbt = null;
                    try {
                        nbt = CompressedStreamTools.read(new File(TerminalRegistry.TERMINAL_PATH, String.format("%s/%d/%d_%d.nbt", getRegistryName(), mode, i, j)));
                    } catch (IOException e) {
                        GTLog.logger.error("error while loading local nbt for {}", getRegistryName(), e);
                    }
                    if (nbt != null) {
                        PacketProspecting packet = PacketProspecting.readPacketData(nbt);
                        if (packet != null) {
                            packet.posX = posX;
                            packet.posZ = posZ;
                            persist.put(i, j, packet);
                            widgetProspectingMap.addPacketToQueue(packet);
                        }
                    }
                }
            }
        }).start();
    }

    @SideOnly(Side.CLIENT)
    protected void savePacketLocalConfig() {
        new Thread(()->{ // thread for better QoL
            File folder = new File(TerminalRegistry.TERMINAL_PATH, String.format("%s/%d", getRegistryName(), mode));
            if (!folder.exists()) {
                if (!folder.mkdirs()) return;
            }
            for (Table.Cell<Integer, Integer, PacketProspecting> cell : persist.cellSet()) {
                if (cell.getValue() != null) {
                    NBTTagCompound nbt = cell.getValue().writePacketData();
                    try {
                        if (!nbt.isEmpty()) {
                            CompressedStreamTools.safeWrite(nbt, new File(folder, String.format("%d_%d.nbt", cell.getRowKey(), cell.getColumnKey())));
                        }
                    } catch (IOException e) {
                        GTLog.logger.error("error while saving local nbt for {}", getRegistryName(), e);
                    }
                }
            }
        }).start();
    }

    @Override
    public NBTTagCompound closeApp() {
        saveLocalConfig(nbt -> {
            nbt.setBoolean("dark", this.widgetProspectingMap.getDarkMode());
        });
        if (isClient) savePacketLocalConfig();
        return super.closeApp();
    }

    @Override
    public int getMaxTier() {
        return 4;
    }

    @Override
    public List<IMenuComponent> getMenuComponents() {
        ClickComponent darkMode = new ClickComponent().setIcon(GuiTextures.ICON_VISIBLE).setHoverText("terminal.prospector.vis_mode").setClickConsumer(cd -> {
            if (cd.isClient) {
                widgetProspectingMap.setDarkMode(!widgetProspectingMap.getDarkMode());
                background.setColor(this.widgetProspectingMap.getDarkMode() ? 0xA0000000 : 0xA0ffffff);
            }
        });
        return Arrays.asList(darkMode, new SearchComponent<>(this));
    }

    @Override
    public String resultDisplay(String result) {
        if (widgetOreList != null) {
            return widgetOreList.ores.get(result);
        }
        return "";
    }

    @Override
    public void selectResult(String result) {
        if (widgetOreList != null) {
            widgetOreList.setSelected(result);
        }
    }

    @Override
    public void search(String word, Consumer<String> find) {
        if (widgetOreList != null) {
            word = word.toLowerCase();
            for (Map.Entry<String, String> entry : widgetOreList.ores.entrySet()) {
                if (entry.getKey().toLowerCase().contains(word) || entry.getValue().toLowerCase().contains(word)) {
                    find.accept(entry.getKey());
                }
            }
        }
    }
}
