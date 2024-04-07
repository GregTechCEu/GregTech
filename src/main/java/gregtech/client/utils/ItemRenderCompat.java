package gregtech.client.utils;

import gregtech.api.util.GTLog;
import gregtech.api.util.Mods;
import gregtech.api.util.world.DummyWorld;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.items.misc.ItemEncodedPattern;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;

public final class ItemRenderCompat {

    private static @Nullable ItemRenderCompat.RepresentativeStackExtractor rsHandler;
    private static @Nullable ItemRenderCompat.RepresentativeStackExtractor ae2Handler;

    private ItemRenderCompat() {}

    @ApiStatus.Internal
    public static void init() {
        ae2Handler = AE2StackExtractor.create();
        rsHandler = RSStackExtractor.create();
    }

    /**
     * Attempts to retrieve the actual ItemStack another stack represents.
     * <p>
     * Primarily used to retrieve the output stack from AE2 or RS Patterns.
     *
     * @param stack the stack to retrieve from
     * @return the actual represented ItemStack
     */
    public static @NotNull ItemStack getRepresentedStack(@NotNull ItemStack stack) {
        if (ae2Handler != null && ae2Handler.canHandleStack(stack)) {
            return ae2Handler.getActualStack(stack);
        }
        if (rsHandler != null && rsHandler.canHandleStack(stack)) {
            return rsHandler.getActualStack(stack);
        }
        return stack;
    }

    /**
     * An extractor to retrieve a represented stack from an ItemStack
     */
    public interface RepresentativeStackExtractor {

        /**
         * @param stack the stack to test
         * @return if the extractor can handle the stack
         */
        boolean canHandleStack(@NotNull ItemStack stack);

        /**
         * @param stack the stack to retrieve from
         * @return the represented stack
         */
        @NotNull
        ItemStack getActualStack(@NotNull ItemStack stack);
    }

    /**
     * Extracts the output stack from AE2 Patterns
     */
    private static final class AE2StackExtractor implements RepresentativeStackExtractor {

        public static @Nullable ItemRenderCompat.AE2StackExtractor create() {
            if (!Mods.AppliedEnergistics2.isModLoaded()) return null;
            GTLog.logger.info("AppliedEnergistics2 found; enabling render integration.");
            return new AE2StackExtractor();
        }

        @Override
        public boolean canHandleStack(@NotNull ItemStack stack) {
            return stack.getItem() instanceof ItemEncodedPattern;
        }

        @Override
        public @NotNull ItemStack getActualStack(@NotNull ItemStack stack) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            if (stack.getItem() instanceof ItemEncodedPattern encodedPattern) {
                return encodedPattern.getOutput(stack);
            }
            return stack;
        }
    }

    /**
     * Extracts the output stack from RS Patterns
     */
    @SuppressWarnings("ClassCanBeRecord")
    private static final class RSStackExtractor implements RepresentativeStackExtractor {

        private final MethodHandle getPatternFromCacheHandle;
        private final MethodHandle getOutputsHandle;
        private final Class<?> itemPatternClass;

        private RSStackExtractor(MethodHandle getPatternFromCacheHandle, MethodHandle getOutputsHandle,
                                 Class<?> itemPatternClass) {
            this.getPatternFromCacheHandle = getPatternFromCacheHandle;
            this.getOutputsHandle = getOutputsHandle;
            this.itemPatternClass = itemPatternClass;
        }

        public static @Nullable ItemRenderCompat.RSStackExtractor create() {
            if (!Mods.RefinedStorage.isModLoaded()) return null;

            Class<?> clazz;
            try {
                clazz = Class.forName("com.raoulvdberge.refinedstorage.item.ItemPattern");
                GTLog.logger.info("RefinedStorage found; enabling render integration.");
            } catch (ClassNotFoundException ignored) {
                GTLog.logger.error("RefinedStorage classes not found; skipping render integration.");
                return null;
            }

            try {
                Method method = clazz.getMethod("getPatternFromCache", World.class, ItemStack.class);

                MethodHandles.Lookup lookup = MethodHandles.publicLookup();

                MethodHandle getPatternFromCacheHandle = lookup.unreflect(method);

                method = method.getReturnType().getMethod("getOutputs");
                MethodHandle getOutputsHandle = lookup.unreflect(method);

                return new RSStackExtractor(getPatternFromCacheHandle, getOutputsHandle, clazz);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                GTLog.logger.error("Failed to enable RefinedStorage integration", e);
                return null;
            }
        }

        @Override
        public boolean canHandleStack(@NotNull ItemStack stack) {
            return itemPatternClass.isAssignableFrom(stack.getItem().getClass());
        }

        @SuppressWarnings("unchecked")
        @Override
        public @NotNull ItemStack getActualStack(@NotNull ItemStack stack) {
            if (stack.isEmpty()) return ItemStack.EMPTY;

            List<ItemStack> outputs;
            try {
                // ItemPattern.getPatternFromCache: (World, ItemStack) -> CraftingPattern
                Object craftingPattern = getPatternFromCacheHandle.invoke(DummyWorld.INSTANCE, stack);
                // CraftingPattern#getOutputs: () -> List<ItemStack>
                outputs = (List<ItemStack>) getOutputsHandle.invoke(craftingPattern);
            } catch (Throwable e) {
                GTLog.logger.error("Failed to obtain item from ItemPattern", e);
                return stack;
            }

            if (outputs.isEmpty()) {
                return stack;
            }
            return outputs.get(0);
        }
    }
}
