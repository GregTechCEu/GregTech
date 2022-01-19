package gregtech.api.recipes.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import static gregtech.integration.jei.multiblock.MultiblockInfoCategory.*;

@ZenClass("mods.gregtech.general.utils")
@ZenRegister
public class CTUtilities {

    @ZenMethod("RemoveMultiblockPreviewFromJei")
    public static void removeMulti(String name) {

        REGISTER.removeIf(multi -> multi.metaTileEntityId.toString().equals(name));

    }
}
