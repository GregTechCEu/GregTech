package gregtech.api.items.metaitem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.capability.IThermalFluidHandlerItemStack;
import gregtech.api.capability.impl.CombinedCapabilityProvider;
import gregtech.api.capability.impl.ElectricItem;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.OreDictNames;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.*;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.ItemMaterialInfo;
import gregtech.api.util.LocalizationUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * MetaItem is item that can have up to Short.MAX_VALUE items inside one id.
 * These items even can be edible, have custom behaviours, be electric or act like fluid containers!
 * They can also have different burn time, plus be handheld, oredicted or invisible!
 * They also can be reactor components.
 * <p>
 * You can also extend this class and occupy some of it's MetaData, and just pass an meta offset in constructor, and everything will work properly.
 * <p>
 * Items are added in MetaItem via {@link #addItem(int, String)}. You will get {@link MetaValueItem} instance, which you can configure in builder-alike pattern:
 * {@code addItem(0, "test_item").addStats(new ElectricStats(10000, 1,  false)) }
 * This will add single-use (not rechargeable) LV battery with initial capacity 10000 EU
 */
public abstract class MetaItem<T extends MetaItem<?>.MetaValueItem> extends Item implements ItemUIFactory {

    private static final List<MetaItem<?>> META_ITEMS = new ArrayList<>();

    public static List<MetaItem<?>> getMetaItems() {
        return Collections.unmodifiableList(META_ITEMS);
    }

    protected final Short2ObjectMap<T> metaItems = new Short2ObjectAVLTreeMap<>();
    private final Map<String, T> names = new Object2ObjectOpenHashMap<>();
    protected final Short2ObjectMap<ModelResourceLocation> metaItemsModels = new Short2ObjectOpenHashMap<>();
    protected final Short2ObjectMap<ModelResourceLocation[]> specialItemsModels = new Short2ObjectOpenHashMap<>();
    protected static final ModelResourceLocation MISSING_LOCATION = new ModelResourceLocation("builtin/missing", "inventory");

    protected final short metaItemOffset;

    public MetaItem(short metaItemOffset) {
        setTranslationKey("meta_item");
        setHasSubtypes(true);
        this.metaItemOffset = metaItemOffset;
        META_ITEMS.add(this);
    }

    @SideOnly(Side.CLIENT)
    public void registerColor() {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(this::getColorForItemStack, this);
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (short itemMetaKey : metaItems.keySet()) {
            T metaValueItem = metaItems.get(itemMetaKey);
            int numberOfModels = metaValueItem.getModelAmount();
            if (numberOfModels > 1) {
                ModelResourceLocation[] resourceLocations = new ModelResourceLocation[numberOfModels];
                for (int i = 0; i < resourceLocations.length; i++) {
                    ResourceLocation resourceLocation = createItemModelPath(metaValueItem, "/" + (i + 1));
                    ModelBakery.registerItemVariants(this, resourceLocation);
                    resourceLocations[i] = new ModelResourceLocation(resourceLocation, "inventory");
                }
                specialItemsModels.put((short) (metaItemOffset + itemMetaKey), resourceLocations);
                continue;
            }
            ResourceLocation resourceLocation = createItemModelPath(metaValueItem, "");
            if (numberOfModels > 0) {
                ModelBakery.registerItemVariants(this, resourceLocation);
            }
            metaItemsModels.put((short) (metaItemOffset + itemMetaKey), new ModelResourceLocation(resourceLocation, "inventory"));
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerTextureMesh() {
        ModelLoader.setCustomMeshDefinition(this, itemStack -> {
            short itemDamage = formatRawItemDamage((short) itemStack.getItemDamage());
            if (specialItemsModels.containsKey(itemDamage)) {
                int modelIndex = getModelIndex(itemStack);
                return specialItemsModels.get(itemDamage)[modelIndex];
            }
            if (metaItemsModels.containsKey(itemDamage)) {
                return metaItemsModels.get(itemDamage);
            }
            return MISSING_LOCATION;
        });
    }

    public ResourceLocation createItemModelPath(T metaValueItem, String postfix) {
        return new ResourceLocation(GTValues.MODID, formatModelPath(metaValueItem) + postfix);
    }

    protected String formatModelPath(T metaValueItem) {
        return "metaitems/" + metaValueItem.unlocalizedName;
    }

    protected int getModelIndex(ItemStack itemStack) {
        T metaValueItem = getItem(itemStack);

        // Electric Items
        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            return (int) Math.min(((electricItem.getCharge() / (electricItem.getMaxCharge() * 1.0)) * 7), 7);
        }

        // Integrated (Config) Circuit
        if (metaValueItem != null) {
            return IntCircuitIngredient.getCircuitConfiguration(itemStack);
        }
        return 0;
    }

    @SideOnly(Side.CLIENT)
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null && metaValueItem.getColorProvider() != null)
            return metaValueItem.getColorProvider().getItemStackColor(stack, tintIndex);
        return 0xFFFFFF;
    }

    @Override
    public boolean showDurabilityBar(@Nonnull ItemStack stack) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null && metaValueItem.getDurabilityManager() != null) {
            return metaValueItem.getDurabilityManager().showsDurabilityBar(stack);
        }
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        return electricItem != null && (stack.getMaxStackSize() == 1 || electricItem.getCharge() > 0L);
    }

    @Override
    public double getDurabilityForDisplay(@Nonnull ItemStack stack) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null && metaValueItem.getDurabilityManager() != null) {
            return metaValueItem.getDurabilityManager().getDurabilityForDisplay(stack);
        }
        IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            return 1.0 - electricItem.getCharge() / (1.0 * electricItem.getMaxCharge());
        }
        return 0.0;
    }

    @Override
    public int getRGBDurabilityForDisplay(@Nonnull ItemStack stack) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null && metaValueItem.getDurabilityManager() != null) {
            return metaValueItem.getDurabilityManager().getRGBDurabilityForDisplay(stack);
        }
        return MathHelper.hsvToRGB(0.33f, 1.0f, 1.0f);
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public EnumRarity getRarity(@Nonnull ItemStack stack) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null && metaValueItem.getRarity() != null) return metaValueItem.getRarity();
        else return super.getRarity(stack);
    }

    protected abstract T constructMetaValueItem(short metaValue, String unlocalizedName);

    public final T addItem(int metaValue, String unlocalizedName) {
        Validate.inclusiveBetween(0, Short.MAX_VALUE - 1, metaValue + metaItemOffset, "MetaItem ID should be in range from 0 to Short.MAX_VALUE-1");
        T metaValueItem = constructMetaValueItem((short) metaValue, unlocalizedName);
        if (metaItems.containsKey((short) metaValue)) {
            T registeredItem = metaItems.get((short) metaValue);
            throw new IllegalArgumentException(String.format("MetaId %d is already occupied by item %s (requested by item %s)", metaValue, registeredItem.unlocalizedName, unlocalizedName));
        }
        metaItems.put((short) metaValue, metaValueItem);
        names.put(unlocalizedName, metaValueItem);
        return metaValueItem;
    }

    public final Collection<T> getAllItems() {
        return Collections.unmodifiableCollection(metaItems.values());
    }

    public final T getItem(short metaValue) {
        return metaItems.get(formatRawItemDamage(metaValue));
    }

    public final T getItem(String valueName) {
        return names.get(valueName);
    }

    public final T getItem(ItemStack itemStack) {
        return getItem((short) (itemStack.getItemDamage() - metaItemOffset));
    }

    protected short formatRawItemDamage(short metaValue) {
        return metaValue;
    }

    public void registerSubItems() {
    }

    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        T metaValueItem = getItem(stack);
        if (metaValueItem == null) {
            return null;
        }
        ArrayList<ICapabilityProvider> providers = new ArrayList<>();
        for (IItemComponent itemComponent : metaValueItem.getAllStats()) {
            if (itemComponent instanceof IItemCapabilityProvider) {
                IItemCapabilityProvider provider = (IItemCapabilityProvider) itemComponent;
                providers.add(provider.createProvider(stack));
            }
        }
        return new CombinedCapabilityProvider(providers);
    }

    //////////////////////////////////////////////////////////////////

    @Override
    public int getItemBurnTime(@Nonnull ItemStack itemStack) {
        T metaValueItem = getItem(itemStack);
        if (metaValueItem == null) {
            return super.getItemBurnTime(itemStack);
        }
        return metaValueItem.getBurnValue();
    }

    //////////////////////////////////////////////////////////////////
    //      Behaviours and Use Manager Implementation               //
    //////////////////////////////////////////////////////////////////

    private IItemUseManager getUseManager(ItemStack itemStack) {
        T metaValueItem = getItem(itemStack);
        if (metaValueItem == null) {
            return null;
        }
        return metaValueItem.getUseManager();
    }

    public List<IItemBehaviour> getBehaviours(ItemStack itemStack) {
        T metaValueItem = getItem(itemStack);
        if (metaValueItem == null) {
            return ImmutableList.of();
        }
        return metaValueItem.getBehaviours();
    }

    @Override
    public int getItemStackLimit(@Nonnull ItemStack stack) {
        T metaValueItem = getItem(stack);
        if (metaValueItem == null) {
            return 64;
        }
        return metaValueItem.getMaxStackSize(stack);
    }

    @Nonnull
    @Override
    public EnumAction getItemUseAction(@Nonnull ItemStack stack) {
        IItemUseManager useManager = getUseManager(stack);
        if (useManager != null) {
            return useManager.getUseAction(stack);
        }
        return EnumAction.NONE;
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack stack) {
        IItemUseManager useManager = getUseManager(stack);
        if (useManager != null) {
            return useManager.getMaxItemUseDuration(stack);
        }
        return 0;
    }

    @Override
    public void onUsingTick(@Nonnull ItemStack stack, @Nonnull EntityLivingBase player, int count) {
        if (player instanceof EntityPlayer) {
            IItemUseManager useManager = getUseManager(stack);
            if (useManager != null) {
                useManager.onItemUsingTick(stack, (EntityPlayer) player, count);
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase player, int timeLeft) {
        if (player instanceof EntityPlayer) {
            IItemUseManager useManager = getUseManager(stack);
            if (useManager != null) {
                useManager.onPlayerStoppedItemUsing(stack, (EntityPlayer) player, timeLeft);
            }
        }
    }

    @Nullable
    @Override
    public ItemStack onItemUseFinish(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase player) {
        if (player instanceof EntityPlayer) {
            IItemUseManager useManager = getUseManager(stack);
            if (useManager != null) {
                return useManager.onItemUseFinish(stack, (EntityPlayer) player);
            }
        }
        return stack;
    }

    @Override
    public boolean onLeftClickEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, @Nonnull Entity entity) {
        boolean returnValue = false;
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            if (behaviour.onLeftClickEntity(stack, player, entity)) {
                returnValue = true;
            }
        }
        return returnValue;
    }

    @Override
    public boolean itemInteractionForEntity(@Nonnull ItemStack stack, @Nonnull EntityPlayer playerIn, @Nonnull EntityLivingBase target, @Nonnull EnumHand hand) {
        boolean returnValue = false;
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            if (behaviour.itemInteractionForEntity(stack, playerIn, target, hand)) {
                returnValue = true;
            }
        }
        return returnValue;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, EntityPlayer player, @Nonnull EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        for (IItemBehaviour behaviour : getBehaviours(itemStack)) {
            ActionResult<ItemStack> behaviourResult = behaviour.onItemRightClick(world, player, hand);
            itemStack = behaviourResult.getResult();
            if (behaviourResult.getType() != EnumActionResult.PASS) {
                return ActionResult.newResult(behaviourResult.getType(), itemStack);
            } else if (itemStack.isEmpty()) {
                return ActionResult.newResult(EnumActionResult.PASS, ItemStack.EMPTY);
            }
        }
        IItemUseManager useManager = getUseManager(itemStack);
        if (useManager != null && useManager.canStartUsing(itemStack, player)) {
            useManager.onItemUseStart(itemStack, player);
            player.setActiveHand(hand);
            return ActionResult.newResult(EnumActionResult.SUCCESS, itemStack);
        }
        return ActionResult.newResult(EnumActionResult.PASS, itemStack);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        for (IItemBehaviour behaviour : getBehaviours(itemStack)) {
            EnumActionResult behaviourResult = behaviour.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
            if (behaviourResult != EnumActionResult.PASS) {
                return behaviourResult;
            } else if (itemStack.isEmpty()) {
                return EnumActionResult.PASS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        ItemStack originalStack = stack.copy();
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            ActionResult<ItemStack> behaviourResult = behaviour.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
            stack = behaviourResult.getResult();
            if (behaviourResult.getType() != EnumActionResult.PASS) {
                if (!ItemStack.areItemStacksEqual(originalStack, stack))
                    player.setHeldItem(hand, stack);
                return behaviourResult.getType();
            } else if (stack.isEmpty()) {
                player.setHeldItem(hand, ItemStack.EMPTY);
                return EnumActionResult.PASS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, @Nonnull ItemStack stack) {
        HashMultimap<String, AttributeModifier> modifiers = HashMultimap.create();
        T metaValueItem = getItem(stack);
        if (metaValueItem != null) {
            for (IItemBehaviour behaviour : getBehaviours(stack)) {
                modifiers.putAll(behaviour.getAttributeModifiers(slot, stack));
            }
        }
        return modifiers;
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null) {
            IEnchantabilityHelper helper = metaValueItem.getEnchantabilityHelper();
            return helper != null && helper.isEnchantable(stack);
        }
        return super.isEnchantable(stack);
    }

    @Override
    public int getItemEnchantability(@Nonnull ItemStack stack) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null) {
            IEnchantabilityHelper helper = metaValueItem.getEnchantabilityHelper();
            return helper == null ? 0 : helper.getItemEnchantability(stack);
        }
        return super.getItemEnchantability(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(@Nonnull ItemStack stack, @Nonnull Enchantment enchantment) {
        T metaValueItem = getItem(stack);
        if (metaValueItem != null) {
            IEnchantabilityHelper helper = metaValueItem.getEnchantabilityHelper();
            return helper != null && helper.canApplyAtEnchantingTable(stack, enchantment);
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void onUpdate(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected) {
        for (IItemBehaviour behaviour : getBehaviours(stack)) {
            behaviour.onUpdate(stack, entityIn);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
        //if item is equal, and old item has electric item capability, remove charge tags to stop reequip animation when charge is altered
        if (ItemStack.areItemsEqual(oldStack, newStack) && oldStack.hasCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null) &&
                oldStack.hasTagCompound() && newStack.hasTagCompound()) {
            oldStack = oldStack.copy();
            newStack = newStack.copy();
            oldStack.getTagCompound().removeTag("Charge");
            newStack.getTagCompound().removeTag("Charge");
            if (oldStack.getTagCompound().hasKey("terminal")) {
                oldStack.getTagCompound().getCompoundTag("terminal").getCompoundTag("_hw").removeTag("battery");
                newStack.getTagCompound().getCompoundTag("terminal").getCompoundTag("_hw").removeTag("battery");
            }
        }
        return !ItemStack.areItemStacksEqual(oldStack, newStack);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        T metaItem = getItem(stack);
        return metaItem == null ? getTranslationKey() : getTranslationKey() + "." + metaItem.unlocalizedName;
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.getItemDamage() >= metaItemOffset) {
            T item = getItem(stack);
            if (item == null) {
                return "invalid item";
            }
            String unlocalizedName = String.format("metaitem.%s.name", item.unlocalizedName);
            if (item.getNameProvider() != null) {
                return item.getNameProvider().getItemStackDisplayName(stack, unlocalizedName);
            }
            IFluidHandlerItem fluidHandlerItem = ItemHandlerHelper.copyStackWithSize(stack, 1)
                    .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandlerItem != null) {
                FluidStack fluidInside = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
                return LocalizationUtils.format(unlocalizedName, fluidInside == null ?
                        LocalizationUtils.format("metaitem.fluid_cell.empty") :
                        fluidInside.getLocalizedName());
            }
            return LocalizationUtils.format(unlocalizedName);
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemStack, @Nullable World worldIn, @Nonnull List<String> lines, @Nonnull ITooltipFlag tooltipFlag) {
        T item = getItem(itemStack);
        if (item == null) return;
        String unlocalizedTooltip = "metaitem." + item.unlocalizedName + ".tooltip";
        if (I18n.hasKey(unlocalizedTooltip)) {
            lines.addAll(Arrays.asList(I18n.format(unlocalizedTooltip).split("/n")));
        }

        IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
        if (electricItem != null) {
            if (electricItem.canProvideChargeExternally()) {
                addDischargeItemTooltip(lines, electricItem.getMaxCharge(), electricItem.getCharge(), electricItem.getTier());
            } else {
                lines.add(I18n.format("metaitem.generic.electric_item.tooltip",
                        electricItem.getCharge(),
                        electricItem.getMaxCharge(),
                        GTValues.VNF[electricItem.getTier()]));
            }
        }

        IFluidHandlerItem fluidHandler = ItemHandlerHelper.copyStackWithSize(itemStack, 1)
                .getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (fluidHandler != null) {
            IFluidTankProperties fluidTankProperties = fluidHandler.getTankProperties()[0];
            FluidStack fluid = fluidTankProperties.getContents();

            lines.add(I18n.format("metaitem.generic.fluid_container.tooltip",
                    fluid == null ? 0 : fluid.amount,
                    fluidTankProperties.getCapacity(),
                    fluid == null ? "" : fluid.getLocalizedName()));

            if (fluidHandler instanceof IThermalFluidHandlerItemStack) {
                IThermalFluidHandlerItemStack thermalFluidHandler = (IThermalFluidHandlerItemStack) fluidHandler;
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                    lines.add(I18n.format("gregtech.fluid_pipe.max_temperature", thermalFluidHandler.getMaxFluidTemperature()));
                    if (thermalFluidHandler.isGasProof()) lines.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
                    if (thermalFluidHandler.isAcidProof()) lines.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
                    if (thermalFluidHandler.isCryoProof()) lines.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
                    if (thermalFluidHandler.isPlasmaProof()) lines.add(I18n.format("gregtech.fluid_pipe.plasma_proof"));
                } else if (thermalFluidHandler.isGasProof() || thermalFluidHandler.isAcidProof() || thermalFluidHandler.isCryoProof() || thermalFluidHandler.isPlasmaProof()) {
                    lines.add(I18n.format("gregtech.tooltip.fluid_pipe_hold_shift"));
                }
            }
        }

        for (IItemBehaviour behaviour : getBehaviours(itemStack)) {
            behaviour.addInformation(itemStack, lines);
        }

        if (tooltipFlag.isAdvanced()) {
            lines.add("MetaItem Id: " + item.unlocalizedName);
        }
    }

    private static void addDischargeItemTooltip(List<String> tooltip, long maxCharge, long currentCharge, int tier) {
        if (currentCharge == 0) { // do not display when empty
            tooltip.add(I18n.format("metaitem.generic.electric_item.tooltip", currentCharge, maxCharge, GTValues.VNF[tier]));
            return;
        }
        Instant start = Instant.now();
        Instant end = Instant.now().plusSeconds((long) ((currentCharge * 1.0) / GTValues.V[tier] / 20));
        Duration duration = Duration.between(start, end);
        double percentRemaining = currentCharge * 1.0 / maxCharge * 100; // used for color

        long timeRemaining;
        String unit;
        if (duration.getSeconds() <= 180) {
            timeRemaining = duration.getSeconds();
            unit = I18n.format("metaitem.battery.charge_unit.second");
        } else if (duration.toMinutes() <= 180) {
            timeRemaining = duration.toMinutes();
            unit = I18n.format("metaitem.battery.charge_unit.minute");
        } else {
            timeRemaining = duration.toHours();
            unit = I18n.format("metaitem.battery.charge_unit.hour");
        }
        tooltip.add(I18n.format("metaitem.battery.charge_detailed", currentCharge, maxCharge, GTValues.VNF[tier],
                percentRemaining < 30 ? 'c' : percentRemaining < 60 ? 'e' : 'a',
                timeRemaining, unit));
    }

    @Override
    public boolean hasContainerItem(@Nonnull ItemStack itemStack) {
        T item = getItem(itemStack);
        if (item == null) {
            return false;
        }
        return item.getContainerItemProvider() != null;
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack itemStack) {
        T item = getItem(itemStack);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        itemStack = itemStack.copy();
        itemStack.setCount(1);
        IItemContainerItemProvider provider = item.getContainerItemProvider();
        return provider == null ? ItemStack.EMPTY : provider.getContainerItem(itemStack);
    }

    @Nonnull
    @Override
    public CreativeTabs[] getCreativeTabs() {
        return new CreativeTabs[]{GregTechAPI.TAB_GREGTECH, GregTechAPI.TAB_GREGTECH_MATERIALS};
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> subItems) {
        if (tab != GregTechAPI.TAB_GREGTECH && tab != CreativeTabs.SEARCH) {
            return;
        }
        for (T enabledItem : metaItems.values()) {
            if (!enabledItem.isVisible())
                continue;
            ItemStack itemStack = enabledItem.getStackForm();
            enabledItem.getSubItemHandler().getSubItems(itemStack, tab, subItems);
        }
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        ItemStack itemStack = holder.getCurrentItem();
        T metaValueItem = getItem(itemStack);
        ItemUIFactory uiFactory = metaValueItem == null ? null : metaValueItem.getUIManager();
        return uiFactory == null ? null : uiFactory.createUI(holder, entityPlayer);
    }

    public class MetaValueItem {

        public MetaItem<T> getMetaItem() {
            return MetaItem.this;
        }

        public final int metaValue;

        public final String unlocalizedName;
        private IItemNameProvider nameProvider;
        private IItemMaxStackSizeProvider stackSizeProvider;
        private IItemContainerItemProvider containerItemProvider;
        private ISubItemHandler subItemHandler = DefaultSubItemHandler.INSTANCE;

        private final List<IItemComponent> allStats = new ArrayList<>();
        private final List<IItemBehaviour> behaviours = new ArrayList<>();
        private IItemUseManager useManager;
        private ItemUIFactory uiManager;
        private IItemColorProvider colorProvider;
        private IItemDurabilityManager durabilityManager;
        private IEnchantabilityHelper enchantabilityHelper;
        private EnumRarity rarity;

        private int burnValue = 0;
        private boolean visible = true;
        private int maxStackSize = 64;
        private int modelAmount = 1;

        protected MetaValueItem(int metaValue, String unlocalizedName) {
            this.metaValue = metaValue;
            this.unlocalizedName = unlocalizedName;
        }

        public MetaValueItem setMaterialInfo(ItemMaterialInfo materialInfo) {
            if (materialInfo == null) {
                throw new IllegalArgumentException("Cannot add null ItemMaterialInfo.");
            }
            OreDictUnifier.registerOre(getStackForm(), materialInfo);
            return this;
        }

        public MetaValueItem setUnificationData(OrePrefix prefix, @Nullable Material material) {
            if (prefix == null) {
                throw new IllegalArgumentException("Cannot add null OrePrefix.");
            }
            OreDictUnifier.registerOre(getStackForm(), prefix, material);
            return this;
        }

        public MetaValueItem addOreDict(String oreDictName) {
            if (oreDictName == null) {
                throw new IllegalArgumentException("Cannot add null OreDictName.");
            }
            OreDictionary.registerOre(oreDictName, getStackForm());
            return this;
        }

        public MetaValueItem addOreDict(OreDictNames oreDictName) {
            if (oreDictName == null) {
                throw new IllegalArgumentException("Cannot add null OreDictName.");
            }
            OreDictionary.registerOre(oreDictName.name(), getStackForm());
            return this;
        }

        public MetaValueItem setInvisible(boolean isVisible) {
            this.visible = isVisible;
            return this;
        }

        public MetaValueItem setInvisible() {
            this.visible = false;
            return this;
        }

        public MetaValueItem setMaxStackSize(int maxStackSize) {
            if (maxStackSize <= 0) {
                throw new IllegalArgumentException("Cannot set Max Stack Size to negative or zero value.");
            }
            this.maxStackSize = maxStackSize;
            return this;
        }

        public MetaValueItem setBurnValue(int burnValue) {
            if (burnValue <= 0) {
                throw new IllegalArgumentException("Cannot set Burn Value to negative or zero number.");
            }
            this.burnValue = burnValue;
            return this;
        }

        public MetaValueItem disableModelLoading() {
            this.modelAmount = 0;
            return this;
        }

        public MetaValueItem setModelAmount(int modelAmount) {
            if (modelAmount <= 0) {
                throw new IllegalArgumentException("Cannot set amount of models to negative or zero number.");
            }
            this.modelAmount = modelAmount;
            return this;
        }

        public MetaValueItem setRarity(EnumRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public MetaValueItem addComponents(IItemComponent... stats) {
            addItemComponentsInternal(stats);
            return this;
        }

        protected void addItemComponentsInternal(IItemComponent... stats) {
            for (IItemComponent itemComponent : stats) {
                if (itemComponent instanceof IItemNameProvider) {
                    this.nameProvider = (IItemNameProvider) itemComponent;
                }
                if (itemComponent instanceof IItemMaxStackSizeProvider) {
                    this.stackSizeProvider = (IItemMaxStackSizeProvider) itemComponent;
                }
                if (itemComponent instanceof ISubItemHandler) {
                    this.subItemHandler = (ISubItemHandler) itemComponent;
                }
                if (itemComponent instanceof IItemContainerItemProvider) {
                    this.containerItemProvider = (IItemContainerItemProvider) itemComponent;
                }
                if (itemComponent instanceof IItemDurabilityManager) {
                    this.durabilityManager = (IItemDurabilityManager) itemComponent;
                }
                if (itemComponent instanceof IItemUseManager) {
                    this.useManager = (IItemUseManager) itemComponent;
                }
                if (itemComponent instanceof IFoodBehavior) {
                    this.useManager = new FoodUseManager((IFoodBehavior) itemComponent);
                }
                if (itemComponent instanceof ItemUIFactory)
                    this.uiManager = (ItemUIFactory) itemComponent;

                if (itemComponent instanceof IItemColorProvider) {
                    this.colorProvider = (IItemColorProvider) itemComponent;
                }
                if (itemComponent instanceof IItemBehaviour) {
                    this.behaviours.add((IItemBehaviour) itemComponent);
                }
                if (itemComponent instanceof IEnchantabilityHelper) {
                    this.enchantabilityHelper = (IEnchantabilityHelper) itemComponent;
                }
                this.allStats.add(itemComponent);
            }
        }

        public int getMetaValue() {
            return metaValue;
        }

        public List<IItemComponent> getAllStats() {
            return Collections.unmodifiableList(allStats);
        }

        public List<IItemBehaviour> getBehaviours() {
            return Collections.unmodifiableList(behaviours);
        }

        public ISubItemHandler getSubItemHandler() {
            return subItemHandler;
        }

        @Nullable
        public IItemDurabilityManager getDurabilityManager() {
            return durabilityManager;
        }

        @Nullable
        public IItemUseManager getUseManager() {
            return useManager;
        }

        @Nullable
        public ItemUIFactory getUIManager() {
            return uiManager;
        }

        @Nullable
        public IItemColorProvider getColorProvider() {
            return colorProvider;
        }

        @Nullable
        public IItemNameProvider getNameProvider() {
            return nameProvider;
        }

        @Nullable
        public IItemContainerItemProvider getContainerItemProvider() {
            return containerItemProvider;
        }

        @Nullable
        public IEnchantabilityHelper getEnchantabilityHelper() {
            return enchantabilityHelper;
        }

        public int getBurnValue() {
            return burnValue;
        }

        public int getMaxStackSize(ItemStack stack) {
            return stackSizeProvider == null ? maxStackSize : stackSizeProvider.getMaxStackSize(stack, maxStackSize);
        }

        public boolean isVisible() {
            return visible;
        }

        public int getModelAmount() {
            return modelAmount;
        }

        public EnumRarity getRarity() {
            return rarity;
        }

        public ItemStack getStackForm(int amount) {
            return new ItemStack(MetaItem.this, amount, metaItemOffset + metaValue);
        }

        public boolean isItemEqual(ItemStack itemStack) {
            return itemStack.getItem() == MetaItem.this && itemStack.getItemDamage() == (metaItemOffset + metaValue);
        }

        public ItemStack getStackForm() {
            return getStackForm(1);
        }

        /**
         * Attempts to get an fully charged variant of this electric item
         *
         * @param chargeAmount amount of charge
         * @return charged electric item stack
         * @throws java.lang.IllegalStateException if this item is not electric item
         */
        public ItemStack getChargedStack(long chargeAmount) {
            ItemStack itemStack = getStackForm(1);
            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem == null) {
                throw new IllegalStateException("Not an electric item.");
            }
            electricItem.charge(chargeAmount, Integer.MAX_VALUE, true, false);
            return itemStack;
        }

        public ItemStack getInfiniteChargedStack() {
            ItemStack itemStack = getStackForm(1);
            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (!(electricItem instanceof ElectricItem)) {
                throw new IllegalStateException("Not a supported electric item.");
            }
            ((ElectricItem) electricItem).setInfiniteCharge(true);
            return itemStack;
        }

        /**
         * Attempts to get an electric item variant with override of max charge
         *
         * @param maxCharge new max charge of this electric item
         * @return item stack with given max charge
         * @throws java.lang.IllegalStateException if this item is not electric item or uses custom implementation
         */
        public ItemStack getMaxChargeOverrideStack(long maxCharge) {
            ItemStack itemStack = getStackForm(1);
            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem == null) {
                throw new IllegalStateException("Not an electric item.");
            }
            if (!(electricItem instanceof ElectricItem)) {
                throw new IllegalStateException("Only standard ElectricItem implementation supported, but this item uses " + electricItem.getClass());
            }
            ((ElectricItem) electricItem).setMaxChargeOverride(maxCharge);
            return itemStack;
        }

        public ItemStack getChargedStackWithOverride(IElectricItem source) {
            ItemStack itemStack = getStackForm(1);
            IElectricItem electricItem = itemStack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (electricItem == null) {
                throw new IllegalStateException("Not an electric item.");
            }
            if (!(electricItem instanceof ElectricItem)) {
                throw new IllegalStateException("Only standard ElectricItem implementation supported, but this item uses " + electricItem.getClass());
            }
            ((ElectricItem) electricItem).setMaxChargeOverride(source.getMaxCharge());
            long charge = source.discharge(Long.MAX_VALUE, Integer.MAX_VALUE, true, false, true);
            electricItem.charge(charge, Integer.MAX_VALUE, true, false);
            return itemStack;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("metaValue", metaValue)
                    .append("unlocalizedName", unlocalizedName)
                    .toString();
        }
    }

}
