package gregtech.common.datafix.fixes;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.common.datafix.GregTechDataFixers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class Fix0PostMachineBlockChangeInWorld implements IFixableData {

    private static final ResourceLocation OLD_NAME = new ResourceLocation(GTValues.MODID, "machine");

    @Override
    public int getFixVersion() {
        return GregTechDataFixers.V0_POST_MACHINE_BLOCK_CHANGE;
    }

    @Nonnull
    @Override
    public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound compound) {
        int oldID = ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getID(OLD_NAME);
        if (oldID < 0) {
            return compound;
        }

        ObjectIntIdentityMap<IBlockState> blockStateIDMap = GameData.getBlockStateIDMap();
        Map<BlockPos, String> metaTileEntityIds = new HashMap<>();
        NBTTagList tileEntityTagList = compound.getTagList("TileEntities", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tileEntityTagList.tagCount(); i++) {
            NBTTagCompound tileEntityTag = tileEntityTagList.getCompoundTagAt(i);
            if (tileEntityTag.hasKey("MetaId", Constants.NBT.TAG_STRING)) {
                BlockPos pos = new BlockPos(tileEntityTag.getInteger("x"), tileEntityTag.getInteger("y"), tileEntityTag.getInteger("z"));
                metaTileEntityIds.put(pos, tileEntityTag.getString("MetaId"));
            }
        }

        ChunkPos chunkPos = new ChunkPos(compound.getInteger("xPos"), compound.getInteger("zPos"));
        NBTTagList sectionTagList = compound.getTagList("Sections", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < sectionTagList.tagCount(); i++) {
            NBTTagCompound chunkSectionTag = sectionTagList.getCompoundTagAt(i);

            int sectionY = chunkSectionTag.getByte("Y");
            byte[] blockIDs = chunkSectionTag.getByteArray("Blocks");
            NibbleArray blockData = new NibbleArray(chunkSectionTag.getByteArray("Data"));
            NibbleArray extendedIDs = chunkSectionTag.hasKey("Add", Constants.NBT.TAG_BYTE_ARRAY)
                    ? new NibbleArray(chunkSectionTag.getByteArray("Add")) : null;
            for (int j = 0; j < 4096; ++j) {
                int x = j & 0x0F, y = j >> 8 & 0x0F, z = j >> 4 & 0x0F;
                BlockPos pos = chunkPos.getBlock(x, sectionY << 4 | y, z);
                String mteID = metaTileEntityIds.get(pos);
                if (mteID == null) continue;
                MetaTileEntity mte = GregTechAPI.MTE_REGISTRY.getObject(new ResourceLocation(mteID));
                if (mte == null) continue;
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
            chunkSectionTag.setByteArray("Blocks", blockIDs);
            chunkSectionTag.setByteArray("Data", blockData.getData());
            if (extendedIDs != null) {
                chunkSectionTag.setByteArray("Add", extendedIDs.getData());
            }
        }
        return compound;
    }
}
