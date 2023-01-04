package gregtech.api.recipes.registration;

import com.google.common.base.CaseFormat;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class ProcessingHandlerRegistry {

    private static final Map<ResourceLocation, Consumer<Material>> registry = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceLocation, Set<Material>> blacklist = new Object2ObjectOpenHashMap<>();

    private ProcessingHandlerRegistry() {/**/}

    /**
     * Register the properly annotated methods of a class as processing handlers.
     * <p>
     * Processing handlers are registered with a {@link ResourceLocation} in format {@code "modid:class_name.annotated_name"}.
     * For example, a mod with id {@code "gregtech"}, a class with name {@code "IngotRecipes"}, and method with name {@code "plateBending"}
     * produces a ResourceLocation of {@code "gregtech:ingot_recipes.plate_bending}
     *
     * @param clazz the class to register
     */
    public static void register(@Nonnull Class<?> clazz) {
        final String modid = Objects.requireNonNull(Loader.instance().activeModContainer()).getModId();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ProcessingHandler.class)) {
                register(modid, clazz, method);
            }
        }
    }

    /**
     * Register an annotated method as a Processing Handler
     *
     * @param modid  the modid registering the method
     * @param clazz  the class containing the method
     * @param method the method to register
     */
    private static void register(@Nonnull String modid, @Nonnull Class<?> clazz, @Nonnull Method method) {
        ProcessingHandler annotation = method.getAnnotation(ProcessingHandler.class);
        if (annotation == null) {
            throw new IllegalStateException("Annotation was null despite being present. This should be impossible.");
        }

        final String group = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName());
        final String name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, annotation.name().isEmpty() ? method.getName() : annotation.name());

        ResourceLocation location = new ResourceLocation(modid, group + "." + name);

        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method " + method.getName() + " of class " + clazz.getName() + " must be static.");
        }

        switch (annotation.type()) {
            case MATERIAL: {
                register(location, invokeMaterial(clazz, method));
                return;
            }
            case PROPERTY: {
                register(location, invokeProperty(clazz, method));
                return;
            }
            case ORE_PREFIX: {
                String[] prefixNames = annotation.prefixes();
                if (prefixNames.length == 0) prefixError(clazz, method);
                register(location, invokePrefix(clazz, method, prefixNames));
                return;
            }
            case PROPERTY_PREFIX: {
                String[] prefixNames = annotation.prefixes();
                if (prefixNames.length == 0) prefixError(clazz, method);
                register(location, invokePropertyPrefix(clazz, method, prefixNames));
            }
        }
    }

    /**
     * Create a Processing Handler for {@link ProcessingHandler.Type#MATERIAL}
     *
     * @param clazz  the clazz of the method
     * @param method the method to invoke
     * @return the processing handler
     */
    @Nonnull
    private static Consumer<Material> invokeMaterial(@Nonnull Class<?> clazz, @Nonnull Method method) {
        validateParameterLength(clazz, method, 1);
        validateParameter(Material.class, clazz, method, method.getParameterTypes(), 0);

        return material -> {
            try {
                method.invoke(null, material);
            } catch (InvocationTargetException | IllegalAccessException e) {
                invokeError(clazz, method, e);
            }
        };
    }

    /**
     * Create a Processing Handler for {@link ProcessingHandler.Type#PROPERTY}
     *
     * @param clazz  the clazz of the method
     * @param method the method to invoke
     * @return the processing handler
     */
    @Nonnull
    private static Consumer<Material> invokeProperty(@Nonnull Class<?> clazz, @Nonnull Method method) {
        validateParameterLength(clazz, method, 2);

        Class<?>[] parameters = method.getParameterTypes();
        validateParameter(IMaterialProperty.class, clazz, method, parameters, 0);
        validateParameter(Material.class, clazz, method, parameters, 1);

        return material -> {
            IMaterialProperty<?> property = getProperty(parameters[0], material);
            if (property != null) try {
                method.invoke(null, property, material);
            } catch (InvocationTargetException | IllegalAccessException e) {
                invokeError(clazz, method, e);
            }
        };
    }

    /**
     * Create an Processing Handler for {@link ProcessingHandler.Type#ORE_PREFIX}
     *
     * @param clazz  the clazz of the method
     * @param method the method to invoke
     * @return the processing handler
     */
    @Nonnull
    private static Consumer<Material> invokePrefix(@Nonnull Class<?> clazz, @Nonnull Method method, @Nonnull String[] prefixNames) {
        validateParameterLength(clazz, method, 2);

        Class<?>[] parameters = method.getParameterTypes();
        validateParameter(OrePrefix.class, clazz, method, parameters, 0);
        validateParameter(Material.class, clazz, method, parameters, 1);

        // Do not get the prefixes in the lambda, so potential errors happen only once
        Collection<OrePrefix> prefixes = getPrefixes(clazz, method, prefixNames);
        return material -> {
            for (OrePrefix prefix : prefixes) {
                try {
                    method.invoke(null, prefix, material);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    invokeError(clazz, method, e);
                }
            }
        };
    }

    /**
     * Create an Processing Handler for {@link ProcessingHandler.Type#PROPERTY_PREFIX}
     *
     * @param clazz  the clazz of the method
     * @param method the method to invoke
     * @return the processing handler
     */
    @Nonnull
    private static Consumer<Material> invokePropertyPrefix(@Nonnull Class<?> clazz, @Nonnull Method method, @Nonnull String[] prefixNames) {
        validateParameterLength(clazz, method, 3);

        Class<?>[] parameters = method.getParameterTypes();
        validateParameter(IMaterialProperty.class, clazz, method, parameters, 0);
        validateParameter(OrePrefix.class, clazz, method, parameters, 1);
        validateParameter(Material.class, clazz, method, parameters, 2);

        // Do not get the prefixes in the lambda, so potential errors happen only once
        Collection<OrePrefix> prefixes = getPrefixes(clazz, method, prefixNames);
        return material -> {
            IMaterialProperty<?> property = getProperty(parameters[0], material);
            if (property == null) return;
            for (OrePrefix prefix : prefixes) {
                try {
                    method.invoke(null, property, prefix, material);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    invokeError(clazz, method, e);
                }
            }
        };
    }

    /**
     * Validate the length of parameters
     *
     * @param clazz  the class of the method, for logging
     * @param method the method whose parameters to check
     * @param length the amount of parameters to enforce
     */
    private static void validateParameterLength(@Nonnull Class<?> clazz, @Nonnull Method method, int length) {
        if (method.getParameterTypes().length != length) {
            throw new IllegalArgumentException("Method " + method.getName() + " of class " + clazz.getName() +
                    " must take exactly " + length + " parameters.");
        }
    }

    /**
     * Validate the position of a specific class in an array of parameters
     *
     * @param validation the class to ensure is in the correct position
     * @param clazz      the class containing the method, used for logging
     * @param method     the method containing the parameters, used for logging
     * @param classes    the parameter classes to check
     * @param index      the index of the specific class
     */
    private static void validateParameter(@Nonnull Class<?> validation, @Nonnull Class<?> clazz, @Nonnull Method method, @Nonnull Class<?>[] classes, int index) {
        if (index >= classes.length || !validation.isAssignableFrom(classes[index])) {
            throw new IllegalArgumentException("Method " + method.getName() + " of class " + clazz.getName() +
                    " must take an " + validation.getName() + " for parameter " + index);
        }
    }

    /**
     * @param clazz       the class of the method to invoke, for logging
     * @param method      the method to invoke, for logging
     * @param prefixNames the ore prefix names to turn into OrePrefixes
     * @return the ore prefixes corresponding to the names
     */
    @Nonnull
    private static Collection<OrePrefix> getPrefixes(@Nonnull Class<?> clazz, @Nonnull Method method, @Nonnull String[] prefixNames) {
        Collection<OrePrefix> prefixes = new ObjectOpenHashSet<>();
        for (String s : prefixNames) {
            OrePrefix prefix = OrePrefix.getPrefix(s);
            if (prefix == null) GTLog.logger.warn("Could not find OrePrefix with name {}", s);
            else prefixes.add(prefix);
        }
        if (prefixes.isEmpty()) prefixError(clazz, method);
        return prefixes;
    }

    /**
     * @param propertyClass the class for the IMaterialProperty to retrieve
     * @param material      the material to retrieve the property from
     * @return the material's property
     */
    @Nullable
    private static IMaterialProperty<?> getProperty(@Nonnull Class<?> propertyClass, @Nonnull Material material) {
        PropertyKey<?> key = PropertyKey.getPropertyKey(propertyClass);
        if (key == null) {
            throw new IllegalArgumentException("No PropertyKey exists for type " + propertyClass.getName());
        }
        return material.getProperty(key);
    }

    /**
     * Throw an error for when an Processing Handler requires OrePrefixes but has none.
     *
     * @param clazz  the class containing the method
     * @param method the method called
     */
    private static void prefixError(@Nonnull Class<?> clazz, @Nonnull Method method) {
        throw new IllegalArgumentException("Method " + method.getName() + " of class " + clazz.getName() + " must supply OrePrefixes.");
    }

    /**
     * Throw an error for when Processing Handler invocation fails.
     *
     * @param clazz  the class containing the method
     * @param method the method called
     * @param e      the error
     */
    private static void invokeError(@Nonnull Class<?> clazz, @Nonnull Method method, @Nonnull Exception e) {
        throw new IllegalStateException("Error invoking Processing Handler " + method.getName() + " of class " + clazz.getName(), e);
    }

    /**
     * Register an Processing Handler
     *
     * @param location the unique name of the ProcessingHandler, in format {@code "modid:class_name.method_name}
     * @param handler  the handler to register
     */
    private static void register(@Nonnull ResourceLocation location, @Nonnull Consumer<Material> handler) {
        if (registry.containsKey(location)) {
            throw new IllegalArgumentException("Processing Handler " + location + " is already registered.");
        }
        registry.put(location, handler);
    }

    /**
     * Remove an Processing Handler
     *
     * @param location the location of the handler to remove
     * @return whether removal was successful
     */
    public static boolean remove(@Nonnull ResourceLocation location) {
        return registry.remove(location) != null;
    }

    /**
     * Remove all registered Processing Handlers and Blacklists.
     * <strong>Use with caution.</strong>
     */
    public static void clear() {
        registry.clear();
        blacklist.clear();
    }

    /**
     * @param location the name of the handler
     * @param material the material to blacklist it for
     * @return if a material is blacklisted for a processing handler
     */
    public static boolean isBlacklisted(@Nonnull ResourceLocation location, @Nonnull Material material) {
        Set<Material> set = blacklist.get(location);
        return set != null && set.contains(material);
    }

    /**
     * BlackList an Processing Handler from running for a material
     *
     * @param location the name of the handler
     * @param material the material to blacklist
     */
    public static boolean blacklist(@Nonnull ResourceLocation location, @Nonnull Material material) {
        if (!registry.containsKey(location)) {
            GTLog.logger.warn("Processing Handler {} could not be found", location);
            return false;
        }
        Set<Material> set = blacklist.computeIfAbsent(location, k -> new ObjectOpenHashSet<>());
        if (set.contains(material)) {
            GTLog.logger.warn("Material {} is already blacklisted for Processing Handler {}", material, location);
            return false;
        }
        return set.add(material);
    }

    /**
     * Unblacklist a Processing Handler and allow it to run for a material
     *
     * @param location the name of the handler
     * @param material the material to unblacklist
     */
    @SuppressWarnings("unused")
    public static boolean unblacklist(@Nonnull ResourceLocation location, @Nonnull Material material) {
        if (!registry.containsKey(location)) {
            GTLog.logger.warn("Processing Handler {} could not be found", location);
            return false;
        }
        Set<Material> set = blacklist.get(location);
        if (!set.contains(material)) {
            GTLog.logger.warn("Material {} is already not blacklisted for Processing Handler {}", material, location);
            return false;
        }
        return set.remove(material);
    }

    /**
     * @return the amount of registered handlers
     */
    public static int size() {
        return registry.size();
    }

    /**
     * Run all registered processing handlers.
     * <p>
     * <strong>Only GregTech should call this.</strong>
     */
    public static void runHandlers() {
        ModContainer container = Loader.instance().activeModContainer();
        if (container == null) throw new IllegalStateException("Active Mod Container is null.");
        if (!GTValues.MODID.equals(container.getModId())) {
            throw new IllegalStateException("Do not call ProcessingHandlerRegistry#runHandlers unless you are GregTech.");
        }

        GregTechAPI.MaterialRegistry.getAllMaterials().forEach(material -> registry.forEach((location, handler) -> {
            Set<Material> set = blacklist.get(location);
            if (set != null && set.contains(material)) return;
            handler.accept(material);
        }));
    }
}
