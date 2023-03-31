package gregtech.api.recipes;

import gregtech.api.util.ItemStackKey;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * @deprecated Use {@link gregtech.api.util.ItemStackHashStrategy} with Custom FastUtil maps.
 */
@Deprecated
public final class KeySharedStack {

    private static final WeakHashMap<ItemStackKey, WeakReference<ItemStackKey>> registeredItemStackKeys = new WeakHashMap<>();
    public static ItemStackKey EMPTY = new ItemStackKey(ItemStack.EMPTY);

    private KeySharedStack() {

    }

    /**
     * @deprecated Use {@link ItemStack#copy()} instead
     */
    public static synchronized ItemStackKey getRegisteredStack(@Nonnull final ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return EMPTY;
        }

        int oldStackSize = itemStack.getCount();
        itemStack.setCount(1);

        ItemStackKey search = new ItemStackKey(itemStack, false);
        WeakReference<ItemStackKey> weak = registeredItemStackKeys.get(search);
        ItemStackKey ret = null;

        if (weak != null) {
            ret = weak.get();
        }

        if (ret == null) {
            ret = new ItemStackKey(itemStack);
            registeredItemStackKeys.put(ret, new WeakReference<>(ret));
        }
        itemStack.setCount(oldStackSize);

        return ret;
    }
}
