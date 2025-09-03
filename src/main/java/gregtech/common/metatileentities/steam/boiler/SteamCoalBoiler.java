package gregtech.common.metatileentities.steam.boiler;

import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.category.ICategoryOverride;
import gregtech.client.renderer.texture.Textures;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import org.jetbrains.annotations.NotNull;

public class SteamCoalBoiler extends SteamBoiler implements ICategoryOverride {

    public SteamCoalBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure, Textures.COAL_BOILER_OVERLAY);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SteamCoalBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected int getBaseSteamOutput() {
        return isHighPressure ? 320 : 160;
    }

    @Override
    protected void tryConsumeNewFuel() {
        ItemStack fuelInSlot = importItems.extractItem(0, 1, true);
        if (fuelInSlot.isEmpty()) return;
        // Prevent consuming buckets with burn time
        if (FluidUtil.getFluidHandler(fuelInSlot) != null) {
            return;
        }
        int burnTime = TileEntityFurnace.getItemBurnTime(fuelInSlot);
        if (burnTime <= 0) return;
        importItems.extractItem(0, 1, false);
        ItemStack remainderAsh = ModHandler.getBurningFuelRemainder(fuelInSlot);
        if (!remainderAsh.isEmpty()) { // we don't care if we can't insert ash - it's chanced anyway
            exportItems.insertItem(0, remainderAsh, false);
        }
        setFuelMaxBurnTime(burnTime);
    }

    @Override
    protected int getCooldownInterval() {
        return isHighPressure ? 40 : 45;
    }

    @Override
    protected int getCoolDownRate() {
        return 1;
    }

    @Override
    public IItemHandlerModifiable createExportItemHandler() {
        return new GTItemStackHandler(this, 1);
    }

    @Override
    public IItemHandlerModifiable createImportItemHandler() {
        return new GTItemStackHandler(this, 1) {

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                if (TileEntityFurnace.getItemBurnTime(stack) <= 0)
                    return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager guiSyncManager) {
        return super.buildUI(guiData, guiSyncManager)
                .child(new ItemSlot()
                        .slot(this.importItems, 0)
                        .pos(115, 62))
                .child(new ItemSlot()
                        .slot(new ModularSlot(this.exportItems, 0)
                                .accessibility(false, true))
                        .pos(115, 26))
                .child(new ProgressWidget()
                        .value(new DoubleSyncValue(this::getFuelLeftPercent))
                        .pos(115, 44)
                        .size(18)
                        .texture(isHighPressure ?
                                GTGuiTextures.PROGRESS_BAR_BOILER_FUEL_STEEL :
                                GTGuiTextures.PROGRESS_BAR_BOILER_FUEL_BRONZE, 18)
                        .direction(ProgressWidget.Direction.UP));
    }

    @Override
    public @NotNull String @NotNull [] getJEICategoryOverrides() {
        return new String[] { "minecraft.fuel" };
    }
}
