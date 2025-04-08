package gregtech.api.pattern;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.pattern.PatternAisle;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTLog;
import gregtech.api.util.RelativeDirection;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Deprecated // this shall be removed soontm, this class has no use atm
public class MultiblockShapeInfo {

    /**
     * Default front facing for jei preview(and what patterns are most likely made for).
     */
    public static final EnumFacing DEFAULT_FRONT = EnumFacing.SOUTH;

    /**
     * Default up facing for jei preview(and what patterns are most likely made for).
     */
    public static final EnumFacing DEFAULT_UP = EnumFacing.UP;

    /**
     * Unmodifiable reverse map from facing to relative direction, using DEFAULT_FRONT and DEFAULT_UP.
     * For example, EnumFacing.NORTH -> RelativeDirection.BACK, since with the defaults the relative back of the
     * controller is north.
     */
    public static final Map<EnumFacing, RelativeDirection> FACING_MAP;
    protected final PatternAisle[] aisles;
    protected final Char2ObjectMap<BlockInfo> symbols;
    protected final Char2ObjectMap<Dot> dotMap;
    protected final RelativeDirection[] directions;

    static {
        EnumMap<EnumFacing, RelativeDirection> facingMap = new EnumMap<>(EnumFacing.class);
        for (RelativeDirection dir : RelativeDirection.VALUES) {
            facingMap.put(dir.getRelativeFacing(DEFAULT_FRONT, DEFAULT_UP, false), dir);
        }

        FACING_MAP = Collections.unmodifiableMap(facingMap);
    }

    public MultiblockShapeInfo(PatternAisle[] aisles, Char2ObjectMap<BlockInfo> symbols, Char2ObjectMap<Dot> dotMap,
                               RelativeDirection[] directions) {
        this.aisles = aisles;
        this.symbols = symbols;
        this.dotMap = dotMap;
        this.directions = directions;
        symbols.defaultReturnValue(BlockInfo.EMPTY);
    }

    /**
     * Gets a map of where blocks should be placed, note that the controller is expected to be front facing SOUTH(and up
     * facing
     * UP).
     * Unlike BlockPattern, the first char in the first string in the first aisle always starts at the origin, instead
     * of being relative to the controller.
     * The passed in map is populated.
     *
     * @return A pos that can be looked up in the given map to find the controller that has the same class as the
     *         argument.
     */
    // this is currently here so that multiblocks can have other multiblocks in their structure without messing
    // everything up
    public BlockPos getMap(MultiblockControllerBase src, BlockPos start, Map<BlockPos, BlockInfo> map) {
        EnumFacing frontFacing = src.getFrontFacing();
        EnumFacing upFacing = src.getUpwardsFacing();
        // todo update cleanroom and charcoal pile igniter with enummap instead of hashmap
        // todo replace the start argument such that now it signifies where the map should be looked up to find the
        // controller, and thus auto detect pattern start
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

                    if (dotMap.containsKey(c)) {
                        map.put(pos.immutable(), new BlockInfo(Blocks.DIAMOND_BLOCK));
                        continue;
                    }

                    if (symbols.get(c).getTileEntity() instanceof MetaTileEntityHolder holder) {
                        MetaTileEntityHolder mteHolder = new MetaTileEntityHolder();
                        mteHolder.setMetaTileEntity(holder.getMetaTileEntity());
                        mteHolder.getMetaTileEntity().onPlacement();

                        // get the relative direction from the part facing, then use that to get the real enum facing
                        EnumFacing newFacing = FACING_MAP.get(holder.getMetaTileEntity().getFrontFacing())
                                .getRelativeFacing(frontFacing, upFacing, false);
                        mteHolder.getMetaTileEntity().setFrontFacing(newFacing);

                        if (holder.getMetaTileEntity() instanceof MultiblockControllerBase holderBase) {
                            MultiblockControllerBase mteBase = (MultiblockControllerBase) mteHolder.getMetaTileEntity();

                            EnumFacing newUpFacing = FACING_MAP.get(holderBase.getUpwardsFacing())
                                    .getRelativeFacing(frontFacing, upFacing, false);
                            if (holderBase.getClass() == src.getClass()) {
                                controller = pos.immutable();
                            }
                            mteBase.setUpwardsFacing(newUpFacing);
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

    /**
     * Gets where the controller is in the pattern.
     *
     * @param clazz The class of the controller.
     * @return A pos where {@code aisles[pos.x()].getCharAt(pos.y(), pos.z())} would return where the controller char
     *         was.
     */
    protected GreggyBlockPos whereController(Class<? extends MultiblockControllerBase> clazz) {
        char c = 'S';
        for (Char2ObjectMap.Entry<BlockInfo> entry : symbols.char2ObjectEntrySet()) {
            if (entry.getValue().getTileEntity() instanceof MetaTileEntityHolder holder &&
                    holder.getMetaTileEntity() instanceof MultiblockControllerBase controller &&
                    controller.getClass() == clazz) {
                c = entry.getCharKey();
                break;
            }
        }

        for (int aisleI = 0; aisleI < aisles.length; aisleI++) {
            for (int stringI = 0; stringI < aisles[0].getStringCount(); stringI++) {
                for (int charI = 0; charI < aisles[0].getCharCount(); charI++) {
                    if (aisles[aisleI].charAt(stringI, charI) == c) return new GreggyBlockPos(aisleI, stringI, charI);
                }
            }
        }

        // maybe throw instead?
        return new GreggyBlockPos(0, 0, 0);
    }

    /**
     * Gets how many times each char occurs in the aisles.
     */
    protected Char2IntMap getChars() {
        Char2IntMap map = new Char2IntOpenHashMap();
        for (PatternAisle aisle : aisles) {
            for (int stringI = 0; stringI < aisles[0].getStringCount(); stringI++) {
                for (int charI = 0; charI < aisles[0].getCharCount(); charI++) {
                    char c = aisle.charAt(stringI, charI);
                    map.put(c, map.get(c) + 1);
                }
            }
        }

        return map;
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

    public void sendDotMessage(EntityPlayer player) {
        Dot[] dots = dotMap.values().toArray(new Dot[0]);
        Arrays.sort(dots, Comparator.comparingInt(Dot::dot));

        for (Dot dot : dots) {
            player.sendMessage(new TextComponentString("Dot Block " + dot.dot));
            player.sendMessage(new TextComponentTranslation(dot.lang));
        }
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
        private Char2ObjectMap<Dot> dotMap = new Char2ObjectOpenHashMap<>();
        private final RelativeDirection[] directions = new RelativeDirection[3];

        public Builder(RelativeDirection aisleDir, RelativeDirection stringDir, RelativeDirection charDir) {
            directions[0] = aisleDir;
            directions[1] = stringDir;
            directions[2] = charDir;
            GreggyBlockPos.validateFacingsArray(directions);
        }

        public Builder aisle(String... data) {
            this.shape.add(new PatternAisle(1, data));
            return this;
        }

        public Builder where(char symbol, BlockInfo value) {
            if (symbolMap.containsKey(symbol)) {
                GTLog.logger.warn("Tried to put symbol " + symbol +
                        " when it was already registered in the as a dot block! Ignoring the call.");
                return this;
            }

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

        /**
         * Adds a dot block to represent the char.
         *
         * @param symbol The symbol in the pattern to put.
         * @param dot    The amount of dots on the block, 0-15
         * @param lang   The lang to show for the block, pass in the lang key and it will format.
         */
        public Builder dot(char symbol, int dot, String lang) {
            if (symbolMap.containsKey(symbol)) {
                GTLog.logger.warn("Tried to put symbol " + symbol + " as dot block " + dot +
                        " when it was already registered in the symbol map! Ignoring the call.");
                return this;
            }

            // todo make lang a Function<Map<String, String>, String> to account for builder map
            dotMap.put(symbol, new Dot(dot, lang));
            return this;
        }

        public Builder shallowCopy() {
            Builder builder = new Builder(directions[0], directions[1], directions[2]);
            builder.shape = new ArrayList<>(this.shape);
            builder.symbolMap = new Char2ObjectOpenHashMap<>(this.symbolMap);
            builder.dotMap = new Char2ObjectOpenHashMap<>(this.dotMap);
            return builder;
        }

        public MultiblockShapeInfo build() {
            return new MultiblockShapeInfo(shape.toArray(new PatternAisle[0]), symbolMap, dotMap, directions);
        }
    }

    @Desugar
    public record Dot(int dot, String lang) {}
}
