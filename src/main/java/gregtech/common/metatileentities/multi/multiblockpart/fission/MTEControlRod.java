package gregtech.common.metatileentities.multi.multiblockpart.fission;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.impl.NotifiableFilteredItemHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.ControlRod;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.impl.data.ControlRodData;
import gregtech.api.fission.reactor.ReactorPathWalker;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MTEControlRod extends MTEFissionItemComponent<ControlRodData> implements ControlRod {

    public MTEControlRod(@NotNull ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MTEControlRod(metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableFilteredItemHandler(this, 1, this, false)
                .setFillPredicate(s -> GregTechAPI.FISSION_COMPONENT_REGISTRY.getData(ControlRodData.class, s) != null);
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(this, 1, this, true);
    }

    @Override
    protected ModularUI createUI(@NotNull EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .label(5, 5, getMetaFullName())
                .slot(importItems, 0, 176 / 2 - 9 - 36, 40, GuiTextures.SLOT)
                .slot(exportItems, 0, 176 / 2 + 9 + 18, 40, GuiTextures.SLOT)
                .bindPlayerInventory(entityPlayer.inventory)
                .build(getHolder(), entityPlayer);
    }

    @Override
    protected @NotNull Class<ControlRodData> getDataClass() {
        return ControlRodData.class;
    }

    @Override
    protected int dataDurability() {
        assert componentData != null;
        return componentData.durability;
    }

    @Override
    public void processNeutronPath(@NotNull ReactorPathWalker walker, @NotNull List<NeutronPathData> neutronData,
                                   @NotNull List<ReactivityPathData> reactivityData, @NotNull FissionComponent source,
                                   @NotNull ComponentDirection direction, int r, int c, float neutrons) {
        // neutron hits a control rod, so the neutron is absorbed
    }

    @Override
    public float adjustNeutrons(float neutrons) {
        if (componentData != null && durability > 0) {
            return neutrons * componentData.reactivityMultiplier;
        }
        return neutrons;
    }

    @Override
    public float adjustHeat(float heat) {
        if (componentData != null && durability > 0) {
            return heat * componentData.heatMultiplier;
        }
        return heat;
    }
}
