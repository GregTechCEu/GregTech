package gregtech.integration.tinkers;

import com.google.common.collect.ImmutableList;
import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import gregtech.integration.tinkers.book.GTBook;
import gregtech.integration.tinkers.effect.GTTinkerEffects;
import gregtech.modules.GregTechModules;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.List;

@GregTechModule(
        moduleID = GregTechModules.MODULE_TCON,
        containerID = GTValues.MODID,
        modDependencies = GTValues.MODID_TCON,
        name = "GregTech Tinkers' Construct Integration",
        description = "Tinkers' Construct Integration Module")
public class TinkersModule extends IntegrationSubmodule {

    @Nonnull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return ImmutableList.<Class<?>>builder().add(TinkersEvents.class, TinkersModule.class).build();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            GTBook.register();
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {
        GTTinkerEffects.registerModifiers();
        GTTinkerEffects.registerTraits();
    }

    @SubscribeEvent
    public static void registerPotionEffects(RegistryEvent.Register<Potion> event) {
        GTTinkerEffects.registerEffects(event.getRegistry());
    }
}
