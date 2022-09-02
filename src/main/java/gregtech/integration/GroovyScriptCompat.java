package gregtech.integration;

import com.cleanroommc.groovyscript.api.BracketHandler;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.crafttweaker.MetaItemBracketHandler;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraftforge.fml.common.Loader;

import static gregtech.api.GregTechAPI.MATERIAL_REGISTRY;

public class GroovyScriptCompat extends ModPropertyContainer {

    private static boolean loaded = false;

    private static ModSupport.Container<GroovyScriptCompat> modSupportContainer;

    public static void init() {
        loaded = Loader.isModLoaded(GTValues.MODID_GROOVYSCRIPT);
        if (!loaded) return;

        BracketHandler.registerBracketHandler("recipemap", RecipeMap::getByName);
        BracketHandler.registerBracketHandler("material", MATERIAL_REGISTRY::getObject);
        BracketHandler.registerBracketHandler("oreprefix", OrePrefix::getPrefix);
        BracketHandler.registerBracketHandler("metaitem", MetaItemBracketHandler::getMetaItem);

        modSupportContainer = new ModSupport.Container<>(GTValues.MODID, "GregTech", GroovyScriptCompat::new, "");
    }

    @Override
    protected void addRegistry(VirtualizedRegistry<?> registry) {
        super.addRegistry(registry);
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static GroovyScriptCompat getInstance() {
        return loaded ? modSupportContainer.get() : null;
    }
}
