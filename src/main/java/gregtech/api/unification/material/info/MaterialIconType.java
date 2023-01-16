package gregtech.api.unification.material.info;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gregtech.api.GTValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nonnull;
import java.io.IOException;
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
    public static final MaterialIconType gear = new MaterialIconType("gear");
    public static final MaterialIconType foil = new MaterialIconType("foil");

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
    public static final MaterialIconType toolHeadScythe = new MaterialIconType("toolHeadScythe");
    public static final MaterialIconType toolHeadScrewdriver = new MaterialIconType("toolHeadScrewdriver");
    public static final MaterialIconType toolHeadWrench = new MaterialIconType("toolHeadWrench");

    public static final MaterialIconType turbineBlade = new MaterialIconType("turbineBlade");

    // BLOCK TEXTURES
    public static final MaterialIconType block = new MaterialIconType("block");
    public static final MaterialIconType fluid = new MaterialIconType("fluid");
    public static final MaterialIconType ore = new MaterialIconType("ore");
    public static final MaterialIconType oreSmall = new MaterialIconType("oreSmall");
    public static final MaterialIconType frameGt = new MaterialIconType("frameGt");

    // USED FOR GREGIFICATION ADDON
    public static final MaterialIconType seed = new MaterialIconType("seed");
    public static final MaterialIconType crop = new MaterialIconType("crop");
    public static final MaterialIconType essence = new MaterialIconType("essence");

    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> ITEM_MODEL_CACHE = HashBasedTable.create();
    private static final Table<MaterialIconType, MaterialIconSet, ResourceLocation> BLOCK_TEXTURE_CACHE = HashBasedTable.create();

    public final String name;
    public final int id;

    public MaterialIconType(String name) {
        this.name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        Preconditions.checkArgument(!ICON_TYPES.containsKey(this.name), "MaterialIconType " + this.name + " already registered!");
        this.id = idCounter++;
        ICON_TYPES.put(this.name, this);
    }

    @Nonnull
    public ResourceLocation getBlockTexturePath(@Nonnull MaterialIconSet materialIconSet) {
        if (BLOCK_TEXTURE_CACHE.contains(this, materialIconSet)) {
            return BLOCK_TEXTURE_CACHE.get(this, materialIconSet);
        }

        MaterialIconSet iconSet = materialIconSet;
        //noinspection ConstantConditions
        if (!iconSet.isRootIconset && FMLCommonHandler.instance().getEffectiveSide().isClient() &&
                Minecraft.getMinecraft() != null) { // check minecraft for null for CI environments
            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            while (!iconSet.isRootIconset) {
                try {
                    // check if the texture file exists
                    manager.getResource(new ResourceLocation(GTValues.MODID, String.format("textures/blocks/material_sets/%s/%s.png", iconSet.name, this.name)));
                    break;
                } catch (IOException ignored) {
                    iconSet = iconSet.parentIconset;
                }
            }
        }
        ResourceLocation location = new ResourceLocation(GTValues.MODID, String.format("blocks/material_sets/%s/%s", iconSet.name, this.name));
        BLOCK_TEXTURE_CACHE.put(this, materialIconSet, location);

        return location;
    }

    @Nonnull
    public ResourceLocation getItemModelPath(@Nonnull MaterialIconSet materialIconSet) {
        if (ITEM_MODEL_CACHE.contains(this, materialIconSet)) {
            return ITEM_MODEL_CACHE.get(this, materialIconSet);
        }

        MaterialIconSet iconSet = materialIconSet;
        //noinspection ConstantConditions
        if (!iconSet.isRootIconset && FMLCommonHandler.instance().getEffectiveSide().isClient() &&
                Minecraft.getMinecraft() != null) { // check minecraft for null for CI environments
            IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
            while (!iconSet.isRootIconset) {
                try {
                    // check if the model file exists
                    manager.getResource(new ResourceLocation(GTValues.MODID, String.format("models/item/material_sets/%s/%s.json", iconSet.name, this.name)));
                    break;
                } catch (IOException ignored) {
                    iconSet = iconSet.parentIconset;
                }
            }
        }

        ResourceLocation location = new ResourceLocation(GTValues.MODID, String.format("material_sets/%s/%s", iconSet.name, this.name));
        ITEM_MODEL_CACHE.put(this, materialIconSet, location);

        return location;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
