package gregtech.integration.theoneprobe.provider;

import gregtech.api.GTValues;
import gregtech.api.graphnet.logic.NetLogicData;
import gregtech.api.graphnet.pipenet.physical.block.PipeBlock;
import gregtech.api.graphnet.pipenet.physical.tile.PipeTileEntity;
import gregtech.api.graphnet.predicate.test.FluidTestObject;
import gregtech.api.graphnet.predicate.test.ItemTestObject;
import gregtech.api.util.GTUtility;
import gregtech.api.util.TextFormattingUtil;
import gregtech.common.pipelike.net.energy.EnergyFlowData;
import gregtech.common.pipelike.net.energy.EnergyFlowLogic;
import gregtech.common.pipelike.net.fluid.FluidFlowLogic;
import gregtech.common.pipelike.net.item.ItemFlowLogic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
                    EnergyFlowLogic energy = data.getLogicEntryNullable(EnergyFlowLogic.INSTANCE);
                    if (energy != null) {
                        addEnergyFlowInformation(probeMode, iProbeInfo, entityPlayer, iProbeHitData, energy);
                    }
                    FluidFlowLogic fluid = data.getLogicEntryNullable(FluidFlowLogic.INSTANCE);
                    if (fluid != null) {
                        addFluidFlowInformation(probeMode, iProbeInfo, entityPlayer, iProbeHitData, fluid);
                    }
                    ItemFlowLogic item = data.getLogicEntryNullable(ItemFlowLogic.INSTANCE);
                    if (item != null) {
                        addItemFlowInformation(probeMode, iProbeInfo, entityPlayer, iProbeHitData, item);
                    }
                }
            }
        }
    }

    private void addEnergyFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer,
                                          IProbeHitData iProbeHitData, EnergyFlowLogic logic) {
        long cumulativeVoltage = 0;
        long cumulativeAmperage = 0;
        for (var memory : logic.getMemory().values()) {
            int count = 0;
            double voltage = 0;
            long amperage = 0;
            for (EnergyFlowData flow : memory) {
                count++;
                long prev = amperage;
                amperage += flow.amperage();
                // weighted average
                voltage = voltage * prev / amperage + (double) (flow.voltage() * flow.amperage()) / amperage;
            }
            if (count != 0) {
                cumulativeVoltage += voltage / count;
                cumulativeAmperage += amperage / count;
            }
        }
        long v = cumulativeVoltage / EnergyFlowLogic.MEMORY_TICKS;
        String voltage = TextFormattingUtil.formatNumbers(v);
        String amperage = TextFormattingUtil.formatNumbers(cumulativeAmperage / EnergyFlowLogic.MEMORY_TICKS);
        String tier = GTValues.VNF[GTUtility.getTierByVoltage(v)];
        iProbeInfo.text(I18n.format("gregtech.top.pipe.energy", voltage, tier, amperage));
    }

    private void addFluidFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer,
                                         IProbeHitData iProbeHitData, FluidFlowLogic logic) {
        if (logic.getMemory().isEmpty()) {
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .text(TextStyleClass.INFO + "{*gregtech.top.pipe.fluid_last*} ")
                    .icon(logic.getLast().getFluid().getStill(logic.getLast()), -1, -1, 14, 14)
                    .text(" " + logic.getLast().getLocalizedName());
        }

        Object2IntOpenHashMap<FluidTestObject> counts = new Object2IntOpenHashMap<>();
        for (var memory : logic.getMemory().values()) {
            for (FluidStack stack : memory) {
                counts.merge(new FluidTestObject(stack), stack.amount, Integer::sum);
            }
        }

        for (var entry : counts.object2IntEntrySet()) {
            FluidStack stack = entry.getKey().recombine();
            String value = TextFormattingUtil.formatNumbers(entry.getIntValue() * 20L / FluidFlowLogic.MEMORY_TICKS);
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .icon(stack.getFluid().getStill(stack), -1, -1, 14, 14)
                    .text(" §b" + value + " L/s §f" + stack.getLocalizedName());
        }
    }

    private void addItemFlowInformation(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer,
                                        IProbeHitData iProbeHitData, ItemFlowLogic logic) {
        if (logic.getMemory().isEmpty()) {
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .text(TextStyleClass.INFO + "{*gregtech.top.pipe.item_last*} ")
                    .item(logic.getLast())
                    .text(" " + logic.getLast().getDisplayName());
        }

        Object2IntOpenHashMap<ItemTestObject> counts = new Object2IntOpenHashMap<>();
        for (var memory : logic.getMemory().values()) {
            for (ItemStack stack : memory) {
                counts.merge(new ItemTestObject(stack), stack.getCount(), Integer::sum);
            }
        }

        for (var entry : counts.object2IntEntrySet()) {
            ItemStack stack = entry.getKey().recombine();
            String value = TextFormattingUtil.formatNumbers(entry.getIntValue() * 20L / ItemFlowLogic.MEMORY_TICKS);
            iProbeInfo.horizontal(iProbeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                    .item(stack)
                    .text(" §b" + value + " /s §f" + stack.getDisplayName());
        }
    }
}
