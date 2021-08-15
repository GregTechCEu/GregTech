package gregtech.api.terminal.app.recipechart.widget;

import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.ModularUIContainer;
import gregtech.api.gui.widgets.SimpleTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.recipes.Recipe;
import gregtech.api.terminal.gui.IDraggable;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Position;
import gregtech.integration.jei.GTJeiPlugin;
import gregtech.integration.jei.recipe.GTRecipeWrapper;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RGNode extends WidgetGroup implements IDraggable {
    protected Object head;
    protected int recipePer;
    private boolean isSelected;
    private WidgetGroup toolGroup;
    private WidgetGroup inputsGroup;
    private RGContainer container;
    protected Map<RGNode, Integer> parentNodes;
    protected Map<Widget, Set<RGNode>> children;

    public RGNode(int x, int y, RGContainer container, Object head, boolean isPhantom) {
        super(x, y, 18, 18);
        init(container);
        this.head = head;
        if (isPhantom) {
            this.addWidget(new PhantomWidget(0, 0, head).setChangeListener(object -> RGNode.this.head = object));
            toolGroup.addWidget(new CircleButtonWidget(-11, 49, 8, 1, 12)
                    .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                    .setIcon(GuiTextures.ICON_CALCULATOR)
                    .setHoverText("Calculator")
                    .setClickListener(cd -> TerminalDialogWidget.showTextFieldDialog(container.os, "Demand", s->{
                        try {
                            return Integer.parseInt(s) > 0;
                        } catch (Exception ignored){
                            return false;
                        }
                    }, s -> {
                        if (s != null) {
                            updateDemand(Integer.parseInt(s));
                        }
                    }).setClientSide().open()));
        } else {
            if (head instanceof ItemStack) {
                ItemStackHandler handler = new ItemStackHandler(1);
                handler.setStackInSlot(0, (ItemStack) head);
                this.addWidget(new SlotWidget(handler, 0, 0, 0, false, false).setBackgroundTexture(TerminalTheme.COLOR_B_2));
            } else if (head instanceof FluidStack) {
                FluidTank tank = new FluidTank((FluidStack) head, Integer.MAX_VALUE);
                this.addWidget(new TankWidget(tank, 0, 0, 18, 18).setAlwaysShowFull(true).setBackgroundTexture(TerminalTheme.COLOR_B_2).setClient());
            }
        }
    }

    private void init(RGContainer container) {
        this.container = container;
        SimpleTextWidget textWidget = new SimpleTextWidget(9, -5, "", -1, () -> {
            if (head instanceof ItemStack) {
                return ((ItemStack) head).getDisplayName();
            } else if (head instanceof FluidStack) {
                return ((FluidStack) head).getLocalizedName();
            }
            return "Drag ingredients into slot.";
        }, true).setShadow(true);
        textWidget.setVisible(false);
        this.addWidget(textWidget);
        inputsGroup = new WidgetGroup(0, 0, 0, 0);
        this.addWidget(inputsGroup);
        toolGroup = new WidgetGroup(0, 0, 0, 0);
        this.addWidget(toolGroup);
        toolGroup.addWidget(new CircleButtonWidget(-11, 9, 8, 1, 12)
                .setColors(0, TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_3.getColor())
                .setIcon(GuiTextures.ICON_REMOVE)
                .setHoverText("remove")
                .setClickListener(cd -> remove()));
        toolGroup.addWidget(new CircleButtonWidget(-11, 29, 8, 1, 12)
                .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                .setIcon(GuiTextures.ICON_VISIBLE)
                .setHoverText("Text Visible")
                .setClickListener(cd -> {
                    textWidget.setActive(!textWidget.isActive());
                    textWidget.setVisible(!textWidget.isVisible());
                }));
        toolGroup.addWidget(new CircleButtonWidget(9, 29, 8, 1, 12)
                .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                .setIcon(GuiTextures.ICON_LOCATION)
                .setHoverText("JEI Focus")
                .setClickListener(cd -> {
                    if (GTJeiPlugin.jeiRuntime != null && head != null) {
                        GTJeiPlugin.jeiRuntime.getRecipesGui().show(new Focus<>(IFocus.Mode.OUTPUT, head));
                    }
                }));
        inputsGroup.setVisible(false);
        inputsGroup.setActive(false);
        toolGroup.setVisible(false);
        toolGroup.setActive(false);
        parentNodes = new HashMap<>();
        children = new HashMap<>();
    }

    public int getHeadDemand() {
        if (head instanceof ItemStack) {
            return ((ItemStack) head).getCount();
        } else if (head instanceof FluidStack) {
            return ((FluidStack) head).amount;
        }
        return 0;
    }

    public int getChildDemand(RGNode child) {
        for (Map.Entry<Widget, Set<RGNode>> entry : children.entrySet()) {
            if (entry.getValue().contains(child)) {
                int perC = 0;
                if (entry.getKey() instanceof SlotWidget) {
                    perC = ((SlotWidget) entry.getKey()).getHandle().getStack().getCount();
                } else if(entry.getKey() instanceof TankWidget) {
                    perC = ((TankWidget) entry.getKey()).fluidTank.getFluidAmount();
                }
                int ratioSum = entry.getValue().stream().mapToInt(it->container.getLine(RGNode.this, it).ratio).sum();
                return MathHelper.ceil(perC * MathHelper.ceil(getHeadDemand() / (float)recipePer) * container.getLine(RGNode.this, child).ratio / (float)ratioSum);
            }
        }
        return 0;
    }

    public boolean canMerge(RGNode node) {
        if (this.head instanceof ItemStack && node.head instanceof ItemStack && ((ItemStack) this.head).isItemEqual((ItemStack) node.head)) {
            Position pos1 = this.getPosition();
            Position pos2 = node.getPosition();
            return Math.abs(pos1.x - pos2.x) < 18 && Math.abs(pos1.y - pos2.y) < 18;
        } else if (this.head instanceof FluidStack && node.head instanceof FluidStack && ((FluidStack) this.head).isFluidEqual((FluidStack) node.head)) {
            Position pos1 = this.getPosition();
            Position pos2 = node.getPosition();
            return Math.abs(pos1.x - pos2.x) < 18 && Math.abs(pos1.y - pos2.y) < 18;
        }
        return false;
    }

    public void mergeNode(RGNode node) {
        for (RGNode parentNode : node.parentNodes.keySet()) {
            for (Set<RGNode> value : parentNode.children.values()) {
                if (value.remove(node)) {
                    value.add(this);
                    addParent(parentNode, container.getLine(parentNode, node).getCatalyst());
                    break;
                }
            }
        }
        node.remove();
    }

    public void remove() {
        if (isSelected) {
            container.setSelectedNode(null);
        }
        container.removeNode(this);
        for (RGNode parentNode : parentNodes.keySet()) {
            container.removeLine(parentNode, this);
            parentNode.onChildRemoved(this);
        }
        parentNodes.clear();
        for (Set<RGNode> childs : children.values()) {
            for (RGNode child : childs) {
                child.removeParent(this);
            }
        }
        children.clear();
    }

    public Position getNodePosition(RGNode child) {
        if (child != null && isSelected) {
            for (Map.Entry<Widget, Set<RGNode>> nodeEntry : children.entrySet()) {
                if (nodeEntry.getValue().contains(child)) {
                    return nodeEntry.getKey().getPosition();
                }
            }
        }
        return this.getPosition();
    }

    public void addParent(RGNode parent, ItemStack catalyst) {
        container.addLine(parent, this, catalyst);
        this.parentNodes.put(parent, parent.getChildDemand(this));
        updateDemand(parentNodes.values().stream().mapToInt(it->it).sum());
    }

    public void updateDemand(int demand) {
        if (head instanceof ItemStack) {
            ((ItemStack) head).setCount(demand);
        } else if (head instanceof FluidStack) {
            ((FluidStack) head).amount = demand;
        }
        for (Set<RGNode> children : children.values()) {
            for (RGNode child : children) {
                child.parentNodes.put(this, this.getChildDemand(child));
                child.updateDemand(child.parentNodes.values().stream().mapToInt(it->it).sum());
            }
        }
    }

    public void removeParent(RGNode parent) {
        this.parentNodes.remove(parent);
        if (parentNodes.size() == 0) {
            for (Set<RGNode> childs : children.values()) {
                for (RGNode child : childs) {
                    child.removeParent(RGNode.this);
                }
            }
            children.clear();
            container.removeNode(this);
        } else {
            updateDemand(parentNodes.values().stream().mapToInt(it->it).sum());
        }
        container.removeLine(parent, this);
    }

    public void onChildRemoved(RGNode child) {
        for (Set<RGNode> childs : children.values()) {
            if (childs.remove(child)) {
                updateDemand(getHeadDemand());
                break;
            }
        }
    }

    @Override
    protected void onPositionUpdate() {
        super.onPositionUpdate();
        for (RGNode parentNode : parentNodes.keySet()) {
            container.updateLine(parentNode, this);
        }
        for (Set<RGNode> childs : children.values()) {
            for (RGNode child : childs) {
                container.updateLine(this, child);
            }
        }
    }

    public boolean transferRecipe(ModularUIContainer x, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (isSelected) {
            Object obj = recipeLayout.getFocus() == null ? null : recipeLayout.getFocus().getValue();
            if (head instanceof ItemStack && obj instanceof ItemStack) {
                if(!((ItemStack) head).isItemEqual((ItemStack) obj)) {
                    return false;
                }
            } else if (head instanceof FluidStack && obj instanceof FluidStack) {
                if(!((FluidStack) head).isFluidEqual((FluidStack)obj)) {
                    return false;
                }
            } else {
                return false;
            }
            if (!doTransfer) return true;
            inputsGroup.clearAllWidgets();
            for (Set<RGNode> childs : children.values()) {
                for (RGNode child : childs) {
                    child.removeParent(this);
                }
            }
            children.clear();
            AtomicInteger y = new AtomicInteger(-20);
            recipeLayout.getItemStacks().getGuiIngredients().values().stream().filter(it -> it.getDisplayedIngredient() != null && it.isInput()).forEach(it -> {
                ItemStackHandler handler = new ItemStackHandler(1);
                handler.setStackInSlot(0, it.getDisplayedIngredient());
                Widget widget = new SlotWidget(handler, 0, 0, y.addAndGet(20), false, false) {
                    @Override
                    public boolean mouseClicked(int mouseX, int mouseY, int button) {
                        return RGNode.this.handleTipsSlotClick(mouseX, mouseY, recipeLayout,this, handler.getStackInSlot(0).copy());
                    }
                }.setBackgroundTexture(TerminalTheme.COLOR_B_2);
                inputsGroup.addWidget(widget);
                children.put(widget, new HashSet<>());
            });
            recipeLayout.getFluidStacks().getGuiIngredients().values().stream().filter(it -> it.getDisplayedIngredient() != null && it.isInput()).forEach(it -> {
                FluidTank tank = new FluidTank(it.getDisplayedIngredient(), Integer.MAX_VALUE);
                Widget widget = new TankWidget(tank, 0, y.addAndGet(20), 18, 18) {
                    @Override
                    public boolean mouseClicked(int mouseX, int mouseY, int button) {
                        return RGNode.this.handleTipsSlotClick(mouseX, mouseY, recipeLayout,this, tank.getFluid().copy());
                    }
                }.setAlwaysShowFull(true).setBackgroundTexture(TerminalTheme.COLOR_B_2);
                inputsGroup.addWidget(widget);
                children.put(widget, new HashSet<>());
            });
            inputsGroup.setSelfPosition(new Position(25, -(inputsGroup.widgets.size() * 20) / 2 + 8));
            if (head instanceof ItemStack) {
                recipeLayout.getItemStacks().getGuiIngredients().values().stream().anyMatch(it->{
                    if (!it.isInput()) {
                        for (ItemStack ingredient : it.getAllIngredients()) {
                            if (((ItemStack) head).isItemEqual(ingredient)) {
                                RGNode.this.recipePer = ingredient.getCount();
                                return true;
                            }
                        }
                    }
                    return false;
                });
            } else if (head instanceof FluidStack) {
                recipeLayout.getFluidStacks().getGuiIngredients().values().stream().anyMatch(it->{
                    if (!it.isInput()) {
                        for (FluidStack ingredient : it.getAllIngredients()) {
                            if (((FluidStack) head).isFluidEqual(ingredient)) {
                                RGNode.this.recipePer = ingredient.amount;
                                return true;
                            }
                        }
                    }
                    return false;
                });
            }
            return true;
        }
        return false;
    }

    private boolean handleTipsSlotClick(int mouseX, int mouseY, IRecipeLayout recipeLayout, Widget slot, Object object){
        if (slot.isMouseOverElement(mouseX, mouseY)) {
            Position position = inputsGroup.getSelfPosition();
            RGNode child = container.addNode(RGNode.this.getSelfPosition().x + 50, RGNode.this.getSelfPosition().y + position.y + slot.getSelfPosition().y, object);
            Set<RGNode> childs = RGNode.this.children.get(slot);
            childs.add(child);

            // CHECK GTCE RECIPES
            Recipe recipe = null;
            if (recipeLayout instanceof RecipeLayout) {
                IRecipeWrapper recipeWrapper = ObfuscationReflectionHelper.getPrivateValue(RecipeLayout.class, (RecipeLayout)recipeLayout, "recipeWrapper");
                if (recipeWrapper instanceof GTRecipeWrapper) {
                    recipe = ((GTRecipeWrapper) recipeWrapper).getRecipe();
                }
            }
            IRecipeCategory<?> category = recipeLayout.getRecipeCategory();
            List<Object> catalysts = GTJeiPlugin.jeiRuntime.getRecipeRegistry().getRecipeCatalysts(category);
            ItemStack catalyst = null;

            if (recipe != null) { // GT
                int tierRequire = GTUtility.getTierByVoltage(recipe.getEUt());
                for (Object o : catalysts) {
                    if (o instanceof ItemStack) {
                        MetaTileEntity mte = MachineItemBlock.getMetaTileEntity((ItemStack) o);
                        if (mte instanceof SimpleMachineMetaTileEntity) {
                            if (tierRequire < ((SimpleMachineMetaTileEntity) mte).getTier()) {
                                catalyst = (ItemStack) o;
                                break;
                            }
                        }
                    }
                }
            }

            if (catalyst == null) {
                for (Object o : catalysts) {
                    if (o instanceof ItemStack) {
                        catalyst = (ItemStack) o;
                        break;
                    }
                }
            }

            child.addParent(RGNode.this, catalyst);
            RGNode.this.updateDemand(RGNode.this.getHeadDemand());
            return true;
        }
        return false;
    }

    public void updateSelected(boolean selected) {
        isSelected = selected;
        if (selected) {
            toolGroup.setActive(true);
            toolGroup.setVisible(true);
            inputsGroup.setActive(true);
            inputsGroup.setVisible(true);
        } else {
            toolGroup.setActive(false);
            toolGroup.setVisible(false);
            inputsGroup.setActive(false);
            inputsGroup.setVisible(false);
        }
        children.forEach((widget, rgNode) -> rgNode.forEach(child -> container.updateLine(RGNode.this, child)));
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        if (isSelected) {
            drawBorder(x, y, width, height, 0xff00ff00, 2);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
    }

    @Override
    public boolean allowDrag(int mouseX, int mouseY, int button) {
        return isMouseOverElement(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (!isSelected) {
                container.setSelectedNode(this);
            }
            return false;
        } else if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else if (isSelected) {
            container.setSelectedNode(null);
        }
        return false;
    }
}
