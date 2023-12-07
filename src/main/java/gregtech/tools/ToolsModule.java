package gregtech.tools;

import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.common.items.tool.rotation.CustomBlockRotations;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;
import gregtech.tools.enchants.EnchantmentEnderDamage;
import gregtech.tools.enchants.EnchantmentHardHammer;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@GregTechModule(
                moduleID = GregTechModules.MODULE_TOOLS,
                containerID = GTValues.MODID,
                name = "GregTech Tools",
                description = "GregTech Tools Module. Cannot be disabled for now.")
public class ToolsModule extends BaseGregTechModule {

    public static final Logger logger = LogManager.getLogger("GregTech Tools");

    @NotNull
    @Override
    public Logger getLogger() {
        return logger;
    }

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ToolsModule.class);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        CustomBlockRotations.init();
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(EnchantmentEnderDamage.INSTANCE);
        event.getRegistry().register(EnchantmentHardHammer.INSTANCE);
    }
}
