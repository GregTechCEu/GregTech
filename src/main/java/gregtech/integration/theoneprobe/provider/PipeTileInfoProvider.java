package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.logic.TemperatureLogic;
import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.api.util.TickUtil;
import gregtech.common.pipelike.net.energy.EnergyFlowData;
import gregtech.common.pipelike.net.energy.EnergyFlowLogic;
import gregtech.common.pipelike.net.fluid.FluidFlowLogic;
import gregtech.common.pipelike.net.item.ItemFlowLogic;
import gregtech.integration.theoneprobe.element.FluidStackElement;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;

public class PipeTileInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return GTValues.MODID + ":pipe_tile_provider";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world,
                             IBlockState iBlockState, IProbeHitData iProbeHitData) {
        if (iBlockState.getBlock() instanceof PipeBlock pipe) {
            PipeTileEntity tile = pipe.getTileEntity(world, iProbeHitData.getPos());
            if (tile != null) {
                for (NetLogicData data : tile.getNetLogicDatas().values()) {
                    TemperatureLogic temp = data.getLogicEntryNullable(TemperatureLogic.TYPE);
                    if (temp != null) {
                        addTemperatureInformation(probeMode, iProbeInfo, entityPlayer, iProbeHitData, temp);
                    }
                    EnergyFlowLogic energy = data.getLogicEntryNullable(EnergyFlowLogic.TYPE);
                    if (energy != null) {
                        addEnergyFlowInformation(probeMode, iProbeInfo, entityPlayer, iProbeHitData, energy);
                    }
                    FluidFlowLogic fluid = data.getLogicEntryNullable(FluidFlowLogic.TYPE);
                    if (fluid != null) {
                        addFluidFlowInformation(probeMode, iProbeInfo, entityPlayer, iProbeHitData, fluid);
                    }
                    ItemFlowLogic item = data.getLogicEntryNullable(ItemFlowLogic.TYPE);
                    if (item != null) {
                        addItemFlowInformation(probeMode, iProbeInfo, entityPlayer, iProbeHitData, item);
                    }
                }
            }
        }
    }

    private void addEnergyFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer,
                                          IProbeHitData iProbeHitData, EnergyFlowLogic logic) {
        if (!logic.getSum(true).isEmpty()) {
            iProbeInfo.text(I18n.format("gregtech.top.pipe.energy"));
            for (var entry : logic.getSum(true).entrySet()) {
                String voltage = TextFormattingUtil.formatNumbers(entry.getKey());
                String tier = GTValues.VNF[GTUtility.getTierByVoltage(entry.getKey())];
                String amperage = TextFormattingUtil.formatNumbers(entry.getValue() / EnergyFlowLogic.MEMORY_TICKS);
                iProbeInfo.text(I18n.format("gregtech.top.pipe.energy_per", voltage, tier, amperage));
            }
        }
        EnergyFlowData last = logic.getLast();
        if (last != null) {
            String voltage = TextFormattingUtil.formatNumbers(last.voltage());
            String tier = GTValues.VNF[GTUtility.getTierByVoltage(last.voltage())];
            String amperage = TextFormattingUtil.formatNumbers(last.amperage());
            iProbeInfo.text(I18n.format("gregtech.top.pipe.energy_last", voltage, tier, amperage));
        }
    }

    private void addFluidFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer,
                                         IProbeHitData iProbeHitData, FluidFlowLogic logic) {
        if (logic.getMemory(true).isEmpty()) {
            FluidStack last = logic.getLast().recombine();
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .text(TextStyleClass.INFO + "{*gregtech.top.pipe.fluid_last*} ")
                    .element(new FluidStackElement(last))
                    .text(" " + last.getLocalizedName());
        }

        Object2LongMap<FluidTestObject> counts = logic.getSum(true);

        for (var entry : counts.object2LongEntrySet()) {
            FluidStack stack = entry.getKey().recombine();
            String value = TextFormattingUtil.formatNumbers(20 * entry.getLongValue() / FluidFlowLogic.MEMORY_TICKS);
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .element(new FluidStackElement(stack))
                    .text(" §b" + value + " L/s §f" + stack.getLocalizedName());
        }
    }

    private void addItemFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer,
                                        IProbeHitData iProbeHitData, ItemFlowLogic logic) {
        if (logic.getMemory(true).isEmpty()) {
            ItemStack last = logic.getLast().recombine();
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .text(TextStyleClass.INFO + "{*gregtech.top.pipe.item_last*} ")
                    .item(last)
                    .text(" " + last.getDisplayName());
        }

        Object2LongMap<ItemTestObject> counts = logic.getSum(true);

        for (var entry : counts.object2LongEntrySet()) {
            ItemStack stack = entry.getKey().recombine();
            String value = TextFormattingUtil.formatNumbers(20 * entry.getLongValue() / ItemFlowLogic.MEMORY_TICKS);
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .item(stack)
                    .text(" §b" + value + " /s §f" + stack.getDisplayName());
        }
    }

    private void addTemperatureInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer,
                                           IProbeHitData iProbeHitData, TemperatureLogic logic) {
        iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                .text(TextStyleClass.INFO + "{*gregtech.top.pipe.temperature*} ")
                .text(" " + TextFormatting.RED + logic.getTemperature(TickUtil.getTick()) + "K");
    }
}
