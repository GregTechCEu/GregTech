package gregtech.integration.jei.multiblock;

import codechicken.lib.render.BlockRenderer;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.resources.RenderUtil;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.render.scene.ImmediateWorldSceneRenderer;
import gregtech.api.render.scene.TrackedDummyWorld;
import gregtech.api.render.scene.WorldSceneRenderer;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.ItemStackKey;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.vecmath.Vector3f;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MultiblockInfoRecipeWrapper implements IRecipeWrapper {
    private static final int MAX_PARTS = 20;
    private static final int PARTS_HEIGHT = 36;
    private final int SLOT_SIZE = 18;
    private final int SLOTS_PER_ROW = 10;
    private final int ICON_SIZE = 20;
    private final int RIGHT_PADDING = 5;

    private static class MBPattern {
        final WorldSceneRenderer sceneRenderer;
        final List<ItemStack> parts;

        public MBPattern(final WorldSceneRenderer sceneRenderer, final List<ItemStack> parts) {
            this.sceneRenderer = sceneRenderer;
            this.parts = parts;
        }
    }

    private final MultiblockInfoPage infoPage;
    private final MBPattern[] patterns;
    private final Map<GuiButton, Runnable> buttons = new HashMap<>();
    private RecipeLayout recipeLayout;
    private final List<ItemStack> allItemStackInputs = new ArrayList<>();
    private final ItemStack controllerStack;

    private int layerIndex = -1;
    private int currentRendererPage = 0;
    private int lastMouseX;
    private int lastMouseY;
    private Vector3f center;
    private float rotationYaw;
    private float rotationPitch;
    private float maxZoom;
    private float zoom;

    private GuiButton buttonPreviousPattern;
    private GuiButton buttonNextPattern;
    private GuiButton nextLayerButton;

    private IDrawable slot;
    private IDrawable infoIcon;

    private ItemStack tooltipBlockStack;

    public MultiblockInfoRecipeWrapper(MultiblockInfoPage infoPage) {
        this.infoPage = infoPage;
        this.controllerStack = infoPage.getController().getStackForm();
        HashSet<ItemStackKey> drops = new HashSet<>();
        drops.add(new ItemStackKey(controllerStack));
        this.patterns = infoPage.getMatchingShapes().stream()
                .map(it -> initializePattern(it, drops))
                .toArray(MBPattern[]::new);
        drops.forEach(it -> allItemStackInputs.add(it.getItemStack()));
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM, allItemStackInputs);
        ingredients.setOutput(VanillaTypes.ITEM, controllerStack);
    }

    public MultiblockInfoPage getInfoPage() {
        return infoPage;
    }

    public void setRecipeLayout(RecipeLayout layout, IGuiHelper guiHelper) {
        this.recipeLayout = layout;

        this.slot = guiHelper.drawableBuilder(GuiTextures.SLOT.imageLocation, 0, 0, SLOT_SIZE, SLOT_SIZE).setTextureSize(SLOT_SIZE, SLOT_SIZE).build();
        this.infoIcon = guiHelper.drawableBuilder(GuiTextures.INFO_ICON.imageLocation, 0, 0, ICON_SIZE, ICON_SIZE).setTextureSize(ICON_SIZE, ICON_SIZE).build();

        IDrawable border = layout.getRecipeCategory().getBackground();
        preparePlaceForParts(border.getHeight());
        this.buttons.clear();
        this.nextLayerButton = new GuiButton(0, border.getWidth() - (ICON_SIZE + RIGHT_PADDING), 70, ICON_SIZE, ICON_SIZE, "");
        this.buttonPreviousPattern = new GuiButton(0, border.getWidth() - ((2 * ICON_SIZE) + RIGHT_PADDING + 1), 90, ICON_SIZE, ICON_SIZE, "<");
        this.buttonNextPattern = new GuiButton(0, border.getWidth() - (ICON_SIZE + RIGHT_PADDING), 90, ICON_SIZE, ICON_SIZE, ">");
        this.buttons.put(nextLayerButton, this::toggleNextLayer);
        this.buttons.put(buttonPreviousPattern, () -> switchRenderPage(-1));
        this.buttons.put(buttonNextPattern, () -> switchRenderPage(1));

        boolean isPagesDisabled = patterns.length == 1;
        this.buttonPreviousPattern.visible = !isPagesDisabled;
        this.buttonNextPattern.visible = !isPagesDisabled;
        this.buttonPreviousPattern.enabled = false;
        this.buttonNextPattern.enabled = patterns.length > 1;

        if (Mouse.getEventDWheel() == 0) {
            this.maxZoom = infoPage.getDefaultZoom() * 15;
            this.zoom = this.maxZoom;
            this.rotationYaw = 20.0f;
            this.rotationPitch = 135.0f;
            this.currentRendererPage = 0;
            setNextLayer(-1);
        } else {
            zoom = (float) MathHelper.clamp(zoom + (Mouse.getEventDWheel() < 0 ? 0.5 : -0.5), 3, maxZoom);
            setNextLayer(getLayerIndex());
        }
        if (center != null) {
            getCurrentRenderer().setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }
        updateParts();
    }

    public WorldSceneRenderer getCurrentRenderer() {
        return patterns[currentRendererPage].sceneRenderer;
    }

    public int getLayerIndex() {
        return layerIndex;
    }

    private void toggleNextLayer() {
        WorldSceneRenderer renderer = getCurrentRenderer();
        int height = (int) ((TrackedDummyWorld)renderer.world).getSize().getY() - 1;
        if (++this.layerIndex > height) {
            //if current layer index is more than max height, reset it
            //to display all layers
            this.layerIndex = -1;
        }
        setNextLayer(layerIndex);
    }

    private void setNextLayer(int newLayer) {
        this.layerIndex = newLayer;
        this.nextLayerButton.displayString = "L:" + (layerIndex == -1 ? "A" : Integer.toString(layerIndex + 1));
        WorldSceneRenderer renderer = getCurrentRenderer();
        if (renderer != null) {
            TrackedDummyWorld world = ((TrackedDummyWorld)renderer.world);
            renderer.renderedBlocksMap.clear();
            int minY = (int) world.getMinPos().getY();
            Collection<BlockPos> renderBlocks;
            if (newLayer == -1) {
                renderBlocks = world.renderedBlocks;
            } else {
                renderBlocks = world.renderedBlocks.stream().filter(pos->pos.getY() - minY == newLayer).collect(Collectors.toSet());
            }
            renderer.addRenderedBlocks(renderBlocks, null);
        }
    }

    private void switchRenderPage(int amount) {
        int maxIndex = patterns.length - 1;
        int newIndex = Math.max(0, Math.min(currentRendererPage + amount, maxIndex));
        if (currentRendererPage != newIndex) {
            this.currentRendererPage = newIndex;
            this.buttonNextPattern.enabled = newIndex < maxIndex;
            this.buttonPreviousPattern.enabled = newIndex > 0;
            updateParts();
        }
    }

    private void preparePlaceForParts(int recipeHeight) {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();

        for (int i = 0; i < MAX_PARTS; ++i)
            itemStackGroup.init(i, true, SLOT_SIZE * i - (SLOT_SIZE * SLOTS_PER_ROW) * (i / SLOTS_PER_ROW), recipeHeight - PARTS_HEIGHT + SLOT_SIZE * (i / SLOTS_PER_ROW));
    }

    private void updateParts() {
        IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
        List<ItemStack> parts = this.patterns[currentRendererPage].parts;
        int limit = Math.min(parts.size(), MAX_PARTS);
        for (int i = 0; i < limit; ++i)
            itemStackGroup.set(i, parts.get(i));
        for (int i = parts.size(); i < limit; ++i)
            itemStackGroup.set(i, (ItemStack) null);
    }

    @Override
    public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        WorldSceneRenderer renderer = getCurrentRenderer();
        int sceneHeight = recipeHeight - PARTS_HEIGHT;

        renderer.render(recipeLayout.getPosX(), recipeLayout.getPosY(), recipeWidth, sceneHeight, Mouse.getX(), Mouse.getY());
        drawMultiblockName(recipeWidth);

        //reset colors (so any elements render after this point are not dark)
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.infoIcon.draw(minecraft, recipeWidth - (ICON_SIZE + RIGHT_PADDING), 49);

        for (int i = 0; i < MAX_PARTS; ++i) {
            this.slot.draw(minecraft, SLOT_SIZE * i - (SLOTS_PER_ROW * SLOT_SIZE) * (i / SLOTS_PER_ROW), sceneHeight + SLOT_SIZE * (i / SLOTS_PER_ROW));
        }

        // Hmmm, the buttons need to be last otherwise sometimes highlighting 
        // the button by mousing over it, leaks into other gui elements?
        for (GuiButton button : buttons.keySet()) {
            button.drawButton(minecraft, mouseX, mouseY, 0.0f);
        }

        drawHoveringInformationText(minecraft, infoPage.informationText(), mouseX, mouseY);

        this.tooltipBlockStack = null;
        RayTraceResult rayTraceResult = renderer.getLastTraceResult();
        boolean insideView = mouseX >= 0 && mouseY >= 0 &&
                mouseX < recipeWidth && mouseY < sceneHeight;
        boolean leftClickHeld = Mouse.isButtonDown(0);
        boolean rightClickHeld = Mouse.isButtonDown(1);
        if (insideView) {
            if (leftClickHeld) {
                rotationPitch += mouseX - lastMouseX + 360;
                rotationPitch = rotationPitch % 360;
                rotationYaw = (float) MathHelper.clamp(rotationYaw + (mouseY - lastMouseY), -89.9, 89.9);
            } else if (rightClickHeld) {
                int mouseDeltaY = mouseY - lastMouseY;
                if (Math.abs(mouseDeltaY) > 1) {
                    this.zoom = (float) MathHelper.clamp(zoom + (mouseDeltaY > 0 ? 0.5 : -0.5), 3, maxZoom);
                }
            }
            renderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
        }

        if (!(leftClickHeld || rightClickHeld) && rayTraceResult != null && !renderer.world.isAirBlock(rayTraceResult.getBlockPos())) {
            IBlockState blockState = renderer.world.getBlockState(rayTraceResult.getBlockPos());
            ItemStack itemStack = blockState.getBlock().getPickBlock(blockState, rayTraceResult, renderer.world, rayTraceResult.getBlockPos(), minecraft.player);
            if (itemStack != null && !itemStack.isEmpty()) {
                this.tooltipBlockStack = itemStack;
            }
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    @SideOnly(Side.CLIENT)
    protected void drawHoveringInformationText(Minecraft minecraft, List<String> tooltip, int mouseX, int mouseY) {
        int minX = recipeLayout.getRecipeCategory().getBackground().getWidth();
        int[] yRange = new int[]{49, 69};
        int[] xRange = new int[]{minX - (ICON_SIZE + RIGHT_PADDING), minX - RIGHT_PADDING};
        //Only draw the hovering information tooltip above the information icon
        if (isMouseWithinRange(yRange, xRange, mouseY, mouseX)) {
            GuiUtils.drawHoveringText(tooltip, mouseX, mouseY,
                    176, 176, -1, minecraft.fontRenderer);
        }
    }

    private boolean isMouseWithinRange(int[] yRange, int[] xRange, int mouseY, int mouseX) {

        return (yRange[0] < mouseY && mouseY < yRange[1] && xRange[0] < mouseX && mouseX < xRange[1]);
    }

    private void drawMultiblockName(int recipeWidth) {
        String localizedName = I18n.format(infoPage.getController().getMetaFullName());
        GTUtility.drawCenteredSizedText(recipeWidth / 2, 0, localizedName, 0x333333, 1.3);
    }

    @Override
    public boolean handleClick(@Nonnull Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        for (Entry<GuiButton, Runnable> button : buttons.entrySet()) {
            if (button.getKey().mousePressed(minecraft, mouseX, mouseY)) {
                button.getValue().run();
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (tooltipBlockStack != null && !tooltipBlockStack.isEmpty() && !Mouse.isButtonDown(0)) {
            Minecraft minecraft = Minecraft.getMinecraft();
            ITooltipFlag flag = minecraft.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL;
            List<String> tooltip = tooltipBlockStack.getTooltip(minecraft.player, flag);
            EnumRarity rarity = tooltipBlockStack.getRarity();
            for (int k = 0; k < tooltip.size(); ++k) {
                if (k == 0) {
                    tooltip.set(k, rarity.color + tooltip.get(k));
                } else {
                    tooltip.set(k, TextFormatting.GRAY + tooltip.get(k));
                }
            }
            Map<ItemStack, List<ITextComponent>> blockTooltipMap = infoPage.getBlockTooltipMap();
            if (blockTooltipMap.containsKey(tooltipBlockStack)) {
                List<ITextComponent> tooltips = blockTooltipMap.get(tooltipBlockStack);
                for (int i = 0; i < tooltips.size(); i++) {
                    //Start at i+1 due to ItemStack name
                    tooltip.add(i + 1, tooltips.get(i).getFormattedText());
                }
            }
            return tooltip;
        }
        return Collections.emptyList();
    }

    public void addBlockTooltips(int slotIndex, boolean input, ItemStack itemStack, List<String> tooltip) {
        Map<ItemStack, List<ITextComponent>> blockTooltipMap = infoPage.getBlockTooltipMap();
        if (blockTooltipMap.containsKey(itemStack)) {
            List<ITextComponent> tooltips = blockTooltipMap.get(itemStack);
            for (int i = 0; i < tooltips.size(); i++) {
                tooltip.add(i + 1, tooltips.get(i).getFormattedText());
            }
        }
    }

    private static class PartInfo {
        final ItemStackKey itemStackKey;
        boolean isController = false;
        boolean isTile = false;
        final int blockId;
        int amount = 0;

        PartInfo(final ItemStackKey itemStackKey, final BlockInfo blockInfo) {
            this.itemStackKey = itemStackKey;
            this.blockId = Block.getIdFromBlock(blockInfo.getBlockState().getBlock());
            TileEntity tileEntity = blockInfo.getTileEntity();
            if (tileEntity != null) {
                this.isTile = true;
                MetaTileEntity mte = ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                if (mte instanceof MultiblockControllerBase)
                    this.isController = true;
            }
        }

        ItemStack getItemStack() {
            ItemStack result = this.itemStackKey.getItemStack();
            result.setCount(this.amount);
            return result;
        }
    }

    private static void gatherBlockDrops(World world, Map<BlockPos, BlockInfo> blocks, Set<ItemStackKey> drops, Map<ItemStackKey, PartInfo> partsMap) {
        NonNullList<ItemStack> dropsList = NonNullList.create();
        for (Entry<BlockPos, BlockInfo> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            IBlockState blockState = world.getBlockState(pos);
            NonNullList<ItemStack> blockDrops = NonNullList.create();
            blockState.getBlock().getDrops(blockDrops, world, pos, blockState, 0);
            dropsList.addAll(blockDrops);

            for (ItemStack itemStack : blockDrops) {
                ItemStackKey itemStackKey = new ItemStackKey(itemStack);
                PartInfo partInfo = partsMap.get(itemStackKey);
                if (partInfo == null) {
                    partInfo = new PartInfo(itemStackKey, entry.getValue());
                    partsMap.put(itemStackKey, partInfo);
                }
                ++partInfo.amount;
            }
        }
        for (ItemStack itemStack : dropsList) {
            drops.add(new ItemStackKey(itemStack));
        }
    }


    private MBPattern initializePattern(MultiblockShapeInfo shapeInfo, Set<ItemStackKey> blockDrops) {
        Map<BlockPos, BlockInfo> blockMap = new HashMap<>();
        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        for (int z = 0; z < blocks.length; z++) {
            BlockInfo[][] aisle = blocks[z];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int x = 0; x < column.length; x++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    BlockInfo blockInfo = column[x];
                    blockMap.put(blockPos, blockInfo);
                }
            }
        }
        TrackedDummyWorld world = new TrackedDummyWorld();
        ImmediateWorldSceneRenderer worldSceneRenderer = new ImmediateWorldSceneRenderer(world);
        worldSceneRenderer.setClearColor(0xC6C6C6);
        world.addBlocks(blockMap);
        Vector3f size = world.getSize();
        Vector3f minPos = world.getMinPos();
        center = new Vector3f(minPos.x + size.x / 2, minPos.y + size.y / 2, minPos.z + size.z / 2);
        HashMap<ItemStackKey, PartInfo> partsMap = new HashMap<>();
        gatherBlockDrops(worldSceneRenderer.world, blockMap, blockDrops, partsMap);
        worldSceneRenderer.addRenderedBlocks(world.renderedBlocks, null);
        worldSceneRenderer.setOnLookingAt(this::renderBlockOverLay);
        worldSceneRenderer.world.updateEntities();
        world.setRenderFilter(pos->worldSceneRenderer.renderedBlocksMap.keySet().stream().anyMatch(c->c.contains(pos)));
        ArrayList<PartInfo> partInfos = new ArrayList<>(partsMap.values());
        partInfos.sort((one, two) -> {
            if (one.isController) return -1;
            if (two.isController) return +1;
            if (one.isTile && !two.isTile) return -1;
            if (two.isTile && !one.isTile) return +1;
            if (one.blockId != two.blockId) return two.blockId - one.blockId;
            return two.amount - one.amount;
        });
        ArrayList<ItemStack> parts = new ArrayList<>();
        for (PartInfo partInfo : partInfos) {
            parts.add(partInfo.getItemStack());
        }
        return new MBPattern(worldSceneRenderer, parts);
    }

    @SideOnly(Side.CLIENT)
    private void renderBlockOverLay(RayTraceResult rayTraceResult) {
        BlockPos pos = rayTraceResult.getBlockPos();
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.disableTexture2D();
        CCRenderState renderState = CCRenderState.instance();
        renderState.startDrawing(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR, tessellator.getBuffer());
        ColourMultiplier multiplier = new ColourMultiplier(0);
        renderState.setPipeline(new Translation(pos), multiplier);
        BlockRenderer.BlockFace blockFace = new BlockRenderer.BlockFace();
        renderState.setModel(blockFace);
        for (EnumFacing renderSide : EnumFacing.VALUES) {
            float diffuse = LightUtil.diffuseLight(renderSide);
            int color = (int) (255 * diffuse);
            multiplier.colour = RenderUtil.packColor(color, color, color, 100);
            blockFace.loadCuboidFace(Cuboid6.full, renderSide.getIndex());
            renderState.render();
        }
        renderState.draw();
        GlStateManager.enableTexture2D();
    }

}
