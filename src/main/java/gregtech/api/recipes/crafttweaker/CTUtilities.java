package gregtech.api.recipes.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.integration.jei.multiblock.MultiblockInfoCategory.REGISTER;

@ZenClass("mods.gregtech.general.utils")
@ZenRegister
public class CTUtilities {

    @ZenMethod("RemoveMultiblockPreviewFromJei")
    public static void removeMulti(String name) {
        REGISTER.removeIf(multi -> multi.metaTileEntityId.toString().equals(name));
    }

    @ZenMethod("GetMetaItem")
    public static IItemStack getMetaItem(String metaId) {
        return MetaItemBracketHandler.getMetaItem(metaId);
    }

    @ZenMethod("GetMetaTileEntity")
    public static IItemStack getMetaTileEntityItem(String metaId) {
        return MetaTileEntityBracketHandler.getMetaTileEntityItem(metaId);
    }
}
