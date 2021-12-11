package gregtech.common.metatileentities.electric.multiblockpart;

import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.IMachineHatchMultiblock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.util.GTUtility;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityProcessingArray;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityMachineHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    private final IItemHandlerModifiable machineHandler;

    public MetaTileEntityMachineHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false);
        this.machineHandler = new LimitedImportHandler();
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMachineHatch(metaTileEntityId, 0);
    }

    @Override
    public MultiblockAbility<IItemHandlerModifiable> getAbility() {
        return MultiblockAbility.MACHINE_HATCH;
    }

    @Override
    public void registerAbilities(List<IItemHandlerModifiable> abilityList) {
        abilityList.add(machineHandler);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return machineHandler;
    }

    @Override
    public IItemHandlerModifiable getImportItems() {
        return machineHandler;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                        18 + 18 + 94)
                .label(10, 5, getMetaFullName());

        builder.widget(new SlotWidget(machineHandler, 0,
                81, 18, true, true)
                .setBackgroundTexture(GuiTextures.SLOT));


        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 + 12).build(getHolder(), entityPlayer);

    }

    @Override
    public boolean canPartShare() {
        return false;
    }

    private class LimitedImportHandler extends NotifiableItemStackHandler {

        public LimitedImportHandler() {
            super(1, null, false);
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

            if (!isItemValid(slot, stack)) {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            MultiblockControllerBase controller = getController();
            if (controller instanceof IMachineHatchMultiblock)
                return GTUtility.isMachineValidForMachineHatch(stack, ((IMachineHatchMultiblock) controller).getBlacklist());

            //If the controller is null, this part is not attached to any Multiblock
            return true;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {

            if (getController() instanceof RecipeMapMultiblockController) {

                RecipeMapMultiblockController controller = (RecipeMapMultiblockController) getController();

                if (controller != null && controller.isActive()) {
                    return ItemStack.EMPTY;
                }
            }

            return super.extractItem(slot, amount, simulate);
        }

        @Override
        public <T> void addToNotifiedList(MetaTileEntity metaTileEntity, T handler, boolean isExport) {
            if (metaTileEntity instanceof MetaTileEntityProcessingArray && metaTileEntity.isValid()) {
                ((MetaTileEntityProcessingArray) metaTileEntity).notifyMachineChanged();
            }
        }
    }
}
