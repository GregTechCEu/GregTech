package gregtech.api.pattern;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.pattern.pattern.PatternAisle;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MultiblockShapeInfo {

    protected final PatternAisle[] aisles;
    protected final Char2ObjectMap<BlockInfo> symbols;
    protected final RelativeDirection[] directions;

    public MultiblockShapeInfo(PatternAisle[] aisles, Char2ObjectMap<BlockInfo> symbols,
                               RelativeDirection[] directions) {
        this.aisles = aisles;
        this.symbols = symbols;
        this.directions = directions;
        symbols.defaultReturnValue(BlockInfo.EMPTY);
    }

    /**
     * Gets a map of where blocks should be placed, note that the controller is always facing south(and up facing
     * NORTH).
     * Unlike BlockPattern, the first char in the first string in the first aisle always starts at the origin, instead
     * of being relative to the controller.
     */
    public Map<BlockPos, BlockInfo> getMap() {
        // seems like MultiblockInfoRecipeWrapper wants the controller to be facing south
        EnumFacing absoluteAisle = directions[0].getRelativeFacing(EnumFacing.SOUTH, EnumFacing.NORTH, false);
        EnumFacing absoluteString = directions[1].getRelativeFacing(EnumFacing.SOUTH, EnumFacing.NORTH, false);
        EnumFacing absoluteChar = directions[2].getRelativeFacing(EnumFacing.SOUTH, EnumFacing.NORTH, false);

        int aisleCount = aisles.length;
        int stringCount = aisles[0].getStringCount();
        int charCount = aisles[0].getCharCount();

        GreggyBlockPos pos = new GreggyBlockPos();
        Map<BlockPos, BlockInfo> map = new HashMap<>();

        for (int aisleI = 0; aisleI < aisleCount; aisleI++) {
            for (int stringI = 0; stringI < stringCount; stringI++) {
                for (int charI = 0; charI < charCount; charI++) {
                    char c = aisles[aisleI].charAt(stringI, charI);
                    pos.zero().offset(absoluteAisle, aisleI).offset(absoluteString, stringI).offset(absoluteChar,
                            charI);
                    if (symbols.get(c).getTileEntity() instanceof MetaTileEntityHolder holder) {
                        MetaTileEntityHolder mteHolder = new MetaTileEntityHolder();
                        mteHolder.setMetaTileEntity(holder.getMetaTileEntity());
                        mteHolder.getMetaTileEntity().onPlacement();
                        mteHolder.getMetaTileEntity().setFrontFacing(holder.getMetaTileEntity().getFrontFacing());
                        map.put(pos.immutable(),
                                new BlockInfo(mteHolder.getMetaTileEntity().getBlock().getDefaultState(), mteHolder));
                    } else {
                        map.put(pos.immutable(), symbols.get(c));
                    }
                }
            }
        }

        if (true) return map;

        // scuffed but tries to make hatches face out the structure
        for (Map.Entry<BlockPos, BlockInfo> entry : map.entrySet()) {
            BlockInfo info = entry.getValue();
            if (info.getTileEntity() != null && info.getTileEntity() instanceof MetaTileEntityHolder holder) {
                MetaTileEntity mte = holder.getMetaTileEntity();
                for (EnumFacing facing : EnumFacing.VALUES) {
                    BlockPos offset = entry.getKey().offset(facing);
                    boolean isOutside = !map.containsKey(offset);
                    if (isOutside && mte.isValidFrontFacing(facing)) {
                        MetaTileEntityHolder mteHolder = new MetaTileEntityHolder();
                        mteHolder.setMetaTileEntity(mte);
                        mteHolder.getMetaTileEntity().onPlacement();
                        mteHolder.getMetaTileEntity().setFrontFacing(facing);
                        entry.setValue(new BlockInfo(mte.getBlock().getDefaultState(), mteHolder));
                    }
                }
            }
        }

        return map;
    }

    public static Builder builder(RelativeDirection aisleDir, RelativeDirection stringDir, RelativeDirection charDir) {
        return new Builder(aisleDir, stringDir, charDir);
    }

    public static Builder builder() {
        // this is front now because idk the old code somehow reversed it and im not about to look into it
        return builder(RelativeDirection.FRONT, RelativeDirection.UP, RelativeDirection.RIGHT);
    }

    public static class Builder {

        private List<PatternAisle> shape = new ArrayList<>();
        private Char2ObjectMap<BlockInfo> symbolMap = new Char2ObjectOpenHashMap<>();
        private final RelativeDirection[] directions = new RelativeDirection[3];

        public Builder(RelativeDirection aisleDir, RelativeDirection stringDir, RelativeDirection charDir) {
            directions[0] = aisleDir;
            directions[1] = stringDir;
            directions[2] = charDir;
            int flags = 0;
            for (int i = 0; i < 3; i++) {
                switch (directions[i]) {
                    case UP:
                    case DOWN:
                        flags |= 0x1;
                        break;
                    case LEFT:
                    case RIGHT:
                        flags |= 0x2;
                        break;
                    case FRONT:
                    case BACK:
                        flags |= 0x4;
                        break;
                }
            }
            if (flags != 0x7) throw new IllegalArgumentException("Must have 3 different axes!");
        }

        public Builder aisle(String... data) {
            this.shape.add(new PatternAisle(1, data));
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

        public Builder where(char symbol, MetaTileEntity tileEntity, EnumFacing frontSide) {
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            holder.setMetaTileEntity(tileEntity);
            holder.getMetaTileEntity().onPlacement();
            holder.getMetaTileEntity().setFrontFacing(frontSide);
            return where(symbol, new BlockInfo(tileEntity.getBlock().getDefaultState(), holder));
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
            } else throw new IllegalArgumentException(
                    "Supplier must supply either a MetaTileEntity or an IBlockState! Actual: " + part.getClass());
        }

        public Builder shallowCopy() {
            Builder builder = new Builder(directions[0], directions[1], directions[2]);
            builder.shape = new ArrayList<>(this.shape);
            builder.symbolMap = new Char2ObjectOpenHashMap<>(this.symbolMap);
            return builder;
        }

        public MultiblockShapeInfo build() {
            return new MultiblockShapeInfo(shape.toArray(new PatternAisle[0]), symbolMap, directions);
        }
    }
}
