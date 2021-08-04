package gregtech.api.terminal.app.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.terminal.util.TreeNode;
import gregtech.common.items.MetaItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;

public class ItemGuideApp extends GuideApp<MetaItem<?>.MetaValueItem> {
    private static TreeNode<String, MetaItem<?>.MetaValueItem> ROOT;
    private static Map<MetaItem<?>.MetaValueItem, JsonObject> MAP;
    private static Language language;

    private static JsonObject DEFAULT;
    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ROOT = new TreeNode<>(0,"root");
            DEFAULT = getConfig("terminal/guide/items/default.json");
            MAP = new HashMap<>();
        }
    }

    public ItemGuideApp() {
        super("Items", new ItemStackTexture(MetaItems.SCANNER.getStackForm()));
    }

    @Override
    protected IGuiTexture itemIcon(MetaItem<?>.MetaValueItem item) {
        return new ItemStackTexture(item.getStackForm());
    }

    @Override
    protected JsonObject getPage(MetaItem<?>.MetaValueItem item) {
        Language currentLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
        if (!currentLanguage.equals(language)) {
            language = currentLanguage;
            MAP.clear();
        }
        if (!MAP.containsKey(item)) {
            JsonObject config = getConfig("terminal/guide/items/" + currentLanguage.getLanguageCode() + "/" + item.unlocalizedName + ".json");
            if (config == null && !currentLanguage.getLanguageCode().equals("en_us")) {
                config = getConfig("terminal/guide/items/" + "en_us/" + item.unlocalizedName + ".json");
            }
            MAP.put(item, config);
        }
        if (MAP.get(item) == null) {
            DEFAULT.addProperty("title", "Missing: §4" + item.unlocalizedName + ".json");
            return DEFAULT;
        }
        return MAP.get(item);
    }

    @Override
    protected TreeNode<String, MetaItem<?>.MetaValueItem> getTree() {
        return ROOT;
    }

    public static void registerItem(MetaItem<?>.MetaValueItem item, String section) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ROOT.getOrCreateChild(section).addContent(String.format("metaitem.%s.name", item.unlocalizedName), item);
        }
    }
}
