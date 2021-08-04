package gregtech.api.terminal.app.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.terminal.util.TreeNode;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.HashMap;
import java.util.Map;

public class MultiBlockGuideApp extends GuideApp<MetaTileEntity> {
    private static TreeNode<String, MetaTileEntity> ROOT;
    private static Map<MetaTileEntity, JsonObject> MAP;
    private static Language language;

    private static JsonObject DEFAULT;
    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ROOT = new TreeNode<>(0, "root");
            DEFAULT = getConfig("terminal/guide/multiblocks/default.json");
            MAP = new HashMap<>();
        }
    }

    public MultiBlockGuideApp() {
        super("Multi-Block Machines", new ItemStackTexture(MetaTileEntities.ELECTRIC_BLAST_FURNACE.getStackForm()));
    }

    @Override
    protected TreeNode<String, MetaTileEntity> getTree() {
        return ROOT;
    }

    @Override
    protected IGuiTexture itemIcon(MetaTileEntity item) {
        return new ItemStackTexture(item.getStackForm());
    }

    @Override
    protected JsonObject getPage(MetaTileEntity mte) {
        Language currentLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage();
        if (!currentLanguage.equals(language)) {
            language = currentLanguage;
            MAP.clear();
        }
        if (!MAP.containsKey(mte)) {
            JsonObject config = getConfig("terminal/guide/multiblocks/" + currentLanguage.getLanguageCode() + "/" + mte.metaTileEntityId.getPath() + ".json");
            if (config == null && !currentLanguage.getLanguageCode().equals("en_us")) {
                config = getConfig("terminal/guide/multiblocks/" + "en_us/" + mte.metaTileEntityId.getPath() + ".json");
            }
            MAP.put(mte, config);
        }
        if (MAP.get(mte) == null) {
            return DEFAULT;
        }
        return MAP.get(mte);
    }

    public static void registerMultiBlock(MetaTileEntity mte, String section) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ROOT.getOrCreateChild(section).addContent(mte.getMetaFullName(), mte);
        }
    }

}
