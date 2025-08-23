package gtqt.common.items.covers;

import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.CoverableView;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityItemBus;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gtqt.common.items.behaviors.ProgrammableCircuit;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityDualHatch;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityHugeMEPatternProvider;
import gtqt.common.metatileentities.multi.multiblockpart.MetaTileEntityMEPatternProvider;

import java.util.Collections;

public class CoverProgrammableHatch extends CoverBase implements CoverWithUI, ITickable {

    public CoverProgrammableHatch(CoverDefinition definition, CoverableView coverableView, EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    public void update() {
        TileEntity tileEntity = getCoverableView().getWorld().getTileEntity(getCoverableView().getPos());
        if (tileEntity instanceof IGregTechTileEntity igtte) {
            MetaTileEntity mte = igtte.getMetaTileEntity();
            if (mte instanceof SimpleMachineMetaTileEntity machineMetaTile) {

                IItemHandlerModifiable importItems = machineMetaTile.getImportItems();

                for (int i = 0; i < importItems.getSlots(); i++) {
                    ItemStack itemStack = importItems.getStackInSlot(i);
                    if (itemStack != ItemStack.EMPTY && isItemValid(itemStack)) {
                        if (getProgrammableCircuit(itemStack).getName().equals("programmable_circuit")) {
                            machineMetaTile.setGhostCircuitConfig(getProgrammableCircuit(itemStack).getType());
                            importItems.extractItem(i, itemStack.getCount(), false);
                            GTTransferUtils.addItemsToItemHandler(machineMetaTile.getExportItems(), false,
                                    Collections.singletonList(itemStack));
                        }
                    }
                }
            }
            if (mte instanceof MetaTileEntityItemBus itemBus) {
                IItemHandlerModifiable importItems = itemBus.getImportItems();

                for (int i = 0; i < importItems.getSlots(); i++) {
                    ItemStack itemStack = importItems.getStackInSlot(i);
                    if (itemStack != ItemStack.EMPTY && isItemValid(itemStack)) {
                        if (getProgrammableCircuit(itemStack).getName().equals("programmable_circuit")) {

                            itemBus.setGhostCircuitConfig(getProgrammableCircuit(itemStack).getType());
                            importItems.extractItem(i, itemStack.getCount(), false);
                            if (itemBus.getController() instanceof RecipeMapMultiblockController controller) {
                                if (controller.getOutputInventory() == null) return;

                                GTTransferUtils.addItemsToItemHandler(controller.getOutputInventory(), false,
                                        Collections.singletonList(itemStack));

                            }
                        }
                    }
                }
            } else if (mte instanceof MetaTileEntityDualHatch itemBus) {
                IItemHandlerModifiable importItems = itemBus.getImportItems();

                for (int i = 0; i < importItems.getSlots(); i++) {
                    ItemStack itemStack = importItems.getStackInSlot(i);
                    if (itemStack != ItemStack.EMPTY && isItemValid(itemStack)) {
                        if (getProgrammableCircuit(itemStack).getName().equals("programmable_circuit")) {

                            itemBus.setGhostCircuitConfig(getProgrammableCircuit(itemStack).getType());
                            importItems.extractItem(i, itemStack.getCount(), false);
                            if (itemBus.getController() instanceof RecipeMapMultiblockController controller) {
                                if (controller.getOutputInventory() == null) return;
                                GTTransferUtils.addItemsToItemHandler(controller.getOutputInventory(), false,
                                        Collections.singletonList(itemStack));
                            }
                        }
                    }
                }
            } else if (mte instanceof MetaTileEntityMEPatternProvider itemBus) {
                IItemHandlerModifiable importItems = itemBus.getImportItems();

                for (int i = 0; i < importItems.getSlots(); i++) {
                    ItemStack itemStack = importItems.getStackInSlot(i);
                    if (itemStack != ItemStack.EMPTY && isItemValid(itemStack)) {
                        if (getProgrammableCircuit(itemStack).getName().equals("programmable_circuit")) {

                            itemBus.setGhostCircuitConfig(getProgrammableCircuit(itemStack).getType());
                            importItems.extractItem(i, itemStack.getCount(), false);
                            if (itemBus.getController() instanceof RecipeMapMultiblockController controller) {
                                if (controller.getOutputInventory() == null) return;
                                GTTransferUtils.addItemsToItemHandler(controller.getOutputInventory(), false,
                                        Collections.singletonList(itemStack));
                            }
                        }
                    }
                }
            } else if (mte instanceof MetaTileEntityHugeMEPatternProvider itemBus) {
                IItemHandlerModifiable importItems = itemBus.getImportItems();

                for (int i = 0; i < importItems.getSlots(); i++) {
                    ItemStack itemStack = importItems.getStackInSlot(i);
                    if (itemStack != ItemStack.EMPTY && isItemValid(itemStack)) {
                        if (getProgrammableCircuit(itemStack).getName().equals("programmable_circuit")) {

                            itemBus.setGhostCircuitConfig(getProgrammableCircuit(itemStack).getType());
                            importItems.extractItem(i, itemStack.getCount(), false);
                            if (itemBus.getController() instanceof RecipeMapMultiblockController controller) {
                                if (controller.getOutputInventory() == null) return;
                                GTTransferUtils.addItemsToItemHandler(controller.getOutputInventory(), false,
                                        Collections.singletonList(itemStack));
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isItemValid(ItemStack stack) {
        return getProgrammableCircuit(stack) != null;
    }

    public ProgrammableCircuit getProgrammableCircuit(ItemStack stack) {
        return ProgrammableCircuit.getInstanceFor(stack);
    }

    @Override
    public boolean canAttach(CoverableView coverable, EnumFacing side) {
        return coverable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getAttachedSide()) != null;
    }

    @Override
    public void renderCover(CCRenderState renderState, Matrix4 translation,
                            IVertexOperation[] pipeline, Cuboid6 plateBox,
                            BlockRenderLayer layer) {
        Textures.FUSION_REACTOR_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }
}
