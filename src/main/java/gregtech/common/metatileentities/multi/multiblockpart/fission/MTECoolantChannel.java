package gregtech.common.metatileentities.multi.multiblockpart.fission;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.NotifiableFilteredFluidHandler;
import gregtech.api.capability.impl.NotifiableFluidTank;
import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.CoolantChannel;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.impl.data.CoolantData;
import gregtech.api.fission.reactor.ReactorPathWalker;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MTECoolantChannel extends AbstractMTEFissionComponent<CoolantData> implements CoolantChannel {

    public MTECoolantChannel(@NotNull ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MTECoolantChannel(metaTileEntityId);
    }

    @Override
    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(true, new NotifiableFilteredFluidHandler(8000, this, false)
                .setFilter(stack -> true)); // TODO coolant filter
    }

    @Override
    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(true, new NotifiableFluidTank(8000, this, true));
    }

    @Override
    public void update() {
        super.update();
        if (getWorld().isRemote) {
            return;
        }

        if (componentData == null && !notifiedFluidInputList.isEmpty()) {
            for (IFluidHandler handler : notifiedFluidInputList) {
                FluidStack stack = handler.drain(Integer.MAX_VALUE, false);
                if (stack == null || stack.getFluid() == null || stack.amount == 0) {
                    continue;
                }

                this.componentData = GregTechAPI.FISSION_COMPONENT_REGISTRY.getData(CoolantData.class, stack);
            }
        }
    }

    @Override
    protected ModularUI createUI(@NotNull EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .label(5, 5, getMetaFullName())
                .widget(new TankWidget(importFluids.getTankAt(0), 176 / 2 - 9 - 36, 40, 18, 18)
                        .setBackgroundTexture(GuiTextures.FLUID_SLOT))
                .widget(new TankWidget(exportFluids.getTankAt(0), 176 / 2 + 9 + 18, 40, 18, 18)
                        .setBackgroundTexture(GuiTextures.FLUID_SLOT))
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public void reduceDurability(int amount) {}

    @Override
    public int durability() {
        return 0;
    }

    @Override
    public void processNeutronPath(@NotNull ReactorPathWalker walker, @NotNull List<NeutronPathData> neutronData,
                                   @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                                   @NotNull ComponentDirection direction, int r, int c, float neutrons) {
        // coolant is fully transparent, so neutrons keep going in that direction
        walker.walkPath(neutronData, reactivityData, source, direction, r, c, neutrons);
    }

    @Override
    public float applyCooling(float heat) {
        if (componentData == null) {
            return 0;
        }

        int coolantNeeded = (int) Math.ceil(heat / componentData.heatPerCoolant);
        FluidStack drained = importFluids.drain(coolantNeeded, false);
        if (drained == null || drained.getFluid() != componentData.coldCoolant.getFluid()) {
            return 0;
        }

        int availableCoolant = drained.amount;
        if (availableCoolant == 0) {
            return 0;
        }

        int hotCoolantProduced = availableCoolant * componentData.hotPerColdCoolant;
        int hotCoolantSpace = exportFluids.fill(new FluidStack(componentData.hotCoolant, hotCoolantProduced), false);
        int usableCoolant = Math.min(availableCoolant, hotCoolantSpace / componentData.hotPerColdCoolant);
        assert usableCoolant <= availableCoolant;

        if (usableCoolant == 0) {
            return 0;
        }

        drained = importFluids.drain(usableCoolant, true);
        if (drained == null) {
            return 0;
        }

        exportFluids.fill(new FluidStack(componentData.hotCoolant, usableCoolant * componentData.hotPerColdCoolant), true);

        return componentData.heatPerCoolant * usableCoolant;
    }

    @Override
    public float coolantHeat() {
        return componentData == null ? 0 : componentData.coldCoolantHeat;
    }

    @Override
    public float adjustNeutrons(float neutrons) {
        if (componentData == null) {
            return neutrons;
        }

        FluidStack stack = importFluids.getTankAt(0).getFluid();
        if (stack == null || stack.amount == 0) {
            return neutrons;
        }

        return componentData.reactivity * neutrons;
    }
}
