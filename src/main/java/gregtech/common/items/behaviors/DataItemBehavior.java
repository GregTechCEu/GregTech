package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class DataItemBehavior implements IItemBehaviour, IDataItem {

    @Override
    public void addInformation(@Nonnull ItemStack itemStack, List<String> lines) {
        String researchId = AssemblyLineManager.readResearchId(itemStack);
        if (researchId == null) return;
        Collection<Recipe> recipes = ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId);
        if (recipes != null && !recipes.isEmpty()) {
            lines.add(I18n.format("behavior.data_item.assemblyline.title"));
            Collection<ItemStack> added = new ObjectOpenHashSet<>();
            for (Recipe recipe : recipes) {
                ItemStack output = recipe.getOutputs().get(0);
                if (added.add(output)) {
                    lines.add(I18n.format("behavior.data_item.assemblyline.data", output.getDisplayName()));
                }
            }
        }
    }
}
