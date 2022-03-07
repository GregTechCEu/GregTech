package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.items.toolitem.ToolBuilder;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.sound.GTSounds;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.TaskScheduler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.ThreadContext;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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
    public static IGTTool SOFT_HAMMER;
    public static IGTTool WRENCH;
    public static IGTTool FILE;
    public static IGTTool CROWBAR;
    public static IGTTool SCREWDRIVER;
    public static IGTTool MORTAR;
    public static IGTTool WIRE_CUTTER;
    public static IGTTool BRANCH_CUTTER;
    public static IGTTool KNIFE;
    public static IGTTool BUTCHERY_KNIFE;
    public static IGTTool SENSE;
    public static IGTTool PLUNGER;
    public static IGTTool DRILL_LV;
    public static IGTTool DRILL_MV;
    public static IGTTool DRILL_HV;
    public static IGTTool DRILL_EV;
    public static IGTTool DRILL_IV;
    public static IGTTool MINING_HAMMER;
    public static IGTTool CHAINSAW_LV;
    public static IGTTool CHAINSAW_MV;
    public static IGTTool CHAINSAW_HV;
    public static IGTTool WRENCH_LV;
    public static IGTTool WRENCH_MV;
    public static IGTTool WRENCH_HV;
    public static IGTTool BUZZSAW;
    public static IGTTool SCREWDRIVER_LV;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ToolItems.class);
        PICKAXE = register(ItemGTTool.Builder.of(GTValues.MODID, "pickaxe")
                .toolStats(b -> b.suitableForBlockBreaking().suitableForAttacking())
                .toolClasses("pickaxe"));
        AXE = register(ItemGTTool.Builder.of(GTValues.MODID, "axe")
                .toolStats(b -> b.suitableForBlockBreaking().suitableForAttacking())
                .toolClasses("axe"));
        HARD_HAMMER = register(ItemGTTool.Builder.of(GTValues.MODID, "hammer")
                .toolStats(b -> b.suitableForBlockBreaking().suitableForAttacking().suitableForCrafting())
                .oreDicts("craftingToolHammer")
                .sound(GTSounds.FORGE_HAMMER)
                .toolClasses("pickaxe", "hammer"));
        WRENCH = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench")
                .toolStats(b -> b.suitableForBlockBreaking().suitableForCrafting())
                .sound(GTSounds.WRENCH_TOOL)
                .oreDicts("craftingToolWrench")
                .toolClasses("wrench"));
        DRILL_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_lv")
                .toolStats(b -> b.suitableForBlockBreaking().aoeDefinition(1, 1, 0).brokenStack(() -> MetaItems.POWER_UNIT_LV.getStackForm()))
                .oreDicts("craftingToolDrill")
                .toolClasses("pickaxe", "shovel", "drill")
                .electric(1));
        DRILL_MV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_mv")
                .toolStats(b -> b.suitableForBlockBreaking().aoeDefinition(1, 1, 0).brokenStack(() -> MetaItems.POWER_UNIT_MV.getStackForm()))
                .oreDicts("craftingToolDrill")
                .toolClasses("pickaxe", "shovel", "drill")
                .electric(1));
        DRILL_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_hv")
                .toolStats(b -> b.suitableForBlockBreaking().aoeDefinition(2, 2, 0).brokenStack(() -> MetaItems.POWER_UNIT_HV.getStackForm()))
                .oreDicts("craftingToolDrill")
                .toolClasses("pickaxe", "shovel", "drill")
                .electric(1));
        DRILL_EV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_ev")
                .toolStats(b -> b.suitableForBlockBreaking().aoeDefinition(2, 2, 0).brokenStack(() -> MetaItems.POWER_UNIT_EV.getStackForm()))
                .oreDicts("craftingToolDrill")
                .toolClasses("pickaxe", "shovel", "drill")
                .electric(1));
        DRILL_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_iv")
                .toolStats(b -> b.suitableForBlockBreaking().aoeDefinition(2, 2, 1).brokenStack(() -> MetaItems.POWER_UNIT_IV.getStackForm()))
                .oreDicts("craftingToolDrill")
                .toolClasses("pickaxe", "shovel", "drill")
                .electric(1));
    }

    private static IGTTool register(ToolBuilder<?> builder) {
        IGTTool tool = builder.build();
        TOOLS.add(tool);
        return tool;
    }

    public static void registerModels() {
        TOOLS.forEach(tool -> ModelLoader.setCustomModelResourceLocation(tool.get(), 0, new ModelResourceLocation(tool.get().getRegistryName(), "inventory")));
    }

    public static void registerColors() {
        TOOLS.forEach(tool -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler(tool::getColor, tool.get()));
    }

    public static void registerOreDict() {
        TOOLS.forEach(tool -> {
            ItemStack stack = new ItemStack(tool.get(), 1, GTValues.W);
            for (String oreDict : tool.getOreDictNames()) {
                OreDictUnifier.registerOre(stack, oreDict);
            }
        });
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
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();
        Item item = stack.getItem();
        if (item instanceof IGTTool) {
            EntityPlayer player = event.getEntityPlayer();
            if (!player.isSneaking()) {
                World world = player.world;
                if (!ThreadContext.containsKey("GT_AoE_BreakSpeed")) {
                    Set<BlockPos> validPositions = ((IGTTool) item).getHarvestableBlocks(stack, world, player);
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
            NBTTagCompound behaviourTag = IGTTool.getBehaviourTag(stack);
            if (!event.isSilkTouching() && event.getState().getBlock() == Blocks.ICE && behaviourTag.getBoolean("SilkHarvestIce")) {
                Item iceBlock = Item.getItemFromBlock(Blocks.ICE);
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
            if (behaviourTag.getBoolean("RelocateMinedBlocks")) {
                event.getDrops().removeIf(player::addItemStackToInventory);
            }
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
                    if (tool.getToolStats().isSuitableForCrafting(stack)) {
                        tooltipIterator.add(TextFormatting.GREEN + " " + tool.getToolStats().getToolDamagePerCraft(stack) + " Crafting Uses");
                    }
                    tooltipIterator.add(TextFormatting.LIGHT_PURPLE + " " + tool.getTotalAttackDamage(stack) + " Mining Speed");
                    tooltipIterator.add(TextFormatting.YELLOW + " " + tool.getTotalHarvestLevel(stack) + " Harvest Level");
                    break; // Exit early
                }
            }
        }
    }

    // Handle client-view of harvestable blocks in AoE (and potentially wrench overlay in the future)
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onDrawHighlightEvent(DrawBlockHighlightEvent event) {
        EntityPlayer player = event.getPlayer();
        if (!player.isSneaking()) {
            ItemStack stack = player.getHeldItemMainhand();
            Item item = stack.getItem();
            if (item instanceof IGTTool) {
                Set<BlockPos> validPositions = ((IGTTool) item).getHarvestableBlocks(stack, player.world, player, event.getTarget());
                float partialTicks = event.getPartialTicks();
                for (BlockPos pos : validPositions) {
                    event.getContext().drawSelectionBox(player, new RayTraceResult(Vec3d.ZERO, null, pos), 0, partialTicks);
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
                        for (BlockPos pos : validPositions) {
                            TileEntity tileEntity = mc.world.getTileEntity(pos);
                            boolean hasBreak = tileEntity != null && tileEntity.canRenderBreaking();
                            if (!hasBreak) {
                                TextureAtlasSprite sprite = event.getContext().destroyBlockIcons[damage];
                                rendererDispatcher.renderBlockDamage(mc.world.getBlockState(pos), pos, sprite, mc.world);
                            }
                        }
                        Tessellator.getInstance().draw();
                        bufferBuilder.setTranslation(0.0D, 0.0D, 0.0D);
                        postRenderDamagedBlocks();
                    }
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

}
