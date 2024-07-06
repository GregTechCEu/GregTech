package gregtech.datafix.migration.impl;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.IFixableData;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;

import org.jetbrains.annotations.NotNull;

import static gregtech.datafix.util.DataFixConstants.*;

public class MigrateMaterialBlock implements IFixableData {

    @Override
    public int getFixVersion() {
        return 2;
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(@NotNull NBTTagCompound compound) {
        var blockStateIDMap = GameData.getBlockStateIDMap();
        var r = ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS);
        NBTTagCompound level = compound.getCompoundTag(LEVEL_TAG);
        ChunkPos chunkPos = new ChunkPos(level.getInteger(LEVEL_CHUNK_X_POS), level.getInteger(LEVEL_CHUNK_Z_POS));
        NBTTagList sectionTagList = level.getTagList(SECTIONS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < sectionTagList.tagCount(); i++) {
            NBTTagCompound chunkSectionTag = sectionTagList.getCompoundTagAt(i);

            int sectionY = chunkSectionTag.getByte(CHUNK_SECTION_Y);
            byte[] blockIDs = chunkSectionTag.getByteArray(CHUNK_SECTION_BLOCKS);
            NibbleArray blockData = new NibbleArray(chunkSectionTag.getByteArray(CHUNK_SECTION_DATA));
            NibbleArray extendedIDs = chunkSectionTag.hasKey(CHUNK_SECTION_ADD, Constants.NBT.TAG_BYTE_ARRAY) ?
                    new NibbleArray(chunkSectionTag.getByteArray(CHUNK_SECTION_ADD)) : null;
            for (int j = 0; j < BLOCKS_PER_SECTION; j++) {
                int x = j & 0x0F;
                int y = j >> 8 & 0x0F;
                int z = j >> 4 & 0x0F;

                BlockPos pos = chunkPos.getBlock(x, sectionY << 4 | y, z);

            }
        }

        return compound;
    }
}
