package gtrmcore;

import gregtech.GTInternalTags;

import gtrmcore.api.GTRMValues;
import gtrmcore.common.CommonProxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

@Mod(modid = GTRMValues.MODID,
     name = GTRMValues.MODNAME,
     version = "0.0.1",
     dependencies = "required-after:mixinbooter;" +
             GTInternalTags.DEP_VERSION_STRING + "required-after:" + GTRMValues.MODID_GCYM + ";")

public class GTRMCoreMod {

    @SidedProxy(modId = GTRMValues.MODID,
                clientSide = "gtrmcore.client.ClientProxy",
                serverSide = "gtrmcore.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {}

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {}

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {}

    @Mod.EventHandler
    public void serverStopped(FMLServerStoppedEvent event) {}

    @Mod.EventHandler
    public void respondIMC(FMLInterModComms.IMCEvent event) {}
}
