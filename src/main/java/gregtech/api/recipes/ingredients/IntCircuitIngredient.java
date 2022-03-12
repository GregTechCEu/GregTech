package gregtech.api.recipes.ingredients;

import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.common.items.MetaItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;

public class IntCircuitIngredient extends Ingredient {

    public static final int CIRCUIT_MAX = 32;

    public static ItemStack getIntegratedCircuit(int configuration) {
        ItemStack stack = MetaItems.INTEGRATED_CIRCUIT.getStackForm();
        setCircuitConfiguration(stack, configuration);
        return stack;
    }

    public static void setCircuitConfiguration(ItemStack itemStack, int configuration) {
        if (!MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack))
            throw new IllegalArgumentException("Given item stack is not an integrated circuit!");
        if (configuration < 0 || configuration > CIRCUIT_MAX)
            throw new IllegalArgumentException("Given configuration number is out of range!");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            itemStack.setTagCompound(tagCompound);
        }
        tagCompound.setInteger("Configuration", configuration);
    }

    public static int getCircuitConfiguration(EntityPlayer player) {
        ItemStack stack = player.getHeldItemMainhand();
        if(!isIntegratedCircuit(stack)) {
            stack = player.getHeldItemOffhand();
        }
        return getCircuitConfiguration(stack);
    }

    public static int getCircuitConfiguration(ItemStack itemStack) {
        if (!isIntegratedCircuit(itemStack)) return 0;
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound.getInteger("Configuration");
    }

    public static boolean isIntegratedCircuit(ItemStack itemStack) {
        boolean isCircuit = MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack);
        if (isCircuit && !itemStack.hasTagCompound()) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("Configuration", 0);
            itemStack.setTagCompound(compound);
        }
        return isCircuit;
    }

    public static void adjustConfiguration(PlayerInventoryHolder holder, int amount) {
        adjustConfiguration(holder.getCurrentItem(), amount);
        holder.markAsDirty();
    }

    public static void adjustConfiguration(EntityPlayer player, int amount) {
        ItemStack stack = player.getHeldItemMainhand();
        if(!isIntegratedCircuit(stack)) {
            stack = player.getHeldItemOffhand();
        }
        adjustConfiguration(stack, amount);
    }

    public static void adjustConfiguration(ItemStack stack, int amount) {
        if (!IntCircuitIngredient.isIntegratedCircuit(stack)) return;
        int configuration = IntCircuitIngredient.getCircuitConfiguration(stack);
        configuration += amount;
        configuration = MathHelper.clamp(configuration, 0, IntCircuitIngredient.CIRCUIT_MAX);
        IntCircuitIngredient.setCircuitConfiguration(stack, configuration);
    }

    private static ItemStack[] gatherMatchingCircuits(int... matchingConfigurations) {
        ItemStack[] resultItems = new ItemStack[matchingConfigurations.length];
        for (int i = 0; i < resultItems.length; i++) {
            resultItems[i] = getIntegratedCircuit(matchingConfigurations[i]);
        }
        return resultItems;
    }

    private final int[] matchingConfigurations;

    public IntCircuitIngredient(int... matchingConfigurations) {
        super(gatherMatchingCircuits(matchingConfigurations));
        this.matchingConfigurations = matchingConfigurations;
    }

    @Override
    public boolean apply(@Nullable ItemStack itemStack) {
        return itemStack != null && MetaItems.INTEGRATED_CIRCUIT.isItemEqual(itemStack) &&
                ArrayUtils.contains(matchingConfigurations, getCircuitConfiguration(itemStack));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

}
