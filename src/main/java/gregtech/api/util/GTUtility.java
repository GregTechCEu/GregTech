package gregtech.api.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;

public class GTUtility {

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
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    private static final DecimalFormat TWO_PLACES_FORMAT = new DecimalFormat("#.##");
    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("/n");
    private static final Pattern UNDERSCORE_TO_SPACE = Pattern.compile("_");

    public static Runnable combine(Runnable... runnables) {
        return () -> {
            for (Runnable runnable : runnables) {
                if (runnable != null)
                    runnable.run();
            }
        };
    }

    public static void copyInventoryItems(IItemHandler src, IItemHandlerModifiable dest) {
        for (int i = 0; i < src.getSlots(); i++) {
            ItemStack itemStack = src.getStackInSlot(i);
            dest.setStackInSlot(i, itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy());
        }
    }

    public static <T> String[] mapToString(T[] array, Function<T, String> mapper) {
        String[] result = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = mapper.apply(array[i]);
        }
        return result;
    }

    //magic is here
    @SuppressWarnings("unchecked")
    public static <T, R> Class<T> getActualTypeParameter(Class<? extends R> thisClass, Class<R> declaringClass, int index) {
        Type type = thisClass.getGenericSuperclass();

        while (!(type instanceof ParameterizedType) || ((ParameterizedType) type).getRawType() != declaringClass) {
            if (type instanceof ParameterizedType) {
                type = ((Class<?>) ((ParameterizedType) type).getRawType()).getGenericSuperclass();
            } else {
                type = ((Class<?>) type).getGenericSuperclass();
            }
        }
        return (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[index];
    }

    /**
     * Exists because for stack equality checks actual ItemStack.itemDamage
     * field is used, and ItemStack.getItemDamage() can be overriden,
     * giving incorrect results for itemstack equality comparisons,
     * which still use raw ItemStack.itemDamage field
     *
     * @return actual value of ItemStack.itemDamage field
     */
    public static int getActualItemDamageFromStack(ItemStack itemStack) {
        return Items.FEATHER.getDamage(itemStack);
    }

    public static boolean harvestBlock(World world, BlockPos pos, EntityPlayer player) {
        IBlockState blockState = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);

        if (blockState.getBlock().isAir(blockState, world, pos)) {
            return false;
        }

        if (!blockState.getBlock().canHarvestBlock(world, pos, player)) {
            return false;
        }

        int expToDrop = 0;
        if (!world.isRemote) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            expToDrop = ForgeHooks.onBlockBreakEvent(world, playerMP.interactionManager.getGameType(), playerMP, pos);
            if (expToDrop == -1) {
                //notify client if block can't be removed because of BreakEvent cancelled on server side
                playerMP.connection.sendPacket(new SPacketBlockChange(world, pos));
                return false;
            }
        }

        world.playEvent(player, 2001, pos, Block.getStateId(blockState));

        boolean wasRemovedByPlayer = blockState.getBlock().removedByPlayer(blockState, world, pos, player, !player.capabilities.isCreativeMode);
        if (wasRemovedByPlayer) {
            blockState.getBlock().onPlayerDestroy(world, pos, blockState);

            if (!world.isRemote && !player.capabilities.isCreativeMode) {
                ItemStack stackInHand = player.getHeldItemMainhand();
                blockState.getBlock().harvestBlock(world, player, pos, blockState, tileEntity, stackInHand);
                if (expToDrop > 0) {
                    blockState.getBlock().dropXpOnBlockBreak(world, pos, expToDrop);
                }
            }
        }

        if (!world.isRemote) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            playerMP.connection.sendPacket(new SPacketBlockChange(world, pos));
        } else {
            Minecraft mc = Minecraft.getMinecraft();
            NetHandlerPlayClient connection = mc.getConnection();
            if (connection != null) {
                connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, mc.objectMouseOver.sideHit));
            }
        }
        return wasRemovedByPlayer;
    }

    public static BiomeDictionary.Type getBiomeTypeTagByName(String name) {
        Map<String, BiomeDictionary.Type> byName = ReflectionHelper.getPrivateValue(BiomeDictionary.Type.class, null, "byName");
        return byName.get(name);
    }

    public static List<Tuple<ItemStack, Integer>> getGrassSeedEntries() {
        ArrayList<Tuple<ItemStack, Integer>> result = new ArrayList<>();
        try {
            Field seedListField = ForgeHooks.class.getDeclaredField("seedList");
            seedListField.setAccessible(true);
            Class<?> seedEntryClass = Class.forName("net.minecraftforge.common.ForgeHooks$SeedEntry");
            Field seedField = seedEntryClass.getDeclaredField("seed");
            seedField.setAccessible(true);
            List<WeightedRandom.Item> seedList = (List<WeightedRandom.Item>) seedListField.get(null);
            for (WeightedRandom.Item seedEntryObject : seedList) {
                ItemStack seedStack = (ItemStack) seedField.get(seedEntryObject);
                int chanceValue = seedEntryObject.itemWeight;
                if (!seedStack.isEmpty())
                    result.add(new Tuple<>(seedStack, chanceValue));
            }
        } catch (ReflectiveOperationException exception) {
            GTLog.logger.error("Failed to get forge grass seed list", exception);
        }
        return result;
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
        List<IFluidTank> backedList = fluidInputs.getFluidTanks();
        return new AbstractList<FluidStack>() {
            @Override
            public FluidStack set(int index, FluidStack element) {
                IFluidTank fluidTank = backedList.get(index);
                FluidStack oldStack = fluidTank.getFluid();
                if (!(fluidTank instanceof FluidTank))
                    return oldStack;
                ((FluidTank) backedList.get(index)).setFluid(element);
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

    public static List<EntityPlayerMP> findPlayersUsing(MetaTileEntity metaTileEntity, double radius) {
        ArrayList<EntityPlayerMP> result = new ArrayList<>();
        AxisAlignedBB box = new AxisAlignedBB(metaTileEntity.getPos())
                .expand(radius, radius, radius)
                .expand(-radius, -radius, -radius);
        List<EntityPlayerMP> entities = metaTileEntity.getWorld().getEntitiesWithinAABB(EntityPlayerMP.class, box);
        for (EntityPlayerMP player : entities) {
            if (player.openContainer instanceof ModularUIContainer) {
                ModularUI modularUI = ((ModularUIContainer) player.openContainer).getModularUI();
                if (modularUI.holder instanceof IGregTechTileEntity &&
                        ((IGregTechTileEntity) modularUI.holder).getMetaTileEntity() == metaTileEntity) {
                    result.add(player);
                }
            }
        }
        return result;
    }

    public static <T> boolean iterableContains(Iterable<T> list, Predicate<T> predicate) {
        for (T t : list) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static int amountOfNonNullElements(List<?> collection) {
        int amount = 0;
        for (Object object : collection) {
            if (object != null) amount++;
        }
        return amount;
    }

    public static int amountOfNonEmptyStacks(List<ItemStack> collection) {
        int amount = 0;
        for (ItemStack object : collection) {
            if (object != null && !object.isEmpty()) amount++;
        }
        return amount;
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

    public static ItemStack copy(ItemStack... stacks) {
        for (ItemStack stack : stacks)
            if (!stack.isEmpty()) return stack.copy();
        return ItemStack.EMPTY;
    }

    public static ItemStack copyAmount(int amount, @Nonnull ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack copy = stack.copy();
        if (amount > 64) amount = 64;
        else if (amount == -1) amount = 111;
        else if (amount < 0) amount = 0;
        copy.setCount(amount);
        return copy;
    }

    public static ItemStack copyAmount(int amount, ItemStack... stacks) {
        ItemStack stack = copy(stacks);
        return copyAmount(amount, stack);
    }

    public static FluidStack copyAmount(int amount, FluidStack fluidStack) {
        if (fluidStack == null) return null;
        FluidStack stack = fluidStack.copy();
        stack.amount = amount;
        return stack;
    }

    public static <T extends Comparable<T>> IBlockState[] getAllPropertyValues(IBlockState blockState, IProperty<T> property) {
        Collection<T> allowedValues = property.getAllowedValues();
        IBlockState[] resultArray = new IBlockState[allowedValues.size()];
        int index = 0;
        for (T propertyValue : allowedValues) {
            resultArray[index++] = blockState.withProperty(property, propertyValue);
        }
        return resultArray;
    }

    public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
        return Collector.of(ImmutableList::builder, Builder::add,
                (b1, b2) -> {
                    b1.addAll(b2.build());
                    return b2;
                },
                ImmutableList.Builder<T>::build);
    }

    public static <M> M selectItemInList(int index, M replacement, List<M> list, Class<M> minClass) {
        if (list.isEmpty())
            return replacement;

        M maybeResult;
        if (list.size() <= index) {
            maybeResult = list.get(list.size() - 1);
        } else if (index < 0) {
            maybeResult = list.get(0);
        } else maybeResult = list.get(index);

        if (maybeResult != null) return maybeResult;
        return replacement;
    }

    public static <M> M getItem(List<? extends M> list, int index, M replacement) {
        if (index >= 0 && index < list.size())
            return list.get(index);
        return replacement;
    }

    // TODO, Remove this, use ItemStackHashStrategy instead
    public static Comparator<ItemStack> createItemStackComparator() {
        return Comparator.<ItemStack, Integer>comparing(it -> Item.REGISTRY.getIDForObject(it.getItem()))
                .thenComparing(ItemStack::getItemDamage)
                .thenComparing(ItemStack::hasTagCompound)
                .thenComparing(it -> -Objects.hashCode(it.getTagCompound()))
                .thenComparing(it -> -it.getCount());
    }

    public static boolean arePosEqual(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() & pos1.getY() == pos2.getY() & pos1.getZ() == pos2.getZ();
    }

    public static boolean isCoverBehaviorItem(ItemStack itemStack) {
        return isCoverBehaviorItem(itemStack, null, null);
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

    public static int getDecompositionReductionRatio(FluidStack fluidInput, FluidStack fluidOutput, ItemStack input, ItemStack output) {
        int[] divisors = new int[]{2, 5, 10, 25, 50};
        int ratio = -1;

        for (int divisor : divisors) {

            if (!(isFluidStackAmountDivisible(fluidInput, divisor)))
                continue;

            if (!(isFluidStackAmountDivisible(fluidOutput, divisor)))
                continue;

            if (input != null && !(GTUtility.isItemStackCountDivisible(input, divisor)))
                continue;

            if (output != null && !(GTUtility.isItemStackCountDivisible(output, divisor)))
                continue;

            ratio = divisor;
        }

        return Math.max(1, ratio);
    }

    public static boolean isFluidStackAmountDivisible(FluidStack fluidStack, int divisor) {
        return fluidStack.amount % divisor == 0 && fluidStack.amount % divisor != fluidStack.amount && fluidStack.amount / divisor != 0;
    }

    public static boolean isItemStackCountDivisible(ItemStack itemStack, int divisor) {
        return itemStack.getCount() % divisor == 0 && itemStack.getCount() % divisor != itemStack.getCount() && itemStack.getCount() / divisor != 0;
    }

    public static AxisAlignedBB rotateAroundYAxis(AxisAlignedBB aabb, EnumFacing from, EnumFacing to) {
        if (from == EnumFacing.UP || from == EnumFacing.DOWN || to == EnumFacing.UP || to == EnumFacing.DOWN)
            throw new IllegalArgumentException("Either the second or third parameters were EnumFacing.DOWN or EnumFacing.UP.");
        AxisAlignedBB rotatedAABB = new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
        while (from != to) {
            from = from.rotateY();
            rotatedAABB = new AxisAlignedBB(1 - rotatedAABB.maxZ, rotatedAABB.minY, rotatedAABB.minX, 1 - rotatedAABB.minZ, rotatedAABB.maxY, rotatedAABB.maxX);
        }
        return rotatedAABB;
    }

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
     * @param world the {@link World} to get the average tick time of
     * @return the mean tick time
     */
    public static double getMeanTickTime(@Nonnull World world) {
        return GTMathUtil.mean(Objects.requireNonNull(world.getMinecraftServer()).tickTimeArray) * 1.0E-6D;
    }

    /**
     * Attempts to find a passed in RecipeMap unlocalized name in a list of names
     *
     * @param unlocalizedName The unlocalized name of a RecipeMap
     * @return {@code true} If the RecipeMap is in the config blacklist
     */
    public static boolean findMachineInBlacklist(String unlocalizedName, String[] recipeMapBlacklist) {
        return Arrays.asList(recipeMapBlacklist).contains(unlocalizedName);
    }


    public static String formatNumbers(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(double number) {
        return NUMBER_FORMAT.format(number);
    }

    @Nonnull
    public static String formatNumber2Places(float number) {
        return TWO_PLACES_FORMAT.format(number);
    }

    /**
     * If pos of this world loaded
     */
    public static boolean isPosChunkLoaded(World world, BlockPos pos) {
        return !world.getChunkProvider().provideChunk(pos.getX() >> 4, pos.getZ() >> 4).isEmpty();
    }

    // TODO, move to machine utils?
    public static MetaTileEntity getMetaTileEntity(IBlockAccess world, BlockPos pos) {
        if (world == null || pos == null) return null;
        TileEntity te = world.getTileEntity(pos);
        return te instanceof IGregTechTileEntity ? ((IGregTechTileEntity) te).getMetaTileEntity() : null;
    }

    public static MetaTileEntity getMetaTileEntity(ItemStack stack) {
        if (!(stack.getItem() instanceof MachineItemBlock)) return null;
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

    /**
     * Gather a list of all registered dimensions. Done as a Supplier so that it can be called at any time and catch
     * dimensions that are registered late
     *
     * @param filter An Optional filter to restrict the returned dimensions
     * @return A Supplier containing a list of all registered dimensions
     */
    public static Supplier<List<Integer>> getAllRegisteredDimensions(@Nullable Predicate<WorldProvider> filter) {
        List<Integer> dims = new ArrayList<>();

        Map<DimensionType, IntSortedSet> dimMap = DimensionManager.getRegisteredDimensions();
        dimMap.values().stream()
                .flatMapToInt(s -> Arrays.stream(s.toIntArray()))
                .filter(num -> filter == null || filter.test(DimensionManager.createProviderFor(num)))
                .forEach(dims::add);

        return () -> dims;
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

    @Nonnull
    public static String convertUnderscoreToSpace(@Nonnull CharSequence sequence) {
        return UNDERSCORE_TO_SPACE.matcher(sequence).replaceAll(" ");
    }

    @Nonnull
    public static Pattern getForwardNewLineRegex() {
        return NEW_LINE_PATTERN;
    }

    /**
     * @param stack the stack to retrieve from
     * @return all the sub-items of an ItemStack
     */
    @Nonnull
    public static Set<ItemStack> getAllSubItems(@Nonnull ItemStack stack) {
        //match subtypes only on wildcard damage value items
        if (stack.getItemDamage() != GTValues.W) return Collections.singleton(stack);

        Set<ItemStack> set = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingItemDamageCount());
        for (CreativeTabs tab : stack.getItem().getCreativeTabs()) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            stack.getItem().getSubItems(tab, subItems);
            set.addAll(subItems);
        }
        return set;
    }
}
