package gregtech.common.items.behaviors;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
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
            for (Recipe recipe : recipes) {
                if (recipe == null) continue;
                lines.add(I18n.format("metaitem.tool.datastick.assemblyline_data_tooltip", recipe.getOutputs().get(0).getDisplayName()));
            }
        }
    }
}
