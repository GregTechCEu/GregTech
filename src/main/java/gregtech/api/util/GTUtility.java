package gregtech.api.util;

import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.MetaTileEntityItemBlock;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

import static gregtech.api.GTValues.V;

public class GTUtility {

    public static <T> String[] mapToString(T[] array, Function<T, String> mapper) {
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = mapper.apply(array[i]);
        }
        return result;
    }

    //just because CCL uses a different color format
    //0xRRGGBBAA
    public static int convertRGBtoOpaqueRGBA_CL(int colorValue) {
        return convertRGBtoRGBA_CL(colorValue, 255);
    }

    public static int convertRGBtoRGBA_CL(int colorValue, int opacity) {
        return colorValue << 8 | (opacity & 0xFF);
    }

    public static int convertOpaqueRGBA_CLtoRGB(int colorAlpha) {
        return colorAlpha >>> 8;
    }

    //0xAARRGGBB
    public static int convertRGBtoOpaqueRGBA_MC(int colorValue) {
        return convertRGBtoOpaqueRGBA_MC(colorValue, 255);
    }

    public static int convertRGBtoOpaqueRGBA_MC(int colorValue, int opacity) {
        return opacity << 24 | colorValue;
    }

    public static int convertRGBtoARGB(int colorValue) {
        return convertRGBtoARGB(colorValue, 0xFF);
    }

    public static int convertRGBtoARGB(int colorValue, int opacity) {
        // preserve existing opacity if present
        if (((colorValue >> 24) & 0xFF) != 0) return colorValue;
        return opacity << 24 | colorValue;
    }

    /**
     * Attempts to merge given ItemStack with ItemStacks in slot list supplied
     * If it's not possible to merge it fully, it will attempt to insert it into first empty slots
     *
     * @param itemStack item stack to merge. It WILL be modified.
     * @param simulate  if true, stack won't actually modify items in other slots
     * @return if merging of at least one item succeed, false otherwise
     */
    public static boolean mergeItemStack(ItemStack itemStack, List<Slot> slots, boolean simulate) {
        if (itemStack.isEmpty())
            return false; //if we are merging empty stack, return

        boolean merged = false;
        //iterate non-empty slots first
        //to try to insert stack into them
        for (Slot slot : slots) {
            if (!slot.isItemValid(itemStack))
                continue; //if itemstack cannot be placed into that slot, continue
            ItemStack stackInSlot = slot.getStack();
            if (!ItemStack.areItemsEqual(itemStack, stackInSlot) ||
                    !ItemStack.areItemStackTagsEqual(itemStack, stackInSlot))
                continue; //if itemstacks don't match, continue
            int slotMaxStackSize = Math.min(stackInSlot.getMaxStackSize(), slot.getItemStackLimit(stackInSlot));
            int amountToInsert = Math.min(itemStack.getCount(), slotMaxStackSize - stackInSlot.getCount());
            // Need to check <= 0 for the PA, which could have this value negative due to slot limits in the Machine Access Interface
            if (amountToInsert <= 0)
                continue; //if we can't insert anything, continue
            //shrink our stack, grow slot's stack and mark slot as changed
            if (!simulate) {
                stackInSlot.grow(amountToInsert);
            }
            itemStack.shrink(amountToInsert);
            slot.onSlotChanged();
            merged = true;
            if (itemStack.isEmpty())
                return true; //if we inserted all items, return
        }

        //then try to insert itemstack into empty slots
        //breaking it into pieces if needed
        for (Slot slot : slots) {
            if (!slot.isItemValid(itemStack))
                continue; //if itemstack cannot be placed into that slot, continue
            if (slot.getHasStack())
                continue; //if slot contains something, continue
            int amountToInsert = Math.min(itemStack.getCount(), slot.getItemStackLimit(itemStack));
            if (amountToInsert == 0)
                continue; //if we can't insert anything, continue
            //split our stack and put result in slot
            ItemStack stackInSlot = itemStack.splitStack(amountToInsert);
            if (!simulate) {
                slot.putStack(stackInSlot);
            }
            merged = true;
            if (itemStack.isEmpty())
                return true; //if we inserted all items, return
        }
        return merged;
    }

    public static void writeItems(IItemHandler handler, String tagName, NBTTagCompound tag) {
        NBTTagList tagList = new NBTTagList();

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                NBTTagCompound stackTag = new NBTTagCompound();
                stackTag.setInteger("Slot", i);
                handler.getStackInSlot(i).writeToNBT(stackTag);
                tagList.appendTag(stackTag);
            }
        }

        tag.setTag(tagName, tagList);
    }

    public static void readItems(IItemHandlerModifiable handler, String tagName, NBTTagCompound tag) {
        if (tag.hasKey(tagName)) {
            NBTTagList tagList = tag.getTagList(tagName, Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.tagCount(); i++) {
                int slot = tagList.getCompoundTagAt(i).getInteger("Slot");

                if (slot >= 0 && slot < handler.getSlots()) {
                    handler.setStackInSlot(slot, new ItemStack(tagList.getCompoundTagAt(i)));
                }
            }
        }
    }

    /**
     * @param array Array sorted with natural order
     * @param value Value to search for
     * @return Index of the nearest value lesser or equal than {@code value},
     * or {@code -1} if there's no entry matching the condition
     */
    public static int nearestLesserOrEqual(@Nonnull long[] array, long value) {
        int low = 0, high = array.length - 1;
        while (true) {
            int median = (low + high) / 2;
            if (array[median] <= value) {
                if (low == high) return low;
                low = median + 1;
            } else {
                if (low == high) return low - 1;
                high = median - 1;
            }
        }
    }

    /**
     * @param array Array sorted with natural order
     * @param value Value to search for
     * @return Index of the nearest value lesser than {@code value},
     * or {@code -1} if there's no entry matching the condition
     */
    public static int nearestLesser(@Nonnull long[] array, long value) {
        int low = 0, high = array.length - 1;
        while (true) {
            int median = (low + high) / 2;
            if (array[median] < value) {
                if (low == high) return low;
                low = median + 1;
            } else {
                if (low == high) return low - 1;
                high = median - 1;
            }
        }
    }

    /**
     * @return Lowest tier of the voltage that can handle {@code voltage}; that is,
     * a voltage with value greater than equal than {@code voltage}. If there's no
     * tier that can handle it, {@code MAX} is returned.
     */
    public static byte getTierByVoltage(long voltage) {
        return (byte) Math.min(GTValues.MAX, nearestLesser(V, voltage) + 1);
    }

    /**
     * Ex: This method turns both 1024 and 512 into HV.
     *
     * @return the highest voltage tier with value below or equal to {@code voltage}, or
     * {@code ULV} if there's no tier below
     */
    public static byte getFloorTierByVoltage(long voltage) {
        return (byte) Math.max(GTValues.ULV, nearestLesserOrEqual(V, voltage));
    }

    @SuppressWarnings("deprecation")
    public static BiomeDictionary.Type getBiomeTypeTagByName(String name) {
        Map<String, BiomeDictionary.Type> byName = ReflectionHelper.getPrivateValue(BiomeDictionary.Type.class, null, "byName");
        return byName.get(name);
    }

    public static <T> int getRandomItem(Random random, List<? extends Entry<Integer, T>> randomList, int size) {
        if (randomList.isEmpty())
            return -1;
        int[] baseOffsets = new int[size];
        int currentIndex = 0;
        for (int i = 0; i < size; i++) {
            Entry<Integer, T> entry = randomList.get(i);
            if (entry.getKey() <= 0) {
                throw new IllegalArgumentException("Invalid weight: " + entry.getKey());
            }
            currentIndex += entry.getKey();
            baseOffsets[i] = currentIndex;
        }
        int randomValue = random.nextInt(currentIndex);
        for (int i = 0; i < size; i++) {
            if (randomValue < baseOffsets[i])
                return i;
        }
        throw new IllegalArgumentException("Invalid weight");
    }

    public static <T> int getRandomItem(List<? extends Entry<Integer, T>> randomList, int size) {
        return getRandomItem(GTValues.RNG, randomList, size);
    }

    @Nullable
    public static EnumFacing determineWrenchingSide(EnumFacing facing, float x, float y, float z) {
        EnumFacing opposite = facing.getOpposite();
        switch (facing) {
            case DOWN:
            case UP:
                if (x < 0.25) {
                    if (z < 0.25) return opposite;
                    if (z > 0.75) return opposite;
                    return EnumFacing.WEST;
                }
                if (x > 0.75) {
                    if (z < 0.25) return opposite;
                    if (z > 0.75) return opposite;
                    return EnumFacing.EAST;
                }
                if (z < 0.25) return EnumFacing.NORTH;
                if (z > 0.75) return EnumFacing.SOUTH;
                return facing;
            case NORTH:
            case SOUTH:
                if (x < 0.25) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return EnumFacing.WEST;
                }
                if (x > 0.75) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return EnumFacing.EAST;
                }
                if (y < 0.25) return EnumFacing.DOWN;
                if (y > 0.75) return EnumFacing.UP;
                return facing;
            case WEST:
            case EAST:
                if (z < 0.25) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return EnumFacing.NORTH;
                }
                if (z > 0.75) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return EnumFacing.SOUTH;
                }
                if (y < 0.25) return EnumFacing.DOWN;
                if (y > 0.75) return EnumFacing.UP;
                return facing;
        }
        return null;
    }

    /**
     * @return a list of itemstack linked with given item handler
     * modifications in list will reflect on item handler and wise-versa
     */
    public static List<ItemStack> itemHandlerToList(IItemHandlerModifiable inputs) {
        return new AbstractList<ItemStack>() {
            @Override
            public ItemStack set(int index, ItemStack element) {
                ItemStack oldStack = inputs.getStackInSlot(index);
                inputs.setStackInSlot(index, element == null ? ItemStack.EMPTY : element);
                return oldStack;
            }

            @Override
            public ItemStack get(int index) {
                return inputs.getStackInSlot(index);
            }

            @Override
            public int size() {
                return inputs.getSlots();
            }
        };
    }

    /**
     * @return a list of fluidstack linked with given fluid handler
     * modifications in list will reflect on fluid handler and wise-versa
     */
    public static List<FluidStack> fluidHandlerToList(IMultipleTankHandler fluidInputs) {
        List<IMultipleTankHandler.MultiFluidTankEntry> backedList = fluidInputs.getFluidTanks();
        return new AbstractList<FluidStack>() {
            @Override
            public FluidStack set(int index, FluidStack element) {
                IFluidTank fluidTank = backedList.get(index).getDelegate();
                FluidStack oldStack = fluidTank.getFluid();
                if (fluidTank instanceof FluidTank) {
                    ((FluidTank) fluidTank).setFluid(element);
                }
                return oldStack;
            }

            @Override
            public FluidStack get(int index) {
                return backedList.get(index).getFluid();
            }

            @Override
            public int size() {
                return backedList.size();
            }
        };
    }

    public static NBTTagCompound getOrCreateNbtCompound(ItemStack stack) {
        NBTTagCompound compound = stack.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
            stack.setTagCompound(compound);
        }
        return compound;
    }

    public static NonNullList<ItemStack> copyStackList(List<ItemStack> itemStacks) {
        ItemStack[] stacks = new ItemStack[itemStacks.size()];
        for (int i = 0; i < itemStacks.size(); i++) {
            stacks[i] = copy(itemStacks.get(i));
        }
        return NonNullList.from(ItemStack.EMPTY, stacks);
    }

    public static List<FluidStack> copyFluidList(List<FluidStack> fluidStacks) {
        FluidStack[] stacks = new FluidStack[fluidStacks.size()];
        for (int i = 0; i < fluidStacks.size(); i++) stacks[i] = fluidStacks.get(i).copy();
        return Lists.newArrayList(stacks);
    }

    /**
     * @deprecated Ambiguous naming; use either {@link #copy(ItemStack)} or {@link #copyFirst(ItemStack...)}
     *
     * </p> This method was deprecated in 2.6 and will be removed in 2.8
     */
    @Deprecated
    @Nonnull
    public static ItemStack copy(@Nonnull ItemStack... stacks) {
        for (ItemStack stack : stacks)
            if (!stack.isEmpty()) return stack.copy();
        return ItemStack.EMPTY;
    }

    /**
     * Copies the ItemStack.
     *
     * @param stack item stack for copying
     * @return a copy of ItemStack, or {@link ItemStack#EMPTY} if the stack is empty
     */
    @Nonnull
    public static ItemStack copy(@Nonnull ItemStack stack) {
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }

    /**
     * Copies the ItemStack with new stack size.
     *
     * @param stack item stack for copying
     * @return a copy of ItemStack, or {@link ItemStack#EMPTY} if the stack is empty
     */
    @Nonnull
    public static ItemStack copy(int newCount, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = stack.copy();
        copy.setCount(newCount);
        return copy;
    }

    /**
     * Copies first non-empty ItemStack from stacks.
     *
     * @param stacks list of candidates for copying
     * @return a copy of ItemStack, or {@link ItemStack#EMPTY} if all the candidates are empty
     * @throws IllegalArgumentException if {@code stacks} is empty
     */
    @Nonnull
    public static ItemStack copyFirst(@Nonnull ItemStack... stacks) {
        if (stacks.length == 0) {
            throw new IllegalArgumentException("Empty ItemStack candidates");
        }
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Copies first non-empty ItemStack from stacks, with new stack size.
     *
     * @param stacks list of candidates for copying
     * @return a copy of ItemStack, or {@link ItemStack#EMPTY} if all the candidates are empty
     * @throws IllegalArgumentException if {@code stacks} is empty
     */
    @Nonnull
    public static ItemStack copyFirst(int newCount, @Nonnull ItemStack... stacks) {
        if (stacks.length == 0) {
            throw new IllegalArgumentException("Empty ItemStack candidates");
        }
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                ItemStack copy = stack.copy();
                copy.setCount(newCount);
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * @deprecated Use {@link #copy(int, ItemStack)}
     *
     * </p> This method was deprecated in 2.6 and will be removed in 2.8
     */
    @Deprecated
    @Nonnull
    public static ItemStack copyAmount(int amount, @Nonnull ItemStack stack) {
        return copy(amount, stack);
    }

    public static int getExplosionPower(long voltage) {
        return getTierByVoltage(voltage) + 1;
    }

    public static int getRedstonePower(World world, BlockPos blockPos, EnumFacing side) {
        BlockPos offsetPos = blockPos.offset(side);
        int worldPower = world.getRedstonePower(offsetPos, side);
        if (worldPower < 15) {
            IBlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() instanceof BlockRedstoneWire) {
                int wirePower = offsetState.getValue(BlockRedstoneWire.POWER);
                return Math.max(worldPower, wirePower);
            }
        }
        return worldPower;
    }

    public static boolean arePosEqual(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() & pos1.getY() == pos2.getY() & pos1.getZ() == pos2.getZ();
    }

    public static boolean isCoverBehaviorItem(ItemStack itemStack, @Nullable BooleanSupplier hasCoverSupplier, @Nullable Predicate<CoverDefinition> canPlaceCover) {
        Item item = itemStack.getItem();
        if (item instanceof MetaItem) {
            MetaItem<?> metaItem = (MetaItem<?>) itemStack.getItem();
            MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(itemStack);
            if (valueItem != null) {
                for (IItemBehaviour behaviour : valueItem.getBehaviours()) {
                    if (behaviour instanceof CoverPlaceBehavior) {
                        return canPlaceCover == null || canPlaceCover.test(((CoverPlaceBehavior) behaviour).coverDefinition);
                    }
                }
            }
        } else if (item.getToolClasses(itemStack).contains(ToolClasses.CROWBAR)) {
            return hasCoverSupplier == null || hasCoverSupplier.getAsBoolean();
        } else if (item.getToolClasses(itemStack).contains(ToolClasses.SOFT_MALLET)) {
            return hasCoverSupplier == null || hasCoverSupplier.getAsBoolean();
        }
        return false;
    }

    /**
     * Default function for tank sizes, takes a tier input and returns the corresponding size
     */
    public static final Function<Integer, Integer> defaultTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 8000;
        if (tier == GTValues.MV)
            return 12000;
        if (tier == GTValues.HV)
            return 16000;
        if (tier == GTValues.EV)
            return 32000;
        // IV+
        return 64000;
    };

    /**
     * Alternative function for tank sizes, takes a tier input and returns the corresponding size
     * <p>
     * This function scales the same as the default function except it stops scaling past HV
     */
    public static final Function<Integer, Integer> hvCappedTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 8000;
        if (tier == GTValues.MV)
            return 12000;
        // HV+
        return 16000;
    };

    /**
     * Alternative function for tank sizes, takes a tier input and returns the corresponding size
     * <p>
     * This function is meant for use with machines that need very large tanks, it stops scaling past HV
     */
    public static final Function<Integer, Integer> largeTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 32000;
        if (tier == GTValues.MV)
            return 48000;
        // HV+
        return 64000;
    };

    /**
     * Alternative function for tank sizes, takes a tier input and returns the corresponding size
     * <p>
     * This function is meant for use with generators
     */
    public static final Function<Integer, Integer> steamGeneratorTankSizeFunction = tier -> Math.min(16000 * (1 << (tier - 1)), 64000);

    public static final Function<Integer, Integer> genericGeneratorTankSizeFunction = tier -> Math.min(4000 * (1 << (tier - 1)), 16000);

    public static ItemStack toItem(IBlockState state) {
        return toItem(state, 1);
    }

    public static ItemStack toItem(IBlockState state, int amount) {
        return new ItemStack(state.getBlock(), amount, state.getBlock().getMetaFromState(state));
    }

    public static boolean isOre(ItemStack item) {
        OrePrefix orePrefix = OreDictUnifier.getPrefix(item);
        return orePrefix != null && orePrefix.name().startsWith("ore");
    }

    /**
     * Checks whether a machine is not a multiblock and has a recipemap not present in a blacklist
     *
     * @param machineStack the ItemStack containing the machine to check the validity of
     * @return whether the machine is valid or not
     */
    public static boolean isMachineValidForMachineHatch(ItemStack machineStack, String[] recipeMapBlacklist) {
        if (machineStack == null || machineStack.isEmpty()) {
            return false;
        }

        MetaTileEntity machine = getMetaTileEntity(machineStack);
        if (machine instanceof WorkableTieredMetaTileEntity && !(machine instanceof SimpleGeneratorMetaTileEntity)) {
            RecipeMap<?> recipeMap = machine.getRecipeMap();
            return recipeMap != null && !ArrayUtils.contains(recipeMapBlacklist, recipeMap.getUnlocalizedName());
        }

        return false;
    }

    /**
     * Does almost the same thing as .to(LOWER_UNDERSCORE, string), but it also inserts underscores between words and numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maragingSteel300" -> "maraging_steel_300"
     */
    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0 && (Character.isUpperCase(string.charAt(i)) || (
                    Character.isDigit(string.charAt(i - 1)) ^ Character.isDigit(string.charAt(i)))))
                result.append("_");
            result.append(Character.toLowerCase(string.charAt(i)));
        }
        return result.toString();
    }

    /**
     * Does almost the same thing as LOWER_UNDERSCORE.to(UPPER_CAMEL, string), but it also removes underscores before numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maraging_steel_300" -> "maragingSteel300"
     */
    public static String lowerUnderscoreToUpperCamel(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '_')
                continue;
            if (i == 0 || string.charAt(i - 1) == '_') {
                result.append(Character.toUpperCase(string.charAt(i)));
            } else {
                result.append(string.charAt(i));
            }
        }
        return result.toString();
    }

    /**
     * @deprecated Use {@link TextFormattingUtil#formatNumbers(long)} instead.
     *
     * </p> This class was deprecated in 2.7 and will be removed in 2.8
     */
    @Deprecated
    public static String formatNumbers(long number) {
        return TextFormattingUtil.formatNumbers(number);
    }

    /**
     * @deprecated Use {@link TextFormattingUtil#formatNumbers(double)} instead.
     *
     * </p> This class was deprecated in 2.7 and will be removed in 2.8
     */
    @Deprecated
    public static String formatNumbers(double number) {
        return TextFormattingUtil.formatNumbers(number);
    }

    public static MetaTileEntity getMetaTileEntity(IBlockAccess world, BlockPos pos) {
        if (world == null || pos == null) return null;
        TileEntity te = world.getTileEntity(pos);
        return te instanceof IGregTechTileEntity igtte ? igtte.getMetaTileEntity() : null;
    }

    public static MetaTileEntity getMetaTileEntity(ItemStack stack) {
        if (!(stack.getItem() instanceof MetaTileEntityItemBlock)) return null;
        return GregTechAPI.MTE_REGISTRY.getObjectById(stack.getItemDamage());
    }

    public static boolean canSeeSunClearly(World world, BlockPos blockPos) {
        if (!world.canSeeSky(blockPos.up())) {
            return false;
        }
        Biome biome = world.getBiome(blockPos.up());
        if (world.isRaining()) {
            if (biome.canRain() || biome.getEnableSnow()) {
                return false;
            }
        }
        Set<BiomeDictionary.Type> biomeTypes = BiomeDictionary.getTypes(biome);
        if (biomeTypes.contains(BiomeDictionary.Type.END)) {
            return false;
        }
        return world.isDaytime();
    }

    public static MapColor getMapColor(int rgb) {
        MapColor color = MapColor.BLACK;
        int originalR = (rgb >> 16) & 0xFF;
        int originalG = (rgb >> 8) & 0xFF;
        int originalB = rgb & 0xFF;
        int distance = Integer.MAX_VALUE;

        for (MapColor mapColor : MapColor.COLORS) {
            // why is there a null in here mojang!?
            if (mapColor == null) continue;

            int colorValue = mapColor.colorValue;
            if (colorValue == 0) continue;

            int colorR = (colorValue >> 16) & 0xFF;
            int colorG = (colorValue >> 8) & 0xFF;
            int colorB = colorValue & 0xFF;

            int distR = originalR - colorR;
            int distG = originalG - colorG;
            int distB = originalB - colorB;
            int dist = distR * distR + distG * distG + distB * distB;

            if (dist < distance) {
                distance = dist;
                color = mapColor;
            }
        }
        return color;
    }

    public static boolean isBlockSnowLayer(@Nonnull IBlockState blockState) {
        return blockState.getBlock() == Blocks.SNOW_LAYER && blockState.getValue(BlockSnow.LAYERS) == 1;
    }

    /**
     * Attempt to break a (single) snow layer at the given BlockPos.
     *
     * @return true if the passed IBlockState was a snow layer
     */
    public static boolean tryBreakSnowLayer(World world, BlockPos pos, @Nonnull IBlockState blockState, boolean playSound) {
        if (isBlockSnowLayer(blockState)) {
            world.destroyBlock(pos, false);
            if (playSound) {
                world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0f, 1.0f);
            }
            return true;
        }
        return false;
    }

    /**
     * @param stack the stack to retrieve from
     * @return all the sub-items of an ItemStack
     * @deprecated Use {@link #getAllSubItems(Item)}
     *
     * </p> This method was deprecated in 2.6 and will be removed in 2.8
     */
    @Nonnull
    @Deprecated
    public static Set<ItemStack> getAllSubItems(@Nonnull ItemStack stack) {
        //match subtypes only on wildcard damage value items
        if (stack.getItemDamage() != GTValues.W) return Collections.singleton(stack);
        return getAllSubItems(stack.getItem());
    }

    /**
     * Returns all known sub-variant of an {@code item}. The sub-variants
     * are set of ItemStacks returned from
     * {@link Item#getSubItems(CreativeTabs, NonNullList)}.
     * <p>
     * Due to how the aforementioned method works, it may not generate all
     * existing variants, especially item variants hidden from creative tab.
     *
     * @param item item
     * @return all the sub-items of an item
     */
    @Nonnull
    public static Set<ItemStack> getAllSubItems(@Nonnull Item item) {
        NonNullList<ItemStack> subItems = NonNullList.create();
        for (CreativeTabs tab : item.getCreativeTabs()) {
            if (tab == null || tab == CreativeTabs.SEARCH) continue;
            item.getSubItems(tab, subItems);
        }
        Set<ItemStack> set = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingItemDamageCount());
        set.addAll(subItems);
        return set;
    }

    /**
     * Get fluidstack from a container.
     *
     * @param ingredient the fluidstack or fluid container item
     * @return the fluidstack in container
     */
    @Nullable
    public static FluidStack getFluidFromContainer(Object ingredient) {
        if (ingredient instanceof FluidStack) {
            return (FluidStack) ingredient;
        } else if (ingredient instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) ingredient;
            IFluidHandlerItem fluidHandler = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null)
                return fluidHandler.drain(Integer.MAX_VALUE, false);
        }
        return null;
    }

    /**
     * Create a new {@link ResourceLocation} with {@link GTValues#MODID} as the namespace and a specified path
     *
     * @param path the path in the location
     * @return the new location
     */
    @Nonnull
    public static ResourceLocation gregtechId(@Nonnull String path) {
        return new ResourceLocation(GTValues.MODID, path);
    }
}
