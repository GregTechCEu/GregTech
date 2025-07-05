package gregtech.integration.jei.multiblock;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;

import gregtech.api.util.GTLog;

import net.minecraft.client.resources.I18n;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.recipes.RecipeLayout;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class MultiblockInfoCategory implements IRecipeCategory<MultiblockInfoRecipeWrapper> {

    public static final String UID = String.format("%s.multiblock_info", GTValues.MODID);

    private final IDrawable background;
    private final IDrawable icon;
    private final IGuiHelper guiHelper;

    public MultiblockInfoCategory(IJeiHelpers helpers) {
        this.guiHelper = helpers.getGuiHelper();
        this.background = this.guiHelper.createBlankDrawable(176, 166);
        this.icon = guiHelper.drawableBuilder(GuiTextures.MULTIBLOCK_CATEGORY.imageLocation, 0, 0, 16, 16)
                .setTextureSize(16, 16).build();
    }

    public static final List<MultiblockControllerBase> REGISTER = new LinkedList<>();

    public static void registerMultiblock(MultiblockControllerBase controllerBase) {
        REGISTER.add(controllerBase);
    }

    public static void registerRecipes(IModRegistry registry) {
        int processorCount = Runtime.getRuntime().availableProcessors();
        int threadCount = Math.max(1, processorCount - 1); // 保留一个核心给主线程
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        int totalRecipes = REGISTER.size();
        int batchSize = Math.max(1, (totalRecipes + threadCount - 1) / threadCount); // 计算每批大小

        List<Future<List<MultiblockInfoRecipeWrapper>>> futures = new ArrayList<>();

        // 将任务分批提交给线程池
        for (int i = 0; i < threadCount; i++) {
            final int start = i * batchSize;
            final int end = Math.min(start + batchSize, totalRecipes);

            if (start >= totalRecipes) break;

            futures.add(executor.submit(() -> {
                List<MultiblockInfoRecipeWrapper> batchWrappers = new ArrayList<>();
                for (int j = start; j < end; j++) {
                    MultiblockControllerBase controller = REGISTER.get(j);
                    batchWrappers.add(new MultiblockInfoRecipeWrapper(controller));
                }
                return batchWrappers;
            }));
        }

        // 收集所有结果
        List<MultiblockInfoRecipeWrapper> allWrappers = new ArrayList<>(totalRecipes);
        for (Future<List<MultiblockInfoRecipeWrapper>> future : futures) {
            try {
                allWrappers.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                GTLog.logger.error("Failed to generate JEI multiblock preview", e);
                // 部分失败时继续处理其他结果
            }
        }

        executor.shutdown(); // 关闭线程池

        registry.addRecipes(allWrappers, UID);
    }

    @NotNull
    @Override
    public String getUid() {
        return UID;
    }
    @NotNull
    @Override
    public String getTitle() {
        return I18n.format("gregtech.multiblock.title");
    }

    @NotNull
    @Override
    public String getModName() {
        return GTValues.MODID;
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayout recipeLayout, MultiblockInfoRecipeWrapper recipeWrapper,
                          @NotNull IIngredients ingredients) {
        recipeWrapper.setRecipeLayout((RecipeLayout) recipeLayout, this.guiHelper);
    }
}
