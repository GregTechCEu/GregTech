package gregtech.common.items;

import gregtech.api.items.toolitem.GTToolDefinition;
import net.minecraft.client.Minecraft;

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

    }

    public static void registerColors() {
        TOOLS.forEach(tool -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler(tool::getColor, tool.get()));
    }

}
