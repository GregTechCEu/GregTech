package gregtech.integration.jei.multiblock.infos;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.jei.multiblock.MultiblockInfoPage;
import gregtech.integration.jei.multiblock.MultiblockShapeInfo;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class LargeChemicalReactorInfo extends MultiblockInfoPage {

    @Override
    public MultiblockControllerBase getController() {
        return MetaTileEntities.LARGE_CHEMICAL_REACTOR;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();

        MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder()
                .where('S', MetaTileEntities.LARGE_CHEMICAL_REACTOR, EnumFacing.WEST)
                .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING))
                .where('P', MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE))
                .where('C', MetaBlocks.WIRE_COIL.getState(BlockWireCoil.CoilType.CUPRONICKEL))
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[3], EnumFacing.WEST)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[3], EnumFacing.EAST)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[3], EnumFacing.WEST)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[3], EnumFacing.WEST)
                .where('H', MetaTileEntities.FLUID_EXPORT_HATCH[3], EnumFacing.WEST)
                .where('M', maintenanceIfEnabled(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING)), EnumFacing.WEST);

        shapeInfo.add(
                baseBuilder.shallowCopy()
                        .aisle("XEX", "XCX", "XXX")
                        .aisle("XXX", "XPX", "XXX")
                        .aisle("IMO", "FSH", "XXX")
                        .build()
        );
        shapeInfo.add(
                baseBuilder.shallowCopy()
                        .aisle("XEX", "XXX", "XXX")
                        .aisle("XXX", "XPX", "XCX")
                        .aisle("IMO", "FSH", "XXX")
                        .build()
        );
        shapeInfo.add(
                baseBuilder.shallowCopy()
                        .aisle("XEX", "XXX", "XXX")
                        .aisle("XCX", "XPX", "XXX")
                        .aisle("IMO", "FSH", "XXX")
                        .build()
        );
        shapeInfo.add(
                baseBuilder.shallowCopy()
                        .aisle("XEX", "XXX", "XXX")
                        .aisle("XXX", "CPX", "XXX")
                        .aisle("IMO", "FSH", "XXX")
                        .build()
        );
        shapeInfo.add(
                baseBuilder.shallowCopy()
                        .aisle("XEX", "XXX", "XXX")
                        .aisle("XXX", "XPC", "XXX")
                        .aisle("IMO", "FSH", "XXX")
                        .build()
        );

        return shapeInfo;
    }

    @Override
    public String[] getDescription() {
        return new String[]{I18n.format("gregtech.multiblock.large_chemical_reactor.description")};
    }

    @Override
    protected void generateBlockTooltips() {
        super.generateBlockTooltips();
        ITextComponent tooltip = new TextComponentTranslation("gregtech.multiblock.preview.limit", 10).setStyle(new Style().setColor(TextFormatting.AQUA));
        addBlockTooltip(MetaBlocks.METAL_CASING.getItemVariant(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING), tooltip);

        ItemStack coils = MetaBlocks.WIRE_COIL.getItemVariant(BlockWireCoil.CoilType.CUPRONICKEL);
        ITextComponent coilTooltip = new TextComponentTranslation("gregtech.multiblock.preview.only", coils.getDisplayName()).setStyle(new Style().setColor(TextFormatting.RED));
        addBlockTooltip(coils, coilTooltip);
    }
}
