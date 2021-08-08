package gregtech.api.util;

import gregtech.api.util.world.DummyWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class ModCompatibility {

    private static RefinedStorage refinedStorage;
    private static AppliedEnergistics2 appliedEnergistics2;

    public static void initCompat() {
        try {
            Class<?> itemClass = Class.forName("com.raoulvdberge.refinedstorage.item.ItemPattern");
            refinedStorage = new RefinedStorage(itemClass);
            GTLog.logger.info("RefinedStorage found; enabling integration.");
        } catch (ClassNotFoundException ignored) {
            GTLog.logger.info("RefinedStorage not found; skipping integration.");
        } catch (Throwable exception) {
            GTLog.logger.error("Failed to enable RefinedStorage integration", exception);
        }
        try {
            Class<?> itemClass = Class.forName("appeng.items.misc.ItemEncodedPattern");
            appliedEnergistics2 = new AppliedEnergistics2(itemClass);
            GTLog.logger.info("AppliedEnergistics2 found; enabling integration.");
        } catch (ClassNotFoundException ignored) {
            GTLog.logger.info("AppliedEnergistics2 not found; skipping integration.");
        } catch (Throwable exception) {
            GTLog.logger.error("Failed to enable AppliedEnergistics2 integration", exception);
        }
    }

    public static ItemStack getRealItemStack(ItemStack itemStack) {
        if (refinedStorage != null && refinedStorage.canHandleItemStack(itemStack)) {
            return refinedStorage.getRealItemStack(itemStack);
        }
        if (appliedEnergistics2 != null && appliedEnergistics2.canHandleItemStack(itemStack)) {
            return appliedEnergistics2.getRealItemStack(itemStack);
        }
        return itemStack;
    }

    private static class RefinedStorage {
        private final Method getPatternFromCacheMethod;
        private final Method getOutputsMethod;

        public RefinedStorage(Class<?> itemPatternClass) throws ReflectiveOperationException {
            this.getPatternFromCacheMethod = itemPatternClass.getMethod("getPatternFromCache", World.class, ItemStack.class);
            this.getOutputsMethod = getPatternFromCacheMethod.getReturnType().getMethod("getOutputs");
        }

        public boolean canHandleItemStack(ItemStack itemStack) {
            ResourceLocation registryName = Objects.requireNonNull(itemStack.getItem().getRegistryName());
            return registryName.getNamespace().equals("refinedstorage") &&
                    registryName.getPath().equals("pattern");
        }

        public ItemStack getRealItemStack(ItemStack itemStack) {
            try {
                Object craftingPattern = getPatternFromCacheMethod.invoke(null, DummyWorld.INSTANCE, itemStack);
                List<ItemStack> outputs = (List<ItemStack>) getOutputsMethod.invoke(craftingPattern);
                return outputs.isEmpty() ? itemStack : outputs.get(0);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to obtain item from ItemPattern", ex);
            }
        }
    }

    private static class AppliedEnergistics2 {
        private final Method getOutputMethod;

        public AppliedEnergistics2(Class<?> itemEncodedPatternClass) throws ReflectiveOperationException {
            this.getOutputMethod = itemEncodedPatternClass.getMethod("getOutput", ItemStack.class);
        }

        public boolean canHandleItemStack(ItemStack itemStack) {
            ResourceLocation registryName = Objects.requireNonNull(itemStack.getItem().getRegistryName());
            return registryName.getNamespace().equals("appliedenergistics2") &&
                    registryName.getPath().equals("encoded_pattern");
        }

        public ItemStack getRealItemStack(ItemStack itemStack) {
            try {
                return (ItemStack) getOutputMethod.invoke(itemStack.getItem(), itemStack);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to obtain item from ItemEncodedPattern", ex);
            }
        }
    }
}
