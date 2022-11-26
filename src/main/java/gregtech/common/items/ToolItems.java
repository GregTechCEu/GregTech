package gregtech.common.items;

import codechicken.lib.vec.Vector3;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.ICoverable;
import gregtech.api.items.toolitem.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.sound.GTSounds;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TaskScheduler;
import gregtech.common.ConfigHolder;
import gregtech.common.items.tool.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class ToolItems {

    private static final List<IGTTool> TOOLS = new ArrayList<>();

    public static List<IGTTool> getAllTools() {
        return TOOLS;
    }

    public static IGTTool SWORD;
    public static IGTTool PICKAXE;
    public static IGTTool SHOVEL;
    public static IGTTool AXE;
    public static IGTTool HOE;
    public static IGTTool SAW;
    public static IGTTool HARD_HAMMER;
    public static IGTTool SOFT_MALLET;
    public static IGTTool MINING_HAMMER;
    public static IGTTool SPADE;
    public static IGTTool WRENCH;
    public static IGTTool FILE;
    public static IGTTool CROWBAR;
    public static IGTTool SCREWDRIVER;
    public static IGTTool MORTAR;
    public static IGTTool WIRE_CUTTER;
    public static IGTTool SCYTHE;
    public static IGTTool KNIFE;
    public static IGTTool BUTCHERY_KNIFE;
    public static IGTTool DRILL_LV;
    public static IGTTool DRILL_MV;
    public static IGTTool DRILL_HV;
    public static IGTTool DRILL_EV;
    public static IGTTool DRILL_IV;
    public static IGTTool CHAINSAW_LV;
    public static IGTTool WRENCH_LV;
    public static IGTTool WRENCH_HV;
    public static IGTTool WRENCH_IV;
    public static IGTTool BUZZSAW;
    public static IGTTool SCREWDRIVER_LV;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ToolItems.class);
        SWORD = register(ItemGTSword.Builder.of(GTValues.MODID, "sword")
                        .toolStats(b -> b.blockBreaking().attacking())
                        .toolClasses(ToolClasses.SWORD));
        PICKAXE = register(ItemGTTool.Builder.of(GTValues.MODID, "pickaxe")
                .toolStats(b -> b.blockBreaking().attacking().behaviors(new TorchPlaceBehavior()))
                .toolClasses(ToolClasses.PICKAXE));
        SHOVEL = register(ItemGTTool.Builder.of(GTValues.MODID, "shovel")
                .toolStats(b -> b.blockBreaking().behaviors(new GrassPathBehavior()))
                .toolClasses(ToolClasses.SHOVEL));
        AXE = register(ItemGTTool.Builder.of(GTValues.MODID, "axe")
                .toolStats(b -> b.blockBreaking().attacking().attackDamage(3.0F)
                        .attackSpeed(-2.6F).baseEfficiency(2.0F)
                        .behaviors(new DisableShieldBehavior(), new TreeFellingBehavior()))
                .toolClasses(ToolClasses.AXE));
        HOE = register(ItemGTTool.Builder.of(GTValues.MODID, "hoe")
                .toolStats(b -> b.behaviors(new HoeGroundBehavior()))
                .toolClasses(ToolClasses.HOE));
        SAW = register(ItemGTTool.Builder.of(GTValues.MODID, "saw")
                .toolStats(b -> b.attacking().crafting().behaviors(new HarvestIceBehavior()))
                .oreDict("craftingToolSaw")
                .symbol('s')
                .toolClasses(ToolClasses.SAW));
        HARD_HAMMER = register(ItemGTTool.Builder.of(GTValues.MODID, "hammer")
                .toolStats(b -> b.blockBreaking().attacking().crafting())
                .oreDict("craftingToolHammer")
                .sound(SoundEvents.BLOCK_ANVIL_LAND)
                .symbol('h')
                .toolClasses(ToolClasses.PICKAXE, ToolClasses.HARD_HAMMER));
        SOFT_MALLET = register(ItemGTTool.Builder.of(GTValues.MODID, "mallet")
                .toolStats(b -> b.crafting())
                .oreDict("craftingToolMallet")
                .sound(GTSounds.MALLET_TOOL)
                .symbol('r')
                .toolClasses(ToolClasses.SOFT_MALLET));
        MINING_HAMMER = register(ItemGTTool.Builder.of(GTValues.MODID, "mining_hammer")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .efficiencyMultiplier(0.4F).behaviors(new TorchPlaceBehavior()))
                .toolClasses(ToolClasses.PICKAXE));
        SPADE = register(ItemGTTool.Builder.of(GTValues.MODID, "spade")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .efficiencyMultiplier(0.4F)
                        .behaviors(new GrassPathBehavior()))
                .toolClasses(ToolClasses.SHOVEL));
        WRENCH = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse())
                .sound(GTSounds.WRENCH_TOOL, true)
                .oreDict("craftingToolWrench")
                .symbol('w')
                .toolClasses(ToolClasses.WRENCH));
        FILE = register(ItemGTTool.Builder.of(GTValues.MODID, "file")
                .toolStats(b -> b.crafting())
                .sound(GTSounds.FILE_TOOL)
                .oreDict("craftingToolFile")
                .symbol('f')
                .toolClasses(ToolClasses.FILE));
        CROWBAR = register(ItemGTTool.Builder.of(GTValues.MODID, "crowbar")
                .toolStats(b -> b.blockBreaking().crafting().attacking()
                        .sneakBypassUse().behaviors(new RotateRailBehavior()))
                .sound(SoundEvents.ENTITY_ITEM_BREAK)
                .oreDict("craftingToolCrowbar")
                .symbol('c')
                .toolClasses(ToolClasses.CROWBAR));
        SCREWDRIVER = register(ItemGTTool.Builder.of(GTValues.MODID, "screwdriver")
                .toolStats(b -> b.crafting().sneakBypassUse())
                .sound(GTSounds.SCREWDRIVER_TOOL)
                .oreDict("craftingToolScrewdriver")
                .symbol('d')
                .toolClasses(ToolClasses.SCREWDRIVER));
        MORTAR = register(ItemGTTool.Builder.of(GTValues.MODID, "mortar")
                .toolStats(b -> b.crafting())
                .sound(GTSounds.MORTAR_TOOL)
                .oreDict("craftingToolMortar")
                .symbol('m')
                .toolClasses(ToolClasses.MORTAR));
        WIRE_CUTTER = register(ItemGTTool.Builder.of(GTValues.MODID, "wire_cutter")
                .toolStats(b -> b.blockBreaking().crafting())
                .sound(GTSounds.WIRECUTTER_TOOL, true)
                .oreDict("craftingToolWireCutter")
                .symbol('x')
                .toolClasses(ToolClasses.WIRE_CUTTER));
        SCYTHE = register(ItemGTSword.Builder.of(GTValues.MODID, "scythe")
                .toolStats(b -> b.blockBreaking().attacking()
                        .aoe(1, 1, 2)
                        .behaviors(new HoeGroundBehavior()))
                .toolClasses(ToolClasses.SCYTHE, ToolClasses.HOE));
        KNIFE = register(ItemGTTool.Builder.of(GTValues.MODID, "knife")
                .toolStats(b -> b.crafting())
                .oreDict("craftingToolKnife")
                .symbol('k')
                .toolClasses(ToolClasses.KNIFE, ToolClasses.SWORD));
        BUTCHERY_KNIFE = register(ItemGTSword.Builder.of(GTValues.MODID, "butchery_knife")
                .toolStats(b -> b.crafting().attacking())
                .oreDict("craftingToolButcheryKnife")
                .toolClasses(ToolClasses.BUTCHERY_KNIFE));
        DRILL_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_lv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(new TorchPlaceBehavior()))
                .sound(GTSounds.DRILL_TOOL)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.LV));
        DRILL_MV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_mv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 2)
                        .efficiencyMultiplier(2.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_MV)
                        .behaviors(new TorchPlaceBehavior()))
                .sound(GTSounds.DRILL_TOOL)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.MV));
        DRILL_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_hv")
                .toolStats(b -> b.blockBreaking().aoe(2, 2, 4)
                        .efficiencyMultiplier(3.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV)
                        .behaviors(new TorchPlaceBehavior()))
                .sound(GTSounds.DRILL_TOOL)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.HV));
        if (ConfigHolder.tools.enableHighTierDrills) {
            DRILL_EV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_ev")
                    .toolStats(b -> b.blockBreaking().aoe(3, 3, 6)
                            .efficiencyMultiplier(4.0F)
                            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_EV)
                            .behaviors(new TorchPlaceBehavior()))
                    .sound(GTSounds.DRILL_TOOL)
                    .toolClasses(ToolClasses.DRILL)
                    .electric(GTValues.EV));
            DRILL_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_iv")
                    .toolStats(b -> b.blockBreaking().aoe(4, 4, 8)
                            .efficiencyMultiplier(5.0F)
                            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV)
                            .behaviors(new TorchPlaceBehavior()))
                    .sound(GTSounds.DRILL_TOOL)
                    .toolClasses(ToolClasses.DRILL)
                    .electric(GTValues.IV));
        }
        CHAINSAW_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "chainsaw_lv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 2)
                        .efficiencyMultiplier(2.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(new HarvestIceBehavior(), new DisableShieldBehavior(), new TreeFellingBehavior()))
                .sound(GTSounds.CHAINSAW_TOOL)
                .toolClasses(ToolClasses.AXE)
                .electric(GTValues.LV));
        WRENCH_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_lv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(2.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSounds.WRENCH_TOOL, true)
                .oreDict("craftingToolWrench")
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.LV));
        WRENCH_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_hv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(8.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV))
                .sound(GTSounds.WRENCH_TOOL, true)
                .oreDict("craftingToolWrench")
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.HV));
        WRENCH_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_iv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(16.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV))
                .sound(GTSounds.WRENCH_TOOL, true)
                .oreDict("craftingToolWrench")
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.IV));
        BUZZSAW = register(ItemGTTool.Builder.of(GTValues.MODID, "buzzsaw")
                .toolStats(b -> b.crafting().brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSounds.CHAINSAW_TOOL)
                .oreDict("craftingToolSaw")
                .toolClasses(ToolClasses.SAW)
                .electric(GTValues.LV));
        SCREWDRIVER_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "screwdriver_lv")
                .toolStats(b -> b.crafting().sneakBypassUse()
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSounds.SCREWDRIVER_TOOL)
                .oreDict("craftingToolScrewdriver")
                .toolClasses(ToolClasses.SCREWDRIVER)
                .electric(GTValues.LV));
    }

    private static IGTTool register(@Nonnull ToolBuilder<?> builder) {
        IGTTool tool = builder.build();
        TOOLS.add(tool);
        return tool;
    }

    private static IGTTool register(@Nonnull IGTTool tool) {
        TOOLS.add(tool);
        return tool;
    }

    public static void registerModels() {
        TOOLS.forEach(tool -> ModelLoader.setCustomModelResourceLocation(tool.get(), 0, tool.getModelLocation()));
    }

    public static void registerColors() {
        TOOLS.forEach(tool -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler(tool::getColor, tool.get()));
    }

    public static void registerOreDict() {
        TOOLS.forEach(tool -> {
            if (tool.getOreDictName() != null) {
                OreDictUnifier.registerOre(new ItemStack(tool.get(), 1, GTValues.W), tool.getOreDictName());
            }
        });
        OreDictUnifier.registerOre(new ItemStack(SOFT_MALLET.get(), 1, GTValues.W), "craftingToolSoftHammer"); // Compatibility
    }

    // Handle returning broken stacks
    @SubscribeEvent
    public static void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        Item item = event.getOriginal().getItem();
        if (item instanceof IGTTool) {
            IGTTool def = (IGTTool) item;
            ItemStack brokenStack = def.getToolStats().getBrokenStack();
            if (!brokenStack.isEmpty()) {
                if (event.getHand() == null) {
                    if (!event.getEntityPlayer().addItemStackToInventory(brokenStack)) {
                        event.getEntityPlayer().dropItem(brokenStack, true);
                    }
                } else {
                    event.getEntityPlayer().setHeldItem(event.getHand(), brokenStack);
                }
            }
        }
    }

    // Handle dynamic AoE break speed
    // Fixme: Under Construction
    /*
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();
        Item item = stack.getItem();
        if (item instanceof IGTTool) {
            EntityPlayer player = event.getEntityPlayer();
            if (!player.isSneaking()) {
                World world = player.world;
                if (!ThreadContext.containsKey("GT_AoE_BreakSpeed")) {
                    Set<BlockPos> validPositions = ToolHelper.getHarvestableBlocks(stack, world, player);
                    if (!validPositions.isEmpty()) {
                        float newSpeed = event.getNewSpeed(); // Take in consideration of higher prioritized event listeners
                        ThreadContext.put("GT_AoE_BreakSpeed", "");
                        for (BlockPos pos : validPositions) {
                            float speed = player.getDigSpeed(world.getBlockState(pos), pos);
                            if (speed < newSpeed) {
                                newSpeed = speed;
                            }
                        }
                        ThreadContext.remove("GT_AoE_BreakSpeed");
                        event.setNewSpeed(newSpeed);
                    }
                }
            }
        }
    }
     */

    // Handle saws harvesting ice without leaving water behind
    // Handle mined blocks teleporting straight into inventory
    // Handles drop conversion when a hammer tool (or tool with hard hammer enchantment) is used
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        EntityPlayer player = event.getHarvester();
        if (player != null) {
            ItemStack stack = event.getHarvester().getHeldItemMainhand();
            if (!stack.hasTagCompound()) {
                return;
            }
            if (!event.isSilkTouching()) {
                ToolHelper.applyHammerDropConversion(stack, event.getState(), event.getDrops(), event.getFortuneLevel(), event.getDropChance(), player.getRNG());
            }
            NBTTagCompound behaviourTag = ToolHelper.getBehavioursTag(stack);
            Block block = event.getState().getBlock();
            if (!event.isSilkTouching() && (block == Blocks.ICE || block == Blocks.PACKED_ICE) && behaviourTag.getBoolean(ToolHelper.HARVEST_ICE_KEY)) {
                Item iceBlock = Item.getItemFromBlock(block);
                if (event.getDrops().stream().noneMatch(drop -> drop.getItem() == iceBlock)) {
                    event.getDrops().add(new ItemStack(iceBlock));
                    final World world = event.getWorld();
                    final BlockPos icePos = event.getPos();
                    TaskScheduler.scheduleTask(world, () -> {
                        IBlockState flowingState = world.getBlockState(icePos);
                        if (flowingState == Blocks.FLOWING_WATER.getDefaultState()) {
                            world.setBlockToAir(icePos);
                        }
                        return true;
                    });
                }
            }
            if (behaviourTag.getBoolean(ToolHelper.RELOCATE_MINED_BLOCKS_KEY)) {
                event.getDrops().removeIf(player::addItemStackToInventory);
            }
            //TODO Hammering ores drops
        }
    }

    // Handle formatting of stat tooltips, this is easier to be done here than in Item#addInformation
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IGTTool) {
            IGTTool tool = (IGTTool) stack.getItem();
            String mainHandTooltip = I18n.format("item.modifiers.mainhand");
            ListIterator<String> tooltipIterator = event.getToolTip().listIterator(event.getToolTip().size());
            // Check where in the tooltip list we are
            while (tooltipIterator.hasPrevious()) {
                if (mainHandTooltip.equals(tooltipIterator.previous())) {
                    tooltipIterator.next(); // Turnover
                    // Push
                    IGTToolDefinition toolStats = tool.getToolStats();
                    if (toolStats.isSuitableForCrafting(stack)) {
                        tooltipIterator.add(I18n.format("item.gt.tool.tooltip.crafting_uses", (tool.getTotalMaxDurability(stack) - stack.getItemDamage()) / Math.max(1, toolStats.getToolDamagePerCraft(stack))));
                    }
                    if (toolStats.isSuitableForAttacking(stack)) {
                        tooltipIterator.add(I18n.format("item.gt.tool.tooltip.attack_damage", tool.getTotalAttackDamage(stack)));
                    }
                    if (toolStats.isSuitableForBlockBreak(stack)) {
                        tooltipIterator.add(I18n.format("item.gt.tool.tooltip.mining_speed", tool.getTotalToolSpeed(stack)));
                        tooltipIterator.add(I18n.format("item.gt.tool.tooltip.harvest_level", tool.getTotalHarvestLevel(stack)));
                    }
                    break; // Exit early
                }
            }
        }
    }

    // Handle client-view of harvestable blocks in AoE (and potentially wrench overlay in the future)
    // Handle machine grid rendering as well
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onDrawHighlightEvent(DrawBlockHighlightEvent event) {
        if (event.getTarget().getBlockPos() == null) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        ItemStack stack = player.getHeldItemMainhand();
        BlockPos pos = event.getTarget().getBlockPos();
        IBlockState state = player.world.getBlockState(pos);
        TileEntity tile = player.world.getTileEntity(event.getTarget().getBlockPos());
        boolean sneaking = player.isSneaking();
        if (tile != null && shouldRenderGridOverlays(state, tile, stack, player.getHeldItemOffhand(), sneaking) &&
                renderGridOverlays(player, pos, state, event.getTarget().sideHit, tile, event.getPartialTicks())) {
            event.setCanceled(true);
            return;
        }
        if (!sneaking) {
            Set<BlockPos> validPositions = ToolHelper.getHarvestableBlocks(stack, player.world, player, event.getTarget());
            if (validPositions.isEmpty()) {
                return;
            }
            float partialTicks = event.getPartialTicks();
            for (BlockPos validPosition : validPositions) {
                event.getContext().drawSelectionBox(player, new RayTraceResult(Vec3d.ZERO, null, validPosition), 0, partialTicks);
            }
            DestroyBlockProgress progress = event.getContext().damagedBlocks.get(player.getEntityId());
            if (progress != null) {
                int damage = progress.getPartialBlockDamage();
                if (damage > -1) {
                    double relX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
                    double relY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
                    double relZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
                    BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                    Minecraft mc = Minecraft.getMinecraft();
                    BlockRendererDispatcher rendererDispatcher = mc.blockRenderDispatcher;
                    preRenderDamagedBlocks();
                    bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                    bufferBuilder.setTranslation(-relX, -relY, -relZ);
                    bufferBuilder.noColor(); // ?
                    for (BlockPos validPosition : validPositions) {
                        TileEntity tileEntity = mc.world.getTileEntity(validPosition);
                        boolean hasBreak = tileEntity != null && tileEntity.canRenderBreaking();
                        if (!hasBreak) {
                            TextureAtlasSprite sprite = event.getContext().destroyBlockIcons[damage];
                            rendererDispatcher.renderBlockDamage(mc.world.getBlockState(validPosition), validPosition, sprite, mc.world);
                        }
                    }
                    Tessellator.getInstance().draw();
                    bufferBuilder.setTranslation(0.0D, 0.0D, 0.0D);
                    postRenderDamagedBlocks();
                }
            }
        }
    }

    private static void preRenderDamagedBlocks() {
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-3.0F, -3.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
    }

    private static void postRenderDamagedBlocks() {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();
    }

    private static boolean shouldRenderGridOverlays(IBlockState state, TileEntity tile, ItemStack mainHand, ItemStack offHand, boolean isSneaking) {
        if (state.getBlock() instanceof BlockPipe) {
            BlockPipe<?, ?, ?> pipe = (BlockPipe<?, ?, ?>) state.getBlock();
            if (isSneaking && mainHand.getItem().getClass() == Item.getItemFromBlock(pipe).getClass()) {
                return true;
            } else {
                if (mainHand.getItem().getToolClasses(mainHand).stream().anyMatch(s -> s.equals(ToolClasses.SCREWDRIVER) || pipe.isToolEffective(s, state))) {
                    return true;
                } else if (offHand.getItem().getToolClasses(offHand).stream().anyMatch(s -> s.equals(ToolClasses.SCREWDRIVER) || pipe.isToolEffective(s, state))) {
                    return true;
                }
                BooleanSupplier hasCover = () -> tile instanceof IPipeTile && ((IPipeTile<?, ?>) tile).getCoverableImplementation().hasAnyCover();
                Predicate<CoverDefinition> canCover = coverDef -> tile instanceof IPipeTile && ICoverable.canPlaceCover(coverDef, ((IPipeTile<?, ?>) tile).getCoverableImplementation());
                if (GTUtility.isCoverBehaviorItem(mainHand, hasCover, canCover) || GTUtility.isCoverBehaviorItem(offHand, hasCover, canCover)) {
                    return true;
                }
            }
        }
        if (tile instanceof MetaTileEntityHolder) {
            MetaTileEntity mte = ((MetaTileEntityHolder) tile).getMetaTileEntity();
            if (mte != null && mte.canRenderMachineGrid()) {
                if (ToolHelper.isTool(mainHand, ToolClasses.WRENCH, ToolClasses.SCREWDRIVER) ||
                        ToolHelper.isTool(offHand, ToolClasses.WRENCH, ToolClasses.SCREWDRIVER)) {
                    return true;
                }
            }
        }
        ICoverable coverable = tile.getCapability(GregtechTileCapabilities.CAPABILITY_COVERABLE, null);
        return coverable != null && GTUtility.isCoverBehaviorItem(mainHand, coverable::hasAnyCover, coverDef -> ICoverable.canPlaceCover(coverDef, coverable));
    }

    private static float rColour;
    private static float gColour;
    private static float bColour;

    private static boolean renderGridOverlays(EntityPlayer player, BlockPos pos, IBlockState state, EnumFacing facing, TileEntity tile, float partialTicks) {
        if (player.world.getWorldBorder().contains(pos)) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            double d3 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
            double d4 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
            double d5 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
            AxisAlignedBB box = state.getSelectedBoundingBox(player.world, pos).grow(0.002D).offset(-d3, -d4, -d5);
            RenderGlobal.drawSelectionBoundingBox(box, 1, 1, 1, 0.4F);

            rColour = gColour = bColour = 0.2F + (float) Math.sin((float) (System.currentTimeMillis() % (Math.PI * 800)) / 800) / 2;

            if (tile instanceof TileEntityPipeBase) {
                TileEntityPipeBase<?, ?> tepb = (TileEntityPipeBase<?, ?>) tile;
                drawGridOverlays(facing, box, face -> tepb.isConnected(face) || tepb.getCoverableImplementation().getCoverAtSide(face) != null);
            } else if (tile instanceof MetaTileEntityHolder) {
                MetaTileEntity mte = ((MetaTileEntityHolder) tile).getMetaTileEntity();
                drawGridOverlays(facing, box, mte::isSideUsed);
            } else {
                drawGridOverlays(box);
            }
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            return true;
        }
        return false;
    }

    private static void drawGridOverlays(AxisAlignedBB box) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

        Vector3 topRight = new Vector3(box.maxX, box.maxY, box.maxZ);
        Vector3 bottomRight = new Vector3(box.maxX, box.minY, box.maxZ);
        Vector3 bottomLeft = new Vector3(box.minX, box.minY, box.maxZ);
        Vector3 topLeft = new Vector3(box.minX, box.maxY, box.maxZ);
        Vector3 shift = new Vector3(0.25, 0, 0);
        Vector3 shiftVert = new Vector3(0, 0.25, 0);

        Vector3 cubeCenter = new Vector3(box.getCenter());

        topRight.subtract(cubeCenter);
        bottomRight.subtract(cubeCenter);
        bottomLeft.subtract(cubeCenter);
        topLeft.subtract(cubeCenter);

        topRight.add(cubeCenter);
        bottomRight.add(cubeCenter);
        bottomLeft.add(cubeCenter);
        topLeft.add(cubeCenter);

        // straight top bottom lines
        startLine(buffer, topRight.copy().add(shift.copy().negate()));
        endLine(buffer, bottomRight.copy().add(shift.copy().negate()));

        startLine(buffer, bottomLeft.copy().add(shift));
        endLine(buffer, topLeft.copy().add(shift));

        // straight side to side lines
        startLine(buffer, topLeft.copy().add(shiftVert.copy().negate()));
        endLine(buffer, topRight.copy().add(shiftVert.copy().negate()));

        startLine(buffer, bottomLeft.copy().add(shiftVert));
        endLine(buffer, bottomRight.copy().add(shiftVert));

        tessellator.draw();
    }

    private static void drawGridOverlays(EnumFacing facing, AxisAlignedBB box, Predicate<EnumFacing> test) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);

        Vector3 topRight = new Vector3(box.maxX, box.maxY, box.maxZ);
        Vector3 bottomRight = new Vector3(box.maxX, box.minY, box.maxZ);
        Vector3 bottomLeft = new Vector3(box.minX, box.minY, box.maxZ);
        Vector3 topLeft = new Vector3(box.minX, box.maxY, box.maxZ);
        Vector3 shift = new Vector3(0.25, 0, 0);
        Vector3 shiftVert = new Vector3(0, 0.25, 0);

        Vector3 cubeCenter = new Vector3(box.getCenter());

        topRight.subtract(cubeCenter);
        bottomRight.subtract(cubeCenter);
        bottomLeft.subtract(cubeCenter);
        topLeft.subtract(cubeCenter);

        boolean leftBlocked;
        boolean topBlocked;
        boolean rightBlocked;
        boolean bottomBlocked;
        boolean frontBlocked = test.test(facing);
        boolean backBlocked = test.test(facing.getOpposite());

        switch (facing) {
            case WEST: {
                topRight.rotate(Math.PI / 2, Vector3.down);
                bottomRight.rotate(Math.PI / 2, Vector3.down);
                bottomLeft.rotate(Math.PI / 2, Vector3.down);
                topLeft.rotate(Math.PI / 2, Vector3.down);
                shift.rotate(Math.PI / 2, Vector3.down);
                shiftVert.rotate(Math.PI / 2, Vector3.down);

                leftBlocked = test.test(EnumFacing.NORTH);
                topBlocked = test.test(EnumFacing.UP);
                rightBlocked = test.test(EnumFacing.SOUTH);
                bottomBlocked = test.test(EnumFacing.DOWN);
                break;
            }
            case EAST: {
                topRight.rotate(-Math.PI / 2, Vector3.down);
                bottomRight.rotate(-Math.PI / 2, Vector3.down);
                bottomLeft.rotate(-Math.PI / 2, Vector3.down);
                topLeft.rotate(-Math.PI / 2, Vector3.down);
                shift.rotate(-Math.PI / 2, Vector3.down);
                shiftVert.rotate(-Math.PI / 2, Vector3.down);

                leftBlocked = test.test(EnumFacing.SOUTH);
                topBlocked = test.test(EnumFacing.UP);
                rightBlocked = test.test(EnumFacing.NORTH);
                bottomBlocked = test.test(EnumFacing.DOWN);
                break;
            }
            case NORTH: {
                topRight.rotate(Math.PI, Vector3.down);
                bottomRight.rotate(Math.PI, Vector3.down);
                bottomLeft.rotate(Math.PI, Vector3.down);
                topLeft.rotate(Math.PI, Vector3.down);
                shift.rotate(Math.PI, Vector3.down);
                shiftVert.rotate(Math.PI, Vector3.down);

                leftBlocked = test.test(EnumFacing.EAST);
                topBlocked = test.test(EnumFacing.UP);
                rightBlocked = test.test(EnumFacing.WEST);
                bottomBlocked = test.test(EnumFacing.DOWN);
                break;
            }
            case UP: {
                Vector3 side = new Vector3(1, 0, 0);
                topRight.rotate(-Math.PI / 2, side);
                bottomRight.rotate(-Math.PI / 2, side);
                bottomLeft.rotate(-Math.PI / 2, side);
                topLeft.rotate(-Math.PI / 2, side);
                shift.rotate(-Math.PI / 2, side);
                shiftVert.rotate(-Math.PI / 2, side);

                leftBlocked = test.test(EnumFacing.WEST);
                topBlocked = test.test(EnumFacing.NORTH);
                rightBlocked = test.test(EnumFacing.EAST);
                bottomBlocked = test.test(EnumFacing.SOUTH);
                break;
            }
            case DOWN: {
                Vector3 side = new Vector3(1, 0, 0);
                topRight.rotate(Math.PI / 2, side);
                bottomRight.rotate(Math.PI / 2, side);
                bottomLeft.rotate(Math.PI / 2, side);
                topLeft.rotate(Math.PI / 2, side);
                shift.rotate(Math.PI / 2, side);
                shiftVert.rotate(Math.PI / 2, side);

                leftBlocked = test.test(EnumFacing.WEST);
                topBlocked = test.test(EnumFacing.SOUTH);
                rightBlocked = test.test(EnumFacing.EAST);
                bottomBlocked = test.test(EnumFacing.NORTH);
                break;
            }
            default: {
                leftBlocked = test.test(EnumFacing.WEST);
                topBlocked = test.test(EnumFacing.UP);
                rightBlocked = test.test(EnumFacing.EAST);
                bottomBlocked = test.test(EnumFacing.DOWN);
            }
        }

        topRight.add(cubeCenter);
        bottomRight.add(cubeCenter);
        bottomLeft.add(cubeCenter);
        topLeft.add(cubeCenter);

        // straight top bottom lines
        startLine(buffer, topRight.copy().add(shift.copy().negate()));
        endLine(buffer, bottomRight.copy().add(shift.copy().negate()));

        startLine(buffer, bottomLeft.copy().add(shift));
        endLine(buffer, topLeft.copy().add(shift));

        // straight side to side lines
        startLine(buffer, topLeft.copy().add(shiftVert.copy().negate()));
        endLine(buffer, topRight.copy().add(shiftVert.copy().negate()));

        startLine(buffer, bottomLeft.copy().add(shiftVert));
        endLine(buffer, bottomRight.copy().add(shiftVert));

        if (leftBlocked) {
            startLine(buffer, topLeft.copy().add(shiftVert.copy().negate()));
            endLine(buffer, bottomLeft.copy().add(shiftVert.copy()).add(shift));

            startLine(buffer, topLeft.copy().add(shiftVert.copy().negate()).add(shift));
            endLine(buffer, bottomLeft.copy().add(shiftVert));
        }
        if (topBlocked) {
            startLine(buffer, topLeft.copy().add(shift));
            endLine(buffer, topRight.copy().add(shift.copy().negate()).add(shiftVert.copy().negate()));

            startLine(buffer, topLeft.copy().add(shift).add(shiftVert.copy().negate()));
            endLine(buffer, topRight.copy().add(shift.copy().negate()));
        }
        if (rightBlocked) {
            startLine(buffer, topRight.copy().add(shiftVert.copy().negate()));
            endLine(buffer, bottomRight.copy().add(shiftVert.copy()).add(shift.copy().negate()));

            startLine(buffer, topRight.copy().add(shiftVert.copy().negate()).add(shift.copy().negate()));
            endLine(buffer, bottomRight.copy().add(shiftVert));
        }
        if (bottomBlocked) {
            startLine(buffer, bottomLeft.copy().add(shift));
            endLine(buffer, bottomRight.copy().add(shift.copy().negate()).add(shiftVert));

            startLine(buffer, bottomLeft.copy().add(shift).add(shiftVert));
            endLine(buffer, bottomRight.copy().add(shift.copy().negate()));
        }
        if (frontBlocked) {
            startLine(buffer, topLeft.copy().add(shift).add(shiftVert.copy().negate()));
            endLine(buffer, bottomRight.copy().add(shift.copy().negate()).add(shiftVert));

            startLine(buffer, topRight.copy().add(shift.copy().negate()).add(shiftVert.copy().negate()));
            endLine(buffer, bottomLeft.copy().add(shift).add(shiftVert));
        }
        if (backBlocked) {
            Vector3 localXShift = new Vector3(0, 0, 0); // Set up translations for the current X.
            for (int i = 0; i < 2; i++) {
                Vector3 localXShiftVert = new Vector3(0, 0, 0);
                for (int j = 0; j < 2; j++) {
                    startLine(buffer, topLeft.copy().add(localXShift).add(localXShiftVert));
                    endLine(buffer, topLeft.copy().add(localXShift).add(localXShiftVert).add(shift).subtract(shiftVert));

                    startLine(buffer, topLeft.copy().add(localXShift).add(localXShiftVert).add(shift));
                    endLine(buffer, topLeft.copy().add(localXShift).add(localXShiftVert).subtract(shiftVert));

                    localXShiftVert.add(bottomLeft.copy().subtract(topLeft).add(shiftVert)); // Move by the vector from the top to the bottom, minus the shift from the edge.
                }
                localXShift.add(topRight.copy().subtract(topLeft).subtract(shift)); // Move by the vector from the left to the right, minus the shift from the edge.
            }
        }

        tessellator.draw();
    }

    private static void startLine(BufferBuilder buffer, Vector3 vec) {
        buffer.pos(vec.x, vec.y, vec.z).color(rColour, gColour, bColour, 0.0F).endVertex();
    }

    private static void endLine(BufferBuilder buffer, Vector3 vec) {
        buffer.pos(vec.x, vec.y, vec.z).color(rColour, gColour, bColour, 1F).endVertex();
    }

}
