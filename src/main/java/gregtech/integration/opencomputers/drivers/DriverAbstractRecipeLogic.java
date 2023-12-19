package gregtech.integration.opencomputers.drivers;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.AbstractRecipeLogic;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.chance.output.impl.ChancedFluidOutput;
import gregtech.api.recipes.chance.output.impl.ChancedItemOutput;
import gregtech.api.recipes.ingredients.GTRecipeInput;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriverAbstractRecipeLogic extends DriverSidedTileEntity {

    @Override
    public Class<?> getTileEntityClass() {
        return AbstractRecipeLogic.class;
    }

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            return tileEntity.hasCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, side);
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            IWorkable capability = tileEntity.getCapability(GregtechTileCapabilities.CAPABILITY_WORKABLE, side);
            if (capability instanceof AbstractRecipeLogic)
                return new EnvironmentAbstractRecipeLogic((IGregTechTileEntity) tileEntity,
                        (AbstractRecipeLogic) capability);
        }
        return null;
    }

    public final static class EnvironmentAbstractRecipeLogic extends EnvironmentMetaTileEntity<AbstractRecipeLogic> {

        public EnvironmentAbstractRecipeLogic(IGregTechTileEntity holder, AbstractRecipeLogic capability) {
            super(holder, capability, "gt_recipeLogic");
        }

        @Callback(doc = "function():table -- Returns previous recipe.")
        public Object[] getCurrentRecipe(final Context context, final Arguments args) {
            Recipe previousRecipe = tileEntity.getPreviousRecipe();
            if (previousRecipe != null && tileEntity.isActive()) {
                Map<String, Object> recipe = new Object2ObjectOpenHashMap<>();
                recipe.put("duration", previousRecipe.getDuration());
                recipe.put("EUt", previousRecipe.getEUt());

                List<Map<String, Object>> itemInput = new ArrayList<>();
                List<GTRecipeInput> inputs = previousRecipe.getInputs();
                inputs.forEach(iR -> {
                    for (ItemStack itemStack : iR.getInputStacks()) {
                        Map<String, Object> input = new Object2ObjectOpenHashMap<>();
                        input.put("count", iR.getAmount());
                        input.put("name", itemStack.getDisplayName());
                        itemInput.add(input);
                    }
                });
                if (!itemInput.isEmpty()) {
                    recipe.put("itemInputs", itemInput);
                }

                List<Map<String, Object>> fluidInput = new ArrayList<>();
                List<GTRecipeInput> fluidInputs = previousRecipe.getFluidInputs();
                fluidInputs.forEach(iR -> {
                    Map<String, Object> input = new Object2ObjectOpenHashMap<>();
                    input.put("amount", iR.getAmount());
                    input.put("name", iR.getInputFluidStack().getFluid().getName());
                    fluidInput.add(input);
                });
                if (!fluidInput.isEmpty()) {
                    recipe.put("fluidInputs", fluidInput);
                }

                List<Map<String, Object>> itemOutput = new ArrayList<>();
                List<ItemStack> outputs = previousRecipe.getOutputs();
                outputs.forEach(iR -> {
                    Map<String, Object> output = new Object2ObjectOpenHashMap<>();
                    output.put("count", iR.getCount());
                    output.put("name", iR.getDisplayName());
                    itemOutput.add(output);
                });
                if (!itemOutput.isEmpty()) {
                    recipe.put("itemOutputs", itemOutput);
                }

                List<Map<String, Object>> chancedItemOutput = new ArrayList<>();
                List<ChancedItemOutput> chancedOutputs = previousRecipe.getChancedOutputs().getChancedEntries();
                chancedOutputs.forEach(iR -> {
                    Map<String, Object> output = new Object2ObjectOpenHashMap<>();
                    output.put("chance", iR.getChance());
                    output.put("boostPerTier", iR.getChanceBoost());
                    output.put("count", iR.getIngredient().getCount());
                    output.put("name", iR.getIngredient().getDisplayName());
                    chancedItemOutput.add(output);
                });
                if (!chancedItemOutput.isEmpty()) {
                    recipe.put("chancedItemOutput", chancedItemOutput);
                }

                List<Map<String, Object>> fluidOutput = new ArrayList<>();
                List<FluidStack> fluidOutputs = previousRecipe.getFluidOutputs();
                fluidOutputs.forEach(iR -> {
                    Map<String, Object> output = new Object2ObjectOpenHashMap<>();
                    output.put("amount", iR.amount);
                    output.put("name", iR.getFluid().getName());
                    fluidOutput.add(output);
                });
                if (!fluidOutput.isEmpty()) {
                    recipe.put("fluidOutputs", fluidOutput);
                }

                List<Map<String, Object>> chancedFluidOutput = new ArrayList<>();
                List<ChancedFluidOutput> chancedFluidOutputs = previousRecipe.getChancedFluidOutputs()
                        .getChancedEntries();
                chancedFluidOutputs.forEach(iR -> {
                    Map<String, Object> output = new Object2ObjectOpenHashMap<>();
                    output.put("chance", iR.getChance());
                    output.put("boostPerTier", iR.getChanceBoost());
                    output.put("count", iR.getIngredient().amount);
                    output.put("name", iR.getIngredient().getFluid().getName());
                    chancedFluidOutput.add(output);
                });
                if (!chancedFluidOutput.isEmpty()) {
                    recipe.put("chancedFluidOutput", chancedFluidOutput);
                }
                return new Object[] { recipe };
            }
            return new Object[] { null };
        }
    }
}
