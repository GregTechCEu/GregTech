package gregtech.common.metatileentities.electric.multiblockpart;

import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityMachineHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IItemHandlerModifiable> {

    private IItemHandlerModifiable machineHandler;

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

    private class LimitedImportHandler extends ItemStackHandler {

        public LimitedImportHandler() {
            super(1);
        }


        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

            if(!isItemValid(slot, stack)) {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {

            MetaTileEntity mte = MachineItemBlock.getMetaTileEntity(stack);

            if(mte == null) {
                return false;
            }


            RecipeMap<?> recipeMap = mte.getRecipeMap();

            if(recipeMap == null) {
                return false;
            }

            MultiblockControllerBase controller = getController();
            if(controller != null) {
                //If this Multiblock Part is attached to a controller, check if it is valid for insertion
                if(controller instanceof RecipeMapMultiblockController) {
                    return ((RecipeMapMultiblockController) controller).getRecipeMapWorkable().isRecipeMapValid(recipeMap);
                }
            }

            //If the controller is null, this part is not attached to any Multiblock
            return true;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {

            if(getController() instanceof RecipeMapMultiblockController) {

                RecipeMapMultiblockController controller = (RecipeMapMultiblockController) getController();

                if(controller != null && controller.isActive()) {
                    return ItemStack.EMPTY;
                }
            }

            return super.extractItem(slot, amount, simulate);
        }
    }
}
