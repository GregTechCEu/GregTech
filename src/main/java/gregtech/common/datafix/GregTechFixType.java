package gregtech.common.datafix;

import net.minecraft.util.datafix.IFixType;

public enum GregTechFixType implements IFixType {

    /**
     * Any compound tag that looks like an item stack.
     * That is to say, it has the fields {@code string id}, {@code byte Count}, and {@code short Damage}.
     *
     * @see gregtech.common.datafix.walker.WalkItemStackLike
     */
    ITEM_STACK_LIKE,

    /**
     * A vertical section of a chunk containing block state data.
     *
     * @see gregtech.common.datafix.walker.WalkChunkSection
     */
    CHUNK_SECTION
}
