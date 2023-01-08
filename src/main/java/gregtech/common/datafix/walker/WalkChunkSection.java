package gregtech.common.datafix.walker;

import gregtech.common.datafix.GregTechFixType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class WalkChunkSection implements IDataWalker {

    @Nonnull
    @Override
    public NBTTagCompound process(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound compound, int versionIn) {
        if (compound.hasKey("Level", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound levelTag = compound.getCompoundTag("Level");
            if (levelTag.hasKey("Sections", Constants.NBT.TAG_LIST) && levelTag.hasKey("TileEntities", Constants.NBT.TAG_LIST)) {
                fixer.process(GregTechFixType.CHUNK_SECTION, levelTag, versionIn);
            }
        }
        return compound;
    }
}
