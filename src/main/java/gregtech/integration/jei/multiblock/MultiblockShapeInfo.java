package gregtech.integration.jei.multiblock;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.BlockInfo;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiblockShapeInfo {

    private final BlockInfo[][][] blocks; //[z][y][x]

    public MultiblockShapeInfo(BlockInfo[][][] blocks) {
        this.blocks = blocks;
    }

    public BlockInfo[][][] getBlocks() {
        return blocks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<String[]> shape = new ArrayList<>();
        private Map<Character, BlockInfo> symbolMap = new HashMap<>();

        public Builder aisle(String... data) {
            this.shape.add(data);
            return this;
        }

        public Builder where(char symbol, BlockInfo value) {
            this.symbolMap.put(symbol, value);
            return this;
        }

        public Builder where(char symbol, IBlockState blockState) {
            return where(symbol, new BlockInfo(blockState));
        }

        public Builder where(char symbol, MetaTileEntity tileEntity, EnumFacing frontSide) {
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            holder.setMetaTileEntity(tileEntity);
            holder.getMetaTileEntity().setFrontFacing(frontSide);
            return where(symbol, new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), holder));
        }

        private BlockInfo[][][] bakeArray() {
            BlockInfo[][][] blockInfos = new BlockInfo[shape.size()][][];
            for (int i = 0; i < blockInfos.length; i++) {
                String[] aisleEntry = shape.get(i);
                BlockInfo[][] aisleData = new BlockInfo[aisleEntry.length][];
                for (int j = 0; j < aisleData.length; j++) {
                    String columnEntry = aisleEntry[j];
                    BlockInfo[] columnData = new BlockInfo[columnEntry.length()];
                    for (int k = 0; k < columnData.length; k++) {
                        columnData[k] = symbolMap.getOrDefault(columnEntry.charAt(k), BlockInfo.EMPTY);
                        TileEntity tileEntity = columnData[k].getTileEntity();
                        if (tileEntity != null) {
                            MetaTileEntityHolder holder = (MetaTileEntityHolder) tileEntity;
                            final MetaTileEntity mte = holder.getMetaTileEntity();
                            holder = new MetaTileEntityHolder();
                            holder.setMetaTileEntity(mte);
                            holder.getMetaTileEntity().setFrontFacing(mte.getFrontFacing());
                            columnData[k] = new BlockInfo(columnData[k].getBlockState(), holder);
                        }
                    }
                    aisleData[j] = columnData;
                }
                blockInfos[i] = aisleData;
            }
            return blockInfos;
        }

        public Builder shallowCopy() {
            Builder builder = new Builder();
            builder.shape = new ArrayList<>(this.shape);
            builder.symbolMap = new HashMap<>(this.symbolMap);
            return builder;
        }

        public MultiblockShapeInfo build() {
            return new MultiblockShapeInfo(bakeArray());
        }

    }

}
