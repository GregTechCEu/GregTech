package gregtech.api.terminal2;

import gregtech.api.util.FileUtility;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.common.terminal2.CapeSelectorApp;
import gregtech.common.terminal2.GuideApp;
import gregtech.common.terminal2.SettingsApp;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class Terminal2 {

    public static final int SCREEN_WIDTH = 340, SCREEN_HEIGHT = 240;
    public static final Map<ResourceLocation, ITerminalApp> appMap = new LinkedHashMap<>();
    public static final ResourceLocation HOME_ID = GTUtility.gregtechId("home");

    @SideOnly(Side.CLIENT)
    public static File TERMINAL_PATH;

    public static void init() {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            TERMINAL_PATH = new File(Loader.instance().getConfigDir(), ConfigHolder.client.terminalRootPath);
            FileUtility.extractJarFiles("/assets/gregtech/terminal", TERMINAL_PATH, false);
            Terminal2Theme.init();
        }
        registerApp(GTUtility.gregtechId("settings"), new SettingsApp());
        registerApp(GTUtility.gregtechId("capes"), new CapeSelectorApp());
        registerApp(GTUtility.gregtechId("guides"), new GuideApp());

        /*
         * TODO potential apps to create/port:
         * guide/tutorial app using mui2 rich text and markup files of some sort
         * terminal games (minesweeper, pong, theseus' escape)
         * recipe chart (if anyone actually wants to port it)
         * teleporter (would require a system allowing gating apps behind some requirement, too powerful to be default)
         */
    }

    /**
     * Register a terminal app. Call this during initialization.
     * 
     * @param id A unique identifier for your app. This is used to determine the lang key for the app name tooltip.
     *           <p>
     *           e.g. <code>gregtech:capes</code> -> <code>terminal.app.gregtech.capes</code>
     */
    public static void registerApp(ResourceLocation id, ITerminalApp app) {
        if (appMap.containsKey(id) || HOME_ID.equals(id)) {
            throw new AssertionError("A terminal app with id " + id + " already exists!");
        }
        appMap.put(id, app);
    }
}
