package gregtech.integration.jei.recipe;

import gregtech.api.GTValues;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.BlankUIHolder;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.category.GTRecipeCategory;
import gregtech.api.recipes.properties.impl.ResearchProperty;
import gregtech.api.recipes.properties.impl.ResearchPropertyData;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import gregtech.common.ConfigHolder;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.integration.jei.utils.render.FluidStackTextRenderer;
import gregtech.integration.jei.utils.render.ItemStackTextRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RecipeMapCategory implements IRecipeCategory<GTRecipeWrapper> {

    private final RecipeMap<?> recipeMap;
    private final GTRecipeCategory category;
    private final ModularUI modularUI;
    private final ItemStackHandler importItems, exportItems;
    private final FluidTankList importFluids, exportFluids;
    private final IDrawable backgroundDrawable;
    private Object iconIngredient;
    private IDrawable icon;

    private static final Map<GTRecipeCategory, RecipeMapCategory> gtCategories = new Object2ObjectOpenHashMap<>();
    private static final Map<RecipeMap<?>, List<RecipeMapCategory>> recipeMapCategories = new Object2ObjectOpenHashMap<>();

    public RecipeMapCategory(@NotNull RecipeMap<?> recipeMap, @NotNull GTRecipeCategory category,
                             IGuiHelper guiHelper) {
        this.recipeMap = recipeMap;
        this.category = category;
        FluidTank[] importFluidTanks = new FluidTank[recipeMap.getMaxFluidInputs()];
        for (int i = 0; i < importFluidTanks.length; i++)
            importFluidTanks[i] = new FluidTank(16000);
        FluidTank[] exportFluidTanks = new FluidTank[recipeMap.getMaxFluidOutputs()];
        for (int i = 0; i < exportFluidTanks.length; i++)
            exportFluidTanks[i] = new FluidTank(16000);
        this.modularUI = recipeMap.getRecipeMapUI().createJeiUITemplate(
                (importItems = new ItemStackHandler(
                        recipeMap.getMaxInputs() + (recipeMap == RecipeMaps.ASSEMBLY_LINE_RECIPES ? 1 : 0))),
                (exportItems = new ItemStackHandler(recipeMap.getMaxOutputs())),
                (importFluids = new FluidTankList(false, importFluidTanks)),
                (exportFluids = new FluidTankList(false, exportFluidTanks)), 0)
                .build(new BlankUIHolder(), Minecraft.getMinecraft().player);
        this.modularUI.initWidgets();
        this.backgroundDrawable = guiHelper.createBlankDrawable(modularUI.getWidth(), modularUI.getHeight());
        gtCategories.put(category, this);
        recipeMapCategories.compute(recipeMap, (k, v) -> {
            if (v == null) v = new ArrayList<>();
            v.add(this);
            return v;
        });
    }

    @Override
    @NotNull
    public String getUid() {
        return category.getUniqueID();
    }

    @Override
    @NotNull
    public String getTitle() {
        return LocalizationUtils.format(category.getTranslationKey());
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        if (icon != null) {
            return icon;
        } else if (iconIngredient instanceof IDrawable drawable) {
            return icon = drawable;
        } else if (iconIngredient != null) {
            // cache the icon drawable for less gc pressure
            return icon = JustEnoughItemsModule.guiHelper.createDrawableIngredient(iconIngredient);
        }
        // JEI will automatically populate the icon as the first registered catalyst if null
        return null;
    }

    public void setIcon(Object icon) {
        if (iconIngredient == null) {
            iconIngredient = icon;
        }
    }

    @Override
    @NotNull
    public String getModName() {
        return GTValues.MODID;
    }

    @Override
    @NotNull
    public IDrawable getBackground() {
        return backgroundDrawable;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, @NotNull GTRecipeWrapper recipeWrapper,
                          @NotNull IIngredients ingredients) {
        try {
            IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
            IGuiFluidStackGroup fluidStackGroup = recipeLayout.getFluidStacks();
            for (Widget uiWidget : modularUI.guiWidgets.values()) {

                if (uiWidget instanceof SlotWidget slotWidget) {
                    if (!(slotWidget.getHandle() instanceof SlotItemHandler handle)) continue;
                    if (handle.getItemHandler() == importItems) {
                        int index = handle.getSlotIndex();
                        // this is input item stack slot widget, so add it to item group
                        itemStackGroup.init(index, true,
                                new ItemStackTextRenderer(recipeWrapper.getInputItemCount(index), index,
                                        recipeWrapper.getItemInDisplayControl()),
                                slotWidget.getPosition().x + 1,
                                slotWidget.getPosition().y + 1,
                                slotWidget.getSize().width - 2,
                                slotWidget.getSize().height - 2, 0, 0);
                    } else if (handle.getItemHandler() == exportItems) {
                        int index = handle.getSlotIndex();
                        // this is output item stack slot widget, so add it to item group
                        itemStackGroup.init(importItems.getSlots() + index, false,
                                new ItemStackTextRenderer(index, recipeWrapper.getItemOutDisplayControl()),
                                slotWidget.getPosition().x + 1,
                                slotWidget.getPosition().y + 1,
                                slotWidget.getSize().width - 2,
                                slotWidget.getSize().height - 2, 0, 0);
                    }
                } else if (uiWidget instanceof TankWidget tankWidget) {
                    if (importFluids.getFluidTanks().contains(tankWidget.fluidTank)) {
                        int importIndex = importFluids.getFluidTanks().indexOf(tankWidget.fluidTank);
                        List<List<FluidStack>> inputsList = ingredients.getInputs(VanillaTypes.FLUID);
                        int fluidAmount = 0;
                        if (inputsList.size() > importIndex && !inputsList.get(importIndex).isEmpty())
                            fluidAmount = inputsList.get(importIndex).get(0).amount;
                        // this is input tank widget, so add it to fluid group
                        fluidStackGroup.init(importIndex, true,
                                new FluidStackTextRenderer(
                                        GTUtility.safeCastLongToInt(recipeWrapper.getInputFluidCount(importIndex)),
                                        false,
                                        tankWidget.getSize().width - (2 * tankWidget.fluidRenderOffset),
                                        tankWidget.getSize().height - (2 * tankWidget.fluidRenderOffset),
                                        null, importIndex, recipeWrapper.getFluidInDisplayControl()),
                                tankWidget.getPosition().x + tankWidget.fluidRenderOffset,
                                tankWidget.getPosition().y + tankWidget.fluidRenderOffset,
                                tankWidget.getSize().width - (2 * tankWidget.fluidRenderOffset),
                                tankWidget.getSize().height - (2 * tankWidget.fluidRenderOffset), 0, 0);

                    } else if (exportFluids.getFluidTanks().contains(tankWidget.fluidTank)) {
                        int exportIndex = exportFluids.getFluidTanks().indexOf(tankWidget.fluidTank);
                        List<List<FluidStack>> inputsList = ingredients.getOutputs(VanillaTypes.FLUID);
                        int fluidAmount = 0;
                        if (inputsList.size() > exportIndex && !inputsList.get(exportIndex).isEmpty())
                            fluidAmount = inputsList.get(exportIndex).get(0).amount;
                        // this is output tank widget, so add it to fluid group
                        fluidStackGroup.init(importFluids.getFluidTanks().size() + exportIndex, false,
                                new FluidStackTextRenderer(fluidAmount, false,
                                        tankWidget.getSize().width - (2 * tankWidget.fluidRenderOffset),
                                        tankWidget.getSize().height - (2 * tankWidget.fluidRenderOffset),
                                        null, exportIndex,
                                        recipeWrapper.getFluidOutDisplayControl()),
                                tankWidget.getPosition().x + tankWidget.fluidRenderOffset,
                                tankWidget.getPosition().y + tankWidget.fluidRenderOffset,
                                tankWidget.getSize().width - (2 * tankWidget.fluidRenderOffset),
                                tankWidget.getSize().height - (2 * tankWidget.fluidRenderOffset), 0, 0);

                    }
                }
            }

            if (ConfigHolder.machines.enableResearch && this.recipeMap == RecipeMaps.ASSEMBLY_LINE_RECIPES) {
                ResearchPropertyData data = recipeWrapper.getRecipe().getProperty(ResearchProperty.getInstance(), null);
                if (data != null) {
                    List<ItemStack> dataItems = new ArrayList<>();
                    for (ResearchPropertyData.ResearchEntry entry : data) {
                        ItemStack dataStick = entry.dataItem().copy();
                        AssemblyLineManager.writeResearchToNBT(GTUtility.getOrCreateNbtCompound(dataStick),
                                entry.researchId());
                        dataItems.add(dataStick);
                    }
                    itemStackGroup.set(16, dataItems);
                }
            }

            itemStackGroup.addTooltipCallback(recipeWrapper::addItemTooltip);
            fluidStackGroup.addTooltipCallback(recipeWrapper::addFluidTooltip);
            itemStackGroup.set(ingredients);
            fluidStackGroup.set(ingredients);
        } catch (Exception e) {
            // don't you dare eat my exceptions, JEI
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void drawExtras(@NotNull Minecraft minecraft) {
        for (Widget widget : modularUI.guiWidgets.values()) {
            if (widget instanceof ProgressWidget) widget.detectAndSendChanges();
            widget.drawInBackground(0, 0, minecraft.getRenderPartialTicks(), new IRenderContext() {});
            widget.drawInForeground(0, 0);
        }
    }

    @Nullable
    public static RecipeMapCategory getCategoryFor(@NotNull GTRecipeCategory category) {
        return gtCategories.get(category);
    }

    @Nullable
    public static Collection<RecipeMapCategory> getCategoriesFor(@NotNull RecipeMap<?> recipeMap) {
        return recipeMapCategories.get(recipeMap);
    }
}
