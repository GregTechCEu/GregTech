package gregtech.integration.forestry.bees;

import gregtech.api.GTValues;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import forestry.api.apiculture.BeeManager;
import forestry.api.arboriculture.TreeManager;
import forestry.api.core.IItemModelRegister;
import forestry.api.core.IModelManager;
import forestry.api.core.Tabs;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.ISpeciesRoot;
import forestry.api.lepidopterology.ButterflyManager;
import forestry.core.items.IColoredItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GTDropItem extends Item implements IColoredItem, IItemModelRegister {

    public GTDropItem() {
        super();
        setHasSubtypes(true);
        setCreativeTab(Tabs.tabApiculture);
        setTranslationKey("gt.honey_drop");
        setRegistryName(GTValues.MODID, "gt.honey_drop");
        setResearchSuitability(BeeManager.beeRoot);
        setResearchSuitability(TreeManager.treeRoot);
        setResearchSuitability(ButterflyManager.butterflyRoot);
        setResearchSuitability(AlleleManager.alleleRegistry.getSpeciesRoot("rootFlowers"));
    }

    private void setResearchSuitability(@Nullable ISpeciesRoot speciesRoot) {
        if (speciesRoot != null) {
            speciesRoot.setResearchSuitability(new ItemStack(this, 1, GTValues.W), 0.5f);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerModel(@NotNull Item item, @NotNull IModelManager manager) {
        manager.registerItemModel(item, 0);
        for (int i = 0; i < GTDropType.VALUES.length; i++) {
            manager.registerItemModel(item, i, GTValues.MODID_FR, "gt.honey_drop");
        }
    }

    @NotNull
    @Override
    public String getTranslationKey(@NotNull ItemStack stack) {
        GTDropType type = GTDropType.getDrop(stack.getItemDamage());
        return super.getTranslationKey(stack) + "." + type.name;
    }

    @Override
    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        if (tab == Tabs.tabApiculture) {
            for (GTDropType type : GTDropType.VALUES) {
                items.add(new ItemStack(this, 1, type.ordinal()));
            }
        }
    }

    @Override
    public int getColorFromItemstack(@NotNull ItemStack stack, int i) {
        GTDropType type = GTDropType.getDrop(stack.getItemDamage());
        return type.color[i == 0 ? 0 : 1];
    }
}
