package gregtech.datafix;

import net.minecraft.util.datafix.IFixType;

public enum GTFixType implements IFixType {
    /**
     * Any NBTTagCompound that looks like an ItemStack.
     * <p>
     * It must have the fields: {@code String id}, {@code int count}, {@code short Damage}.
     *
     * @see gregtech.datafix.walker.WalkItemStackLike
     */
    ITEM_STACK_LIKE,
}
