package gregtech.api.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.items.IToolItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.common.ConfigHolder;
import gregtech.common.items.behaviors.CoverPlaceBehavior;
import gregtech.common.items.behaviors.CrowbarBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
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
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static gregtech.api.GTValues.V;

public class GTUtility {

    private static final XSTR random = new XSTR();

    public static Runnable combine(Runnable... runnables) {
        return () -> {
            for (Runnable runnable : runnables) {
                if (runnable != null)
                    runnable.run();
            }
        };
    }

    public static Stream<Object> flatten(Object[] array) {
        return Arrays.stream(array).flatMap(o -> o instanceof Object[] ? flatten((Object[]) o): Stream.of(o));
    }

    public static void copyInventoryItems(IItemHandler src, IItemHandlerModifiable dest) {
        for (int i = 0; i < src.getSlots(); i++) {
            ItemStack itemStack = src.getStackInSlot(i);
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
     * @param simulate if true, stack won't actually modify items in other slots
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
            if (amountToInsert == 0)
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

    public static boolean harvestBlock(World world, BlockPos pos, EntityPlayer player) {
        IBlockState blockState = world.getBlockState(pos);
        TileEntity tileEntity = world.getTileEntity(pos);

        if(blockState.getBlock().isAir(blockState, world, pos)) {
            return false;
        }

        if(!blockState.getBlock().canHarvestBlock(world, pos, player)) {
            return false;
        }

        int expToDrop = 0;
        if(!world.isRemote) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            expToDrop = ForgeHooks.onBlockBreakEvent(world, playerMP.interactionManager.getGameType(), playerMP, pos);
            if(expToDrop == -1) {
                //notify client if block can't be removed because of BreakEvent cancelled on server side
                playerMP.connection.sendPacket(new SPacketBlockChange(world, pos));
                return false;
            }
        }

        world.playEvent(player, 2001, pos, Block.getStateId(blockState));

        boolean wasRemovedByPlayer = blockState.getBlock().removedByPlayer(blockState, world, pos, player, !player.capabilities.isCreativeMode);
        if(wasRemovedByPlayer) {
            blockState.getBlock().onPlayerDestroy(world, pos, blockState);

            if(!world.isRemote && !player.capabilities.isCreativeMode) {
                ItemStack stackInHand = player.getHeldItemMainhand();
                blockState.getBlock().harvestBlock(world, player, pos, blockState, tileEntity, stackInHand);
                if(expToDrop > 0) {
                    blockState.getBlock().dropXpOnBlockBreak(world, pos, expToDrop);
                }
            }
        }

        if(!world.isRemote) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            playerMP.connection.sendPacket(new SPacketBlockChange(world, pos));
        } else {
            Minecraft mc = Minecraft.getMinecraft();
            mc.getConnection().sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, pos, mc.objectMouseOver.sideHit));
        }
        return wasRemovedByPlayer;
    }


    @SideOnly(Side.CLIENT)
    public static void drawCenteredSizedText(int x, int y, String string, int color, double sizeMultiplier) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int textWidth = fontRenderer.getStringWidth(string);
        int textHeight = fontRenderer.FONT_HEIGHT;
        GlStateManager.pushMatrix();
        GlStateManager.scale(sizeMultiplier, sizeMultiplier, 0.0);
        GlStateManager.translate(-textWidth * sizeMultiplier / 2.0, -textHeight * sizeMultiplier / 2.0, 0);
        fontRenderer.drawString(string, x, y, color);
        GlStateManager.popMatrix();
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
            return damagableItem.damageItem(itemStack, vanillaDamage, simulate);

        } else if (itemStack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null)) {
            //if we're using electric item, use default energy multiplier for textures
            IElectricItem capability = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            int energyNeeded = vanillaDamage * ConfigHolder.energyUsageMultiplier;
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
        byte tier = 0;
        while (++tier < V.length) {
            if (voltage == V[tier]) {
                return tier;
            } else if (voltage < V[tier]) {
                return (byte) Math.max(0, tier - 1);
            }
        }
        return (byte) Math.min(V.length -1, tier);
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
                if (modularUI.holder instanceof MetaTileEntityHolder &&
                    ((MetaTileEntityHolder) modularUI.holder).getMetaTileEntity() == metaTileEntity) {
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
        return compound == null ? new NBTTagCompound() : compound;
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
            (b1, b2) -> { b1.addAll(b2.build()); return b2; },
            ImmutableList.Builder<T>::build);
    }

    public static <M, E extends M> E selectItemInList(int index, E replacement, List<? extends M> list, Class<E> minClass) {
        if (list.isEmpty())
            return replacement;

        M maybeResult;
        if (list.size() <= index) {
            maybeResult = list.get(list.size() - 1);
        } else if (index < 0) {
            maybeResult = list.get(0);
        } else maybeResult = list.get(index);

        if (minClass.isAssignableFrom(maybeResult.getClass())) {
            return minClass.cast(maybeResult);
        }
        return replacement;
    }

    public static <M> M getItem(List<? extends M> list, int index, M replacement) {
        if (index >= 0 && index < list.size())
            return list.get(index);
        return replacement;
    }

    public static long createFlag(int id) {
        return 1L << id;
    }

    public static void doOvervoltageExplosion(MetaTileEntity metaTileEntity, long voltage) {
        BlockPos pos = metaTileEntity.getPos();
        metaTileEntity.getWorld().setBlockToAir(pos);
        if (!metaTileEntity.getWorld().isRemote) {
            double posX = pos.getX() + 0.5;
            double posY = pos.getY() + 0.5;
            double posZ = pos.getZ() + 0.5;
            ((WorldServer) metaTileEntity.getWorld()).spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX, posY, posZ,
                10, 0.2, 0.2, 0.2, 0.0);
            metaTileEntity.getWorld().createExplosion(null, posX, posY, posZ, getTierByVoltage(voltage), ConfigHolder.doExplosions);
        }
    }

    public static int getRedstonePower(World world, BlockPos blockPos, EnumFacing side) {
        BlockPos offsetPos = blockPos.offset(side);
        int worldPower = world.getRedstonePower(offsetPos, side);
        if (worldPower >= 15) {
            return worldPower;
        } else {
            IBlockState offsetState = world.getBlockState(offsetPos);
            if(offsetState.getBlock() instanceof BlockRedstoneWire) {
                int wirePower = offsetState.getValue(BlockRedstoneWire.POWER);
                return Math.max(worldPower, wirePower);
            }
            return worldPower;
        }
    }

    public static Comparator<ItemStack> createItemStackComparator() {
        return Comparator.<ItemStack, Integer>comparing(it -> Item.REGISTRY.getIDForObject(it.getItem()))
            .thenComparing(ItemStack::getItemDamage)
            .thenComparing(ItemStack::hasTagCompound)
            .thenComparing(it -> -Objects.hashCode(it.getTagCompound()))
            .thenComparing(it -> -it.getCount());
    }

    public static int getRandomIntXSTR(int bound) {
        return random.nextInt(bound);
    }

    public static RayTraceResult getBlockLookingAt(EntityPlayer player) {
        Vec3d pos2 = player.getPositionVector().add(0, player.getEyeHeight(), 0);
        RayTraceResult result = player.world.rayTraceBlocks(pos2, pos2.add(player.getLookVec().scale(12)), false, true, true);
        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK)
            return result;
        return null;
    }

    public static RayTraceResult getBlockLookingAt(EntityPlayer player, BlockPos exclude) {
        Vec3d pos2 = player.getPositionVector().add(0, player.getEyeHeight(), 0);
        RayTraceResult result = rayTraceBlocks(pos2, pos2.add(player.getLookVec().scale(12)), false, true, true, player.world, exclude);
        if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK)
            return result;
        return null;
    }

    public static RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock, World world, BlockPos ignore) {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int i = MathHelper.floor(vec32.x);
                int j = MathHelper.floor(vec32.y);
                int k = MathHelper.floor(vec32.z);
                int l = MathHelper.floor(vec31.x);
                int i1 = MathHelper.floor(vec31.y);
                int j1 = MathHelper.floor(vec31.z);
                BlockPos blockpos = new BlockPos(l, i1, j1);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid) && !arePosEqual(ignore, blockpos))
                    return iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

                RayTraceResult raytraceresult2 = null;
                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z))
                        return null;

                    if (l == i && i1 == j && j1 == k)
                        return returnLastUncollidableBlock ? raytraceresult2 : null;

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l)
                        d0 = (double)l + 1.0D;
                    else if (i < l)
                        d0 = (double)l + 0.0D;
                    else
                        flag2 = false;

                    if (j > i1)
                        d1 = (double)i1 + 1.0D;
                    else if (j < i1)
                        d1 = (double)i1 + 0.0D;
                    else
                        flag = false;

                    if (k > j1)
                        d2 = (double)j1 + 1.0D;
                    else if (k < j1)
                        d2 = (double)j1 + 0.0D;
                    else
                        flag1 = false;

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;

                    if (flag2)
                        d3 = (d0 - vec31.x) / d6;

                    if (flag)
                        d4 = (d1 - vec31.y) / d7;

                    if (flag1)
                        d5 = (d2 - vec31.z) / d8;

                    if (d3 == -0.0D)
                        d3 = -1.0E-4D;

                    if (d4 == -0.0D)
                        d4 = -1.0E-4D;

                    if (d5 == -0.0D)
                        d5 = -1.0E-4D;

                    EnumFacing enumfacing;

                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    IBlockState iblockstate1 = world.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();

                    if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB && !arePosEqual(blockpos, ignore)) {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                            return iblockstate1.collisionRayTrace(world, blockpos, vec31, vec32);
                        } else {
                            raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
                        }
                    }
                }
                return returnLastUncollidableBlock ? raytraceresult2 : null;
            } else return null;
        } else return null;
    }

    public static boolean arePosEqual(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() & pos1.getY() == pos2.getY() & pos1.getZ() == pos2.getZ();
    }

    public static boolean isCoverBehaviorItem(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaItem) {
            MetaItem<?> metaItem = (MetaItem<?>) itemStack.getItem();
            MetaItem<?>.MetaValueItem valueItem = metaItem.getItem(itemStack);
            if (valueItem != null) {
                List<IItemBehaviour> behaviourList = valueItem.getBehaviours();
                return behaviourList.stream().anyMatch(it ->
                        it instanceof CoverPlaceBehavior || it instanceof CrowbarBehaviour);
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
}
