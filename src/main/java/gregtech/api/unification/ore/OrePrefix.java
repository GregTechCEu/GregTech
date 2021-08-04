package gregtech.api.unification.ore;

import com.google.common.base.Preconditions;
import gregtech.api.unification.material.MarkerMaterials;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.properties.IMaterialProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.util.function.TriConsumer;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static gregtech.api.GTValues.M;
import static gregtech.api.unification.material.info.MaterialFlags.*;
import static gregtech.api.unification.ore.OrePrefix.Conditions.*;
import static gregtech.api.unification.ore.OrePrefix.Flags.*;

public enum OrePrefix {

    ore("Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // Regular Ore Prefix. Ore -> Material is a Oneway Operation! Introduced by Eloraam

    oreGranite("Granite Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // Regular Ore Prefix. Ore -> Material is a Oneway Operation! Introduced by Eloraam
    oreDiorite("Diorite Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // Regular Ore Prefix. Ore -> Material is a Oneway Operation! Introduced by Eloraam
    oreAndesite("Andesite Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // Regular Ore Prefix. Ore -> Material is a Oneway Operation! Introduced by Eloraam

    oreBlackgranite("Black Granite Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!
    oreRedgranite("Red Granite Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!

    oreMarble("Marble Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!
    oreBasalt("Basalt Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!

    oreSand("Sand Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, null), // In case of an Sand-Ores Mod. Ore -> Material is a Oneway Operation!
    oreRedSand("Red Sand Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, null), // In case of an Sand-Ores Mod. Ore -> Material is a Oneway Operation!
    oreGravel("Gravel Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, null), // In case of an Gravel-Ores Mod. Ore -> Material is a Oneway Operation!

    oreNetherrack("Netherrack Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // Prefix of the Nether-Ores Mod. Causes Ores to double. Ore -> Material is a Oneway Operation!
    oreEndstone("Endstone Ores", -1, null, MaterialIconType.ore, ENABLE_UNIFICATION, hasOreProperty), // In case of an End-Ores Mod. Ore -> Material is a Oneway Operation!

    crushedCentrifuged("Centrifuged Ores", -1, null, MaterialIconType.crushedCentrifuged, ENABLE_UNIFICATION, hasOreProperty),
    crushedPurified("Purified Ores", -1, null, MaterialIconType.crushedPurified, ENABLE_UNIFICATION, hasOreProperty),
    crushed("Crushed Ores", -1, null, MaterialIconType.crushed, ENABLE_UNIFICATION, hasOreProperty),

    shard("Crystallised Shards", -1, null, null, ENABLE_UNIFICATION, null), // Introduced by Mekanism
    clump("Clumps", -1, null, null, ENABLE_UNIFICATION, null),
    reduced("Reduced Gravels", -1, null, null, ENABLE_UNIFICATION, null),
    crystalline("Crystallised Metals", -1, null, null, ENABLE_UNIFICATION, null),

    cleanGravel("Clean Gravels", -1, null, null, ENABLE_UNIFICATION, null),
    dirtyGravel("Dirty Gravels", -1, null, null, ENABLE_UNIFICATION, null),

    ingotHot("Hot Ingots", M, null, MaterialIconType.ingotHot, ENABLE_UNIFICATION, hasBlastProperty.and(mat -> mat.getProperty(PropertyKey.BLAST).getBlastTemperature() > 1750)), // A hot Ingot, which has to be cooled down by a Vacuum Freezer.
    ingot("Ingots", M, null, MaterialIconType.ingot, ENABLE_UNIFICATION, hasIngotProperty), // A regular Ingot. Introduced by Eloraam

    gem("Gemstones", M, null, MaterialIconType.gem, ENABLE_UNIFICATION, hasGemProperty), // A regular Gem worth one Dust. Introduced by Eloraam
    gemChipped("Chipped Gemstones", M / 4, null, MaterialIconType.gemChipped, ENABLE_UNIFICATION, hasGemProperty), // A regular Gem worth one small Dust. Introduced by TerraFirmaCraft
    gemFlawed("Flawed Gemstones", M / 2, null, MaterialIconType.gemFlawed, ENABLE_UNIFICATION, hasGemProperty), // A regular Gem worth two small Dusts. Introduced by TerraFirmaCraft
    gemFlawless("Flawless Gemstones", M * 2, null, MaterialIconType.gemFlawless, ENABLE_UNIFICATION, hasGemProperty), // A regular Gem worth two Dusts. Introduced by TerraFirmaCraft
    gemExquisite("Exquisite Gemstones", M * 4, null, MaterialIconType.gemExquisite, ENABLE_UNIFICATION, hasGemProperty), // A regular Gem worth four Dusts. Introduced by TerraFirmaCraft

    dustSmall("Small Dusts", M / 4, null, MaterialIconType.dustSmall, ENABLE_UNIFICATION, hasDustProperty), // 1/4th of a Dust.
    dustTiny("Tiny Dusts", M / 9, null, MaterialIconType.dustTiny, ENABLE_UNIFICATION, hasDustProperty), // 1/9th of a Dust.
    dustImpure("Impure Dusts", M, null, MaterialIconType.dustImpure, ENABLE_UNIFICATION, hasOreProperty), // Dust with impurities. 1 Unit of Main Material and 1/9 - 1/4 Unit of secondary Material
    dustPure("Purified Dusts", M, null, MaterialIconType.dustPure, ENABLE_UNIFICATION, hasOreProperty),
    dust("Dusts", M, null, MaterialIconType.dust, ENABLE_UNIFICATION, hasDustProperty), // Pure Dust worth of one Ingot or Gem. Introduced by Alblaka.

    nugget("Nuggets", M / 9, null, MaterialIconType.nugget, ENABLE_UNIFICATION, hasIngotProperty), // A Nugget. Introduced by Eloraam

    plateCurved("Curved Plates", M, null, MaterialIconType.plateCurved, ENABLE_UNIFICATION, null),
    plateDense("Dense Plates", M * 9, null, MaterialIconType.plateDense, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_DENSE) && !mat.hasFlag(NO_SMASHING)), // 9 Plates combined in one Item.
    plateDouble("Double Plates", M * 2, null, MaterialIconType.plateDouble, ENABLE_UNIFICATION, hasIngotProperty.and(mat -> mat.hasFlag(GENERATE_PLATE) && !mat.hasFlag(NO_SMASHING))), // 2 Plates combined in one Item
    plate("Plates", M, null, MaterialIconType.plate, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_PLATE)), // Regular Plate made of one Ingot/Dust. Introduced by Calclavia
    compressed("Compressed Materials", M * 2, null, null, ENABLE_UNIFICATION, null), // Compressed Material, worth 1 Unit. Introduced by Galacticraft

    round("Rounds", M / 9, null, MaterialIconType.round, OrePrefix.Flags.ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROUND)), // Round made of 1 Nugget
    foil("Foils", M / 4, null, MaterialIconType.foil, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_FOIL)), // Foil made of 1/4 Ingot/Dust.

    stickLong("Long Sticks/Rods", M, null, MaterialIconType.stickLong, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_LONG_ROD)), // Stick made of an Ingot.
    stick("Sticks/Rods", M / 2, null, MaterialIconType.stick, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROD)), // Stick made of half an Ingot. Introduced by Eloraam

    bolt("Bolts", M / 8, null, MaterialIconType.bolt, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BOLT_SCREW)), // consisting out of 1/8 Ingot or 1/4 Stick.
    screw("Screws", M / 9, null, MaterialIconType.screw, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_BOLT_SCREW)), // consisting out of a Bolt.
    ring("Rings", M / 4, null, MaterialIconType.ring, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_RING)), // consisting out of 1/2 Stick.
    springSmall("Small Springs", M / 4, null, MaterialIconType.springSmall, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SPRING_SMALL) && !mat.hasFlag(NO_SMASHING)), // consisting out of 1 Fine Wire.
    spring("Springs", M, null, MaterialIconType.spring, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SPRING) && !mat.hasFlag(NO_SMASHING)), // consisting out of 2 Sticks.
    wireFine("Fine Wires", M / 8, null, MaterialIconType.wireFine, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_FINE_WIRE)), // consisting out of 1/8 Ingot or 1/4 Wire.
    rotor("Rotors", M * 4, null, MaterialIconType.rotor, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_ROTOR)), // consisting out of 4 Plates, 1 Ring and 1 Screw.
    gearSmall("Small Gears", M, null, MaterialIconType.gearSmall, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_SMALL_GEAR)),
    gear("Gears", M * 4, null, MaterialIconType.gear, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_GEAR)), // Introduced by me because BuildCraft has ruined the gear Prefix...
    lens("Lenses", (M * 3) / 4, null, MaterialIconType.lens, ENABLE_UNIFICATION, mat -> mat.hasFlag(GENERATE_LENS)), // 3/4 of a Plate or Gem used to shape a Lense. Normally only used on Transparent Materials.

    toolHeadSword("Sword Blades", M * 2, null, MaterialIconType.toolHeadSword, ENABLE_UNIFICATION, hasToolProperty), // made of 2 Ingots.
    toolHeadPickaxe("Pickaxe Heads", M * 3, null, MaterialIconType.toolHeadPickaxe, ENABLE_UNIFICATION, hasToolProperty), // made of 3 Ingots.
    toolHeadShovel("Shovel Heads", M, null, MaterialIconType.toolHeadShovel, ENABLE_UNIFICATION, hasToolProperty), // made of 1 Ingots.
    toolHeadUniversalSpade("Universal Spade Heads", M, null, MaterialIconType.toolHeadUniversalSpade, ENABLE_UNIFICATION, hasToolProperty), // made of 1 Ingots.
    toolHeadAxe("Axe Heads", M * 3, null, MaterialIconType.toolHeadAxe, ENABLE_UNIFICATION, hasToolProperty), // made of 3 Ingots.
    toolHeadHoe("Hoe Heads", M * 2, null, MaterialIconType.toolHeadHoe, ENABLE_UNIFICATION, hasToolProperty), // made of 2 Ingots.
    toolHeadSense("Sense Blades", M * 3, null, MaterialIconType.toolHeadSense, ENABLE_UNIFICATION, hasToolProperty), // made of 3 Ingots.
    toolHeadFile("File Heads", M * 2, null, MaterialIconType.toolHeadFile, ENABLE_UNIFICATION, hasToolProperty), // made of 2 Ingots.
    toolHeadHammer("Hammer Heads", M * 6, null, MaterialIconType.toolHeadHammer, ENABLE_UNIFICATION, hasToolProperty), // made of 6 Ingots.
    toolHeadSaw("Saw Blades", M * 2, null, MaterialIconType.toolHeadSaw, ENABLE_UNIFICATION, hasToolProperty), // made of 2 Ingots.
    toolHeadBuzzSaw("Buzzsaw Blades", M * 4, null, MaterialIconType.toolHeadBuzzSaw, ENABLE_UNIFICATION, hasToolProperty), // made of 4 Ingots.
    toolHeadScrewdriver("Screwdriver Tips", M, null, MaterialIconType.toolHeadScrewdriver, ENABLE_UNIFICATION, hasToolProperty), // made of 1 Ingots.
    toolHeadDrill("Drill Tips", M * 4, null, MaterialIconType.toolHeadDrill, ENABLE_UNIFICATION, hasToolProperty), // made of 4 Ingots.
    toolHeadChainsaw("Chainsaw Tips", M * 2, null, MaterialIconType.toolHeadChainsaw, ENABLE_UNIFICATION, hasToolProperty), // made of 2 Ingots.
    toolHeadWrench("Wrench Tips", M * 4, null, MaterialIconType.toolHeadWrench, ENABLE_UNIFICATION, hasToolProperty), // made of 4 Ingots.
    turbineBlade("Turbine Blades", M * 10, null, MaterialIconType.turbineBlade, ENABLE_UNIFICATION, hasToolProperty.and(m -> m.hasFlags(GENERATE_BOLT_SCREW, GENERATE_PLATE))), // made of 5 Ingots.

    glass("Glasses", -1, Materials.Glass, null, SELF_REFERENCING, null),
    paneGlass("Glass Panes", -1, MarkerMaterials.Color.Colorless, null, SELF_REFERENCING, null),
    blockGlass("Glass Blocks", -1, MarkerMaterials.Color.Colorless, null, SELF_REFERENCING, null),

    blockWool("Wool Blocks", -1, MarkerMaterials.Color.Colorless, null, SELF_REFERENCING, null),
    block("Storage Blocks", M * 9, null, MaterialIconType.block, ENABLE_UNIFICATION, null), // Storage Block consisting out of 9 Ingots/Gems/Dusts. Introduced by CovertJaguar

    log("Logs", -1, null, null, 0, null), // Prefix used for Logs. Usually as "logWood". Introduced by Eloraam
    plank("Planks", -1, null, null, 0, null), // Prefix for Planks. Usually "plankWood". Introduced by Eloraam

    stone("Stones", -1, Materials.Stone, null, SELF_REFERENCING, null), // Prefix to determine which kind of Rock this is.
    cobblestone("Cobblestones", -1, Materials.Stone, null, SELF_REFERENCING, null),
    rock("Rocks", -1, Materials.Stone, null, SELF_REFERENCING, null), // Prefix to determine which kind of Rock this is.
    stoneCobble("Cobblestones", -1, Materials.Stone, null, SELF_REFERENCING, null), // Cobblestone Prefix for all Cobblestones.

    frameGt("Frame Boxes", (long) (M * 1.375), null, null, ENABLE_UNIFICATION, material -> material.hasFlag(GENERATE_FRAME)),

    pipeTinyFluid("Tiny Fluid Pipes", M / 2, null, MaterialIconType.pipeTiny, ENABLE_UNIFICATION, null),
    pipeSmallFluid("Small Fluid Pipes", M, null, MaterialIconType.pipeSmall, ENABLE_UNIFICATION, null),
    pipeNormalFluid("Normal Fluid Pipes", M * 3, null, MaterialIconType.pipeMedium, ENABLE_UNIFICATION, null),
    pipeLargeFluid("Large Fluid Pipes", M * 6, null, MaterialIconType.pipeLarge, ENABLE_UNIFICATION, null),
    pipeHugeFluid("Huge Fluid Pipes", M * 12, null, MaterialIconType.pipeHuge, ENABLE_UNIFICATION, null),

    pipeTinyItem("Tiny Item Pipes", M / 2, null, MaterialIconType.pipeTiny, ENABLE_UNIFICATION, null),
    pipeSmallItem("Small Item Pipes", M, null, MaterialIconType.pipeSmall, ENABLE_UNIFICATION, null),
    pipeNormalItem("Normal Item Pipes", M * 3, null, MaterialIconType.pipeMedium, ENABLE_UNIFICATION, null),
    pipeLargeItem("Large Item Pipes", M * 6, null, MaterialIconType.pipeLarge, ENABLE_UNIFICATION, null),
    pipeHugeItem("Huge Item Pipes", M * 12, null, MaterialIconType.pipeHuge, ENABLE_UNIFICATION, null),

    pipeSmallRestrictive("Small Restrictive Item Pipes", M, null, MaterialIconType.pipeSmall, ENABLE_UNIFICATION, null),
    pipeNormalRestrictive("Normal Restrictive Item Pipes", M * 3, null, MaterialIconType.pipeMedium, ENABLE_UNIFICATION, null),
    pipeLargeRestrictive("Large Restrictive Item Pipes", M * 6, null, MaterialIconType.pipeLarge, ENABLE_UNIFICATION, null),

    wireGtHex("Hex wires", M * 8, null, null, ENABLE_UNIFICATION, null),
    wireGtOctal("Octal wires", M * 4, null, null, ENABLE_UNIFICATION, null),
    wireGtQuadruple("Quadruple wires", M * 2, null, null, ENABLE_UNIFICATION, null),
    wireGtDouble("Double wires", M, null, null, ENABLE_UNIFICATION, null),
    wireGtSingle("Single wires", M / 2, null, null, ENABLE_UNIFICATION, null),

    cableGtHex("Hex cables", M * 8, null, null, ENABLE_UNIFICATION, null),
    cableGtOctal("Octal cables", M * 4, null, null, ENABLE_UNIFICATION, null),
    cableGtQuadruple("Quadruple cables", M * 2, null, null, ENABLE_UNIFICATION, null),
    cableGtDouble("Double cables", M, null, null, ENABLE_UNIFICATION, null),
    cableGtSingle("Single cables", M / 2, null, null, ENABLE_UNIFICATION, null),

    craftingLens("Crafting Ingredients", -1, null, null, 0, null), // Special Prefix used mainly for the Crafting Handler.
    dye("Dyes", -1, null, null, 0, null), // Used for the 16 dyes. Introduced by Eloraam

    /**
     * Electric Components.
     *
     * @see MarkerMaterials.Tier
     */
    batterySingleUse("Single Use Batteries", -1, null, null, 0, null),
    battery("Reusable Batteries", -1, null, null, 0, null), // Introduced by Calclavia
    circuit("Circuits", -1, null, null, ENABLE_UNIFICATION, null), // Introduced by Calclavia
    chipset("Chipsets", -1, null, null, ENABLE_UNIFICATION, null), // Introduced by Buildcraft
    component("Components", -1, null, null, ENABLE_UNIFICATION, null),

    // Used for Gregification Addon

    // Ex Nihilo Compat
    oreChunk("Ore Chunk", -1, null, MaterialIconType.oreChunk, ENABLE_UNIFICATION, null),
    oreEnderChunk("Ore Chunk", -1, null, MaterialIconType.oreEnderChunk, ENABLE_UNIFICATION, null),
    oreNetherChunk("Ore Chunk", -1, null, MaterialIconType.oreNetherChunk, ENABLE_UNIFICATION, null),
    oreSandyChunk("Ore Chunk", -1, null, MaterialIconType.oreSandyChunk, ENABLE_UNIFICATION, null),

    // Myst Ag Compat
    seed("Seed", -1, null, MaterialIconType.seed, ENABLE_UNIFICATION, null),
    crop("Crop", -1, null, MaterialIconType.crop, ENABLE_UNIFICATION, null),
    essence("Essence", -1, null, MaterialIconType.essence, ENABLE_UNIFICATION, null),
    ;

    public static final String DUST_REGULAR = "dustRegular";

    public static class Flags {
        public static final long ENABLE_UNIFICATION = 1;
        public static final long SELF_REFERENCING = 1 << 1;
    }

    public static class Conditions {
        public static final Predicate<Material> hasToolProperty = mat -> mat.hasProperty(PropertyKey.TOOL);
        public static final Predicate<Material> hasOreProperty = mat -> mat.hasProperty(PropertyKey.ORE);
        public static final Predicate<Material> hasGemProperty = mat -> mat.hasProperty(PropertyKey.GEM);
        public static final Predicate<Material> hasDustProperty = mat -> mat.hasProperty(PropertyKey.DUST);
        public static final Predicate<Material> hasIngotProperty = mat -> mat.hasProperty(PropertyKey.INGOT);
        public static final Predicate<Material> hasBlastProperty = mat -> mat.hasProperty(PropertyKey.BLAST);
    }

    static {
        ingotHot.heatDamage = 3.0F;
        ingotHot.maxStackSize = 16;
        gemFlawless.maxStackSize = 32;
        gemExquisite.maxStackSize = 16;

        plateDense.maxStackSize = 7;
        rotor.maxStackSize = 16;
        gear.maxStackSize = 16;

        toolHeadSword.maxStackSize = 16;
        toolHeadPickaxe.maxStackSize = 16;
        toolHeadShovel.maxStackSize = 16;
        toolHeadUniversalSpade.maxStackSize = 16;
        toolHeadAxe.maxStackSize = 16;
        toolHeadHoe.maxStackSize = 16;
        toolHeadSense.maxStackSize = 16;
        toolHeadFile.maxStackSize = 16;
        toolHeadHammer.maxStackSize = 16;
        toolHeadSaw.maxStackSize = 16;
        toolHeadBuzzSaw.maxStackSize = 16;
        toolHeadScrewdriver.maxStackSize = 16;
        toolHeadDrill.maxStackSize = 16;
        toolHeadChainsaw.maxStackSize = 16;
        toolHeadWrench.maxStackSize = 16;

        craftingLens.setMarkerPrefix(true);
        dye.setMarkerPrefix(true);
        batterySingleUse.setMarkerPrefix(true);
        battery.setMarkerPrefix(true);
        circuit.setMarkerPrefix(true);
        chipset.setMarkerPrefix(true);

        gem.setIgnored(Materials.Diamond);
        gem.setIgnored(Materials.Emerald);
        gem.setIgnored(Materials.Lapis);
        gem.setIgnored(Materials.NetherQuartz);
        gem.setIgnored(Materials.Coal);

        excludeAllGems(Materials.Charcoal);
        excludeAllGems(Materials.NetherStar);
        excludeAllGems(Materials.EnderPearl);
        excludeAllGems(Materials.EnderEye);
        excludeAllGems(Materials.Flint);

        dust.setIgnored(Materials.Redstone);
        dust.setIgnored(Materials.Glowstone);
        dust.setIgnored(Materials.Gunpowder);
        dust.setIgnored(Materials.Sugar);
        dust.setIgnored(Materials.Bone);
        dust.setIgnored(Materials.Blaze);

        stick.setIgnored(Materials.Wood);
        stick.setIgnored(Materials.Bone);
        stick.setIgnored(Materials.Blaze);
        stick.setIgnored(Materials.Paper);

        ingot.setIgnored(Materials.Iron);
        ingot.setIgnored(Materials.Gold);
        ingot.setIgnored(Materials.Wood);
        ingot.setIgnored(Materials.Paper);

        nugget.setIgnored(Materials.Wood);
        nugget.setIgnored(Materials.Gold);
        nugget.setIgnored(Materials.Paper);
        nugget.setIgnored(Materials.Iron);
        plate.setIgnored(Materials.Paper);

        block.setIgnored(Materials.Iron);
        block.setIgnored(Materials.Gold);
        block.setIgnored(Materials.Lapis);
        block.setIgnored(Materials.Emerald);
        block.setIgnored(Materials.Redstone);
        block.setIgnored(Materials.Diamond);
        block.setIgnored(Materials.Coal);
        block.setIgnored(Materials.Glass);
        block.setIgnored(Materials.Marble);
        block.setIgnored(Materials.GraniteRed);
        block.setIgnored(Materials.Stone);
        block.setIgnored(Materials.Glowstone);
        block.setIgnored(Materials.Endstone);
        block.setIgnored(Materials.Wheat);
        block.setIgnored(Materials.Oilsands);
        block.setIgnored(Materials.Wood);
        block.setIgnored(Materials.RawRubber);
        block.setIgnored(Materials.Clay);
        block.setIgnored(Materials.Brick);
        block.setIgnored(Materials.Bone);
        block.setIgnored(Materials.NetherQuartz);
        block.setIgnored(Materials.Ice);
        block.setIgnored(Materials.Netherrack);
        block.setIgnored(Materials.Concrete);
        block.setIgnored(Materials.Blaze);

        ore.addSecondaryMaterial(new MaterialStack(Materials.Stone, dust.materialAmount));
        oreGranite.addSecondaryMaterial(new MaterialStack(Materials.Granite, dust.materialAmount));
        oreDiorite.addSecondaryMaterial(new MaterialStack(Materials.Diorite, dust.materialAmount));
        oreAndesite.addSecondaryMaterial(new MaterialStack(Materials.Andesite, dust.materialAmount));
        oreRedgranite.addSecondaryMaterial(new MaterialStack(Materials.GraniteRed, dust.materialAmount));
        oreBlackgranite.addSecondaryMaterial(new MaterialStack(Materials.GraniteBlack, dust.materialAmount));
        oreBasalt.addSecondaryMaterial(new MaterialStack(Materials.Basalt, dust.materialAmount));
        oreMarble.addSecondaryMaterial(new MaterialStack(Materials.Marble, dust.materialAmount));
        oreSand.addSecondaryMaterial(new MaterialStack(Materials.SiliconDioxide, dustTiny.materialAmount));
        oreRedSand.addSecondaryMaterial(new MaterialStack(Materials.SiliconDioxide, dustTiny.materialAmount));
        oreGravel.addSecondaryMaterial(new MaterialStack(Materials.Flint, dustTiny.materialAmount));
        oreNetherrack.addSecondaryMaterial(new MaterialStack(Materials.Netherrack, dust.materialAmount));
        oreEndstone.addSecondaryMaterial(new MaterialStack(Materials.Endstone, dust.materialAmount));

        crushed.addSecondaryMaterial(new MaterialStack(Materials.Stone, dust.materialAmount));

        toolHeadDrill.addSecondaryMaterial(new MaterialStack(Materials.Steel, plate.materialAmount * 4));
        toolHeadChainsaw.addSecondaryMaterial(new MaterialStack(Materials.Steel, plate.materialAmount * 4 + ring.materialAmount * 2));
        toolHeadWrench.addSecondaryMaterial(new MaterialStack(Materials.Steel, ring.materialAmount + screw.materialAmount * 2));

        pipeTinyFluid.setIgnored(Materials.Wood);
        pipeHugeFluid.setIgnored(Materials.Wood);
        plate.setIgnored(Materials.BorosilicateGlass);
        foil.setIgnored(Materials.BorosilicateGlass);
    }

    private static void excludeAllGems(Material material) {
        gem.setIgnored(material);
        gemChipped.setIgnored(material);
        gemFlawed.setIgnored(material);
        gemFlawless.setIgnored(material);
        gemExquisite.setIgnored(material);
    }

    public final String categoryName;

    public final boolean isUnificationEnabled;
    public final boolean isSelfReferencing;

    private @Nullable
    Predicate<Material> generationCondition;
    public final @Nullable
    MaterialIconType materialIconType;

    public final long materialAmount;

    /**
     * Contains a default material type for self-referencing OrePrefix
     * For self-referencing prefixes, it is always guaranteed for it to be not null
     * <p>
     * NOTE: Ore registrations with self-referencing OrePrefix still can occur with other materials
     */
    public @Nullable
    Material materialType;

    private final List<IOreRegistrationHandler> oreProcessingHandlers = new ArrayList<>();
    private final Set<Material> ignoredMaterials = new HashSet<>();
    private final Set<Material> generatedMaterials = new HashSet<>();
    private boolean isMarkerPrefix = false;

    public byte maxStackSize = 64;
    public final List<MaterialStack> secondaryMaterials = new ArrayList<>();
    public float heatDamage = 0.0F; // Negative for Frost Damage

    OrePrefix(String categoryName, long materialAmount, Material material, MaterialIconType materialIconType, long flags, Predicate<Material> condition) {
        this.categoryName = categoryName;
        this.materialAmount = materialAmount;
        this.isSelfReferencing = (flags & SELF_REFERENCING) != 0;
        this.isUnificationEnabled = (flags & ENABLE_UNIFICATION) != 0;
        this.materialIconType = materialIconType;
        this.generationCondition = condition;
        if (isSelfReferencing) {
            Preconditions.checkNotNull(material, "Material is null for self-referencing OrePrefix");
            this.materialType = material;
        }
    }

    public void addSecondaryMaterial(MaterialStack secondaryMaterial) {
        Preconditions.checkNotNull(secondaryMaterial, "secondaryMaterial");
        secondaryMaterials.add(secondaryMaterial);
    }

    public void setMarkerPrefix(boolean isMarkerPrefix) {
        this.isMarkerPrefix = isMarkerPrefix;
    }

    public long getMaterialAmount(Material material) {
        if (this == block) {
            //glowstone and nether quartz blocks use 4 gems (dusts)
            if (material == Materials.Glowstone ||
                    material == Materials.NetherQuartz ||
                    material == Materials.Brick ||
                    material == Materials.Clay)
                return M * 4;
                //glass, ice and obsidian gain only one dust
            else if (material == Materials.Glass ||
                    material == Materials.Ice ||
                    material == Materials.Obsidian)
                return M;
        } else if (this == stick) {
            if (material == Materials.Blaze)
                return M * 4;
            else if (material == Materials.Bone)
                return M * 5;
        }
        return materialAmount;
    }

    public static OrePrefix getPrefix(String prefixName) {
        return getPrefix(prefixName, null);
    }

    public static OrePrefix getPrefix(String prefixName, @Nullable OrePrefix replacement) {
        try {
            return Enum.valueOf(OrePrefix.class, prefixName);
        } catch (IllegalArgumentException invalidPrefixName) {
            return replacement;
        }
    }

    public boolean doGenerateItem(Material material) {
        return !isSelfReferencing && generationCondition != null && !isIgnored(material) && generationCondition.test(material);
    }

    public void setGenerationCondition(Predicate<Material> in) {
        generationCondition = in;
    }

    public boolean addProcessingHandler(IOreRegistrationHandler... processingHandler) {
        Preconditions.checkNotNull(processingHandler);
        Validate.noNullElements(processingHandler);
        return oreProcessingHandlers.addAll(Arrays.asList(processingHandler));
    }

    public <T extends IMaterialProperty<T>> void addProcessingHandler(PropertyKey<T> propertyKey, TriConsumer<OrePrefix, Material, T> handler) {
        addProcessingHandler((orePrefix, material) -> {
            if (material.hasProperty(propertyKey)) {
                handler.accept(orePrefix, material, material.getProperty(propertyKey));
            }
        });
    }

    public void processOreRegistration(@Nullable Material material) {
        if (this.isSelfReferencing && material == null) {
            material = materialType; //append default material for self-referencing OrePrefix
        }
        if (material != null) {
            generatedMaterials.add(material);
        }
    }

    public static void runMaterialHandlers() {
        for (OrePrefix orePrefix : values()) {
            orePrefix.runGeneratedMaterialHandlers();
        }
    }

    private static final ThreadLocal<OrePrefix> currentProcessingPrefix = new ThreadLocal<>();
    private static final ThreadLocal<Material> currentMaterial = new ThreadLocal<>();

    public static OrePrefix getCurrentProcessingPrefix() {
        return currentProcessingPrefix.get();
    }

    public static Material getCurrentMaterial() {
        return currentMaterial.get();
    }

    private void runGeneratedMaterialHandlers() {
        currentProcessingPrefix.set(this);
        for (Material registeredMaterial : generatedMaterials) {
            currentMaterial.set(registeredMaterial);
            for (IOreRegistrationHandler registrationHandler : oreProcessingHandlers) {
                registrationHandler.processMaterial(this, registeredMaterial);
            }
            currentMaterial.set(null);
        }
        //clear generated materials for next pass
        generatedMaterials.clear();
        currentProcessingPrefix.set(null);
    }

    @SideOnly(Side.CLIENT)
    public String getLocalNameForItem(Material material) {
        String specifiedUnlocalized = "item." + material.toString() + "." + this.name();
        if (I18n.hasKey(specifiedUnlocalized)) return I18n.format(specifiedUnlocalized);
        String unlocalized = "item.material.oreprefix." + this.name();
        String matLocalized = material.getLocalizedName();
        String formatted = I18n.format(unlocalized, matLocalized);
        return formatted.equals(unlocalized) ? matLocalized : formatted;
    }

    public boolean isIgnored(Material material) {
        return ignoredMaterials.contains(material);
    }

    public void setIgnored(Material material) {
        ignoredMaterials.add(material);
    }

    public boolean isMarkerPrefix() {
        return isMarkerPrefix;
    }
}
