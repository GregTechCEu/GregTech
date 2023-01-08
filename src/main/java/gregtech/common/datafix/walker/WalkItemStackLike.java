package gregtech.common.datafix.walker;

import gregtech.common.datafix.GregTechFixType;
import gregtech.common.datafix.util.DataFixHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class WalkItemStackLike implements IDataWalker {

    @Nonnull
    @Override
    public NBTTagCompound process(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound compound, int versionIn) {
        DataFixHelper.rewriteCompoundTags(compound, tag -> {
            if (tag.hasKey("id", Constants.NBT.TAG_STRING) && tag.hasKey("Count", Constants.NBT.TAG_BYTE)
                    && tag.hasKey("Damage", Constants.NBT.TAG_SHORT)) {
                return fixer.process(GregTechFixType.ITEM_STACK_LIKE, tag, versionIn);
            }
            return null;
        });
        return compound;
    }
}
