package gregtech.integration;

import com.cleanroommc.groovyscript.GroovyScript;
import com.cleanroommc.groovyscript.brackets.BracketHandlerManager;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import gregtech.api.GTValues;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.crafttweaker.MetaItemBracketHandler;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import static gregtech.api.GregTechAPI.MATERIAL_REGISTRY;

/**
 * A utility class to manage GroovyScript compat. Is safe to be called when GroovyScript is not installed.
 */
public class GroovyScriptCompat {

    private static boolean loaded = false;

    private static ModSupport.Container<Container> modSupportContainer;

    private GroovyScriptCompat() {
    }

    public static void init() {
        loaded = Loader.isModLoaded(GTValues.MODID_GROOVYSCRIPT);
        if (!loaded) return;

        BracketHandlerManager.registerBracketHandler("recipemap", RecipeMap::getByName);
        BracketHandlerManager.registerBracketHandler("material", MATERIAL_REGISTRY::getObject);
        BracketHandlerManager.registerBracketHandler("oreprefix", OrePrefix::getPrefix);
        BracketHandlerManager.registerBracketHandler("metaitem", MetaItemBracketHandler::getMetaItem);

        modSupportContainer = new ModSupport.Container<>(GTValues.MODID, "GregTech", Container::new, "gregtech", "gt");
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isCurrentlyRunning() {
        return loaded && GroovyScript.getSandbox().isRunning();
    }

    public static Container getInstance() {
        return modSupportContainer.get();
    }

    /**
     * A GroovyScript mod compat container. This should not be referenced when GrS is not installed!
     */
    public static class Container extends ModPropertyContainer {

        private Container() {
        }

        @Override
        protected void addRegistry(VirtualizedRegistry<?> registry) {
            super.addRegistry(registry);
        }
    }
}
