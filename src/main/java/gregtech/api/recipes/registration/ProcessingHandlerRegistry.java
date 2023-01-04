package gregtech.api.recipes.registration;

import com.google.common.base.CaseFormat;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.GTLog;
import gregtech.api.util.function.TriConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nonnull;
import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ProcessingHandlerRegistry {

    private static final Map<ResourceLocation, Consumer<Material>> registry = new Object2ObjectOpenHashMap<>();
    private static final Map<ResourceLocation, Set<Material>> blacklist = new Object2ObjectOpenHashMap<>();

    // must include private lookup for LambdaMetaFactory
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodType CONSUMER_TYPE = MethodType.methodType(Consumer.class);
    private static final MethodType CONSUMER = MethodType.methodType(void.class, Material.class);
    private static final MethodType GENERIC_CONSUMER = MethodType.methodType(void.class, CONSUMER.generic().parameterList());

    private static final MethodType BI_CONSUMER_TYPE = MethodType.methodType(BiConsumer.class);
    private static final Function<Class<?>, MethodType> BI_CONSUMER = clazz -> MethodType.methodType(void.class, clazz, Material.class);
    private static final Function<Class<?>, MethodType> GENERIC_BI_CONSUMER = clazz -> MethodType.methodType(void.class, BI_CONSUMER.apply(clazz).generic().parameterList());

    private static final MethodType TRI_CONSUMER_TYPE = MethodType.methodType(TriConsumer.class);
    private static final BiFunction<Class<?>, Class<?>, MethodType> TRI_CONSUMER = (clazz1, clazz2) -> MethodType.methodType(void.class, clazz1, clazz2, Material.class);
    private static final BiFunction<Class<?>, Class<?>, MethodType> GENERIC_TRI_CONSUMER = (clazz1, clazz2) -> MethodType.methodType(void.class, TRI_CONSUMER.apply(clazz1, clazz2).generic().parameterList());

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
                validateParameterLength(clazz, method, 1);
                validateParameter(Material.class, clazz, method, method.getParameterTypes(), 0);
                try {
                    register(location, toConsumer(clazz, method));
                } catch (Throwable e) {
                    invokeError(clazz, method, e);
                }
                return;
            }
            case PROPERTY: {
                validateParameterLength(clazz, method, 2);
                validateParameter(Material.class, clazz, method, method.getParameterTypes(), 1);

                //noinspection rawtypes
                final Class<IMaterialProperty> propertyClass = validateParameter(IMaterialProperty.class, clazz, method, method.getParameterTypes(), 0);
                final PropertyKey<?> propertyKey = getPropertyKey(propertyClass);

                try {
                    //noinspection rawtypes
                    final BiConsumer<IMaterialProperty, Material> biConsumer = toBiConsumer(clazz, method, propertyClass);

                    register(location, material -> {
                        if (material.hasProperty(propertyKey)) {
                            biConsumer.accept(material.getProperty(propertyKey), material);
                        }
                    });
                } catch (Throwable e) {
                    invokeError(clazz, method, e);
                }
                return;
            }
            case ORE_PREFIX: {
                validateParameterLength(clazz, method, 2);
                validateParameter(Material.class, clazz, method, method.getParameterTypes(), 1);

                Class<OrePrefix> prefixClass = validateParameter(OrePrefix.class, clazz, method, method.getParameterTypes(), 0);

                String[] prefixNames = annotation.prefixes();
                Collection<OrePrefix> prefixes = getPrefixes(clazz, method, prefixNames);

                try {
                    final BiConsumer<OrePrefix, Material> biConsumer = toBiConsumer(clazz, method, prefixClass);

                    register(location, material -> {
                        for (OrePrefix prefix : prefixes) {
                            biConsumer.accept(prefix, material);
                        }
                    });
                } catch (Throwable e) {
                    invokeError(clazz, method, e);
                }
                return;
            }
            case PROPERTY_PREFIX: {
                validateParameterLength(clazz, method, 3);
                validateParameter(Material.class, clazz, method, method.getParameterTypes(), 2);

                Class<OrePrefix> prefixClass = validateParameter(OrePrefix.class, clazz, method, method.getParameterTypes(), 1);

                String[] prefixNames = annotation.prefixes();
                Collection<OrePrefix> prefixes = getPrefixes(clazz, method, prefixNames);

                //noinspection rawtypes
                final Class<IMaterialProperty> propertyClass = validateParameter(IMaterialProperty.class, clazz, method, method.getParameterTypes(), 0);
                final PropertyKey<?> propertyKey = getPropertyKey(propertyClass);

                try {
                    //noinspection rawtypes
                    final TriConsumer<IMaterialProperty, OrePrefix, Material> triConsumer = toTriConsumer(clazz, method, propertyClass, prefixClass);

                    register(location, material -> {
                        if (material.hasProperty(propertyKey)) {
                            for (OrePrefix prefix : prefixes) {
                                triConsumer.accept(material.getProperty(propertyKey), prefix, material);
                            }
                        }
                    });
                } catch (Throwable e) {
                    invokeError(clazz, method, e);
                }
            }
        }
    }

    /**
     * Converts a method to a Consumer
     *
     * @param clazz  the class containing the method
     * @param method the method to convert
     * @return a Consumer equivalent to the method
     * @throws Throwable if there was any error calling the method during conversion
     */
    private static Consumer<Material> toConsumer(@Nonnull Class<?> clazz, @Nonnull Method method) throws Throwable {
        MethodHandle handle = LOOKUP.findStatic(clazz, method.getName(), CONSUMER);
        CallSite site = LambdaMetafactory.metafactory(LOOKUP,
                "accept",
                CONSUMER_TYPE,
                GENERIC_CONSUMER,
                handle,
                CONSUMER);

        //noinspection unchecked
        return (Consumer<Material>) site.getTarget().invokeExact();
    }

    /**
     * Converts a method to a BiConsumer
     *
     * @param clazz  the class containing the method
     * @param method the method to convert
     * @return a BiConsumer equivalent to the method
     * @throws Throwable if there was any error calling the method during conversion
     */
    private static <T> BiConsumer<T, Material> toBiConsumer(@Nonnull Class<?> clazz, @Nonnull Method method, Class<T> parameter1Class) throws Throwable {
        MethodHandle handle = LOOKUP.findStatic(clazz, method.getName(), BI_CONSUMER.apply(parameter1Class));
        CallSite site = LambdaMetafactory.metafactory(LOOKUP,
                "accept",
                BI_CONSUMER_TYPE,
                GENERIC_BI_CONSUMER.apply(parameter1Class),
                handle,
                BI_CONSUMER.apply(parameter1Class));

        //noinspection unchecked
        return (BiConsumer<T, Material>) site.getTarget().invokeExact();
    }

    /**
     * Converts a method to a TriConsumer
     *
     * @param clazz  the class containing the method
     * @param method the method to convert
     * @return a BiConsumer equivalent to the method
     * @throws Throwable if there was any error calling the method during conversion
     */
    private static <T, U> TriConsumer<T, U, Material> toTriConsumer(@Nonnull Class<?> clazz, @Nonnull Method method,
                                                                    Class<T> parameter1Class, Class<U> parameter2Class) throws Throwable {
        MethodHandle handle = LOOKUP.findStatic(clazz, method.getName(), TRI_CONSUMER.apply(parameter1Class, parameter2Class));
        CallSite site = LambdaMetafactory.metafactory(LOOKUP,
                "accept",
                TRI_CONSUMER_TYPE,
                GENERIC_TRI_CONSUMER.apply(parameter1Class, parameter2Class),
                handle,
                TRI_CONSUMER.apply(parameter1Class, parameter2Class));

        //noinspection unchecked
        return (TriConsumer<T, U, Material>) site.getTarget().invokeExact();
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
    private static <T> Class<T> validateParameter(@Nonnull Class<T> validation, @Nonnull Class<?> clazz, @Nonnull Method method, @Nonnull Class<?>[] classes, int index) {
        if (index >= classes.length || !validation.isAssignableFrom(classes[index])) {
            throw new IllegalArgumentException("Method " + method.getName() + " of class " + clazz.getName() +
                    " must take an " + validation.getName() + " for parameter " + index);
        }
        //noinspection unchecked
        return (Class<T>) classes[index];
    }

    /**
     * @param clazz       the class of the method to invoke, for logging
     * @param method      the method to invoke, for logging
     * @param prefixNames the ore prefix names to turn into OrePrefixes
     * @return the ore prefixes corresponding to the names
     * @throws IllegalArgumentException if no prefixes are found
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
     * @return the material's property
     * @throws IllegalArgumentException if no PropertyKey is found
     */
    @Nonnull
    private static PropertyKey<?> getPropertyKey(@Nonnull Class<?> propertyClass) {
        PropertyKey<?> key = PropertyKey.getPropertyKey(propertyClass);
        if (key == null) {
            throw new IllegalArgumentException("No PropertyKey exists for type " + propertyClass.getName());
        }
        return key;
    }

    /**
     * Throw an error for when a Processing Handler requires OrePrefixes but has none.
     *
     * @param clazz  the class containing the method
     * @param method the method called
     * @throws IllegalArgumentException if no ore prefixes are found
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
     * @throws IllegalStateException if a ProcessingHandler had errors while running
     */
    private static void invokeError(@Nonnull Class<?> clazz, @Nonnull Method method, @Nonnull Throwable e) {
        throw new IllegalStateException("Error invoking Processing Handler " + method.getName() + " of class " + clazz.getName(), e);
    }

    /**
     * Register an Processing Handler
     *
     * @param location the unique name of the ProcessingHandler, in format {@code "modid:class_name.method_name}
     * @param handler  the handler to register
     * @throws IllegalArgumentException if the processing handler is already registered
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
     * BlackList a Processing Handler from running for a material
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
     *
     * @throws IllegalStateException If a mod other than GregTech calls this
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
