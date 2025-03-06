package gregtech.api.metatileentity.multiblock;

import gregtech.api.capability.IDistinctBusController;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.logic.distinct.DefaultInputGroup;
import gregtech.api.recipes.logic.distinct.DistinctInputGroup;
import gregtech.api.recipes.logic.statemachine.builder.RecipeStandardStateMachineBuilder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class DistinctRecipeMapMultiblockController extends RecipeMapMultiblockController implements
                                                            IDistinctBusController {

    private int currentIndex = 0;
    private final List<DistinctInputGroup> distinctInputGroups = new ObjectArrayList<>();
    private boolean isDistinct = false;

    public DistinctRecipeMapMultiblockController(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    @Override
    protected void modifyRecipeLogicStandardBuilder(RecipeStandardStateMachineBuilder builder) {
        super.modifyRecipeLogicStandardBuilder(builder);
        builder.setDistinct(this::hasNextDistinctGroup, this::getCurrentDistinctGroup, this::getNextDistinctGroup,
                () -> this.distinctInputGroups);
    }

    protected DistinctInputGroup getCurrentDistinctGroup() {
        return distinctInputGroups.get(currentIndex);
    }

    protected boolean hasNextDistinctGroup() {
        int localIndex = currentIndex;
        do {
            localIndex++;
            if (localIndex >= distinctInputGroups.size()) {
                localIndex = 0;
            }
            if (!distinctInputGroups.get(localIndex).isAwaitingUpdate()) return true;
        } while (localIndex != currentIndex);
        return false;
    }

    protected DistinctInputGroup getNextDistinctGroup() {
        int localIndex = currentIndex;
        do {
            currentIndex++;
            if (currentIndex >= distinctInputGroups.size()) {
                currentIndex = 0;
            }
            DistinctInputGroup group = distinctInputGroups.get(currentIndex);
            if (!group.isAwaitingUpdate()) return group;
        } while (localIndex != currentIndex);
        return null;
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        initializeDistinctGroups();
    }

    @Override
    protected void resetTileAbilities() {
        super.resetTileAbilities();
    }

    @Override
    public boolean isDistinct() {
        return isDistinct;
    }

    @Override
    public void setDistinct(boolean isDistinct) {
        this.isDistinct = isDistinct;
        getMultiblockParts().forEach(part -> part.onDistinctChange(isDistinct));
        // mark buses as changed on distinct toggle
        if (this.isDistinct) {
            this.notifiedItemInputList.addAll(this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
        } else {
            this.notifiedItemInputList.add(this.inputInventory);
        }
        initializeDistinctGroups();
    }

    protected void initializeDistinctGroups() {
        distinctInputGroups.clear();
        List<IFluidTank> ih = this.getAbilities(MultiblockAbility.IMPORT_FLUIDS);
        Set<IFluidHandler> inputHatches = new ObjectOpenHashSet<>(ih.size());
        for (IFluidTank tank : ih) {
            if (tank instanceof IFluidHandler h) inputHatches.add(h);
        }
        if (isDistinct) {
            for (IItemHandlerModifiable input : this.getAbilities(MultiblockAbility.IMPORT_ITEMS)) {
                distinctInputGroups.add(new DefaultInputGroup(input, Collections.singleton(input),
                        getInputFluidInventory(), inputHatches));
            }
        } else {
            Set<IItemHandlerModifiable> inputBuses = new ObjectOpenHashSet<>(
                    this.getAbilities(MultiblockAbility.IMPORT_ITEMS));
            distinctInputGroups.add(
                    new DefaultInputGroup(getInputInventory(), inputBuses, getInputFluidInventory(),
                            inputHatches));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        isDistinct = data.getBoolean("isDistinct");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("isDistinct", isDistinct);
        return data;
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        buf.writeBoolean(isDistinct);
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        isDistinct = buf.readBoolean();
    }
}
