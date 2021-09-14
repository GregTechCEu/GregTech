package gregtech.common.metatileentities.electric.multiblockpart;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntityMachineHatch extends MetaTileEntityMultiblockPart implements IMultiblockAbilityPart<MetaTileEntityMachineHatch> {

    private IItemHandlerModifiable machineHandler;

    public MetaTileEntityMachineHatch(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, 0);
        machineHandler = new MachineImportHandler();
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityMachineHatch(metaTileEntityId);
    }

    @Override
    public MultiblockAbility<MetaTileEntityMachineHatch> getAbility() {
        return MultiblockAbility.MACHINE_HATCH;
    }

    @Override
    public void registerAbilities(List<MetaTileEntityMachineHatch> abilityList) {
        abilityList.add(this);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = machineHandler;
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

        for (int y = 0; y < 1; y++) {
            for (int x = 0; x < 1; x++) {
                builder.widget(new SlotWidget(machineHandler, x,
                        (88 - 9), 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 + 12).build(getHolder(), entityPlayer);

    }


    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return machineHandler;
    }

    private class MachineImportHandler extends ItemStackHandler {

        public MachineImportHandler() {
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

            RecipeMap<?> recipeMap = RecipeMap.findRecipeMapByItemStack(stack);

            //TODO, check against the valid recipe map logic in  AbstractRecipeLogic

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
