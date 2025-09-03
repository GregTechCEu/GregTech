package gregtech.common.items.behaviors;

import com.cleanroommc.modularui.api.drawable.IDrawable;

import gregtech.api.items.metaitem.stats.IDataItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.ItemStackHashStrategy;

import gregtech.api.util.KeyUtil;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class DataItemBehavior implements IItemBehaviour, IDataItem {

    private final boolean requireDataBank;

    public DataItemBehavior() {
        this.requireDataBank = false;
    }

    public DataItemBehavior(boolean requireDataBank) {
        this.requireDataBank = requireDataBank;
    }

    @Override
    public boolean requireDataBank() {
        return requireDataBank;
    }

    @Override
    public void addInformation(@NotNull ItemStack itemStack, List<String> lines) {
        String researchId = AssemblyLineManager.readResearchId(itemStack);
        if (researchId == null) return;
        collectResearchItemsI18(researchId, lines);
    }

    public static void collectResearchItems(String id, List<IDrawable> lines) {
        Collection<Recipe> recipes = ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES)
                .getDataStickEntry(id);
        if (recipes != null && !recipes.isEmpty()) {
            lines.add(KeyUtil.lang("behavior.data_item.assemblyline.title"));
            Collection<ItemStack> added = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAllButCount());
            for (Recipe recipe : recipes) {
                ItemStack output = recipe.getOutputs().get(0);
                if (added.add(output)) {
                    lines.add(KeyUtil.lang("behavior.data_item.assemblyline.data", output.getDisplayName()));
                }
            }
        }
    }

    public static void collectResearchItemsI18(String id, List<String> lines) {
        Collection<Recipe> recipes = ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES)
                .getDataStickEntry(id);
        if (recipes != null && !recipes.isEmpty()) {
            lines.add(I18n.format("behavior.data_item.assemblyline.title"));
            Collection<ItemStack> added = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAllButCount());
            for (Recipe recipe : recipes) {
                ItemStack output = recipe.getOutputs().get(0);
                if (added.add(output)) {
                    lines.add(I18n.format("behavior.data_item.assemblyline.data", output.getDisplayName()));
                }
            }
        }
    }
}
