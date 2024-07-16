package gregtech.api.pattern;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static gregtech.api.pattern.FactoryBlockPattern.COMMA_JOIN;

/**
 * Class holding data for a concrete multiblock. This multiblock can be formed or unformed in this.
 */
public class PreviewBlockPattern {

    /**
     * In [ aisleDir, stringDir, charDir ]
     */
    protected final RelativeDirection[] structureDir;

    protected final PatternAisle[] aisles;
    protected final int[] dimensions, startOffset;
    protected final Char2ObjectMap<BlockInfo> symbols;

    /**
     * Legacy compat only, do not use for new code.
     */
    public PreviewBlockPattern(MultiblockShapeInfo info) {
        this.aisles = info.aisles;
        this.dimensions = new int[] { aisles.length, aisles[0].getStringCount(), aisles[0].getCharCount() };
        structureDir = new RelativeDirection[] { RelativeDirection.BACK, RelativeDirection.UP,
                RelativeDirection.RIGHT };
        this.startOffset = new int[3];
        // i am lazy so hopefully addons follow the convention of using 'S' for their self predicate(aka center predicate)
        legacyStartOffset('S');
        this.symbols = info.symbols;
    }

    public PreviewBlockPattern(PatternAisle[] aisles, int[] dimensions, RelativeDirection[] directions, int[] startOffset, Char2ObjectMap<BlockInfo> symbols) {
        this.aisles = aisles;
        this.dimensions = dimensions;
        this.structureDir = directions;
        this.startOffset = startOffset;
        this.symbols = symbols;
    }

    private void legacyStartOffset(char center) {
        // could also use aisles.length but this is cooler
        for (int aisleI = 0; aisleI < dimensions[0]; aisleI++) {
            int[] result = aisles[aisleI].firstInstanceOf(center);
            if (result != null) {
                startOffset[0] = aisleI;
                startOffset[1] = result[0];
                startOffset[2] = result[1];
                return;
            }
        }

        System.out.println("FAILED TO FIND PREDICATE");
    }

    public static class Builder {

        /**
         * In [ aisle count, string count, char count ]
         */
        protected final int[] dimensions = new int[3];

        /**
         * In relative directions opposite to {@code directions}
         */
        protected final int[] offset = new int[3];

        /**
         * Way the builder progresses
         * @see FactoryBlockPattern
         */
        protected final RelativeDirection[] directions = new RelativeDirection[3];
        protected final List<PatternAisle> aisles = new ArrayList<>();
        protected final Char2ObjectMap<BlockInfo> symbolMap = new Char2ObjectOpenHashMap<>();

        /**
         * @see Builder#start(RelativeDirection, RelativeDirection, RelativeDirection)
         */
        protected Builder(RelativeDirection aisleDir, RelativeDirection stringDir, RelativeDirection charDir) {
            directions[0] = charDir;
            directions[1] = stringDir;
            directions[2] = aisleDir;

            boolean[] flags = new boolean[3];
            for (int i = 0; i < 3; i++) {
                flags[directions[i].ordinal() / 2] = true;
            }
            if (!(flags[0] && flags[1] && flags[2])) throw new IllegalArgumentException("Must have 3 different axes!");
        }

        /**
         * Same as calling {@link Builder#start(RelativeDirection, RelativeDirection, RelativeDirection)} with BACK, UP, RIGHT
         */
        public static Builder start() {
            return new Builder(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT);
        }

        /**
         * Starts the builder, each pair of relative directions must be used exactly once!
         * @param aisleDir The direction for aisles to advance in.
         * @param stringDir The direction for strings to advance in.
         * @param charDir The direction for chars to advance in.
         */
        public static Builder start(RelativeDirection aisleDir, RelativeDirection stringDir,
                                    RelativeDirection charDir) {
            return new Builder(aisleDir, stringDir, charDir);
        }

        // protected because it doesn't do any dimension checks and just uses trust
        protected Builder aisle(int repeats, PatternAisle aisle) {
            for (String str : aisle.pattern) {
                for (char c : str.toCharArray()) {
                    if (!this.symbolMap.containsKey(c)) {
                        this.symbolMap.put(c, null);
                    }
                }
            }
            PatternAisle copy = aisle.copy();
            copy.actualRepeats = repeats;
            aisles.add(copy);
            return this;
        }

        /**
         * Adds a new aisle to the builder.
         * @param repeats Amount of repeats.
         * @param aisle The aisle.
         */
        public Builder aisle(int repeats, String... aisle) {
            // straight up copied from factory block pattern's code
            if (ArrayUtils.isEmpty(aisle) || StringUtils.isEmpty(aisle[0]))
                throw new IllegalArgumentException("Empty pattern for aisle");

            // set the dimensions if the user hasn't already
            if (dimensions[2] == -1) {
                dimensions[2] = aisle[0].length();
            }
            if (dimensions[1] == -1) {
                dimensions[1] = aisle.length;
            }

            if (aisle.length != dimensions[1]) {
                throw new IllegalArgumentException("Expected aisle with height of " + dimensions[1] +
                        ", but was given one with a height of " + aisle.length + ")");
            } else {
                for (String s : aisle) {
                    if (s.length() != dimensions[2]) {
                        throw new IllegalArgumentException(
                                "Not all rows in the given aisle are the correct width (expected " + dimensions[2] +
                                        ", found one with " + s.length() + ")");
                    }

                    for (char c : s.toCharArray()) {
                        if (!this.symbolMap.containsKey(c)) {
                            this.symbolMap.put(c, null);
                        }
                    }
                }

                aisles.add(new PatternAisle(repeats, repeats, aisle));
                return this;
            }
        }

        /**
         * Same as calling {@link Builder#aisle(int, String...)} with the first argument being 1
         */
        public Builder aisle(String... aisle) {
            return aisle(1, aisle);
        }

        // all the .where() are copied from MultiblockShapeInfo, yay for code stealing
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

        protected void validateMissingValues() {
            List<Character> chars = new ArrayList<>();

            for (Char2ObjectMap.Entry<BlockInfo> entry : symbolMap.char2ObjectEntrySet()) {
                if (entry.getValue() == null) {
                    chars.add(entry.getCharKey());
                }
            }

            if (!chars.isEmpty()) {
                throw new IllegalStateException(
                        "Predicates for character(s) " + COMMA_JOIN.join(chars) + " are missing");
            }
        }

        public PreviewBlockPattern build() {
            validateMissingValues();
            dimensions[0] = aisles.size();
            return new PreviewBlockPattern(aisles.toArray(new PatternAisle[0]), dimensions, directions, offset, symbolMap);
        }
    }
}
