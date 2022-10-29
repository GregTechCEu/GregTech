package gregtech.api.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.items.IToolItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.ToolMetaItem;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleGeneratorMetaTileEntity;
import gregtech.api.metatileentity.WorkableTieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.ConfigHolder;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import gregtech.common.items.behaviors.CrowbarBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static gregtech.api.GTValues.V;

public class GTUtility {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private static TreeMap<Integer, String> romanNumeralConversions = new TreeMap<>();

    private static final NavigableMap<Long, Byte> tierByVoltage = new TreeMap<>();

    static {
        for (int i = 0; i < V.length; i++) {
            tierByVoltage.put(V[i], (byte) i);
        }
    }

    public static Runnable combine(Runnable... runnables) {
        return () -> {
            for (Runnable runnable : runnables) {
                if (runnable != null)
                    runnable.run();
            }
        };
    }

    public static Stream<Object> flatten(Object[] array) {
        return Arrays.stream(array).flatMap(o -> o instanceof Object[] ? flatten((Object[]) o) : Stream.of(o));
    }

    public static void copyInventoryItems(IItemHandler src, IItemHandlerModifiable dest, boolean fixTools) {
        for (int i = 0; i < src.getSlots(); i++) {
            ItemStack itemStack = src.getStackInSlot(i);
            if (fixTools && itemStack.getItem() instanceof ToolMetaItem) {
                ItemStack toolStack = itemStack.copy();
                NBTTagCompound toolStats = toolStack.getTagCompound().getCompoundTag("GT.ToolStats");
                toolStats.setInteger("Dmg", 0);
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setTag("GT.ToolStats", toolStats);
                toolStack.setTagCompound(itemTag);
                dest.setStackInSlot(i, toolStack);
            } else
                dest.setStackInSlot(i, itemStack.isEmpty() ? ItemStack.EMPTY : itemStack.copy());
        }
    }

    public static <T> IntStream indices(T[] array) {
        int[] indices = new int[array.length];
        for (int i = 0; i < indices.length; i++)
            indices[i] = i;
        return Arrays.stream(indices);
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

    public static PotionEffect copyPotionEffect(PotionEffect sample) {
        PotionEffect potionEffect = new PotionEffect(sample.getPotion(), sample.getDuration(), sample.getAmplifier(), sample.getIsAmbient(), sample.doesShowParticles());
        potionEffect.setCurativeItems(sample.getCurativeItems());
        return potionEffect;
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

    public static int convertOpaqueRGBA_MCtoRGB(int alphaColor) {
        return alphaColor & 0xFFFFFF;
    }

    public static void setItem(ItemStack itemStack, ItemStack newStack) {
        try {
            Field itemField = Arrays.stream(ItemStack.class.getDeclaredFields())
                    .filter(field -> field.getType() == Item.class)
                    .findFirst().orElseThrow(ReflectiveOperationException::new);
            itemField.setAccessible(true);
            //replace item field instance
            itemField.set(itemStack, newStack.getItem());
            //set damage then
            itemStack.setItemDamage(newStack.getItemDamage());
            itemStack.setTagCompound(newStack.getTagCompound());

            Method forgeInit = ItemStack.class.getDeclaredMethod("forgeInit");
            forgeInit.setAccessible(true);
            //reinitialize forge capabilities and delegate reference
            forgeInit.invoke(itemStack);
        } catch (ReflectiveOperationException exception) {
            //should be impossible, actually
            throw new RuntimeException(exception);
        }
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

    /**
     * Attempts to merge given ItemStack with ItemStacks in list supplied
     * growing up to their max stack size
     *
     * @param stackToAdd item stack to merge.
     * @return a list of stacks, with optimized stack sizes
     */

    public static List<ItemStack> addStackToItemStackList(ItemStack stackToAdd, List<ItemStack> itemStackList) {
        if (!itemStackList.isEmpty()) {
            for (int i = 0; i < itemStackList.size(); i++) {
                ItemStack stackInList = itemStackList.get(i);
                if (ItemStackHashStrategy.comparingAllButCount().equals(stackInList, stackToAdd)) {
                    if (stackInList.getCount() < stackInList.getMaxStackSize()) {
                        int insertable = stackInList.getMaxStackSize() - stackInList.getCount();
                        if (insertable >= stackToAdd.getCount()) {
                            stackInList.grow(stackToAdd.getCount());
                            stackToAdd = ItemStack.EMPTY;
                        } else {
                            stackInList.grow(insertable);
                            stackToAdd = stackToAdd.copy();
                            stackToAdd.setCount(stackToAdd.getCount() - insertable);
                        }
                        if (stackToAdd.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            if (!stackToAdd.isEmpty()) {
                itemStackList.add(stackToAdd);
            }
        } else {
            itemStackList.add(stackToAdd.copy());
        }
        return itemStackList;
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
            mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, mc.objectMouseOver.sideHit));
        }
        return wasRemovedByPlayer;
    }

    /**
     * Applies specific amount of damage to item, either to durable items (which implement IDamagableItem)
     * or to electric items, which have capability IElectricItem
     * Damage amount is equal to EU amount used for electric items
     *
     * @return if damage was applied successfully
     */
    //TODO get rid of that
    public static boolean doDamageItem(ItemStack itemStack, int vanillaDamage, boolean simulate) {
        Item item = itemStack.getItem();
        if (item instanceof IToolItem) {
            //if item implements IDamagableItem, it manages it's own durability itself
            IToolItem damagableItem = (IToolItem) item;
            return damagableItem.damageItem(itemStack, null, vanillaDamage, simulate);

        } else if (itemStack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            //if we're using electric item, use default energy multiplier for textures
            IElectricItem capability = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            int energyNeeded = vanillaDamage * ConfigHolder.machines.energyUsageMultiplier;
            //noinspection ConstantConditions
            return capability.discharge(energyNeeded, Integer.MAX_VALUE, true, false, simulate) == energyNeeded;

        } else if (itemStack.isItemStackDamageable()) {
            if (!simulate && itemStack.attemptDamageItem(vanillaDamage, new Random(), null)) {
                //if we can't accept more damage, just shrink stack and mark it as broken
                //actually we would play broken animation here, but we don't have an entity who holds item
                itemStack.shrink(1);
            }
            return true;
        }
        return false;
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

    public static boolean isBetweenInclusive(long start, long end, long value) {
        return start <= value && value <= end;
    }

    /**
     * Capitalizes string, making first letter upper case
     *
     * @return capitalized string
     */
    public static String capitalizeString(String string) {
        if (string != null && string.length() > 0)
            return string.substring(0, 1).toUpperCase() + string.substring(1);
        return "";
    }

    /**
     * @return lowest tier that can handle passed voltage
     */
    public static byte getTierByVoltage(long voltage) {
        if (voltage > V[GTValues.MAX]) return GTValues.MAX;
        return tierByVoltage.ceilingEntry(voltage).getValue();
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

    public static ItemStack copy(ItemStack... stacks) {
        for (ItemStack stack : stacks)
            if (!stack.isEmpty()) return stack.copy();
        return ItemStack.EMPTY;
    }

    public static ItemStack copyAmount(int amount, ItemStack... stacks) {
        ItemStack stack = copy(stacks);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (amount > 64) amount = 64;
        else if (amount == -1) amount = 111;
        else if (amount < 0) amount = 0;
        stack.setCount(amount);
        return stack;
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

    public static boolean isCoverBehaviorItem(ItemStack itemStack, BooleanSupplier hasCoverSupplier, Function<CoverDefinition, Boolean> canPlaceCover) {
        if (itemStack.getItem() instanceof MetaItem) {
            MetaItem<?> metaItem = (MetaItem<?>) itemStack.getItem();
            MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(itemStack);
            if (valueItem != null) {
                List<IItemBehaviour> behaviourList = valueItem.getBehaviours();
                for (IItemBehaviour behaviour : behaviourList) {
                    if (behaviour instanceof CoverPlaceBehavior)
                        return canPlaceCover == null || canPlaceCover.apply(((CoverPlaceBehavior) behaviour).coverDefinition);
                    if (behaviour instanceof CrowbarBehaviour)
                        return hasCoverSupplier == null || hasCoverSupplier.getAsBoolean();
                }
            }
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

    public static String romanNumeralString(int num) {

        if (romanNumeralConversions.isEmpty()) { // Initialize on first run-through.
            romanNumeralConversions.put(1000, "M");
            romanNumeralConversions.put(900, "CM");
            romanNumeralConversions.put(500, "D");
            romanNumeralConversions.put(400, "CD");
            romanNumeralConversions.put(100, "C");
            romanNumeralConversions.put(90, "XC");
            romanNumeralConversions.put(50, "L");
            romanNumeralConversions.put(40, "XL");
            romanNumeralConversions.put(10, "X");
            romanNumeralConversions.put(9, "IX");
            romanNumeralConversions.put(5, "V");
            romanNumeralConversions.put(4, "IV");
            romanNumeralConversions.put(1, "I");
        }

        int conversion = romanNumeralConversions.floorKey(num);
        if (num == conversion) {
            return romanNumeralConversions.get(num);
        }
        return romanNumeralConversions.get(conversion) + romanNumeralString(num - conversion);
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
     * @param values to find the mean of
     * @return the mean value
     */
    public static long mean(@Nonnull long[] values) {
        if (values.length == 0L)
            return 0L;

        long sum = 0L;
        for (long v : values)
            sum += v;
        return sum / values.length;
    }

    /**
     * @param world the {@link World} to get the average tick time of
     * @return the mean tick time
     */
    public static double getMeanTickTime(@Nonnull World world) {
        return mean(Objects.requireNonNull(world.getMinecraftServer()).tickTimeArray) * 1.0E-6D;
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
        if (machine instanceof WorkableTieredMetaTileEntity && !(machine instanceof SimpleGeneratorMetaTileEntity))
            return !findMachineInBlacklist(machine.getRecipeMap().getUnlocalizedName(), recipeMapBlacklist);

        return false;
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

    public static String formatNumbers(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(double number) {
        return NUMBER_FORMAT.format(number);
    }

    /**
     * If pos of this world loaded
     */
    public static boolean isPosChunkLoaded(World world, BlockPos pos) {
        return !world.getChunkProvider().provideChunk(pos.getX() >> 4, pos.getZ() >> 4).isEmpty();
    }

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

            int distR = Math.abs(originalR - colorR);
            int distG = Math.abs(originalG - colorG);
            int distB = Math.abs(originalB - colorB);
            int dist = distR * distR + distG * distG + distB * distB;

            if (dist < distance) {
                distance = dist;
                color = mapColor;
            }
        }
        return color;
    }
}
