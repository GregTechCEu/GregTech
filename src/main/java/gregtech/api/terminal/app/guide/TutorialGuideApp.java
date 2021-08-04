package gregtech.api.terminal.app.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.terminal.util.TreeNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.init.Items;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TutorialGuideApp extends GuideApp<String> {
    private static TreeNode<String, String> ROOT;
    private static Map<String, JsonObject> MAP;
    private static Language language;

    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ROOT = new TreeNode<>(0,"root");
            MAP = new HashMap<>();
            try {
                URL folder = ClassLoader.getSystemClassLoader().getResource("assets/gregtech/terminal/guide/tutorials/en_us");
                if (folder != null) {
                    new BufferedReader(new InputStreamReader(folder.openStream())).lines().forEach(file->{
                        JsonObject config = getConfig("terminal/guide/tutorials/en_us/" + file);
                        if (config != null) {
                            ROOT.getOrCreateChild(config.get("section").getAsString()).addContent(config.get("title").getAsString(), file);
                            MAP.put(file, config);
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public TutorialGuideApp() {
        super("Tutorials", new ItemStackTexture(Items.PAPER));
    }

    @Override
    protected JsonObject getPage(String file) {
        Language currentLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
        if (!currentLanguage.equals(language)) {
            language = currentLanguage;
            MAP.clear();
        }
        if (!MAP.containsKey(file)) {
            JsonObject config = getConfig("terminal/guide/tutorials/" + currentLanguage.getLanguageCode() + "/" + file);
            if (config == null && !currentLanguage.getLanguageCode().equals("en_us")) {
                config = getConfig("terminal/guide/tutorials/" + "en_us/" + file);
            }
            MAP.put(file, config);
        }
        return MAP.get(file);
    }

    @Override
    protected TreeNode<String, String> getTree() {
        return ROOT;
    }
}
