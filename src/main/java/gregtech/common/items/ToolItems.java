package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.*;
import gregtech.api.unification.OreDictUnifier;
import gregtech.common.ConfigHolder;
import gregtech.common.items.tool.*;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

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
                .oreDict(ToolOreDicts.craftingToolSaw)
                .symbol('s')
                .toolClasses(ToolClasses.SAW));
        HARD_HAMMER = register(ItemGTTool.Builder.of(GTValues.MODID, "hammer")
                .toolStats(b -> b.blockBreaking().attacking().crafting())
                .oreDict(ToolOreDicts.craftingToolHammer)
                .sound(SoundEvents.BLOCK_ANVIL_LAND)
                .symbol('h')
                .toolClasses(ToolClasses.PICKAXE, ToolClasses.HARD_HAMMER));
        SOFT_MALLET = register(ItemGTTool.Builder.of(GTValues.MODID, "mallet")
                .toolStats(b -> b.crafting())
                .oreDict(ToolOreDicts.craftingToolMallet)
                .sound(GTSoundEvents.SOFT_MALLET_TOOL)
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
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWrench)
                .symbol('w')
                .toolClasses(ToolClasses.WRENCH));
        FILE = register(ItemGTTool.Builder.of(GTValues.MODID, "file")
                .toolStats(b -> b.crafting())
                .sound(GTSoundEvents.FILE_TOOL)
                .oreDict(ToolOreDicts.craftingToolFile)
                .symbol('f')
                .toolClasses(ToolClasses.FILE));
        CROWBAR = register(ItemGTTool.Builder.of(GTValues.MODID, "crowbar")
                .toolStats(b -> b.blockBreaking().crafting().attacking()
                        .sneakBypassUse().behaviors(new RotateRailBehavior()))
                .sound(SoundEvents.ENTITY_ITEM_BREAK)
                .oreDict(ToolOreDicts.craftingToolCrowbar)
                .symbol('c')
                .toolClasses(ToolClasses.CROWBAR));
        SCREWDRIVER = register(ItemGTTool.Builder.of(GTValues.MODID, "screwdriver")
                .toolStats(b -> b.crafting().sneakBypassUse())
                .sound(GTSoundEvents.SCREWDRIVER_TOOL)
                .oreDict(ToolOreDicts.craftingToolScrewdriver)
                .symbol('d')
                .toolClasses(ToolClasses.SCREWDRIVER));
        MORTAR = register(ItemGTTool.Builder.of(GTValues.MODID, "mortar")
                .toolStats(b -> b.crafting())
                .sound(GTSoundEvents.MORTAR_TOOL)
                .oreDict(ToolOreDicts.craftingToolMortar)
                .symbol('m')
                .toolClasses(ToolClasses.MORTAR));
        WIRE_CUTTER = register(ItemGTTool.Builder.of(GTValues.MODID, "wire_cutter")
                .toolStats(b -> b.blockBreaking().crafting())
                .sound(GTSoundEvents.WIRECUTTER_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWireCutter)
                .symbol('x')
                .toolClasses(ToolClasses.WIRE_CUTTER));
        SCYTHE = register(ItemGTSword.Builder.of(GTValues.MODID, "scythe")
                .toolStats(b -> b.blockBreaking().attacking()
                        .aoe(2, 2, 2)
                        .behaviors(new HoeGroundBehavior(), new HarvestCropsBehavior()).canApplyEnchantment(EnumEnchantmentType.DIGGER))
                .toolClasses(ToolClasses.SCYTHE, ToolClasses.HOE));
        KNIFE = register(ItemGTTool.Builder.of(GTValues.MODID, "knife")
                .toolStats(b -> b.crafting().attacking())
                .oreDict(ToolOreDicts.craftingToolKnife)
                .symbol('k')
                .toolClasses(ToolClasses.KNIFE, ToolClasses.SWORD));
        BUTCHERY_KNIFE = register(ItemGTSword.Builder.of(GTValues.MODID, "butchery_knife")
                .toolStats(b -> b.crafting().attacking())
                .oreDict(ToolOreDicts.craftingToolButcheryKnife)
                .toolClasses(ToolClasses.BUTCHERY_KNIFE));
        DRILL_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_lv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(new TorchPlaceBehavior()))
                .oreDict(ToolOreDicts.craftingToolDrill)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.LV));
        DRILL_MV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_mv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 2)
                        .efficiencyMultiplier(2.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_MV)
                        .behaviors(new TorchPlaceBehavior()))
                .oreDict(ToolOreDicts.craftingToolDrill)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.MV));
        DRILL_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_hv")
                .toolStats(b -> b.blockBreaking().aoe(2, 2, 4)
                        .efficiencyMultiplier(3.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV)
                        .behaviors(new TorchPlaceBehavior()))
                .oreDict(ToolOreDicts.craftingToolDrill)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.HV));
        if (ConfigHolder.tools.enableHighTierDrills) {
            DRILL_EV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_ev")
                    .toolStats(b -> b.blockBreaking().aoe(3, 3, 6)
                            .efficiencyMultiplier(4.0F)
                            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_EV)
                            .behaviors(new TorchPlaceBehavior()))
                    .oreDict(ToolOreDicts.craftingToolDrill)
                    .sound(GTSoundEvents.DRILL_TOOL, true)
                    .toolClasses(ToolClasses.DRILL)
                    .electric(GTValues.EV));
            DRILL_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_iv")
                    .toolStats(b -> b.blockBreaking().aoe(4, 4, 8)
                            .efficiencyMultiplier(5.0F)
                            .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV)
                            .behaviors(new TorchPlaceBehavior()))
                    .oreDict(ToolOreDicts.craftingToolDrill)
                    .sound(GTSoundEvents.DRILL_TOOL, true)
                    .toolClasses(ToolClasses.DRILL)
                    .electric(GTValues.IV));
        }
        CHAINSAW_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "chainsaw_lv")
                .toolStats(b -> b.blockBreaking()
                        .efficiencyMultiplier(2.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(new HarvestIceBehavior(), new DisableShieldBehavior(), new TreeFellingBehavior()))
                .sound(GTSoundEvents.CHAINSAW_TOOL, true)
                .toolClasses(ToolClasses.AXE)
                .electric(GTValues.LV));
        WRENCH_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_lv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(2.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWrench)
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.LV));
        WRENCH_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_hv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(8.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWrench)
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.HV));
        WRENCH_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_iv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(16.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWrench)
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.IV));
        BUZZSAW = register(ItemGTTool.Builder.of(GTValues.MODID, "buzzsaw")
                .toolStats(b -> b.crafting().brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.CHAINSAW_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolSaw)
                .toolClasses(ToolClasses.SAW)
                .electric(GTValues.LV));
        SCREWDRIVER_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "screwdriver_lv")
                .toolStats(b -> b.crafting().sneakBypassUse()
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.SCREWDRIVER_TOOL)
                .oreDict(ToolOreDicts.craftingToolScrewdriver)
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
}
