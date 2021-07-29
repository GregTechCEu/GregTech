package gregtech.api.terminal.app.guide;

import com.google.gson.JsonObject;
import gregtech.api.gui.resources.IGuiTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.terminal.util.TreeNode;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class MultiBlockGuideApp extends GuideApp<MetaTileEntity> {
    private static TreeNode<String, Tuple<MetaTileEntity, JsonObject>> ROOT;
    private static JsonObject DEFAULT;
    static {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ROOT = new TreeNode<>(0, "root");
            DEFAULT = getConfig("terminal/guide/multiblocks/default.json");
        }
    }

    public MultiBlockGuideApp() {
        super("Multi-Block Machines", new ItemStackTexture(MetaTileEntities.ELECTRIC_BLAST_FURNACE.getStackForm()));
    }

    @Override
    protected TreeNode<String, Tuple<MetaTileEntity, JsonObject>> getTree() {
        return ROOT;
    }

    @Override
    protected IGuiTexture itemIcon(MetaTileEntity item) {
        return new ItemStackTexture(item.getStackForm());
    }

    public static void registerMultiBlock(MetaTileEntity mte) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            JsonObject config = getConfig("terminal/guide/multiblocks/" + mte.metaTileEntityId.getPath() + ".json");
            if (config == null) {
                ROOT.getOrCreateChild("default").addContent(mte.getMetaFullName(), new Tuple<>(mte, DEFAULT));
            } else {
                ROOT.getOrCreateChild(config.get("section").getAsString()).addContent(mte.getMetaFullName(), new Tuple<>(mte, config));
            }
        }
    }

}
