package gregtech;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.GTFluidRegistration;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.registry.MarkerMaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.common.items.MetaItems;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import gregtech.modules.ModuleManager;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class Bootstrap {

    private static boolean bootstrapped = false;

    private Bootstrap() {}

    public static void perform() {
        if (bootstrapped) {
            return;
        }
        try {
            Field deobfuscatedEnvironment = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
            deobfuscatedEnvironment.setAccessible(true);
            deobfuscatedEnvironment.setBoolean(null, true);

            Method setLocale = I18n.class.getDeclaredMethod("setLocale", Locale.class); // No need to care about
                                                                                        // obfuscation
            setLocale.setAccessible(true);
            setLocale.invoke(null, new Locale());

            // set FMLCommonHandler#sidedDelegate, since MaterialIconType and LocalizationUtils uses it
            Field sidedDelegate = FMLCommonHandler.class.getDeclaredField("sidedDelegate");
            sidedDelegate.setAccessible(true);
            sidedDelegate.set(FMLCommonHandler.instance(), new TestSidedHandler());
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Unexpected exception on test bootstrap", e);
        }
        net.minecraft.init.Bootstrap.register();
        ModMetadata meta = new ModMetadata();
        meta.modId = GTValues.MODID;
        Loader.instance().setupTestHarness(new DummyModContainer(meta));
        GregTechAPI.moduleManager = ModuleManager.getInstance();

        MaterialRegistryManager managerInternal = MaterialRegistryManager.getInstance();
        GregTechAPI.materialManager = managerInternal;
        GregTechAPI.markerMaterialRegistry = MarkerMaterialRegistry.getInstance();
        managerInternal.unfreezeRegistries();
        Materials.register();
        managerInternal.closeRegistries();
        managerInternal.freezeRegistries();

        // loadGregtechLangFile();

        OrePrefix.runMaterialHandlers();
        GTFluidRegistration.INSTANCE.register();
        MetaItems.init();
        bootstrapped = true;
    }

    private static void loadGregtechLangFile() {
        try (InputStream is = LanguageMap.class.getResourceAsStream("/assets/gregtech/lang/en_us.lang")) {
            if (is != null) {
                LanguageMap.inject(is);
            } else {
                GTLog.logger.error("Couldn't find en_us.lang file");
            }
        } catch (IOException ex) {
            GTLog.logger.error("Unable to read en_us.lang file", ex);
        }
    }

    private static final class TestSidedHandler implements IFMLSidedHandler {

        @Override
        public List<String> getAdditionalBrandingInformation() {
            return Collections.emptyList();
        }

        @Override
        public Side getSide() {
            return Side.SERVER;
        }

        @Override
        public void haltGame(String message, Throwable exception) {
            throw new RuntimeException(message, exception);
        }

        @Override
        public void showGuiScreen(Object clientGuiElement) {}

        @Override
        public void queryUser(StartupQuery query) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void beginServerLoading(MinecraftServer server) {}

        @Override
        public void finishServerLoading() {}

        @Override
        public File getSavesDirectory() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MinecraftServer getServer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDisplayCloseRequested() {
            return false;
        }

        @Override
        public boolean shouldServerShouldBeKilledQuietly() {
            return false;
        }

        @Override
        public void addModAsResource(ModContainer container) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCurrentLanguage() {
            return "en_US";
        }

        @Override
        public void serverStopped() {}

        @Override
        public NetworkManager getClientToServerNetworkManager() {
            throw new UnsupportedOperationException();
        }

        @Override
        public INetHandler getClientPlayHandler() {
            return null;
        }

        @Override
        public void fireNetRegistrationEvent(EventBus bus, NetworkManager manager, Set<String> channelSet,
                                             String channel, Side side) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean shouldAllowPlayerLogins() {
            return false;
        }

        @Override
        public void allowLogins() {}

        @Override
        public IThreadListener getWorldThread(INetHandler net) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void processWindowMessages() {}

        @Override
        public String stripSpecialChars(String message) {
            return message;
        }

        @Override
        public void reloadRenderers() {}

        @Override
        public void fireSidedRegistryEvents() {}

        @Override
        public CompoundDataFixer getDataFixer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDisplayVSyncForced() {
            return false;
        }
    }
}
