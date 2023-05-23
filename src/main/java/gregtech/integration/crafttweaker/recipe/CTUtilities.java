package gregtech.integration.crafttweaker.recipe;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.integration.jei.multiblock.MultiblockInfoCategory.REGISTER;

@ZenClass("mods.gregtech.general.utils")
@ZenRegister
@SuppressWarnings("unused")
public class CTUtilities {

    // TODO YEET

    @ZenMethod("RemoveMultiblockPreviewFromJei")
    public static void removeMulti(String name) {
        REGISTER.removeIf(multi -> multi.metaTileEntityId.toString().equals(name));
    }
}
