package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class DataItemBehavior implements IItemBehaviour {

    @Override
    public void addInformation(@Nonnull ItemStack itemStack, List<String> lines) {
        NBTTagCompound researchItemNBT = itemStack.getSubCompound(IResearchRecipeMap.RESEARCH_NBT_TAG);
        if (researchItemNBT != null) {
            String researchId = researchItemNBT.getString(IResearchRecipeMap.RESEARCH_ID_NBT_TAG);
            if (researchId.isEmpty()) return;

            Set<Recipe> recipes = ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId);
            if (!recipes.isEmpty()) {
                lines.add(I18n.format("behavior.data_item.assemblyline.title"));
                Set<ItemStack> itemsAdded = new ObjectOpenHashSet<>();
                for (Recipe recipe : recipes) {
                    if (recipe == null) continue;
                    ItemStack stack = recipe.getOutputs().get(0);
                    if (!itemsAdded.contains(stack)) {
                        itemsAdded.add(stack);
                        lines.add(I18n.format("behavior.data_item.assemblyline.data", stack.getDisplayName()));
                    }
                }
            }
        }
    }
}
