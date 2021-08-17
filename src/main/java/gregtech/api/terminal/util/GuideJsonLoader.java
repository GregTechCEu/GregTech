package gregtech.api.terminal.util;

import com.google.gson.JsonObject;
import gregtech.api.terminal.TerminalRegistry;
import gregtech.common.terminal.app.guide.GuideApp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GuideJsonLoader implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        if(Loader.instance().activeModContainer() == null) return;
        if (Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage() == null) return;
        String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        for (String appName : TerminalRegistry.getAllApps()) {
            if (TerminalRegistry.getApplication(appName) instanceof GuideApp) {
                List<JsonObject> jsons = new ArrayList<>();
                GuideApp<?> app = (GuideApp<?>) TerminalRegistry.getApplication(appName);
                CraftingHelper.findFiles(Loader.instance().activeModContainer(), "assets/gregtech/terminal/guide/" + appName + "/en_us", Files::exists, (path, file) -> {
                    if(file.toString().endsWith(".json")) {
                        String fileName = file.getFileName().toString();
                        JsonObject json = app.getConfig(fileName, lang);
                        if (json == null) {
                            json = app.getConfig(fileName, "en_us");
                        }
                        if (json != null) {
                            jsons.add(json);
                        }
                    }
                    return true;
                },false, true);
                app.loadJsonFiles(jsons);
            }
        }


    }
}
