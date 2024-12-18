package gregtech.common.metatileentities.multi.multiblockpart.fission;

import gregtech.api.GregTechAPI;
import gregtech.api.capability.impl.NotifiableFilteredItemHandler;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.fission.component.ComponentDirection;
import gregtech.api.fission.component.FissionComponent;
import gregtech.api.fission.component.ReactiveComponent;
import gregtech.api.fission.component.impl.data.ModeratorData;
import gregtech.api.fission.reactor.ReactorPathWalker;
import gregtech.api.fission.reactor.pathdata.NeutronPathData;
import gregtech.api.fission.reactor.pathdata.ReactivityPathData;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.function.FloatSupplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MTEModerator extends MTEFissionItemComponent<ModeratorData> {

    public MTEModerator(@NotNull ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MTEModerator(metaTileEntityId);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableFilteredItemHandler(this, 1, this, false)
                .setFillPredicate(s -> GregTechAPI.FISSION_COMPONENT_REGISTRY.getData(ModeratorData.class, s) != null);
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
    protected @NotNull Class<ModeratorData> getDataClass() {
        return ModeratorData.class;
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
        if (source instanceof ReactiveComponent reactor) {
            // neutron hits a moderator, so the neutron bounces back along the path and hits the source
            // only if the source reacts with neutrons
            FloatSupplier supplier = () -> durability > 0 ? neutrons : 0;
            neutronData.add(NeutronPathData.of(this, direction, supplier));
            neutronData.add(NeutronPathData.of(reactor, direction, supplier));
        }

        assert componentData != null;
        if (componentData.reactivity != 0) {
            reactivityData.add(ReactivityPathData.of(componentData.reactivity));
        }
    }
}
