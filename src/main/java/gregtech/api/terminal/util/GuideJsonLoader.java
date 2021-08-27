package gregtech.api.terminal.util;

import com.google.gson.JsonObject;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.api.util.GTLog;
import gregtech.common.terminal.app.guide.GuideApp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GuideJsonLoader implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        for (String appName : TerminalRegistry.getAllApps()) {
            if (TerminalRegistry.getApplication(appName) instanceof GuideApp) {
                List<JsonObject> jsons = new ArrayList<>();
                GuideApp<?> app = (GuideApp<?>) TerminalRegistry.getApplication(appName);
                try {
                    Path guidePath = TerminalRegistry.TERMINAL_PATH.toPath().resolve("guide/" + appName);
                    Path en_us = guidePath.resolve("en_us");
                    Files.walk(en_us).filter(Files::isRegularFile).filter(f -> f.toString().endsWith(".json")).forEach(file -> {
                        File langFile = guidePath.resolve(lang + "/" + en_us.relativize(file).toString()).toFile();
                        JsonObject json = app.getConfig(langFile);
                        if (json == null) {
                            json = app.getConfig(file.toFile());
                        }
                        if (json != null) {
                            jsons.add(json);
                        }
                    });
                } catch (IOException e) {
                    GTLog.logger.error("Failed to save file on path {}", "terminal", e);
                }
                app.loadJsonFiles(jsons);
            }
        }
    }
}
