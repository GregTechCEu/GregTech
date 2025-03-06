package gregtech.api.recipes.logic.statemachine.running;

import gregtech.api.recipes.logic.RecipeRun;
import gregtech.api.recipes.logic.statemachine.overclock.RecipeStandardOverclockingOperator;
import gregtech.api.statemachine.GTStateMachineTransientOperator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;

public class RecipeFinalizingOperator implements GTStateMachineTransientOperator {

    public static final String STANDARD_RESULT_KEY = "ActiveRecipe";
    public static final RecipeFinalizingOperator STANDARD_INSTANCE = new RecipeFinalizingOperator();

    protected final String keyRun;
    protected final String keyResult;

    protected RecipeFinalizingOperator() {
        this.keyRun = RecipeStandardOverclockingOperator.STANDARD_RESULT_KEY;
        this.keyResult = STANDARD_RESULT_KEY;
    }

    public RecipeFinalizingOperator(String keyRun, String keyResult) {
        this.keyRun = keyRun;
        this.keyResult = keyResult;
    }

    @Override
    public void operate(NBTTagCompound data, Map<String, Object> transientData) {
        RecipeRun run = (RecipeRun) transientData.get(keyRun);
        if (run == null) throw new IllegalStateException();
        NBTTagCompound tag = new NBTTagCompound();

        tag.setDouble("Duration", run.getDuration());
        tag.setLong("Voltage", run.getRequiredVoltage());
        tag.setLong("Amperage", run.getRequiredAmperage());
        tag.setBoolean("Generating", run.isGenerating());
        tag.setInteger("Parallel", run.getParallel());
        tag.setInteger("Overclock", run.getOverclocks());

        NBTTagList list = new NBTTagList();
        for (ItemStack item : run.getItemsConsumed()) {
            list.appendTag(item.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("ItemsIn", list);

        list = new NBTTagList();
        for (FluidStack fluid : run.getFluidsConsumed()) {
            list.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("FluidsIn", list);

        list = new NBTTagList();
        for (ItemStack item : run.getItemsOut()) {
            list.appendTag(item.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("ItemsOut", list);

        list = new NBTTagList();
        for (FluidStack fluid : run.getFluidsOut()) {
            list.appendTag(fluid.writeToNBT(new NBTTagCompound()));
        }
        tag.setTag("FluidsOut", list);

        data.setTag(keyResult, tag);
    }
}
