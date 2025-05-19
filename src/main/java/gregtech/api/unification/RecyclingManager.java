package gregtech.api.unification;

import gregtech.api.unification.stack.ItemAndMetadata;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.util.GTUtility;

import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public final class RecyclingManager {

    private final Map<ItemAndMetadata, ItemMaterialInfo> recyclingData = new Object2ObjectOpenHashMap<>();

    /**
     * @param stack        the stack to give recycling data
     * @param materialInfo the recycling data
     */
    public void registerRecyclingData(@NotNull ItemStack stack, @NotNull ItemMaterialInfo materialInfo) {
        if (stack.isEmpty()) {
            return;
        }
        registerRecyclingData(new ItemAndMetadata(stack), materialInfo);
    }

    /**
     * @param key          the key to give recycling data
     * @param materialInfo the recycling data
     */
    public void registerRecyclingData(@NotNull ItemAndMetadata key,
                                      @NotNull ItemMaterialInfo materialInfo) {
        recyclingData.put(key, materialInfo);
    }

    /**
     * @param stack the stack
     * @return the recycling data associated with the stack
     */
    public @Nullable ItemMaterialInfo getRecyclingData(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return getRecyclingData(new ItemAndMetadata(stack));
    }

    /**
     * @param key the key
     * @return the recycling data associated with the key
     */
    public @Nullable ItemMaterialInfo getRecyclingData(@NotNull ItemAndMetadata key) {
        return GTUtility.getOrWildcardMeta(recyclingData, key);
    }

    /**
     * @param stack the stack whose data should be removed
     */
    public void removeRecyclingData(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        removeRecyclingData(new ItemAndMetadata(stack));
    }

    /**
     * @param key the key whose data should be removed
     */
    public void removeRecyclingData(@NotNull ItemAndMetadata key) {
        recyclingData.remove(key);
    }

    /**
     * Iterate all registered recycling entries
     *
     * @param action the action to apply to each entry
     */
    public void iterate(@NotNull BiConsumer<ItemAndMetadata, ItemMaterialInfo> action) {
        recyclingData.forEach(action);
    }
}
