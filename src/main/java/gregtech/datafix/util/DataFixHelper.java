package gregtech.datafix.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public final class DataFixHelper {

    private DataFixHelper() {}

    /**
     * Recursively rewrites NBTTagCompounds
     *
     * @param tag      the tag to rewrite
     * @param rewriter the tag rewriter
     */
    public static void rewriteCompoundTags(@NotNull NBTTagCompound tag,
                                           @NotNull UnaryOperator<NBTTagCompound> rewriter) {
        for (String key : tag.getKeySet()) {
            NBTBase child = tag.getTag(key);

            final byte id = child.getId();
            if (id == Constants.NBT.TAG_LIST) {
                rewriteCompoundTags((NBTTagList) child, rewriter);
            } else if (id == Constants.NBT.TAG_COMPOUND) {
                NBTTagCompound childCompound = (NBTTagCompound) child;
                rewriteCompoundTags(childCompound, rewriter);
                childCompound = rewriter.apply(childCompound);
                if (childCompound != null) {
                    tag.setTag(key, childCompound);
                }
            }
        }
    }

    /**
     * @param tagList  recursively rewrites NBTTagCompounds in an NBTTagList
     * @param rewriter the tag rewriter
     */
    public static void rewriteCompoundTags(@NotNull NBTTagList tagList,
                                           @NotNull UnaryOperator<NBTTagCompound> rewriter) {
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTBase child = tagList.get(i);

            final byte id = child.getId();
            if (id == Constants.NBT.TAG_LIST) {
                rewriteCompoundTags((NBTTagList) child, rewriter);
            } else if (id == Constants.NBT.TAG_COMPOUND) {
                NBTTagCompound childCompound = (NBTTagCompound) child;
                rewriteCompoundTags(childCompound, rewriter);
                childCompound = rewriter.apply(childCompound);
                if (childCompound != null) {
                    tagList.set(i, childCompound);
                }
            }
        }
    }
}
