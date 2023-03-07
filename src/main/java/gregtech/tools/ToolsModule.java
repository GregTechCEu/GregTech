package gregtech.tools;

import gregtech.api.GTValues;
import gregtech.tools.enchants.EnchantmentEnderDamage;
import gregtech.tools.enchants.EnchantmentHardHammer;
import gregtech.api.modules.GregTechModule;
import gregtech.modules.BaseGregTechModule;
import gregtech.modules.GregTechModules;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

@GregTechModule(
        moduleID = GregTechModules.MODULE_TOOLS,
        containerID = GTValues.MODID,
        name = "GregTech Tools",
        descriptionKey = "gregtech.modules.tools.description"
)
public class ToolsModule extends BaseGregTechModule {

    public static final Logger logger = LogManager.getLogger("GregTech Tools");

    @Nonnull
    @Override
    public Logger getLogger() {
        return logger;
    }

    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(ToolsModule.class);
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(EnchantmentEnderDamage.INSTANCE);
        event.getRegistry().register(EnchantmentHardHammer.INSTANCE);
    }
}
