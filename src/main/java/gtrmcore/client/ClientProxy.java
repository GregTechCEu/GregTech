package gtrmcore.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import gtrmcore.common.CommonProxy;
import gtrmcore.common.items.GTRMItems;
import gtrmcore.common.metatileentities.GTRMMetaBlocks;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // GTRMTextures.preInit();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void onPreLoad() {
        super.onPreLoad();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        GTRMMetaBlocks.registerItemModels();
        GTRMItems.registerItemModels();
    }
}
