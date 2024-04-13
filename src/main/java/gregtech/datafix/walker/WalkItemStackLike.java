package gregtech.datafix.walker;

import gregtech.datafix.GTFixType;
import gregtech.datafix.util.DataFixHelper;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

public final class WalkItemStackLike implements IDataWalker {

    @Override
    public @NotNull NBTTagCompound process(@NotNull IDataFixer fixer, @NotNull NBTTagCompound compound, int versionIn) {
        DataFixHelper.rewriteCompoundTags(compound, tag -> {
            if (tag.hasKey("id", Constants.NBT.TAG_STRING) && tag.hasKey("Count", Constants.NBT.TAG_BYTE) &&
                    tag.hasKey("Damage", Constants.NBT.TAG_SHORT)) {
                return fixer.process(GTFixType.ITEM_STACK_LIKE, tag, versionIn);
            }
            return null;
        });
        return compound;
    }
}
