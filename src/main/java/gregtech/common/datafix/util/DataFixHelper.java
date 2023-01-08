package gregtech.common.datafix.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public final class DataFixHelper {

    private DataFixHelper() {/**/}

    public static void rewriteCompoundTags(NBTTagCompound tag, CompoundRewriter rewriter) {
        for (String key : tag.getKeySet()) {
            NBTBase childTag = tag.getTag(key);
            switch (childTag.getId()) {
                case Constants.NBT.TAG_LIST:
                    rewriteCompoundTags((NBTTagList) childTag, rewriter);
                    break;
                case Constants.NBT.TAG_COMPOUND:
                    NBTTagCompound childTagCompound = (NBTTagCompound) childTag;
                    rewriteCompoundTags(childTagCompound, rewriter);
                    childTagCompound = rewriter.rewrite(childTagCompound);
                    if (childTagCompound != null) {
                        tag.setTag(key, childTagCompound);
                    }
                    break;
            }
        }
    }

    public static void rewriteCompoundTags(NBTTagList tag, CompoundRewriter rewriter) {
        for (int i = 0; i < tag.tagCount(); i++) {
            NBTBase childTag = tag.get(i);
            switch (childTag.getId()) {
                case Constants.NBT.TAG_LIST:
                    rewriteCompoundTags((NBTTagList) childTag, rewriter);
                    break;
                case Constants.NBT.TAG_COMPOUND:
                    NBTTagCompound childTagCompound = (NBTTagCompound) childTag;
                    rewriteCompoundTags(childTagCompound, rewriter);
                    childTagCompound = rewriter.rewrite(childTagCompound);
                    if (childTagCompound != null) {
                        tag.set(i, childTagCompound);
                    }
                    break;
            }
        }
    }

    @FunctionalInterface
    public interface CompoundRewriter {
        @Nullable NBTTagCompound rewrite(NBTTagCompound tag);
    }
}
