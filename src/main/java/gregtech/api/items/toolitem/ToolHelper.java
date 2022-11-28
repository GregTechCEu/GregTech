package gregtech.api.items.toolitem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.util.function.QuadFunction;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import gregtech.tools.enchants.EnchantmentHardHammer;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

/**
 * Collection of tool related helper methods
 */
public final class ToolHelper {

    public static final String TOOL_TAG_KEY = "GT.Tool";
    public static final String BEHAVIOURS_TAG_KEY = "GT.Behaviours";

    // Base item keys

    // Electric item keys
    public static final String MAX_CHARGE_KEY = "MaxCharge";
    public static final String CHARGE_KEY = "Charge";

    // Vanilla keys
    public static final String UNBREAKABLE_KEY = "Unbreakable";

    // Misc keys
    public static final String DISALLOW_CONTAINER_ITEM_KEY = "DisallowContainerItem";

    // Keys that resides in tool tag
    public static final String MATERIAL_KEY = "Material";
    public static final String DURABILITY_KEY = "Durability";
    public static final String MAX_DURABILITY_KEY = "MaxDurability";
    public static final String TOOL_SPEED_KEY = "ToolSpeed";
    public static final String ATTACK_DAMAGE_KEY = "AttackDamage";
    public static final String ENCHANTABILITY_KEY = "Enchantability";
    public static final String HARVEST_LEVEL_KEY = "HarvestLevel";
    public static final String LAST_CRAFTING_USE_KEY = "LastCraftingUse";

    // Keys that resides in behaviours tag

    // AoE
    public static final String MAX_AOE_COLUMN_KEY = "MaxAoEColumn";
    public static final String MAX_AOE_ROW_KEY = "MaxAoERow";
    public static final String MAX_AOE_LAYER_KEY = "MaxAoELayer";
    public static final String AOE_COLUMN_KEY = "AoEColumn";
    public static final String AOE_ROW_KEY = "AoERow";
    public static final String AOE_LAYER_KEY = "AoELayer";

    // Others
    public static final String HARVEST_ICE_KEY = "HarvestIce";
    public static final String TORCH_PLACING_KEY = "TorchPlacing";
    public static final String TORCH_PLACING_CACHE_SLOT_KEY = "TorchPlacing$Slot";
    public static final String TREE_FELLING_KEY = "TreeFelling";
    public static final String DISABLE_SHIELDS_KEY = "DisableShields";
    public static final String RELOCATE_MINED_BLOCKS_KEY = "RelocateMinedBlocks";

    // Crafting Symbols
    private static final BiMap<Character, IGTTool> symbols = HashBiMap.create();

    // Effective Vanilla Blocks
    public static final Set<Block> PICKAXE_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.DOUBLE_STONE_SLAB, Blocks.GOLDEN_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);
    public static final Set<Block> STONE_PICKAXE_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE);
    public static final Set<Block> IRON_PICKAXE_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE);
    public static final Set<Block> SHOVEL_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SNOW_LAYER, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.CONCRETE_POWDER);
    public static final Set<Block> AXE_HARVESTABLE_BLOCKS = ImmutableSet.of(Blocks.PLANKS, Blocks.BOOKSHELF, Blocks.LOG, Blocks.LOG2, Blocks.CHEST, Blocks.PUMPKIN, Blocks.LIT_PUMPKIN, Blocks.MELON_BLOCK, Blocks.LADDER, Blocks.WOODEN_BUTTON, Blocks.WOODEN_PRESSURE_PLATE);

    // Suppliers for broken tool stacks
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_LV = () -> MetaItems.POWER_UNIT_LV.getStackForm();
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_MV = () -> MetaItems.POWER_UNIT_MV.getStackForm();
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_HV = () -> MetaItems.POWER_UNIT_HV.getStackForm();
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_EV = () -> MetaItems.POWER_UNIT_EV.getStackForm();
    public static final Supplier<ItemStack> SUPPLY_POWER_UNIT_IV = () -> MetaItems.POWER_UNIT_IV.getStackForm();

    /**
     * @return finds the registered crafting symbol with the tool
     */
    public static Character getSymbolFromTool(IGTTool tool) {
        return symbols.inverse().get(tool);
    }

    /**
     * @return finds the registered tool with the crafting symbol
     */
    public static IGTTool getToolFromSymbol(Character symbol) {
        return symbols.get(symbol);
    }

    /**
     * Registers the tool against a crafting symbol, this is used in {@link gregtech.api.recipes.ModHandler}
     */
    public static void registerToolSymbol(Character symbol, IGTTool tool) {
        symbols.put(symbol, tool);
    }

    public static NBTTagCompound getToolTag(ItemStack stack) {
        return stack.getOrCreateSubCompound(TOOL_TAG_KEY);
    }

    public static NBTTagCompound getBehavioursTag(ItemStack stack) {
        return stack.getOrCreateSubCompound(BEHAVIOURS_TAG_KEY);
    }

    public static ItemStack getAndSetToolData(IGTTool tool, Material material, int maxDurability, int harvestLevel, float toolSpeed, float attackDamage) {
        ItemStack stack = tool.getRaw();
        NBTTagCompound toolTag = getToolTag(stack);
        toolTag.setString(MATERIAL_KEY, material.toString());
        toolTag.setInteger(MAX_DURABILITY_KEY, maxDurability);
        toolTag.setInteger(HARVEST_LEVEL_KEY, harvestLevel);
        toolTag.setFloat(TOOL_SPEED_KEY, toolSpeed);
        toolTag.setFloat(ATTACK_DAMAGE_KEY, attackDamage);
        ToolProperty toolProperty = material.getProperty(PropertyKey.TOOL);
        if (toolProperty != null) {
            toolProperty.getEnchantments().forEach((enchantment, level) -> {
                if (stack.getItem().canApplyAtEnchantingTable(stack, enchantment)) {
                    stack.addEnchantment(enchantment, level);
                }
            });
        }
        return stack;
    }

    /**
     * Damages tools in a context where the tool had been used to craft something.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     */
    public static void damageItemWhenCrafting(ItemStack stack, EntityLivingBase entity) {
        int damage = 2;
        if (stack.getItem() instanceof IGTTool) {
            damage = ((IGTTool) stack.getItem()).getToolStats().getToolDamagePerCraft(stack);
        } else {
            if (OreDictUnifier.getOreDictionaryNames(stack).stream().anyMatch(s -> s.startsWith("craftingTool"))) {
                damage = 1;
            }
        }
        damageItem(stack, entity, damage);
    }

    /**
     * Damages tools appropriately.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool.
     *
     * This method only takes 1 durability off, it ignores the tool's effectiveness because of the lack of context.
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     */
    public static void damageItem(ItemStack stack, EntityLivingBase entity) {
        damageItem(stack, entity, 1);
    }

    /**
     * Damages tools appropriately.
     * This supports both vanilla-esque and GT tools in case it does get called on a vanilla-esque tool
     *
     * @param stack  stack to be damaged
     * @param entity entity that has damaged this stack
     * @param damage how much damage the stack will take
     */
    public static void damageItem(ItemStack stack, EntityLivingBase entity, int damage) {
        if (!(stack.getItem() instanceof IGTTool)) {
            stack.damageItem(damage, entity);
        } else {
            if (stack.getTagCompound() != null && stack.getTagCompound().getBoolean("Unbreakable")) {
                return;
            }
            IGTTool tool = (IGTTool) stack.getItem();
            if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode) {
                if (tool.isElectric()) {
                    int electricDamage = damage * ConfigHolder.machines.energyUsageMultiplier;
                    IElectricItem electricItem = stack.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
                    if (electricItem != null) {
                        electricItem.discharge(electricDamage, tool.getElectricTier(), true, false, false);
                        if (electricItem.getCharge() > 0 && entity.getRNG().nextInt(100) > ConfigHolder.tools.rngDamageElectricTools) {
                            return;
                        }
                    } else {
                        throw new IllegalStateException("Electric tool does not have an attached electric item capability.");
                    }
                }
                int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
                int negated = 0;
                for (int k = 0; unbreakingLevel > 0 && k < damage; k++) {
                    if (EnchantmentDurability.negateDamage(stack, unbreakingLevel, entity.getRNG())) {
                        negated++;
                    }
                }
                damage -= negated;
                if (damage <= 0) {
                    return;
                }
                int newDurability = stack.getItemDamage() + damage;
                if (entity instanceof EntityPlayerMP) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger((EntityPlayerMP) entity, stack, newDurability);
                }
                stack.setItemDamage(newDurability);
                if (newDurability > stack.getMaxDamage()) {
                    if (entity instanceof EntityPlayer) {
                        EntityPlayer entityplayer = (EntityPlayer) entity;
                        entityplayer.addStat(StatList.getObjectBreakStats(stack.getItem()));
                    }
                    entity.renderBrokenItemStack(stack);
                    stack.shrink(1);
                }
            }
        }
    }

    /**
     * @return if any of the specified tool classes exists in the tool
     */
    public static boolean isTool(ItemStack tool, String... toolClasses) {
        if (toolClasses.length == 1) {
            return tool.getItem().getToolClasses(tool).contains(toolClasses[0]);
        }
        for (String toolClass : tool.getItem().getToolClasses(tool)) {
            for (String specified : toolClasses) {
                if (toolClass.equals(specified)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return if all the specified tool classes exists in the tool
     */
    public static boolean areTools(ItemStack tool, String... toolClasses) {
        if (toolClasses.length == 1) {
            return tool.getItem().getToolClasses(tool).contains(toolClasses[0]);
        }
        return tool.getItem().getToolClasses(tool).containsAll(new ObjectArraySet<String>(toolClasses));
    }

    public static AoESymmetrical getMaxAoEDefinition(ItemStack stack) {
        return AoESymmetrical.readMax(getBehavioursTag(stack));
    }

    public static AoESymmetrical getAoEDefinition(ItemStack stack) {
        return AoESymmetrical.read(getBehavioursTag(stack), getMaxAoEDefinition(stack));
    }

    /**
     * AoE Block Breaking Routine.
     */
    public static boolean areaOfEffectBlockBreakRoutine(ItemStack stack, EntityPlayerMP player, float maximumHardness) {
        Set<BlockPos> harvestableBlocks = getHarvestableBlocks(stack, player);
        if (!harvestableBlocks.isEmpty()) {
            for (BlockPos pos : harvestableBlocks) {
                if (player.world.getBlockState(pos).getBlockHardness(player.world, pos) > maximumHardness) {
                    continue; // if mining a block takes longer than the center block, do not mine it
                }
                if (!breakBlockRoutine(player, stack, pos)) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    public static Set<BlockPos> iterateAoE(ItemStack stack, AoESymmetrical aoeDefinition, World world, EntityPlayer player, RayTraceResult rayTraceResult, QuadFunction<ItemStack, World, EntityPlayer, BlockPos, Boolean> function) {
        if (aoeDefinition != AoESymmetrical.none() && rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.sideHit != null) {
            int column = aoeDefinition.column;
            int row = aoeDefinition.row;
            int layer = aoeDefinition.layer;
            EnumFacing playerFacing = player.getHorizontalFacing();
            EnumFacing.Axis playerAxis = playerFacing.getAxis();
            EnumFacing.Axis sideHitAxis = rayTraceResult.sideHit.getAxis();
            EnumFacing.AxisDirection sideHitAxisDir = rayTraceResult.sideHit.getAxisDirection();
            Set<BlockPos> validPositions = new ObjectOpenHashSet<>();
            if (sideHitAxis.isVertical()) {
                boolean isX = playerAxis == EnumFacing.Axis.X;
                boolean isDown = sideHitAxisDir == EnumFacing.AxisDirection.NEGATIVE;
                for (int y = 0; y <= layer; y++) {
                    for (int x = isX ? -row : -column; x <= (isX ? row : column); x++) {
                        for (int z = isX ? -column : -row; z <= (isX ? column : row); z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = rayTraceResult.getBlockPos().add(x, isDown ? y : -y, z);
                                if (player.canPlayerEdit(pos.offset(rayTraceResult.sideHit), rayTraceResult.sideHit, stack)) {
                                    if (function.apply(stack, world, player, pos)) {
                                        validPositions.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                boolean isX = sideHitAxis == EnumFacing.Axis.X;
                boolean isNegative = sideHitAxisDir == EnumFacing.AxisDirection.NEGATIVE;
                for (int x = 0; x <= layer; x++) {
                    // Special case for any additional column > 1: https://i.imgur.com/Dvcx7Vg.png
                    // Same behaviour as the Flux Bore
                    for (int y = (row == 0 ? 0 : -1); y <= (row == 0 ? 0 : row * 2 - 1); y++) {
                        for (int z = -column; z <= column; z++) {
                            if (!(x == 0 && y == 0 && z == 0)) {
                                BlockPos pos = rayTraceResult.getBlockPos().add(isX ? (isNegative ? x : -x) : (isNegative ? z : -z), y, isX ? (isNegative ? z : -z) : (isNegative ? x : -x));
                                if (function.apply(stack, world, player, pos)) {
                                    validPositions.add(pos);
                                }
                            }
                        }
                    }
                }
            }
            return validPositions;
        }
        return Collections.emptySet();
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, AoESymmetrical aoeDefinition, World world, EntityPlayer player, RayTraceResult rayTraceResult) {
        return iterateAoE(stack, aoeDefinition, world, player, rayTraceResult, ToolHelper::isBlockAoEHarvestable);
    }

    private static boolean isBlockAoEHarvestable(ItemStack stack, World world, EntityPlayer player, BlockPos pos) {
        if (world.isAirBlock(pos)) return false;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockLiquid) return false;

        if (state.getBlock().getHarvestTool(state) == null && state.getBlock().isReplaceable(world, pos)) {
            return true;
        }

        if (stack.canHarvestBlock(state)) return true;
        String tool = state.getBlock().getHarvestTool(state);
        if (stack.isEmpty() || tool == null) return true;

        return stack.getItem().getHarvestLevel(stack, tool, player, state) >= state.getBlock().getHarvestLevel(state);
    }

    // encompasses all vanilla special case tool checks for harvesting
    public static boolean isToolEffective(IBlockState state, Set<String> toolClasses, int harvestLevel) {
        Block block = state.getBlock();
        if (toolClasses.contains(block.getHarvestTool(state))) return true;

        net.minecraft.block.material.Material material = state.getMaterial();
        if (toolClasses.contains(ToolClasses.PICKAXE)) {
            if (Blocks.OBSIDIAN == block && harvestLevel >= 3) return true;
            if (IRON_PICKAXE_HARVESTABLE_BLOCKS.contains(block) && harvestLevel >= 2) return true;
            if (STONE_PICKAXE_HARVESTABLE_BLOCKS.contains(block) && harvestLevel >= 1) return true;
            if (PICKAXE_HARVESTABLE_BLOCKS.contains(block)) return true;
            if (material == net.minecraft.block.material.Material.ROCK ||
                    material == net.minecraft.block.material.Material.IRON ||
                    material == net.minecraft.block.material.Material.ANVIL) return true;
        }
        if (toolClasses.contains(ToolClasses.SHOVEL)) {
            if (SHOVEL_HARVESTABLE_BLOCKS.contains(block)) return true;
            if (block == Blocks.SNOW_LAYER || block == Blocks.SNOW) return true;
        }
        if (toolClasses.contains(ToolClasses.AXE)) {
            if (AXE_HARVESTABLE_BLOCKS.contains(block)) return true;
            if (material == net.minecraft.block.material.Material.WOOD ||
                    material == net.minecraft.block.material.Material.PLANTS ||
                    material == net.minecraft.block.material.Material.VINE) return true;
        }
        if (toolClasses.contains(ToolClasses.SWORD)) {
            if (block instanceof BlockWeb) return true;
            if (material == net.minecraft.block.material.Material.PLANTS ||
                    material == net.minecraft.block.material.Material.VINE ||
                    material == net.minecraft.block.material.Material.CORAL ||
                    material == net.minecraft.block.material.Material.LEAVES ||
                    material == net.minecraft.block.material.Material.GOURD) return true;
        }
        if (toolClasses.contains(ToolClasses.SCYTHE)) {
            if (material == net.minecraft.block.material.Material.LEAVES ||
                    material == net.minecraft.block.material.Material.VINE ||
                    material == net.minecraft.block.material.Material.CACTUS ||
                    material == net.minecraft.block.material.Material.PLANTS) {
                return true;
            }
        }
        if (toolClasses.contains(ToolClasses.FILE)) {
            if (block instanceof BlockPane && material == net.minecraft.block.material.Material.IRON) {
                return true;
            }
        }
        if (toolClasses.contains(ToolClasses.CROWBAR)) {
            if (block instanceof BlockRailBase || material == net.minecraft.block.material.Material.CIRCUITS) {
                return true;
            }
        }
        return false;
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, World world, EntityPlayer player, RayTraceResult rayTraceResult) {
        return getHarvestableBlocks(stack, getAoEDefinition(stack), world, player, rayTraceResult);
    }

    public static Set<BlockPos> getHarvestableBlocks(ItemStack stack, EntityPlayer player) {
        AoESymmetrical aoeDefiniton = getAoEDefinition(stack);
        if (aoeDefiniton == AoESymmetrical.none()) {
            return Collections.emptySet();
        }
        Vec3d lookPos = player.getPositionEyes(1F);
        Vec3d rotation = player.getLook(1);
        Vec3d realLookPos = lookPos.add(rotation.x * 5, rotation.y * 5, rotation.z * 5);
        RayTraceResult rayTraceResult = player.world.rayTraceBlocks(lookPos, realLookPos);
        return getHarvestableBlocks(stack, aoeDefiniton, player.world, player, rayTraceResult);
    }

    /**
     * Tree Felling routine. Improved from GTI, GTCE, TiCon and other tree felling solutions:
     * - Works with weird Oak Trees (thanks to Syrcan for pointing out)
     * - Brought back tick-spread behaviour:
     *     - Tree-felling is validated in the same tick as the stem being broken
     *     - 1 block broken per tick, akin to chorus fruit
     * - Fix cheating durability loss
     * - Eliminates leaves as well as logs
     */
    public static void treeFellingRoutine(EntityPlayerMP player, ItemStack stack, BlockPos start) {
        IBlockState state = player.world.getBlockState(start);
        if (state.getBlock().isWood(player.world, start)) {
            TreeFellingListener.start(state, stack, start, player);
        }
    }

    /**
     * Applies Forge Hammer recipes to block broken, used for hammers or tools with hard hammer enchant applied.
     */
    public static void applyHammerDropConversion(ItemStack tool, IBlockState state, List<ItemStack> drops, int fortune, float dropChance, Random random) {
        if (tool.getItem().getToolClasses(tool).contains(ToolClasses.HARD_HAMMER) || EnchantmentHelper.getEnchantmentLevel(EnchantmentHardHammer.INSTANCE, tool) > 0) {
            ItemStack silktouchDrop = GTVisibilityHackBlock.getSilkTouchDrop(state.getBlock(), state);
            if (!silktouchDrop.isEmpty()) {
                // Stack lists can be immutable going into Recipe#matches barring no rewrites
                List<ItemStack> dropAsList = Collections.singletonList(silktouchDrop);
                // Search for forge hammer recipes from all drops individually (only LV or under)
                Recipe hammerRecipe = RecipeMaps.FORGE_HAMMER_RECIPES.findRecipe(GTValues.V[1], dropAsList, Collections.emptyList(), 0, false);
                if (hammerRecipe != null && hammerRecipe.matches(true, dropAsList, Collections.emptyList())) {
                    drops.clear();
                    OrePrefix prefix = OreDictUnifier.getPrefix(silktouchDrop);
                    if (prefix == null) {
                        for (ItemStack output : hammerRecipe.getOutputs()) {
                            if (dropChance == 1.0F || random.nextFloat() <= dropChance) {
                                drops.add(output.copy());
                            }
                        }
                    } else if (prefix.name.startsWith("ore")) {
                        for (ItemStack output : hammerRecipe.getOutputs()) {
                            if (dropChance == 1.0F || random.nextFloat() <= dropChance) {
                                // Only apply fortune on ore -> crushed forge hammer recipes
                                if (OreDictUnifier.getPrefix(output) == OrePrefix.crushed) {
                                    output = output.copy();
                                    output.grow(random.nextInt(fortune));
                                    drops.add(output);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean breakBlockRoutine(EntityPlayerMP player, ItemStack tool, BlockPos pos) {
        // This is *not* a vanilla/forge convention, Forge never added "shears" to ItemShear's tool classes.
        if (isTool(tool, ToolClasses.SHEARS) && shearBlockRoutine(player, tool, pos) == 0) {
            return false;
        }
        World world = player.world;
        int exp = ForgeHooks.onBlockBreakEvent(world, player.interactionManager.getGameType(), player, pos);
        if (exp == -1) {
            return false;
        }
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        TileEntity tile = world.getTileEntity(pos);
        if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !player.canUseCommandBlock()) {
            world.notifyBlockUpdate(pos, state, state, 3);
            return false;
        } else {
            world.playEvent(player, 2001, pos, Block.getStateId(state));
            boolean successful;
            if (player.isCreative()) {
                successful = removeBlockRoutine(state, world, player, pos, false);
                player.connection.sendPacket(new SPacketBlockChange(world, pos));
            } else {
                ItemStack copiedTool = tool.isEmpty() ? ItemStack.EMPTY : tool.copy();
                boolean canHarvest = block.canHarvestBlock(world, pos, player);
                if (!tool.isEmpty()) {
                    tool.onBlockDestroyed(world, state, pos, player);
                    if (tool.isEmpty()) {
                        ForgeEventFactory.onPlayerDestroyItem(player, copiedTool, EnumHand.MAIN_HAND);
                    }
                }
                successful = removeBlockRoutine(null, world, player, pos, canHarvest);
                if (successful && canHarvest) {
                    block.harvestBlock(world, player, pos, state, tile, copiedTool);
                }
            }
            if (!player.isCreative() && successful && exp > 0) {
                block.dropXpOnBlockBreak(world, pos, exp);
            }
            return successful;
        }
    }

    /**
     * Shearing a Block.
     * @return -1 if not shearable, otherwise return 0 or 1, 0 if tool is now broken.
     */
    public static int shearBlockRoutine(EntityPlayerMP player, ItemStack tool, BlockPos pos) {
        if (!player.isCreative()) {
            World world = player.world;
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof IShearable) {
                IShearable shearable = (IShearable) state.getBlock();
                if (shearable.isShearable(tool, world, pos)) {
                    List<ItemStack> shearedDrops = shearable.onSheared(tool, world, pos, EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool));
                    boolean relocateMinedBlocks = getBehavioursTag(tool).getBoolean(RELOCATE_MINED_BLOCKS_KEY);
                    Iterator<ItemStack> iter = shearedDrops.iterator();
                    while (iter.hasNext()) {
                        ItemStack stack = iter.next();
                        if (relocateMinedBlocks && player.addItemStackToInventory(stack)) {
                            iter.remove();
                        } else {
                            float f = 0.7F;
                            double xo = world.rand.nextFloat() * f + 0.15D;
                            double yo = world.rand.nextFloat() * f + 0.15D;
                            double zo = world.rand.nextFloat() * f + 0.15D;
                            EntityItem entityItem = new EntityItem(world, pos.getX() + xo, pos.getY() + yo, pos.getZ() + zo, stack);
                            entityItem.setDefaultPickupDelay();
                            player.world.spawnEntity(entityItem);
                        }
                    }
                    ToolHelper.damageItem(tool, player);
                    player.addStat(StatList.getBlockStats((Block) shearable));
                    player.world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
                    return tool.isEmpty() ? 0 : 1;
                }
            }
        }
        return -1;
    }

    public static boolean removeBlockRoutine(@Nullable IBlockState state, World world, EntityPlayerMP player, BlockPos pos, boolean canHarvest) {
        state = state == null ? world.getBlockState(pos) : state;
        boolean successful = state.getBlock().removedByPlayer(state, world, pos, player, canHarvest);
        if (successful) {
            state.getBlock().onPlayerDestroy(world, pos, state);
        }
        return successful;
    }

    private ToolHelper() { }
}
