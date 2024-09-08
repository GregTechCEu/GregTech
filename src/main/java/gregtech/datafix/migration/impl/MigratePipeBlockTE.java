package gregtech.datafix.migration.impl;

import gregtech.api.util.GTLog;
import gregtech.common.pipelike.block.pipe.MaterialPipeStructure;
import gregtech.datafix.GTDataFixers;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static gregtech.datafix.util.DataFixConstants.*;

public class MigratePipeBlockTE implements IFixableData {

    private static final String PIPE_BLOCK_TAG = "PipeBlock";
    private static final String PIPE_TYPE_TAG = "PipeType";

    private final int version;

    public MigratePipeBlockTE(int version) {
        this.version = version;
    }

    @Override
    public int getFixVersion() {
        return version;
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(@NotNull NBTTagCompound compound) {
        if (!compound.hasKey(LEVEL_TAG, Constants.NBT.TAG_COMPOUND)) {
            return compound;
        }

        NBTTagCompound level = compound.getCompoundTag(LEVEL_TAG);
        processChunkSections(level, gatherTEs(level));
        return compound;
    }

    /**
     * @param level the level tag
     * @return the TEs in the level
     */
    private static @NotNull Map<BlockPos, ResourceLocation> gatherTEs(@NotNull NBTTagCompound level) {
        Map<BlockPos, ResourceLocation> tileEntityIds = new Object2ObjectOpenHashMap<>();
        NBTTagList tileEntityTagList = level.getTagList(TILE_ENTITIES_TAG, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tileEntityTagList.tagCount(); i++) {
            NBTTagCompound tileEntityTag = tileEntityTagList.getCompoundTagAt(i);
            if (tileEntityTag.hasKey(PIPE_BLOCK_TAG, Constants.NBT.TAG_STRING) &&
                    tileEntityTag.hasKey(PIPE_TYPE_TAG, Constants.NBT.TAG_INT)) {
                BlockPos pos = new BlockPos(tileEntityTag.getInteger(X), tileEntityTag.getInteger(Y),
                        tileEntityTag.getInteger(Z));
                ResourceLocation blockId = fixTileEntityTag(tileEntityTag);
                tileEntityIds.put(pos, blockId);
            }
        }
        return tileEntityIds;
    }

    private static @NotNull ResourceLocation fixTileEntityTag(@NotNull NBTTagCompound tag) {
        ResourceLocation fixedTileEntityId = fixTileEntityId(new ResourceLocation(tag.getString(TILE_ENTITY_ID)));
        if (fixedTileEntityId != null) {
            tag.setString(TILE_ENTITY_ID, fixedTileEntityId.toString());
        }

        ResourceLocation blockRegistryName = new ResourceLocation(tag.getString(PIPE_BLOCK_TAG));
        ResourceLocation fixedBlockRegistryName = fixBlockRegistryName(blockRegistryName);
        if (fixedBlockRegistryName == null) {
            GTDataFixers.LOGGER.warn("Cannot find pipe structure for PipeType: {}", blockRegistryName);
            return blockRegistryName;
        } else {
            blockRegistryName = fixedBlockRegistryName;
        }
        tag.setByte("ConnectionMask", (byte) tag.getInteger("Connections"));
        tag.setByte("BlockedMask", (byte) tag.getInteger("BlockedConnections"));
        if (tag.hasKey("InsulationColor", Constants.NBT.TAG_INT)) {
            tag.setInteger("Paint", tag.getInteger("InsulationColor"));
        } else {
            tag.setInteger("Paint", -1);
        }
        GTLog.logger.fatal("PAINING COLOR: {}", tag.getInteger("Paint"));
        tag.setString("Frame", tag.getString("FrameMaterial"));
        tag.setBoolean("Legacy", true);

        if (tag.hasKey("PipeMaterial")) {
            tag.setString("Material", tag.getString("PipeMaterial"));
        }

        // the "Fluids" key is discarded for fluid pipes

        return blockRegistryName;
    }

    private static @Nullable ResourceLocation fixTileEntityId(@NotNull ResourceLocation id) {
        String value = switch (id.getPath()) {
            case "cable", "fluid_pipe", "fluid_pipe_active", "item_pipe" -> "material_pipe";
            case "optical_pipe", "laser_pipe" -> "activatable_pipe";
            default -> null;
        };

        if (value == null) {
            GTDataFixers.LOGGER.warn("Cannot find pipe tile class for id: {}", id);
            return null;
        }

        return new ResourceLocation(id.getNamespace(), value);
    }

    public static @Nullable ResourceLocation fixBlockRegistryName(@NotNull ResourceLocation name) {
        String value = name.getPath();
        if (value.startsWith("wire_") || value.startsWith("cable_") || value.startsWith("laser_pipe_") ||
                value.startsWith("optical_pipe_")) {
            // unchanged values
            return name;
        }

        value = switch (value) {
            case "fluid_pipe_tiny" -> MaterialPipeStructure.TINY.getName();
            case "fluid_pipe_small", "item_pipe_small" -> MaterialPipeStructure.SMALL.getName();
            case "fluid_pipe_normal", "item_pipe_normal" -> MaterialPipeStructure.NORMAL.getName();
            case "fluid_pipe_large", "item_pipe_large" -> MaterialPipeStructure.LARGE.getName();
            case "fluid_pipe_huge", "item_pipe_huge" -> MaterialPipeStructure.HUGE.getName();
            case "fluid_pipe_quadruple" -> MaterialPipeStructure.QUADRUPLE.getName();
            case "fluid_pipe_nonuple" -> MaterialPipeStructure.NONUPLE.getName();
            case "item_pipe_small_restrictive" -> MaterialPipeStructure.SMALL_RESTRICTIVE.getName();
            case "item_pipe_normal_restrictive" -> MaterialPipeStructure.NORMAL_RESTRICTIVE.getName();
            case "item_pipe_large_restrictive" -> MaterialPipeStructure.LARGE_RESTRICTIVE.getName();
            case "item_pipe_huge_restrictive" -> MaterialPipeStructure.HUGE_RESTRICTIVE.getName();
            default -> null;
        };

        if (value == null) {
            return null;
        }

        return new ResourceLocation(name.getNamespace(), value);
    }

    /**
     * Processes the chunk sections to remap blocks.
     *
     * @param level    the level tag
     * @param blockIds the Blocks present in the chunk section
     */
    private static void processChunkSections(@NotNull NBTTagCompound level,
                                             @NotNull Map<BlockPos, ResourceLocation> blockIds) {
        if (blockIds.isEmpty()) {
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
                ResourceLocation blockId = blockIds.get(pos);
                if (blockId == null) {
                    continue;
                }

                Block block = Block.REGISTRY.getObject(blockId);
                if (block == Blocks.AIR) {
                    continue;
                }

                int newStateID = blockStateIDMap.get(block.getDefaultState());
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
