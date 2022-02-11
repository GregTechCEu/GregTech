package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.GTToolDefinition;
import gregtech.api.items.toolitem.GTToolItem;
import gregtech.api.sound.GTSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

public class ToolItems {

    private static final List<GTToolDefinition> TOOLS = new ArrayList<>();

    public static List<GTToolDefinition> getAllTools() {
        return TOOLS;
    }

    public static GTToolDefinition SWORD;
    public static GTToolDefinition PICKAXE;
    public static GTToolDefinition SHOVEL;
    public static GTToolDefinition AXE;
    public static GTToolDefinition HOE;
    public static GTToolDefinition SAW;
    public static GTToolDefinition HARD_HAMMER;
    public static GTToolDefinition SOFT_HAMMER;
    public static GTToolDefinition WRENCH;
    public static GTToolDefinition FILE;
    public static GTToolDefinition CROWBAR;
    public static GTToolDefinition SCREWDRIVER;
    public static GTToolDefinition MORTAR;
    public static GTToolDefinition WIRE_CUTTER;
    public static GTToolDefinition BRANCH_CUTTER;
    public static GTToolDefinition KNIFE;
    public static GTToolDefinition BUTCHERY_KNIFE;
    public static GTToolDefinition SENSE;
    public static GTToolDefinition PLUNGER;
    public static GTToolDefinition DRILL_LV;
    public static GTToolDefinition DRILL_MV;
    public static GTToolDefinition DRILL_HV;
    public static GTToolDefinition DRILL_EV;
    public static GTToolDefinition DRILL_IV;
    public static GTToolDefinition MINING_HAMMER;
    public static GTToolDefinition CHAINSAW_LV;
    public static GTToolDefinition CHAINSAW_MV;
    public static GTToolDefinition CHAINSAW_HV;
    public static GTToolDefinition WRENCH_LV;
    public static GTToolDefinition WRENCH_MV;
    public static GTToolDefinition WRENCH_HV;
    public static GTToolDefinition BUZZSAW;
    public static GTToolDefinition SCREWDRIVER_LV;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ToolItems.class);
        WRENCH = GTToolItem.Builder.of(GTValues.MODID, "wrench")
                .toolStats(b -> b.damagePerCraft(8).usedForAttacking())
                .sound(GTSounds.WRENCH_TOOL)
                .build();
        TOOLS.add(WRENCH);
    }

    public static void registerModels() {
        TOOLS.forEach(tool -> ModelLoader.setCustomModelResourceLocation(tool.get(), 0, new ModelResourceLocation(tool.get().getRegistryName(), "inventory")));
    }

    public static void registerColors() {
        TOOLS.forEach(tool -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler(tool::getColor, tool.get()));
    }

    // Handle returning broken stacks
    @SubscribeEvent
    public static void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        Item item = event.getOriginal().getItem();
        if (item instanceof GTToolDefinition) {
            GTToolDefinition def = (GTToolDefinition) item;
            ItemStack brokenStack = def.getToolStats().getBrokenStack();
            if (!brokenStack.isEmpty()) {
                brokenStack = brokenStack.copy();
                if (event.getHand() == null) {
                    // Special-case container items?
                    if (!event.getEntityPlayer().addItemStackToInventory(brokenStack)) {
                        event.getEntityPlayer().dropItem(brokenStack, true);
                    }
                } else {
                    event.getEntityPlayer().setHeldItem(event.getHand(), brokenStack);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.player != null && !event.player.world.isRemote) {
            for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
                Item item = event.craftMatrix.getStackInSlot(i).getItem();
                if (item instanceof GTToolDefinition) {
                    ((GTToolDefinition) item).playSound(event.player);
                }
            }
        }
    }

}
