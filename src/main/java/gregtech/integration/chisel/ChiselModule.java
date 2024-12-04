package gregtech.integration.chisel;

import gregtech.api.GTValues;
import gregtech.api.block.VariantBlock;
import gregtech.api.modules.GregTechModule;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.Mods;
import gregtech.common.blocks.BlockColored;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockWarningSign;
import gregtech.common.blocks.BlockWarningSign1;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.blocks.StoneVariantBlock.StoneType;
import gregtech.common.blocks.StoneVariantBlock.StoneVariant;
import gregtech.common.blocks.wood.BlockGregPlanks;
import gregtech.integration.IntegrationSubmodule;
import gregtech.modules.GregTechModules;

import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import team.chisel.common.carving.Carving;

import java.util.Objects;

@GregTechModule(
                moduleID = GregTechModules.MODULE_CHISEL,
                containerID = GTValues.MODID,
                modDependencies = Mods.Names.CHISEL,
                name = "GregTech Chisel Integration",
                description = "Chisel Integration Module")
public class ChiselModule extends IntegrationSubmodule {

    @Override
    public void init(FMLInitializationEvent event) {
        // GT custom groups
        addVariations("gt_warning_sign", MetaBlocks.WARNING_SIGN, BlockWarningSign.SignType.values());
        addVariations("gt_warning_sign", MetaBlocks.WARNING_SIGN_1, BlockWarningSign1.SignType.values());
        addVariations("gt_studs", MetaBlocks.STUDS);
        addVariations("gt_metal_sheet", MetaBlocks.METAL_SHEET);
        addVariations("gt_large_metal_sheet", MetaBlocks.LARGE_METAL_SHEET);
        for (EnumDyeColor color : EnumDyeColor.values()) {
            Block lamp = MetaBlocks.LAMPS.get(color);
            Block lampBorderless = MetaBlocks.BORDERLESS_LAMPS.get(color);
            String group = "gt_lamp_" + color.getName().toLowerCase();
            for (int i = 0; i < 8; i++) {
                addVariation(group, lamp, i);
                addVariation(group, lampBorderless, i);
            }
        }

        // Chisel shared groups
        addVariations("marble", StoneType.MARBLE, false);
        addVariations("basalt", StoneType.BASALT, false);
        addVariations("black_granite", StoneType.BLACK_GRANITE, false);
        addVariations("red_granite", StoneType.RED_GRANITE, false);
        addVariations("light_concrete", StoneType.CONCRETE_LIGHT, true);
        addVariations("dark_concrete", StoneType.CONCRETE_DARK, true);

        // Mod-dependent groups
        if (doesGroupExist("treated_wood")) { // IE Treated Wood group
            addVariations("treated_wood", MetaBlocks.PLANKS, BlockGregPlanks.BlockType.TREATED_PLANK);
        }
        if (doesGroupExist("certus")) { // AE2 Certus Quartz group
            addVariation("certus", Materials.CertusQuartz);
        }
    }

    @SafeVarargs
    private <U extends Enum<U> & IStringSerializable, T extends VariantBlock<U>> void addVariations(String group,
                                                                                                    T block,
                                                                                                    U... variants) {
        if (variants != null) {
            for (U variant : variants) {
                addVariation(group, block, block.getMetaFromState(block.getState(variant)));
            }
        }
    }

    private void addVariations(String group, BlockColored block) {
        for (EnumDyeColor color : EnumDyeColor.values()) {
            addVariation(group, block, color.getMetadata());
        }
    }

    private void addVariations(String group, StoneType type, boolean enableCobbles) {
        for (StoneVariantBlock.StoneVariant variant : StoneVariant.values()) {
            if (!enableCobbles && (variant == StoneVariant.COBBLE || variant == StoneVariant.COBBLE_MOSSY)) {
                continue;
            }
            StoneVariantBlock block = MetaBlocks.STONE_BLOCKS.get(variant);
            int meta = block.getMetaFromState(block.getState(type));
            addVariation(group, block, meta);
        }
    }

    private void addVariation(String group, Material material) {
        BlockCompressed block = MetaBlocks.COMPRESSED.get(material);
        int meta = block.getMetaFromState(block.getBlock(material));
        addVariation(group, block, meta);
    }

    private void addVariation(String group, Block block, int meta) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("group", group);
        tag.setString("block", Objects.requireNonNull(block.getRegistryName()).toString());
        tag.setInteger("meta", meta);
        FMLInterModComms.sendMessage(Mods.Names.CHISEL, "add_variation", tag);
    }

    private boolean doesGroupExist(String group) {
        return Carving.chisel.getGroup(group) != null;
    }
}
