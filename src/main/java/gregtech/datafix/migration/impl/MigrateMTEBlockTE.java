package gregtech.datafix.migration.impl;

import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.registry.MTERegistry;
import gregtech.datafix.migration.api.MTEMigrator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.GameData;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static gregtech.datafix.util.DataFixConstants.*;

public class MigrateMTEBlockTE implements IFixableData {

    private static final String META_ID = "MetaId";
    private static final String META_TILE_ENTITY = "MetaTileEntity";

    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private static final String X_POS = "xPos";
    private static final String Z_POS = "zPos";
    private static final String CHUNK_SECTION_Y = "Y";
    private static final String CHUNK_SECTION_BLOCKS = "Blocks";
    private static final String CHUNK_SECTION_DATA = "Data";
    private static final String CHUNK_SECTION_ADD = "Add";

    private static final int BLOCKS_PER_SECTION = 4096;

    private final MTEMigrator migrator;

    public MigrateMTEBlockTE(@NotNull MTEMigrator migrator) {
        this.migrator = migrator;
    }

    @Override
    public int getFixVersion() {
        return migrator.fixVersion();
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(@NotNull NBTTagCompound compound) {
        if (!compound.hasKey(LEVEL_TAG, Constants.NBT.TAG_COMPOUND)) {
            return compound;
        }

        NBTTagCompound level = compound.getCompoundTag(LEVEL_TAG);
        processChunkSections(level, gatherMTEs(level));
        return compound;
    }

    /**
     * @param level the level tag
     * @return the MTEs in the level
     */
    private @NotNull Map<BlockPos, ResourceLocation> gatherMTEs(@NotNull NBTTagCompound level) {
        Map<BlockPos, ResourceLocation> mteIds = new Object2ObjectOpenHashMap<>();
        NBTTagList tileEntityTagList = level.getTagList(TILE_ENTITIES_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tileEntityTagList.tagCount(); i++) {
            NBTTagCompound tileEntityTag = tileEntityTagList.getCompoundTagAt(i);
            if (tileEntityTag.hasKey(META_ID, Constants.NBT.TAG_STRING) &&
                    tileEntityTag.hasKey(META_TILE_ENTITY, Constants.NBT.TAG_COMPOUND)) {
                BlockPos pos = new BlockPos(tileEntityTag.getInteger(X), tileEntityTag.getInteger(Y),
                        tileEntityTag.getInteger(Z));
                ResourceLocation mteId = new ResourceLocation(tileEntityTag.getString(META_ID));
                migrator.fixMTEData(mteId, tileEntityTag.getCompoundTag(META_TILE_ENTITY));

                ResourceLocation fixedId = migrator.fixMTEid(mteId);
                if (fixedId != null) {
                    tileEntityTag.setString(META_ID, fixedId.toString());
                    mteId = fixedId;
                }
                mteIds.put(pos, mteId);
            }
        }
        return mteIds;
    }

    /**
     * Processes the chunk sections to remap blocks.
     *
     * @param level  the level tag
     * @param mteIds the MTEs present in the chunk section
     */
    private static void processChunkSections(@NotNull NBTTagCompound level,
                                             @NotNull Map<BlockPos, ResourceLocation> mteIds) {
        if (mteIds.isEmpty()) {
            return;
        }

        var blockStateIDMap = GameData.getBlockStateIDMap();
        ChunkPos chunkPos = new ChunkPos(level.getInteger(X_POS), level.getInteger(Z_POS));
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
                ResourceLocation mteID = mteIds.get(pos);
                if (mteID == null) {
                    continue;
                }

                MTERegistry registry = GregTechAPI.mteManager.getRegistry(mteID.getNamespace());
                MetaTileEntity mte = registry.getObject(mteID);
                if (mte == null) {
                    continue;
                }

                int newStateID = blockStateIDMap.get(mte.getBlock().getDefaultState());
                byte newBlockID = (byte) (newStateID >> 4 & 0xFF);
                byte newBlockIDExt = (byte) (newStateID >> 12 & 0x0F);
                byte newBlockData = (byte) (newStateID & 0x0F);

                blockIDs[j] = newBlockID;
                if (newBlockIDExt != 0) {
                    if (extendedIDs == null) {
                        extendedIDs = new NibbleArray();
                    }
                    extendedIDs.set(x, y, z, newBlockIDExt);
                }
                blockData.set(x, y, z, newBlockData);
            }

            chunkSectionTag.setByteArray(CHUNK_SECTION_BLOCKS, blockIDs);
            chunkSectionTag.setByteArray(CHUNK_SECTION_DATA, blockData.getData());
            if (extendedIDs != null) {
                chunkSectionTag.setByteArray(CHUNK_SECTION_ADD, extendedIDs.getData());
            }
        }
    }
}
