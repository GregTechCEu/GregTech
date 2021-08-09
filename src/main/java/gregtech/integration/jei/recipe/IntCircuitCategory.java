package gregtech.integration.jei.recipe;

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterators;
import gregtech.api.GTValues;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import gregtech.common.items.MetaItems;
import gregtech.integration.jei.GTJeiPlugin;
import gregtech.integration.jei.utils.render.CompositeDrawable;
import gregtech.integration.jei.utils.render.CompositeRenderer;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IntCircuitCategory implements IRecipeCategory<IntCircuitRecipeWrapper> {

    public static final String UID = GTValues.MODID + ":" + MetaItems.INTEGRATED_CIRCUIT.unlocalizedName;
    private static final int SLOT_SIZE = 18;
    private static final int ROW_LENGTH = 6;
    private static final int FIRST_SLOT_SCALE = 2;
    private final IDrawable slotBase;
    private final IDrawable scaledSlot;

    private final IDrawable iconDrawable;
    private final IDrawable backgroundDrawable;

    private final Supplier<IIngredientRenderer<ItemStack>> otherItemRenderer =
            Suppliers.memoize(() -> {
                IIngredientRenderer<ItemStack> defaultRenderer = GTJeiPlugin.ingredientRegistry.getIngredientRenderer(VanillaTypes.ITEM);
                return CompositeRenderer.startBuilder(defaultRenderer)
                        .then(this::slice)
                        .then(defaultRenderer::render)
                        .then(() -> GL11.glDisable(GL11.GL_STENCIL_TEST))
                        .build();
            });

    private final Supplier<IIngredientRenderer<ItemStack>> firstItemRenderer =
            Suppliers.memoize(() -> CompositeRenderer.startBuilder(otherItemRenderer.get())
                    .then(GlStateManager::pushMatrix)
                    .then((minecraft, xPosition, yPosition, ingredient) -> {
                        if (xPosition * yPosition != 0)
                            GlStateManager.translate(xPosition, yPosition, 0);
                        GlStateManager.scale(FIRST_SLOT_SCALE, FIRST_SLOT_SCALE, 0);
                    })
                    .then((minecraft, xPosition, yPosition, ingredient) -> otherItemRenderer.get()
                            .render(minecraft, 0, 0, ingredient))
                    .then(GlStateManager::popMatrix)
                    .build());

    public IntCircuitCategory(IGuiHelper guiHelper) {
        iconDrawable = guiHelper.createDrawableIngredient(MetaItems.INTEGRATED_CIRCUIT.getStackForm());
        backgroundDrawable = guiHelper.createBlankDrawable(108, 108);

        Iterator<Integer> counter = Iterators.cycle(IntStream.range(0, IntCircuitIngredient.CIRCUIT_MAX + 1)
                .boxed()
                .collect(Collectors.toList()));

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        slotBase = CompositeDrawable.startBuilder(SLOT_SIZE, SLOT_SIZE)
                .then(guiHelper.drawableBuilder(new ResourceLocation(GTValues.MODID, "textures/gui/base/slot.png"), 0, 0, SLOT_SIZE, SLOT_SIZE)
                        .setTextureSize(SLOT_SIZE, SLOT_SIZE)
                        .build()::draw)
                .then((minecraft, xOffset, yOffset) ->
                        fontRenderer.drawString(counter.next().toString(), xOffset + 1, yOffset + 1, 0x555555))
                .build();

        scaledSlot = CompositeDrawable.startBuilder(SLOT_SIZE * FIRST_SLOT_SCALE, SLOT_SIZE * FIRST_SLOT_SCALE)
                .then(GlStateManager::pushMatrix)
                .then((minecraft, xOffset, yOffset) -> {
                    if (xOffset * yOffset != 0)
                        GlStateManager.translate(xOffset, yOffset, 0);
                    GlStateManager.scale(FIRST_SLOT_SCALE, FIRST_SLOT_SCALE, 0);
                })
                .then((minecraft, xOffset, yOffset) -> slotBase.draw(minecraft, 0, 0))
                .then(GlStateManager::popMatrix)
                .build();
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("metaitem.circuit.integrated.gui");
    }

    @Override
    public String getModName() {
        return GTValues.MODID;
    }

    @Override
    public IDrawable getBackground() {
        return backgroundDrawable;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return iconDrawable;
    }

    private static final int shortenedRowLength = ROW_LENGTH - FIRST_SLOT_SCALE;
    private static final int[][] positions =
            Stream.concat(
                    IntStream.range(0, shortenedRowLength * FIRST_SLOT_SCALE)
                            .mapToObj(i -> new int[]{
                                    SLOT_SIZE * FIRST_SLOT_SCALE + SLOT_SIZE * (i % shortenedRowLength),
                                    SLOT_SIZE * (i / shortenedRowLength)
                            }),
                    IntStream.range(0, IntCircuitIngredient.CIRCUIT_MAX - (shortenedRowLength * FIRST_SLOT_SCALE))
                            .mapToObj(i -> new int[]{
                                    SLOT_SIZE * (i % ROW_LENGTH),
                                    SLOT_SIZE * FIRST_SLOT_SCALE + SLOT_SIZE * (i / ROW_LENGTH)
                            })
            )
                    .toArray(int[][]::new);

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IntCircuitRecipeWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
        stacks.init(0, recipeWrapper.input, firstItemRenderer.get(), 0, 0, SLOT_SIZE * FIRST_SLOT_SCALE, SLOT_SIZE * FIRST_SLOT_SCALE, FIRST_SLOT_SCALE, FIRST_SLOT_SCALE);
        stacks.setBackground(0, scaledSlot);
        for (int i = 0; i < positions.length; i++) {
            stacks.init(i + 1,
                    recipeWrapper.input,
                    otherItemRenderer.get(),
                    positions[i][0],
                    positions[i][1],
                    SLOT_SIZE,
                    SLOT_SIZE,
                    1,
                    1
            );
            stacks.setBackground(i + 1, slotBase);
        }
        stacks.set(ingredients);
    }

    public static final int ITEM_WIDTH = 16;

    private void slice(Minecraft minecraft, int xPosition, int yPosition, @Nullable ItemStack ingredient) {
        Framebuffer fb = minecraft.getFramebuffer();
        if (!fb.isStencilEnabled()) {
            fb.enableStencil();
        }

        final int mask = 0xFF;
        final int val = 1;

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glStencilFunc(GL11.GL_ALWAYS, val, mask);
        GL11.glStencilMask(mask);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.colorMask(false, false, false, false);
        GlStateManager.depthMask(false);

        GlStateManager.disableTexture2D();

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buf = tes.getBuffer();
        buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION);
        buf.pos(xPosition + ITEM_WIDTH, yPosition, 0).endVertex();
        buf.pos(xPosition, yPosition + ITEM_WIDTH, 0).endVertex();
        buf.pos(xPosition + ITEM_WIDTH, yPosition + ITEM_WIDTH, 0).endVertex();
        tes.draw();

        GlStateManager.enableTexture2D();

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);

        GL11.glStencilFunc(GL11.GL_EQUAL, val, mask);
    }

}
