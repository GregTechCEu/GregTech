package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.sound.GTSounds;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.TaskScheduler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

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
                .toolStats(b -> b.damagePerCraft(DAMAGE_FOR_WRENCH).usedForAttacking())
                .sound(GTSounds.WRENCH_TOOL)
                .oreDicts("craftingToolWrench")
                .toolClasses("wrench")
                .build();
        TOOLS.add(WRENCH);
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

    // Handle Saws harvesting Ice Blocks correctly
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
        if (!event.isSilkTouching() && event.getState().getBlock() == Blocks.ICE) {
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

}
