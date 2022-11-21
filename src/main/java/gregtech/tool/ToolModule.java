package gregtech.tool;

import gregtech.api.GTValues;
import gregtech.api.module.GregTechModule;
import gregtech.module.BaseGregTechModule;
import gregtech.module.GregTechModules;
import gregtech.tool.enchantment.EnchantmentEnderDamage;
import gregtech.tool.enchantment.EnchantmentHardHammer;
import gregtech.tool.sound.ToolSounds;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@GregTechModule(
        moduleID = GregTechModules.MODULE_TOOL,
        containerID = GTValues.MODID,
        name = "GregTech Tools",
        descriptionKey = "gregtech.modules.tool.description"
)
public class ToolModule extends BaseGregTechModule {

    public static final Logger logger = LogManager.getLogger("GregTech Tools");

    @Nonnull
    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        ToolSounds.register();
    }

    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ToolModule.class);
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(EnchantmentEnderDamage.INSTANCE);
        event.getRegistry().register(EnchantmentHardHammer.INSTANCE);
    }
}
