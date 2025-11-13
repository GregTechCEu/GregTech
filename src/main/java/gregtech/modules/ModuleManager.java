package gregtech.modules;

import gregtech.api.GTValues;
import gregtech.api.modules.*;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.event.*;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public final class ModuleManager implements IModuleManager {

    private static final ModuleManager INSTANCE = new ModuleManager();
    private static final String MODULE_CFG_FILE_NAME = "modules.cfg";
    private static final String MODULE_CFG_CATEGORY_NAME = "modules";
    private static File configFolder;

    private Map<String, IModuleContainer> containers = new Object2ReferenceLinkedOpenHashMap<>();
    private final Map<ResourceLocation, IGregTechModule> sortedModules = new Object2ReferenceLinkedOpenHashMap<>();
    private final Set<IGregTechModule> loadedModules = new ReferenceLinkedOpenHashSet<>();

    private @Nullable IModuleContainer currentContainer;

    private ModuleStage currentStage = ModuleStage.C_SETUP;
    private static final Logger logger = LogManager.getLogger("GregTech Module Loader");
    private Configuration config;

    private ModuleManager() {}

    public static ModuleManager getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean isModuleEnabled(@NotNull ResourceLocation id) {
        return sortedModules.containsKey(id);
    }

    private boolean isModuleEnabled(@NotNull IGregTechModule module) {
        GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
        String comment = getComment(module);
        Property prop = getConfiguration().get(MODULE_CFG_CATEGORY_NAME,
                annotation.containerID() + ":" + annotation.moduleID(), true, comment);
        return prop.getBoolean();
    }

    @Override
    public @Nullable IModuleContainer getLoadedContainer() {
        return currentContainer;
    }

    @Override
    public @NotNull ModuleStage getStage() {
        return currentStage;
    }

    @Override
    public boolean hasPassedStage(@NotNull ModuleStage stage) {
        return currentStage.ordinal() > stage.ordinal();
    }

    @Override
    public void registerContainer(@NotNull IModuleContainer container) {
        if (currentStage != ModuleStage.C_SETUP) {
            logger.error("Failed to register module container {}, as module loading has already begun", container);
            return;
        }
        Preconditions.checkNotNull(container);
        containers.put(container.getID(), container);
    }

    /**
     * Set up the Module Manager
     *
     * @param asmDataTable    the data table containing all of the Module Container and Module classes
     * @param configDirectory the directory containing the GT config directory
     */
    public void setup(@NotNull ASMDataTable asmDataTable, @NotNull File configDirectory) {
        // find and register all containers registered with the @ModuleContainer annotation
        discoverContainers(asmDataTable);
        // then sort them by container name
        containers = containers.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, Object2ReferenceLinkedOpenHashMap::new));

        currentStage = ModuleStage.M_SETUP;
        configFolder = new File(configDirectory, GTValues.MODID);
        Map<String, List<IGregTechModule>> modules = getModules(asmDataTable);
        configureModules(modules);

        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Registering event handlers");
            for (Class<?> clazz : module.getEventBusSubscribers()) {
                MinecraftForge.EVENT_BUS.register(clazz);
            }
            for (Class<?> clazz : module.getTerrainGenBusSubscribers()) {
                MinecraftForge.TERRAIN_GEN_BUS.register(clazz);
            }
            for (Class<?> clazz : module.getOreGenBusSubscribers()) {
                MinecraftForge.ORE_GEN_BUS.register(clazz);
            }
        }
        currentContainer = null;
    }

    public void onConstruction(@NotNull FMLConstructionEvent event) {
        currentStage = ModuleStage.CONSTRUCTION;
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Construction start");
            module.construction(event);
            module.getLogger().debug("Construction complete");
        }
        currentContainer = null;
    }

    public void onPreInit(@NotNull FMLPreInitializationEvent event) {
        currentStage = ModuleStage.PRE_INIT;
        // Separate loops for strict ordering
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Registering packets");
            module.registerPackets();
        }
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Pre-init start");
            module.preInit(event);
            module.getLogger().debug("Pre-init complete");
        }
        currentContainer = null;
    }

    public void onInit(@NotNull FMLInitializationEvent event) {
        currentStage = ModuleStage.INIT;
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Init start");
            module.init(event);
            module.getLogger().debug("Init complete");
        }
        currentContainer = null;
    }

    public void onPostInit(@NotNull FMLPostInitializationEvent event) {
        currentStage = ModuleStage.POST_INIT;
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Post-init start");
            module.postInit(event);
            module.getLogger().debug("Post-init complete");
        }
        currentContainer = null;
    }

    public void onLoadComplete(@NotNull FMLLoadCompleteEvent event) {
        currentStage = ModuleStage.FINISHED;
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Load-complete start");
            module.loadComplete(event);
            module.getLogger().debug("Load-complete complete");
        }
        currentContainer = null;
    }

    public void onServerAboutToStart(@NotNull FMLServerAboutToStartEvent event) {
        currentStage = ModuleStage.SERVER_ABOUT_TO_START;
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Server-about-to-start start");
            module.serverAboutToStart(event);
            module.getLogger().debug("Server-about-to-start complete");
        }
        currentContainer = null;
    }

    public void onServerStarting(@NotNull FMLServerStartingEvent event) {
        currentStage = ModuleStage.SERVER_STARTING;
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Server-starting start");
            module.serverStarting(event);
            module.getLogger().debug("Server-starting complete");
        }
        currentContainer = null;
    }

    public void onServerStarted(@NotNull FMLServerStartedEvent event) {
        currentStage = ModuleStage.SERVER_STARTED;
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.getLogger().debug("Server-started start");
            module.serverStarted(event);
            module.getLogger().debug("Server-started complete");
        }
        currentContainer = null;
    }

    public void onServerStopping(@NotNull FMLServerStoppingEvent event) {
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.serverStopping(event);
        }
        currentContainer = null;
    }

    public void onServerStopped(@NotNull FMLServerStoppedEvent event) {
        for (IGregTechModule module : loadedModules) {
            currentContainer = containers.get(getContainerID(module));
            module.serverStopped(event);
        }
        currentContainer = null;
    }

    /**
     * Forward incoming IMC messages to each loaded module
     *
     * @param messages the messages to forward
     */
    public void processIMC(@NotNull @Unmodifiable List<FMLInterModComms.IMCMessage> messages) {
        for (FMLInterModComms.IMCMessage message : messages) {
            for (IGregTechModule module : loadedModules) {
                if (module.processIMC(message)) {
                    break;
                }
            }
        }
    }

    /**
     * Configure the modules according to the module Configuration
     *
     * @param modules the modules to configure
     */
    private void configureModules(@NotNull Map<String, List<IGregTechModule>> modules) {
        Locale locale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        Set<ResourceLocation> toLoad = new ObjectLinkedOpenHashSet<>();
        Set<IGregTechModule> modulesToLoad = new ReferenceLinkedOpenHashSet<>();
        Configuration config = getConfiguration();
        config.load();
        config.addCustomCategoryComment(MODULE_CFG_CATEGORY_NAME,
                "Module configuration file. Can individually enable/disable modules from GregTech and its addons");

        for (IModuleContainer container : containers.values()) {
            String containerID = container.getID();
            List<IGregTechModule> containerModules = modules.get(containerID);
            IGregTechModule coreModule = getCoreModule(containerModules);
            if (coreModule == null) {
                throw new IllegalStateException("Could not find core module for module container " + containerID);
            } else {
                containerModules.remove(coreModule);
                containerModules.add(0, coreModule);
            }

            // Remove disabled modules and gather potential modules to load
            Iterator<IGregTechModule> iterator = containerModules.iterator();
            while (iterator.hasNext()) {
                IGregTechModule module = iterator.next();
                if (!isModuleEnabled(module)) {
                    iterator.remove();
                    logger.debug("Module disabled: {}", module);
                    continue;
                }
                GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
                toLoad.add(new ResourceLocation(containerID, annotation.moduleID()));
                modulesToLoad.add(module);
            }
        }

        // Check any module dependencies
        Iterator<IGregTechModule> iterator;
        boolean changed;
        do {
            changed = false;
            iterator = modulesToLoad.iterator();
            while (iterator.hasNext()) {
                IGregTechModule module = iterator.next();

                // Check module dependencies
                Set<ResourceLocation> dependencies = module.getDependencyUids();
                if (!toLoad.containsAll(dependencies)) {
                    iterator.remove();
                    changed = true;
                    GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
                    String moduleID = annotation.moduleID();
                    toLoad.remove(new ResourceLocation(moduleID));
                    logger.info("Module {} is missing at least one of module dependencies: {}, skipping loading...",
                            moduleID, dependencies);
                }
            }
        } while (changed);

        // Sort modules by their module dependencies
        do {
            changed = false;
            iterator = modulesToLoad.iterator();
            while (iterator.hasNext()) {
                IGregTechModule module = iterator.next();
                if (sortedModules.keySet().containsAll(module.getDependencyUids())) {
                    iterator.remove();
                    GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
                    sortedModules.put(new ResourceLocation(annotation.containerID(), annotation.moduleID()), module);
                    changed = true;
                    break;
                }
            }
        } while (changed);

        loadedModules.addAll(sortedModules.values());

        if (config.hasChanged()) {
            config.save();
        }
        Locale.setDefault(locale);
    }

    /**
     * @param modules the list of modules possibly containing a Core Module
     * @return the first found Core Module found
     */
    private static @Nullable IGregTechModule getCoreModule(@NotNull Iterable<IGregTechModule> modules) {
        for (IGregTechModule module : modules) {
            GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
            if (annotation.coreModule()) {
                return module;
            }
        }
        return null;
    }

    /**
     * @param module the module to get the container ID for
     * @return the container ID
     */
    private static @NotNull String getContainerID(@NotNull IGregTechModule module) {
        GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);
        return annotation.containerID();
    }

    /**
     * @param table the ASM Data Table containing the module data
     * @return a map of Container ID to list of associated modules sorted by Module ID
     */
    private static @NotNull Map<String, List<IGregTechModule>> getModules(@NotNull ASMDataTable table) {
        List<IGregTechModule> instances = getInstances(table);
        Map<String, List<IGregTechModule>> modules = new Object2ReferenceLinkedOpenHashMap<>();
        for (IGregTechModule module : instances) {
            GregTechModule info = module.getClass().getAnnotation(GregTechModule.class);
            modules.computeIfAbsent(info.containerID(), k -> new ArrayList<>()).add(module);
        }
        return modules;
    }

    /**
     * @param table the ASM Data Table containing the module data
     * @return all IGregTechModule instances in sorted order by Container and Module ID
     */
    @SuppressWarnings("unchecked")
    private static @NotNull List<IGregTechModule> getInstances(@NotNull ASMDataTable table) {
        Set<ASMDataTable.ASMData> dataSet = table.getAll(GregTechModule.class.getCanonicalName());
        List<IGregTechModule> instances = new ArrayList<>();
        for (ASMDataTable.ASMData data : dataSet) {
            String moduleID = (String) data.getAnnotationInfo().get("moduleID");
            List<String> modDependencies = (List<String>) data.getAnnotationInfo().get("modDependencies");
            if (modDependencies == null || modDependencies.stream().allMatch(Loader::isModLoaded)) {
                try {
                    Class<?> clazz = Class.forName(data.getClassName());
                    if (IGregTechModule.class.isAssignableFrom(clazz)) {
                        instances.add((IGregTechModule) clazz.getConstructor().newInstance());
                    } else {
                        logger.error("Module of class {} with id {} is not an instanceof IGregTechModule",
                                clazz.getName(), moduleID);
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                         NoSuchMethodException | InvocationTargetException e) {
                    logger.error("Could not initialize module {}", moduleID, e);
                }
            } else {
                logger.info("Module {} is missing at least one of mod dependencies: {}, skipping loading...", moduleID,
                        modDependencies);
            }
        }
        return instances.stream()
                .sorted(Comparator.comparing((m) -> m.getClass()
                        .getAnnotation(GregTechModule.class),
                        Comparator.comparing(GregTechModule::containerID)
                                .thenComparing(GregTechModule::moduleID)))
                .collect(Collectors.toList());
    }

    /**
     * Discovers ModuleContainers and registers them
     *
     * @param table the table containing the ModuleContainer data
     */
    private void discoverContainers(@NotNull ASMDataTable table) {
        Set<ASMDataTable.ASMData> dataSet = table.getAll(ModuleContainer.class.getCanonicalName());
        for (ASMDataTable.ASMData data : dataSet) {
            try {
                Class<?> clazz = Class.forName(data.getClassName());
                if (IModuleContainer.class.isAssignableFrom(clazz)) {
                    registerContainer((IModuleContainer) clazz.getConstructor().newInstance());
                } else {
                    logger.error("Module Container Class {} is not an instanceof IModuleContainer", clazz.getName());
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                     InvocationTargetException e) {
                logger.error("Could not initialize module container {}", data.getClassName(), e);
            }
        }
    }

    /**
     * @param module the module to get the comment for
     * @return the comment for the module's configuration
     */
    private static String getComment(@NotNull IGregTechModule module) {
        GregTechModule annotation = module.getClass().getAnnotation(GregTechModule.class);

        String comment = annotation.description();
        Set<ResourceLocation> dependencies = module.getDependencyUids();
        if (!dependencies.isEmpty()) {
            Iterator<ResourceLocation> iterator = dependencies.iterator();
            StringBuilder builder = new StringBuilder(comment);
            builder.append("\n");
            builder.append("Module Dependencies: [ ");
            builder.append(iterator.next());
            while (iterator.hasNext()) {
                builder.append(", ").append(iterator.next());
            }
            builder.append(" ]");
            comment = builder.toString();
        }
        String[] modDependencies = annotation.modDependencies();
        if (modDependencies != null && modDependencies.length > 0) {
            Iterator<String> iterator = Arrays.stream(modDependencies).iterator();
            StringBuilder builder = new StringBuilder(comment);
            builder.append("\n");
            builder.append("Mod Dependencies: [ ");
            builder.append(iterator.next());
            while (iterator.hasNext()) {
                builder.append(", ").append(iterator.next());
            }
            builder.append(" ]");
            comment = builder.toString();
        }
        return comment;
    }

    /**
     * @return the module configuration instance
     */
    private @NotNull Configuration getConfiguration() {
        if (config == null) {
            config = new Configuration(new File(configFolder, MODULE_CFG_FILE_NAME));
        }
        return config;
    }
}
