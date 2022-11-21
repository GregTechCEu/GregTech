package gregtech.tool.sound;

import net.minecraft.util.SoundEvent;

import static gregtech.api.GregTechAPI.soundManager;

public class ToolSounds {

    public static SoundEvent DRILL_TOOL;
    public static SoundEvent PLUNGER_TOOL;
    public static SoundEvent FILE_TOOL;
    public static SoundEvent SAW_TOOL;
    public static SoundEvent SCREWDRIVER_TOOL;
    public static SoundEvent CHAINSAW_TOOL;
    public static SoundEvent WIRECUTTER_TOOL;
    public static SoundEvent SPRAY_CAN_TOOL;
    public static SoundEvent TRICORDER_TOOL;
    public static SoundEvent WRENCH_TOOL;
    public static SoundEvent MORTAR_TOOL;
    public static SoundEvent SOFT_HAMMER_TOOL;

    public static void register() {
        DRILL_TOOL = soundManager.registerSound("use.drill");
        PLUNGER_TOOL = soundManager.registerSound("use.plunger");
        FILE_TOOL = soundManager.registerSound("use.file");
        SAW_TOOL = soundManager.registerSound("use.saw");
        SCREWDRIVER_TOOL = soundManager.registerSound("use.screwdriver");
        CHAINSAW_TOOL = soundManager.registerSound("use.chainsaw");
        WIRECUTTER_TOOL = soundManager.registerSound("use.wirecutter");
        SPRAY_CAN_TOOL = soundManager.registerSound("use.spray_can");
        TRICORDER_TOOL = soundManager.registerSound("use.tricorder");
        WRENCH_TOOL = soundManager.registerSound("use.wrench");
        MORTAR_TOOL = soundManager.registerSound("use.mortar");
        SOFT_HAMMER_TOOL = soundManager.registerSound("use.soft_hammer");
    }
}
