package gregtech.api.terminal.app.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.terminal.util.TreeNode;
import net.minecraft.init.Items;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class TutorialGuideApp extends GuideApp<IGuiTexture> {
    private static TreeNode<String, Tuple<IGuiTexture, JsonObject>> ROOT;
    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ROOT = new TreeNode<>(0,"root");
            try {
                URL folder = ClassLoader.getSystemClassLoader().getResource("assets/gregtech/terminal/guide/tutorials");
                if (folder != null) {
                    new BufferedReader(new InputStreamReader(folder.openStream())).lines().forEach(file->{
                        JsonObject config = getConfig("terminal/guide/tutorials/" + file);
                        if (config != null) {
                            ROOT.getOrCreateChild(config.get("section").getAsString()).addContent(config.get("title").getAsString(), new Tuple<>(null, config));
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
    protected GuideApp<IGuiTexture> createAPP() {
        return new TutorialGuideApp();
    }

    @Override
    protected TreeNode<String, Tuple<IGuiTexture, JsonObject>> getTree() {
        return ROOT;
    }
}
