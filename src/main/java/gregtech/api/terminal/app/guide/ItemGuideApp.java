package gregtech.api.terminal.app.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.terminal.util.TreeNode;
import gregtech.common.items.MetaItems;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class ItemGuideApp extends GuideApp<MetaItem<?>> {
    private static TreeNode<String, Tuple<MetaItem<?>, JsonObject>> ROOT;
    private static JsonObject DEFAULT;
    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ROOT = new TreeNode<>(0,"root");
            DEFAULT = getConfig("terminal/guide/items/default.json");
        }
    }

    public ItemGuideApp() {
        super("Items Machines", new ItemStackTexture(MetaItems.SCANNER.getStackForm()));
    }

    @Override
    protected GuideApp<MetaItem<?>> createAPP() {
        return new ItemGuideApp();
    }

    @Override
    protected TreeNode<String, Tuple<MetaItem<?>, JsonObject>> getTree() {
        return ROOT;
    }

    public static void registerItem(MetaTileEntity mte) {

    }
}
