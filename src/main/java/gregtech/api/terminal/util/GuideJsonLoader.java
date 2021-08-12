package gregtech.api.terminal.util;

import com.google.gson.JsonObject;
import gregtech.api.terminal.TerminalBuilder;
import gregtech.api.terminal.app.guide.GuideApp;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GuideJsonLoader implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        if(Loader.instance().activeModContainer() == null) return;
        List<ResourceLocation> files = new ArrayList<>();
        CraftingHelper.findFiles(Loader.instance().activeModContainer(), "assets/gregtech/terminal/guide", Files::exists, (path, file) -> {
            if(file.toString().endsWith(".json")) {
                String f = file.toString().replaceAll("\\\\", "/");
                files.add(new ResourceLocation("gregtech", f.split("assets/gregtech/")[1]));
            }
            return true;
        }, false, true);

        files.forEach(rl -> {
            JsonObject json = GuideApp.getConfig(rl.getPath());
            GuideApp<?> app = (GuideApp<?>) TerminalBuilder.getApplication(rl.getPath().split("/")[2]);
            if(json != null && app != null) {
                app.loadJsonFile(json, rl.getPath().split("/")[3].toLowerCase());
            }
        });
    }
}
