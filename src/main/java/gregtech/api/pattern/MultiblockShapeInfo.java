package gregtech.api.pattern;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.BlockInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MultiblockShapeInfo {

    /** {@code [x][y][z]} */
    private final BlockInfo[][][] blocks;

    public MultiblockShapeInfo(BlockInfo[][][] blocks) {
        this.blocks = blocks;
    }

    /**
     * @return the blocks in an array of format: {@code [x][y][z]}
     */
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

        public Builder where(char symbol, IBlockState blockState, TileEntity tileEntity) {
            return where(symbol, new BlockInfo(blockState, tileEntity));
        }

        public Builder where(char symbol, @Nonnull MetaTileEntity tileEntity, EnumFacing frontSide) {
            MetaTileEntity mte = tileEntity.createMetaTileEntity();
            mte.onPlacement();
            mte.setFrontFacing(frontSide);
            return where(symbol, new BlockInfo(tileEntity.getBlock().getDefaultState(), mte));
        }

        /**
         * @param partSupplier Should supply either a MetaTileEntity or an IBlockState.
         */
        public Builder where(char symbol, Supplier<?> partSupplier, EnumFacing frontSideIfTE) {
            Object part = partSupplier.get();
            if (part instanceof IBlockState) {
                return where(symbol, (IBlockState) part);
            } else if (part instanceof MetaTileEntity) {
                return where(symbol, (MetaTileEntity) part, frontSideIfTE);
            } else throw new IllegalArgumentException("Supplier must supply either a MetaTileEntity or an IBlockState! Actual: " + part.getClass());
        }

        @Nonnull
        private BlockInfo[][][] bakeArray() {
            final int maxZ = shape.size();
            final int maxY = shape.get(0).length;
            final int maxX = shape.get(0)[0].length();
            BlockInfo[][][] blockInfos = new BlockInfo[maxX][maxY][maxZ];
            for (int z = 0; z < maxZ; z++) {
                String[] aisleEntry = shape.get(z);
                for (int y = 0; y < maxY; y++) {
                    String columnEntry = aisleEntry[y];
                    for (int x = 0; x < maxX; x++) {
                        BlockInfo info = symbolMap.getOrDefault(columnEntry.charAt(x), BlockInfo.EMPTY);
                        TileEntity tileEntity = info.getTileEntity();
                        if (tileEntity instanceof MetaTileEntity mte) {
                            mte.onPlacement();
                            mte.setFrontFacing(mte.getFrontFacing());
                            info = new BlockInfo(info.getBlockState(), mte);
                        } else if (tileEntity != null) {
                            info = new BlockInfo(info.getBlockState(), tileEntity);
                        }
                        blockInfos[x][y][z] = info;
                    }
                }
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
