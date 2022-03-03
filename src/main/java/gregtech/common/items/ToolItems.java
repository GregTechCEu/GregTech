package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.AoEDefinition;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
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

    public static final int DAMAGE_FOR_SCREWDRIVER = 1;
    public static final int DAMAGE_FOR_WRENCH = 2;
    public static final int DAMAGE_FOR_CUTTER = 2;
    public static final int DAMAGE_FOR_CROWBAR = 1;
    public static final int DAMAGE_FOR_SOFT_HAMMER = 3;
    public static final int DAMAGE_FOR_HAMMER = 3;
    public static final int DAMAGE_FOR_HOE = 2;
    public static final int DAMAGE_FOR_PLUNGER = 1;

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
        WRENCH = ItemGTTool.Builder.of(GTValues.MODID, "wrench")
                .toolStats(b -> b.damagePerAction(DAMAGE_FOR_WRENCH).suitableForBlockBreaking().suitableForCrafting())
                .sound(GTSounds.WRENCH_TOOL)
                .oreDicts("craftingToolWrench")
                .toolClasses("wrench")
                .build();
        PICKAXE = ItemGTTool.Builder.of(GTValues.MODID, "pickaxe")
                .toolStats(b -> b.suitableForBlockBreaking().suitableForAttacking())
                .oreDicts("craftingToolPickaxe")
                .toolClasses("pickaxe")
                .aoeData(1, 1, 0)
                .build();
        AXE = ItemGTTool.Builder.of(GTValues.MODID, "axe")
                .toolStats(b -> b.suitableForBlockBreaking().suitableForAttacking())
                .oreDicts("craftingToolAxe")
                .toolClasses("axe")
                .build();
        DRILL_LV = ItemGTTool.Builder.of(GTValues.MODID, "drill_lv")
                .toolStats(b -> b.suitableForBlockBreaking())
                .oreDicts("craftingToolDrill")
                .toolClasses("pickaxe", "drill", "shovel")
                .electric(1)
                .build();
        TOOLS.add(WRENCH);
        TOOLS.add(PICKAXE);
        TOOLS.add(AXE);
        TOOLS.add(DRILL_LV);
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
                    Set<BlockPos> validPositions = ((IGTTool) item).getHarvestableBlocks(world, player);
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

    // Handle Saws harvesting Ice Blocks correctly
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (!event.isSilkTouching() && event.getHarvester() != null && event.getState().getBlock() == Blocks.ICE) {
            ItemStack stack = event.getHarvester().getHeldItemMainhand();
            Item item = stack.getItem();
            if (item == SAW) {
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
                Set<BlockPos> validPositions = ((IGTTool) item).getHarvestableBlocks(player.world, player, event.getTarget());
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
