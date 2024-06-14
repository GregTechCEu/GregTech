package gregtech.datafix.walker;

import gregtech.datafix.GTFixType;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

import static gregtech.datafix.util.DataFixConstants.LEVEL_TAG;
import static gregtech.datafix.util.DataFixConstants.TILE_ENTITIES_TAG;

public final class WalkTileEntities implements IDataWalker {

    @Override
    public @NotNull NBTTagCompound process(@NotNull IDataFixer fixer, @NotNull NBTTagCompound compound, int version) {
        if (compound.hasKey(LEVEL_TAG, Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound levelTag = compound.getCompoundTag(LEVEL_TAG);
            if (levelTag.hasKey(TILE_ENTITIES_TAG, Constants.NBT.TAG_LIST)) {
                NBTTagList list = levelTag.getTagList(TILE_ENTITIES_TAG, Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < list.tagCount(); i++) {
                    list.set(i, fixer.process(GTFixType.TILE_ENTITIES, list.getCompoundTagAt(i), version));
                }
            }
        }
        return compound;
    }
}
