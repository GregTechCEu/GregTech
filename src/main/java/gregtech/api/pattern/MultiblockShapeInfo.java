package gregtech.api.pattern;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
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
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MultiblockShapeInfo {

    /**
     * Default front facing for jei preview(and what patterns are most likely made for).
     */
    public static final EnumFacing DEFAULT_FRONT = EnumFacing.SOUTH;

    /**
     * Default up facing for jei preview(and what patterns are most likely made for).
     */
    public static final EnumFacing DEFAULT_UP = EnumFacing.NORTH;

    /**
     * Unmodifiable reverse map from facing to relative direction, using DEFAULT_FRONT and DEFAULT_UP.
     * For example, EnumFacing.NORTH -> RelativeDirection.BACK, since with the defaults the relative back of the controller is north.
     */
    public static final Map<EnumFacing, RelativeDirection> FACING_MAP;
    protected final PatternAisle[] aisles;
    protected final Char2ObjectMap<BlockInfo> symbols;
    protected final RelativeDirection[] directions;

    static {
        EnumMap<EnumFacing, RelativeDirection> facingMap = new EnumMap<>(EnumFacing.class);
        for (RelativeDirection dir : RelativeDirection.VALUES) {
            facingMap.put(dir.getRelativeFacing(DEFAULT_FRONT, DEFAULT_UP, false), dir);
        }

        FACING_MAP = Collections.unmodifiableMap(facingMap);
    }

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
     * The passed in map is populated.
     *
     * @return A pos that can be looked up in the given map to find the controller that has the same class as the argument.
     */
    // this is currently here so that multiblocks can have other multiblocks in their structure without messing
    // everything up
    public BlockPos getMap(MultiblockControllerBase src, BlockPos start, EnumFacing frontFacing, EnumFacing upFacing,
                                           Map<BlockPos, BlockInfo> map) {
        // todo update cleanroom and charcoal pile igniter with enummap instead of hashmap
        // seems like MultiblockInfoRecipeWrapper wants the controller to be facing south
        EnumFacing absoluteAisle = directions[0].getRelativeFacing(frontFacing, upFacing, false);
        EnumFacing absoluteString = directions[1].getRelativeFacing(frontFacing, upFacing, false);
        EnumFacing absoluteChar = directions[2].getRelativeFacing(frontFacing, upFacing, false);

        int aisleCount = aisles.length;
        int stringCount = aisles[0].getStringCount();
        int charCount = aisles[0].getCharCount();

        GreggyBlockPos pos = new GreggyBlockPos();

        BlockPos controller = null;

        for (int aisleI = 0; aisleI < aisleCount; aisleI++) {
            for (int stringI = 0; stringI < stringCount; stringI++) {
                for (int charI = 0; charI < charCount; charI++) {
                    char c = aisles[aisleI].charAt(stringI, charI);
                    pos.from(start).offset(absoluteAisle, aisleI).offset(absoluteString, stringI).offset(absoluteChar,
                            charI);
                    if (symbols.get(c).getTileEntity() instanceof MetaTileEntityHolder holder) {
                        MetaTileEntityHolder mteHolder = new MetaTileEntityHolder();
                        mteHolder.setMetaTileEntity(holder.getMetaTileEntity());
                        mteHolder.getMetaTileEntity().onPlacement();

                        // get the relative direction from the part facing, then use that to get the real enum facing
                        EnumFacing newFacing = FACING_MAP.get(holder.getMetaTileEntity().getFrontFacing()).getRelativeFacing(frontFacing, upFacing, false);
                        mteHolder.getMetaTileEntity().setFrontFacing(newFacing);


                        if (mteHolder.getMetaTileEntity() instanceof MultiblockControllerBase base) {
                            // there is no way to determine upwards facing with only a front facing
                            // so if you want to have a multiblock with an upward facings that isn't UP
                            // use the (IBlockState, TileEntity) ctor for BlockInfo and set upwardsFacing there
                            // currently this just sets the controller's upwards facing
                            if (base.getClass() == src.getClass()) {
                                controller = pos.immutable();
                                base.setUpwardsFacing(upFacing);
                            }
                        }

                        map.put(pos.immutable(),
                                new BlockInfo(mteHolder.getMetaTileEntity().getBlock().getDefaultState(), mteHolder));
                    } else {
                        map.put(pos.immutable(), symbols.get(c));
                    }
                }
            }
        }

        // todo figure out how to fix the below code without returning here
        if (true) return controller;

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

        return controller;
    }

    public int getUpCount(EnumFacing frontFacing, EnumFacing upFacing) {

        int index = 0;
        for (int i = 0; i < 3; i++) {
            EnumFacing facing = directions[i].getRelativeFacing(frontFacing, upFacing, false);
            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                index = i;
                break;
            }
        }

        return switch (index) {
            case 0 -> aisles.length;
            case 1 -> aisles[0].getStringCount();
            case 2 -> aisles[0].getCharCount();
            default -> throw new IllegalStateException();
        };
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
