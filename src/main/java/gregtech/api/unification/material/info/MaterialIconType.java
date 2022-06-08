package gregtech.api.unification.material.info;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import gregtech.api.GTValues;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class MaterialIconType {

    public static final Map<String, MaterialIconType> ICON_TYPES = new HashMap<>();

    static int idCounter = 0;

    public static final MaterialIconType dustTiny = new MaterialIconType("dustTiny");
    public static final MaterialIconType dustSmall = new MaterialIconType("dustSmall");
    public static final MaterialIconType dust = new MaterialIconType("dust");
    public static final MaterialIconType dustImpure = new MaterialIconType("dustImpure");
    public static final MaterialIconType dustPure = new MaterialIconType("dustPure");

    public static final MaterialIconType crushed = new MaterialIconType("crushed");
    public static final MaterialIconType crushedPurified = new MaterialIconType("crushedPurified");
    public static final MaterialIconType crushedCentrifuged = new MaterialIconType("crushedCentrifuged");

    public static final MaterialIconType gem = new MaterialIconType("gem");
    public static final MaterialIconType gemChipped = new MaterialIconType("gemChipped");
    public static final MaterialIconType gemFlawed = new MaterialIconType("gemFlawed");
    public static final MaterialIconType gemFlawless = new MaterialIconType("gemFlawless");
    public static final MaterialIconType gemExquisite = new MaterialIconType("gemExquisite");

    public static final MaterialIconType nugget = new MaterialIconType("nugget");

    public static final MaterialIconType ingot = new MaterialIconType("ingot");
    public static final MaterialIconType ingotHot = new MaterialIconType("ingotHot");
    public static final MaterialIconType ingotDouble = new MaterialIconType("ingotDouble");
    public static final MaterialIconType ingotTriple = new MaterialIconType("ingotTriple");
    public static final MaterialIconType ingotQuadruple = new MaterialIconType("ingotQuadruple");
    public static final MaterialIconType ingotQuintuple = new MaterialIconType("ingotQuintuple");

    public static final MaterialIconType plate = new MaterialIconType("plate");
    public static final MaterialIconType plateDouble = new MaterialIconType("plateDouble");
    public static final MaterialIconType plateTriple = new MaterialIconType("plateTriple");
    public static final MaterialIconType plateQuadruple = new MaterialIconType("plateQuadruple");
    public static final MaterialIconType plateQuintuple = new MaterialIconType("plateQuintuple");
    public static final MaterialIconType plateDense = new MaterialIconType("plateDense");

    public static final MaterialIconType stick = new MaterialIconType("stick");
    public static final MaterialIconType lens = new MaterialIconType("lens");
    public static final MaterialIconType round = new MaterialIconType("round");
    public static final MaterialIconType bolt = new MaterialIconType("bolt");
    public static final MaterialIconType screw = new MaterialIconType("screw");
    public static final MaterialIconType ring = new MaterialIconType("ring");
    public static final MaterialIconType wireFine = new MaterialIconType("wireFine");
    public static final MaterialIconType gearSmall = new MaterialIconType("gearSmall");
    public static final MaterialIconType rotor = new MaterialIconType("rotor");
    public static final MaterialIconType stickLong = new MaterialIconType("stickLong");
    public static final MaterialIconType springSmall = new MaterialIconType("springSmall");
    public static final MaterialIconType spring = new MaterialIconType("spring");
    public static final MaterialIconType arrow = new MaterialIconType("arrow");
    public static final MaterialIconType gear = new MaterialIconType("gear");
    public static final MaterialIconType foil = new MaterialIconType("foil");

    public static final MaterialIconType cell = new MaterialIconType("cell");
    public static final MaterialIconType cellPlasma = new MaterialIconType("cellPlasma");

    public static final MaterialIconType toolHeadSword = new MaterialIconType("toolHeadSword");
    public static final MaterialIconType toolHeadPickaxe = new MaterialIconType("toolHeadPickaxe");
    public static final MaterialIconType toolHeadShovel = new MaterialIconType("toolHeadShovel");
    public static final MaterialIconType toolHeadAxe = new MaterialIconType("toolHeadAxe");
    public static final MaterialIconType toolHeadHoe = new MaterialIconType("toolHeadHoe");
    public static final MaterialIconType toolHeadHammer = new MaterialIconType("toolHeadHammer");
    public static final MaterialIconType toolHeadFile = new MaterialIconType("toolHeadFile");
    public static final MaterialIconType toolHeadSaw = new MaterialIconType("toolHeadSaw");
    public static final MaterialIconType toolHeadBuzzSaw = new MaterialIconType("toolHeadBuzzSaw");
    public static final MaterialIconType toolHeadDrill = new MaterialIconType("toolHeadDrill");
    public static final MaterialIconType toolHeadChainsaw = new MaterialIconType("toolHeadChainsaw");
    public static final MaterialIconType toolHeadSickle = new MaterialIconType("toolHeadSickle");
    public static final MaterialIconType toolHeadScrewdriver = new MaterialIconType("toolHeadScrewdriver");
    public static final MaterialIconType toolHeadWrench = new MaterialIconType("toolHeadWrench");

    public static final MaterialIconType turbineBlade = new MaterialIconType("turbineBlade");
    public static final MaterialIconType coke = new MaterialIconType("coke");

    // BLOCK TEXTURES
    public static final MaterialIconType block = new MaterialIconType("block");
    public static final MaterialIconType fluid = new MaterialIconType("fluid");
    public static final MaterialIconType foilBlock = new MaterialIconType("foilBlock");
    public static final MaterialIconType wire = new MaterialIconType("wire"); // TODO unused
    public static final MaterialIconType ore = new MaterialIconType("ore");
    public static final MaterialIconType frameGt = new MaterialIconType("frameGt");

    public static final MaterialIconType pipeSide = new MaterialIconType("pipeSide"); // TODO unused
    public static final MaterialIconType pipeTiny = new MaterialIconType("pipeTiny");
    public static final MaterialIconType pipeSmall = new MaterialIconType("pipeSmall");
    public static final MaterialIconType pipeMedium = new MaterialIconType("pipeMedium");
    public static final MaterialIconType pipeLarge = new MaterialIconType("pipeLarge");
    public static final MaterialIconType pipeHuge = new MaterialIconType("pipeHuge");
    public static final MaterialIconType pipeQuadruple = new MaterialIconType("pipeQuadruple");
    public static final MaterialIconType pipeNonuple = new MaterialIconType("pipeNonuple");

    // USED FOR GREGIFICATION ADDON
    public static final MaterialIconType seed = new MaterialIconType("seed");
    public static final MaterialIconType crop = new MaterialIconType("crop");
    public static final MaterialIconType essence = new MaterialIconType("essence");

    public final String name;
    public final int id;

    public MaterialIconType(String name) {
        this.name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        Preconditions.checkArgument(!ICON_TYPES.containsKey(this.name), "MaterialIconType " + this.name + " already registered!");
        this.id = idCounter++;
        ICON_TYPES.put(this.name, this);
    }

    public ResourceLocation getBlockPath(MaterialIconSet materialIconSet) {
        return new ResourceLocation(GTValues.MODID, "blocks/material_sets/" + materialIconSet.name + "/" + this.name);
    }

    public ResourceLocation getBlockPath(MaterialIconSet materialIconSet, boolean emissive) {
        if (emissive) {
            return new ResourceLocation(GTValues.MODID, "blocks/material_sets/" + materialIconSet.name + "/" + this.name + "_emissive");
        } else return getBlockPath(materialIconSet);
    }

    public ResourceLocation getItemModelPath(MaterialIconSet materialIconSet) {
        return new ResourceLocation(GTValues.MODID, "material_sets/" + materialIconSet.name + "/" + this.name);
    }

    public ResourceLocation getItemOverlayPath(MaterialIconSet materialIconSet) {
        return new ResourceLocation(GTValues.MODID, "material_sets/" + materialIconSet.name + "/" + this.name + "_overlay");
    }

}
