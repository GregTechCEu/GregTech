package gregtech.common.gui.widget;

import com.google.common.collect.Lists;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.api.util.oreglob.OreGlob;
import it.unimi.dsi.fastutil.objects.Object2BooleanAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author brachy84
 */
public class OreFilterTestSlot extends WidgetGroup implements IGhostIngredientTarget {

    @Nullable
    private OreGlob glob;
    private ItemStack testStack = ItemStack.EMPTY;

    private Object2BooleanMap<String> testResult;
    @Nullable
    private Boolean emptyMatch;

    private final ImageWidget match;
    private final ImageWidget noMatch;

    public OreFilterTestSlot(int xPosition, int yPosition) {
        super(xPosition, yPosition, 18, 18);
        this.match = new ImageWidget(18 - 5, -3, 9, 6, GuiTextures.ORE_FILTER_MATCH);
        this.noMatch = new ImageWidget(18 - 5, -3, 7, 7, GuiTextures.ORE_FILTER_NO_MATCH);
        addWidget(this.match);
        addWidget(this.noMatch);
        updatePreview();
    }

    public void setGlob(@Nullable OreGlob glob) {
        this.glob = glob;
        updatePreview();
    }

    public void setTestStack(ItemStack testStack) {
        if (testStack != null) {
            this.testStack = testStack;
            updatePreview();
        }
    }

    private void updatePreview() {
        if (this.glob == null || this.testStack.isEmpty()) {
            this.testResult = Object2BooleanMaps.emptyMap();
            this.emptyMatch = null;
            this.match.setVisible(false);
            this.noMatch.setVisible(false);
        } else {
            Set<String> oreDicts = OreDictUnifier.getOreDictionaryNames(this.testStack);
            boolean success;
            if (oreDicts.isEmpty()) {
                this.testResult = Object2BooleanMaps.singleton("", success = this.glob.matches(""));
                this.emptyMatch = success;
            } else {
                this.testResult = new Object2BooleanAVLTreeMap<>();
                this.emptyMatch = null;
                success = false;
                for (String oreDict : oreDicts) {
                    boolean matches = this.glob.matches(oreDict);
                    this.testResult.put(oreDict, matches);
                    success |= matches;
                }
            }
            this.match.setVisible(success);
            this.noMatch.setVisible(!success);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            putItem(player.inventory.getItemStack());
            return true;
        }
        return false;
    }

    private void putItem(ItemStack stack) {
        if ((stack.isEmpty() ^ testStack.isEmpty()) || !testStack.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(testStack, stack)) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            setTestStack(copy);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        GuiTextures.SLOT.draw(pos.x, pos.y, 18, 18);

        if (!testStack.isEmpty()) {
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            RenderHelper.enableStandardItemLighting();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
            itemRender.renderItemAndEffectIntoGUI(testStack, pos.x + 1, pos.y + 1);
            itemRender.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, testStack, pos.x + 1, pos.y + 1, null);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
        }
        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            GlStateManager.disableDepth();
            GlStateManager.colorMask(true, true, true, false);
            drawSolidRect(getPosition().x + 1, getPosition().y + 1, 16, 16, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }

        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        if (isActive() && isMouseOverElement(mouseX, mouseY)) {
            List<String> list;
            if (!this.testStack.isEmpty()) {
                if (this.emptyMatch != null) {
                    list = Collections.singletonList(I18n.format(this.emptyMatch ?
                            "cover.ore_dictionary_filter.test_slot.no_oredict.matches" :
                            "cover.ore_dictionary_filter.test_slot.no_oredict.matches_not"));
                } else {
                    list = this.testResult.object2BooleanEntrySet().stream().map(
                            e -> I18n.format(e.getBooleanValue() ?
                                    "cover.ore_dictionary_filter.test_slot.matches" :
                                    "cover.ore_dictionary_filter.test_slot.matches_not", e.getKey())
                    ).collect(Collectors.toList());
                }
            } else {
                list = Arrays.asList(GTUtility.getForwardNewLineRegex()
                        .split(I18n.format("cover.ore_dictionary_filter.test_slot.info")));
            }
            drawHoveringText(ItemStack.EMPTY, list, 300, mouseX, mouseY);
        }
    }

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(new IGhostIngredientHandler.Target<Object>() {
            @Nonnull
            @Override
            public Rectangle getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                if (ingredient instanceof ItemStack) {
                    putItem((ItemStack) ingredient);
                }
            }
        });
    }
}
