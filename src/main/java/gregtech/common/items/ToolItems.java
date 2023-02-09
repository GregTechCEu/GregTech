package gregtech.common.items;

import gregtech.api.GTValues;
import gregtech.api.items.toolitem.*;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Materials;
import gregtech.common.items.tool.*;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class ToolItems {

    private static final List<IGTTool> TOOLS = new ArrayList<>();

    private ToolItems() {/**/}

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
    public static IGTTool PLUNGER;

    public static void init() {
        SWORD = register(ItemGTSword.Builder.of(GTValues.MODID, "sword")
                        .toolStats(b -> b.attacking()
                                .attackDamage(3.0F).attackSpeed(-2.4F))
                        .toolClasses(ToolClasses.SWORD));
        PICKAXE = register(ItemGTTool.Builder.of(GTValues.MODID, "pickaxe")
                .toolStats(b -> b.blockBreaking().attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .toolClasses(ToolClasses.PICKAXE));
        SHOVEL = register(ItemGTTool.Builder.of(GTValues.MODID, "shovel")
                .toolStats(b -> b.blockBreaking().attackDamage(1.5F).attackSpeed(-3.0F)
                        .behaviors(GrassPathBehavior.INSTANCE))
                .toolClasses(ToolClasses.SHOVEL));
        AXE = register(ItemGTAxe.Builder.of(GTValues.MODID, "axe")
                .toolStats(b -> b.blockBreaking()
                        .attackDamage(5.0F).attackSpeed(-3.2F).baseEfficiency(2.0F)
                        .behaviors(DisableShieldBehavior.INSTANCE, TreeFellingBehavior.INSTANCE))
                .toolClasses(ToolClasses.AXE));
        HOE = register(ItemGTHoe.Builder.of(GTValues.MODID, "hoe")
                .toolStats(b -> b.cannotAttack().attackSpeed(-1.0F))
                .toolClasses(ToolClasses.HOE));
        SAW = register(ItemGTTool.Builder.of(GTValues.MODID, "saw")
                .toolStats(b -> b.crafting().damagePerCraftingAction(2)
                        .attackDamage(-1.0F).attackSpeed(-2.6F)
                        .behaviors(HarvestIceBehavior.INSTANCE))
                .oreDict(ToolOreDicts.craftingToolSaw)
                .symbol('s')
                .toolClasses(ToolClasses.SAW));
        HARD_HAMMER = register(ItemGTTool.Builder.of(GTValues.MODID, "hammer")
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(2)
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(new EntityDamageBehavior(2.0F, EntityGolem.class)))
                .oreDict(ToolOreDicts.craftingToolHammer)
                .sound(SoundEvents.BLOCK_ANVIL_LAND)
                .symbol('h')
                .toolClasses(ToolClasses.PICKAXE, ToolClasses.HARD_HAMMER));
        SOFT_MALLET = register(ItemGTTool.Builder.of(GTValues.MODID, "mallet")
                .toolStats(b -> b.crafting().cannotAttack().attackSpeed(-2.4F))
                .oreDict(ToolOreDicts.craftingToolMallet)
                .sound(GTSoundEvents.SOFT_MALLET_TOOL)
                .symbol('r')
                .toolClasses(ToolClasses.SOFT_MALLET)
                .markerItem(() -> ToolHelper.getAndSetToolData(SOFT_MALLET, Materials.Wood, 48, 1, 4F, 1F)));
        MINING_HAMMER = register(ItemGTTool.Builder.of(GTValues.MODID, "mining_hammer")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .efficiencyMultiplier(0.4F).attackDamage(1.5F).attackSpeed(-3.2F)
                        .durabilityMultiplier(3.0F)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .toolClasses(ToolClasses.PICKAXE));
        SPADE = register(ItemGTTool.Builder.of(GTValues.MODID, "spade")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .efficiencyMultiplier(0.4F).attackDamage(1.5F).attackSpeed(-3.2F)
                        .durabilityMultiplier(3.0F)
                        .behaviors(GrassPathBehavior.INSTANCE))
                .toolClasses(ToolClasses.SHOVEL));
        WRENCH = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, EntityGolem.class)))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWrench)
                .symbol('w')
                .toolClasses(ToolClasses.WRENCH));
        FILE = register(ItemGTTool.Builder.of(GTValues.MODID, "file")
                .toolStats(b -> b.crafting().damagePerCraftingAction(4)
                        .cannotAttack().attackSpeed(-2.4F))
                .sound(GTSoundEvents.FILE_TOOL)
                .oreDict(ToolOreDicts.craftingToolFile)
                .symbol('f')
                .toolClasses(ToolClasses.FILE));
        CROWBAR = register(ItemGTTool.Builder.of(GTValues.MODID, "crowbar")
                .toolStats(b -> b.blockBreaking().crafting()
                        .attackDamage(2.0F).attackSpeed(-2.4F)
                        .sneakBypassUse().behaviors(RotateRailBehavior.INSTANCE))
                .sound(SoundEvents.ENTITY_ITEM_BREAK)
                .oreDict(ToolOreDicts.craftingToolCrowbar)
                .symbol('c')
                .toolClasses(ToolClasses.CROWBAR));
        SCREWDRIVER = register(ItemGTTool.Builder.of(GTValues.MODID, "screwdriver")
                .toolStats(b -> b.crafting().damagePerCraftingAction(4).sneakBypassUse()
                        .attackDamage(-1.0F).attackSpeed(3.0F)
                        .behaviors(new EntityDamageBehavior(3.0F, EntitySpider.class)))
                .sound(GTSoundEvents.SCREWDRIVER_TOOL)
                .oreDict(ToolOreDicts.craftingToolScrewdriver)
                .symbol('d')
                .toolClasses(ToolClasses.SCREWDRIVER));
        MORTAR = register(ItemGTTool.Builder.of(GTValues.MODID, "mortar")
                .toolStats(b -> b.crafting().damagePerCraftingAction(2)
                        .cannotAttack().attackSpeed(-2.4F))
                .sound(GTSoundEvents.MORTAR_TOOL)
                .oreDict(ToolOreDicts.craftingToolMortar)
                .symbol('m')
                .toolClasses(ToolClasses.MORTAR));
        WIRE_CUTTER = register(ItemGTTool.Builder.of(GTValues.MODID, "wire_cutter")
                .toolStats(b -> b.blockBreaking().crafting().damagePerCraftingAction(4)
                        .attackDamage(-1.0F).attackSpeed(-2.4F))
                .sound(GTSoundEvents.WIRECUTTER_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWireCutter)
                .symbol('x')
                .toolClasses(ToolClasses.WIRE_CUTTER));
        SCYTHE = register(ItemGTSword.Builder.of(GTValues.MODID, "scythe")
                .toolStats(b -> b.blockBreaking().attacking()
                        .attackDamage(5.0F).attackSpeed(-3.0F).durabilityMultiplier(3.0F)
                        .aoe(2, 2, 2)
                        .behaviors(HoeGroundBehavior.INSTANCE, HarvestCropsBehavior.INSTANCE).canApplyEnchantment(EnumEnchantmentType.DIGGER))
                .toolClasses(ToolClasses.SCYTHE, ToolClasses.HOE));
        KNIFE = register(ItemGTSword.Builder.of(GTValues.MODID, "knife")
                .toolStats(b -> b.crafting().attacking().attackSpeed(3.0F))
                .oreDict(ToolOreDicts.craftingToolKnife)
                .symbol('k')
                .toolClasses(ToolClasses.KNIFE, ToolClasses.SWORD));
        BUTCHERY_KNIFE = register(ItemGTSword.Builder.of(GTValues.MODID, "butchery_knife")
                .toolStats(b -> b.crafting().attacking()
                        .attackDamage(1.5F).attackSpeed(-1.3F))
                .oreDict(ToolOreDicts.craftingToolButcheryKnife)
                .toolClasses(ToolClasses.BUTCHERY_KNIFE));
        DRILL_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_lv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 0)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(3.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDicts.craftingToolDrill)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.LV));
        DRILL_MV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_mv")
                .toolStats(b -> b.blockBreaking().aoe(1, 1, 2)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(4.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_MV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDicts.craftingToolDrill)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.MV));
        DRILL_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_hv")
                .toolStats(b -> b.blockBreaking().aoe(2, 2, 4)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(5.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDicts.craftingToolDrill)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.HV));
        DRILL_EV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_ev")
                .toolStats(b -> b.blockBreaking().aoe(3, 3, 6)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(6.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_EV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDicts.craftingToolDrill)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.EV));
        DRILL_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "drill_iv")
                .toolStats(b -> b.blockBreaking().aoe(4, 4, 8)
                        .attackDamage(1.0F).attackSpeed(-3.2F).durabilityMultiplier(7.0F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV)
                        .behaviors(TorchPlaceBehavior.INSTANCE))
                .oreDict(ToolOreDicts.craftingToolDrill)
                .sound(GTSoundEvents.DRILL_TOOL, true)
                .toolClasses(ToolClasses.DRILL)
                .electric(GTValues.IV));

        CHAINSAW_LV = register(ItemGTAxe.Builder.of(GTValues.MODID, "chainsaw_lv")
                .toolStats(b -> b.blockBreaking()
                        .efficiencyMultiplier(2.0F)
                        .attackDamage(5.0F).attackSpeed(-3.2F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV)
                        .behaviors(HarvestIceBehavior.INSTANCE, DisableShieldBehavior.INSTANCE, TreeFellingBehavior.INSTANCE))
                .sound(GTSoundEvents.CHAINSAW_TOOL, true)
                .toolClasses(ToolClasses.AXE)
                .electric(GTValues.LV));
        WRENCH_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_lv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(2.0F)
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, EntityGolem.class))
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWrench)
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.LV));
        WRENCH_HV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_hv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(3.0F)
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, EntityGolem.class))
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_HV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWrench)
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.HV));
        WRENCH_IV = register(ItemGTTool.Builder.of(GTValues.MODID, "wrench_iv")
                .toolStats(b -> b.blockBreaking().crafting().sneakBypassUse()
                        .efficiencyMultiplier(4.0F)
                        .attackDamage(1.0F).attackSpeed(-2.8F)
                        .behaviors(BlockRotatingBehavior.INSTANCE, new EntityDamageBehavior(3.0F, EntityGolem.class))
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_IV))
                .sound(GTSoundEvents.WRENCH_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolWrench)
                .toolClasses(ToolClasses.WRENCH)
                .electric(GTValues.IV));
        BUZZSAW = register(ItemGTTool.Builder.of(GTValues.MODID, "buzzsaw")
                .toolStats(b -> b.crafting().attackDamage(1.5F).attackSpeed(-3.2F)
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.CHAINSAW_TOOL, true)
                .oreDict(ToolOreDicts.craftingToolSaw)
                .toolClasses(ToolClasses.SAW)
                .electric(GTValues.LV));
        SCREWDRIVER_LV = register(ItemGTTool.Builder.of(GTValues.MODID, "screwdriver_lv")
                .toolStats(b -> b.crafting().sneakBypassUse()
                        .attackDamage(-1.0F).attackSpeed(3.0F)
                        .behaviors(new EntityDamageBehavior(3.0F, EntitySpider.class))
                        .brokenStack(ToolHelper.SUPPLY_POWER_UNIT_LV))
                .sound(GTSoundEvents.SCREWDRIVER_TOOL)
                .oreDict(ToolOreDicts.craftingToolScrewdriver)
                .toolClasses(ToolClasses.SCREWDRIVER)
                .electric(GTValues.LV));
        PLUNGER = register(ItemGTTool.Builder.of(GTValues.MODID, "plunger")
                .toolStats(b -> b.cannotAttack().attackSpeed(-2.4F).sneakBypassUse()
                        .behaviors(PlungerBehavior.INSTANCE))
                .sound(GTSoundEvents.PLUNGER_TOOL)
                .toolClasses(ToolClasses.PLUNGER)
                .markerItem(() -> ToolHelper.getAndSetToolData(PLUNGER, Materials.Rubber, 256, 1, 4F, 0F)));
    }

    public static IGTTool register(@Nonnull ToolBuilder<?> builder) {
        IGTTool tool = builder.build();
        TOOLS.add(tool);
        return tool;
    }

    public static IGTTool register(@Nonnull IGTTool tool) {
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
