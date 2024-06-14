package gregtech.datafix;

import net.minecraft.util.datafix.IFixType;

public enum GTFixType implements IFixType {
    /**
     * Any NBTTagCompound that looks like an ItemStack.
     * <p>
     * It must have the fields: {@code String id}, {@code byte count}, {@code short Damage}.
     *
     * @see gregtech.datafix.walker.WalkItemStackLike
     */
    ITEM_STACK_LIKE,
    /**
     * A vertical section of a chunk containing BlockState data.
     *
     * @see gregtech.datafix.walker.WalkChunkSection
     */
    CHUNK_SECTION,
    /**
     * The tile entities contained in a chunk.
     *
     * @see gregtech.datafix.walker.WalkTileEntities
     */
    TILE_ENTITIES,
}
