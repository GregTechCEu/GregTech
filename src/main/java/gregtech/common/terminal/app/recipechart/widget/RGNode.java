package gregtech.common.terminal.app.recipechart.widget;

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
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RGNode extends WidgetGroup implements IDraggable {
    protected Object head;
    protected int recipePer;
    protected ItemStack catalyst;
    private boolean isSelected;
    private WidgetGroup toolGroup;
    private WidgetGroup inputsGroup;
    private RGContainer container;
    private SimpleTextWidget textWidget;
    protected Map<RGNode, Integer> parentNodes;
    protected Map<Widget, Set<RGNode>> children;

    public RGNode(int x, int y, RGContainer container, Object head, boolean isPhantom) {
        super(x, y, 18, 18);
        init(container);
        this.head = head;
        if (isPhantom) {
            PhantomWidget phantom = new PhantomWidget(0, 0, head).setChangeListener(object -> {
                RGNode.this.head = object;
                // Reset any children nodes, now that the parent has changed
                for (Set<RGNode> childs : children.values()) {
                    for (RGNode child : childs) {
                        child.removeParent(this);
                    }
                }
                children.clear();

                // Clear the Inputs for the replaced parent
                this.inputsGroup.widgets.clear();

            });
            this.addWidget(phantom);
            toolGroup.addWidget(new CircleButtonWidget(-11, 49, 8, 1, 12)
                    .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                    .setIcon(GuiTextures.ICON_CALCULATOR)
                    .setHoverText("terminal.recipe_chart.calculator")
                    .setClickListener(cd -> TerminalDialogWidget.showTextFieldDialog(container.os, "terminal.recipe_chart.demand", s->{
                        try {
                            return Integer.parseInt(s) > 0;
                        } catch (Exception ignored){
                            return false;
                        }
                    }, s -> {
                        if (s != null && !s.isEmpty()) {
                            updateDemand(Integer.parseInt(s));
                        }
                    }).setClientSide().open()));
            toolGroup.addWidget(new CircleButtonWidget(9, 49, 8, 1, 12)
                    .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                    .setIcon(GuiTextures.ICON_ADD)
                    .setHoverText("terminal.recipe_chart.add")
                    .setClickListener(cd -> TerminalDialogWidget.showItemSelector(container.os, "terminal.recipe_chart.demand", false, itemStack -> true,
                        itemStack -> {
                            if (itemStack != null && !itemStack.isEmpty()){
                                IFluidHandler handlerItem = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                                if (handlerItem != null && handlerItem.getTankProperties().length > 0) {
                                    FluidStack fluidStack = handlerItem.getTankProperties()[0].getContents();
                                    if (fluidStack != null) {
                                        phantom.setObject(fluidStack);
                                        return;
                                    }
                                }
                                phantom.setObject(itemStack);

                                // Reset any children nodes, now that the parent has changed
                                for (Set<RGNode> childs : children.values()) {
                                    for (RGNode child : childs) {
                                        child.removeParent(this);
                                    }
                                }
                                children.clear();

                                // Clear the Inputs for the replaced parent
                                this.inputsGroup.widgets.clear();
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
        textWidget = new SimpleTextWidget(9, -5, "", -1, () -> {
            if (head instanceof ItemStack) {
                return ((ItemStack) head).getDisplayName();
            } else if (head instanceof FluidStack) {
                return ((FluidStack) head).getLocalizedName();
            }
            return "terminal.recipe_chart.drag";
        }, true).setShadow(true);
        textWidget.setVisible(false);
        textWidget.setActive(false);
        this.addWidget(textWidget);
        inputsGroup = new WidgetGroup(0, 0, 0, 0);
        this.addWidget(inputsGroup);
        toolGroup = new WidgetGroup(0, 0, 0, 0);
        this.addWidget(toolGroup);
        toolGroup.addWidget(new CircleButtonWidget(-11, 9, 8, 1, 12)
                .setColors(0, TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_3.getColor())
                .setIcon(GuiTextures.ICON_REMOVE)
                .setHoverText("terminal.guide_editor.remove")
                .setClickListener(cd -> remove()));
        toolGroup.addWidget(new CircleButtonWidget(-11, 29, 8, 1, 12)
                .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                .setIcon(GuiTextures.ICON_VISIBLE)
                .setHoverText("terminal.recipe_chart.visible")
                .setClickListener(cd -> {
                    textWidget.setActive(!textWidget.isActive());
                    textWidget.setVisible(!textWidget.isVisible());
                }));
        toolGroup.addWidget(new CircleButtonWidget(9, 29, 8, 1, 12)
                .setColors(0, TerminalTheme.COLOR_7.getColor(), 0)
                .setIcon(GuiTextures.ICON_LOCATION)
                .setHoverText("terminal.recipe_chart.jei")
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
        children = new LinkedHashMap<>(); // important
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
            return checkMergeAvailable(node);
        } else if (this.head instanceof FluidStack && node.head instanceof FluidStack && ((FluidStack) this.head).isFluidEqual((FluidStack) node.head)) {
            return checkMergeAvailable(node);
        }
        return false;
    }

    private boolean checkMergeAvailable(RGNode node) {
        Position pos1 = this.getPosition();
        Position pos2 = node.getPosition();
        return Math.abs(pos1.x - pos2.x) < 18 && Math.abs(pos1.y - pos2.y) < 18&&
                !this.findAllChildren().contains(node) &&
                !node.findAllChildren().contains(this);
    }

    public Set<RGNode> findAllChildren() {
        Set<RGNode> result = new HashSet<>();
        for (Set<RGNode> nodes : children.values()) {
            for (RGNode node : nodes) {
                result.add(node);
                result.addAll(node.findAllChildren());
            }
        }
        return result;
    }

    public void mergeNode(RGNode node) {
        for (RGNode parentNode : node.parentNodes.keySet()) {
            for (Set<RGNode> value : parentNode.children.values()) {
                if (value.remove(node)) {
                    value.add(this);
                    addParent(parentNode);
                    parentNode.updateDemand(parentNode.getHeadDemand());
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

    public void addParent(RGNode parent) {
        container.addOrUpdateLine(parent, this);
        this.parentNodes.put(parent, parent.getChildDemand(this));
        updateDemand(parentNodes.values().stream().mapToInt(it->it).sum());
    }

    public void updateDemand(int demand) {
        dfsUpdateDemand(demand, new Stack<>());
    }

    private void dfsUpdateDemand(int demand, Stack<RGNode> updated) {
        if (updated.contains(this)) return;
        updated.push(this);
        if (head instanceof ItemStack) {
            ((ItemStack) head).setCount(demand);
        } else if (head instanceof FluidStack) {
            ((FluidStack) head).amount = demand;
        }
        for (Set<RGNode> children : children.values()) {
            for (RGNode child : children) {
                child.parentNodes.put(this, this.getChildDemand(child));
                child.dfsUpdateDemand(child.parentNodes.values().stream().mapToInt(it->it).sum(), updated);
            }
        }
        updated.pop();
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
            container.addOrUpdateLine(parentNode, this);
        }
        for (Set<RGNode> childs : children.values()) {
            for (RGNode child : childs) {
                container.addOrUpdateLine(this, child);
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
            RGNode.this.recipePer = 0;

            // items
            List<ItemStack> itemInputs = new ArrayList<>();
            recipeLayout.getItemStacks().getGuiIngredients().values().forEach(it->{
                if (it.isInput() && it.getDisplayedIngredient()!= null) {
                    ItemStack input = it.getDisplayedIngredient();
                    for (ItemStack itemInput : itemInputs) {
                        if (itemInput.isItemEqual(input)) {
                            itemInput.setCount(itemInput.getCount() + input.getCount());
                            return;
                        }
                    }
                    itemInputs.add(input.copy());
                } else if (head instanceof ItemStack && !it.isInput()) {
                    for (ItemStack ingredient : it.getAllIngredients()) {
                        if (((ItemStack) head).isItemEqual(ingredient)) {
                            RGNode.this.recipePer += ingredient.getCount();
                            break;
                        }
                    }
                }
            });

            // fluids
            List<FluidStack> fluidInputs = new ArrayList<>();
            recipeLayout.getFluidStacks().getGuiIngredients().values().forEach(it->{
                if (it.isInput() && it.getDisplayedIngredient()!= null) {
                    FluidStack input = it.getDisplayedIngredient();
                    for (FluidStack fluidInput : fluidInputs) {
                        if (fluidInput.isFluidEqual(input)) {
                            fluidInput.amount += input.amount;
                            return;
                        }
                    }
                    fluidInputs.add(input.copy());
                } else if (head instanceof FluidStack && !it.isInput()) {
                    for (FluidStack ingredient : it.getAllIngredients()) {
                        if (((FluidStack) head).isFluidEqual(ingredient)) {
                            RGNode.this.recipePer += ingredient.amount;
                            break;
                        }
                    }
                }
            });
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
                        MetaTileEntity mte = GTUtility.getMetaTileEntity((ItemStack) o);
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
            setRecipe(itemInputs, fluidInputs, catalyst, recipePer);
            return true;
        }
        return false;
    }

    public void deserializeRelationNBT(NBTTagList parentsTag, NBTTagList childrenTag) {
        for (NBTBase nbtBase : parentsTag) {
            int[] nbt = ((NBTTagIntArray) nbtBase).getIntArray();
            parentNodes.put(container.nodes.get(nbt[0]), nbt[1]);
        }
        Iterator<Widget> iterator = children.keySet().iterator();
        for (NBTBase nbtBase : childrenTag) {
            int[] nbt = ((NBTTagIntArray) nbtBase).getIntArray();
            children.get(iterator.next()).addAll(Arrays.stream(nbt).mapToObj(it->container.nodes.get(it)).collect(Collectors.toList()));
        }
    }

    public static RGNode deserializeNodeNBT(NBTTagCompound nodeTag, RGContainer container) {
        byte type = nodeTag.getByte("type"); // 0-null 1-itemstack 2-fluidstack
        Object head = null;
        if (type == 1) {
            head = new ItemStack(nodeTag.getCompoundTag("nbt"));
            ((ItemStack)head).setCount(nodeTag.getInteger("count"));
        } else if (type == 2) {
            head = FluidStack.loadFluidStackFromNBT(nodeTag.getCompoundTag("nbt"));
            assert head != null;
            ((FluidStack)head).amount = nodeTag.getInteger("count");
        }
        RGNode node;
        if (nodeTag.getBoolean("phantom")) {
            node = new RGNode(nodeTag.getInteger("x"), nodeTag.getInteger("y"), container, head, true);
        } else {
            node = new RGNode(nodeTag.getInteger("x"), nodeTag.getInteger("y"), container, head, false);
        }
        NBTTagList itemsList = nodeTag.getTagList("items", Constants.NBT.TAG_COMPOUND);
        NBTTagList fluidsList = nodeTag.getTagList("fluids", Constants.NBT.TAG_COMPOUND);
        List<ItemStack> itemInputs = new LinkedList<>();
        itemsList.forEach(base->{
            if (base instanceof NBTTagCompound) {
                itemInputs.add(new ItemStack((NBTTagCompound) base));
            }
        });
        List<FluidStack> fluidsInputs = new LinkedList<>();
        fluidsList.forEach(base->{
            if (base instanceof NBTTagCompound) {
                fluidsInputs.add(FluidStack.loadFluidStackFromNBT((NBTTagCompound) base));
            }
        });
        node.setRecipe(itemInputs, fluidsInputs,
                nodeTag.hasKey("catalyst") ? new ItemStack(nodeTag.getCompoundTag("catalyst")) : null,
                nodeTag.getInteger("per"));
        boolean visible = nodeTag.getBoolean("visible");
        node.textWidget.setVisible(visible);
        node.textWidget.setActive(visible);
        return node;
    }

    public NBTTagCompound serializeNodeNBT() {
        //head
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("x", getSelfPosition().x + container.getScrollXOffset());
        nbt.setInteger("y", getSelfPosition().y + container.getScrollYOffset());
        nbt.setByte("type", head instanceof ItemStack ? (byte)1 : head instanceof FluidStack ? (byte)2 : 0);
        if (head instanceof ItemStack) {
            nbt.setTag("nbt", ((ItemStack) head).serializeNBT());
            nbt.setInteger("count", ((ItemStack) head).getCount());
        } else if (head instanceof FluidStack) {
            nbt.setTag("nbt", ((FluidStack) head).writeToNBT(new NBTTagCompound()));
            nbt.setInteger("count", ((FluidStack) head).amount);
        }
        nbt.setBoolean("phantom", widgets.stream().anyMatch(it->it instanceof PhantomWidget));
        // recipe + children
        NBTTagList itemsList = new NBTTagList();
        NBTTagList fluidsList = new NBTTagList();
        NBTTagList childrenList = new NBTTagList();
        for (Map.Entry<Widget, Set<RGNode>> entry : children.entrySet()) {
            Widget widget = entry.getKey();
            if (widget instanceof SlotWidget) {
                itemsList.appendTag(((SlotWidget) widget).getHandle().getStack().serializeNBT());
            } else if (widget instanceof TankWidget) {
                fluidsList.appendTag(((TankWidget) widget).fluidTank.getFluid().writeToNBT(new NBTTagCompound()));
            } else {
                continue;
            }
            NBTTagIntArray childList = new NBTTagIntArray(entry.getValue().stream().mapToInt(it->container.nodes.indexOf(it)).toArray());
            childrenList.appendTag(childList);
        }
        nbt.setTag("items", itemsList);
        nbt.setTag("fluids", fluidsList);
        nbt.setTag("children", childrenList);
        if (catalyst != null) {
            nbt.setTag("catalyst", catalyst.serializeNBT());
        }
        nbt.setInteger("per", recipePer);
        // parent
        NBTTagList parentsList = new NBTTagList();
        for (Map.Entry<RGNode, Integer> entry : parentNodes.entrySet()) {
            parentsList.appendTag(new NBTTagIntArray(new int[]{container.nodes.indexOf(entry.getKey()), entry.getValue()}));
        }
        nbt.setTag("parents", parentsList);
        nbt.setBoolean("visible", textWidget.isVisible());
        return nbt;
    }

    private void setRecipe(List<ItemStack> itemInputs, List<FluidStack> fluidInputs, ItemStack catalyst, int recipePer) {
        this.recipePer = recipePer;
        this.catalyst = catalyst;
        inputsGroup.clearAllWidgets();
        for (Set<RGNode> childs : children.values()) {
            for (RGNode child : childs) {
                child.removeParent(this);
            }
        }
        children.clear();
        AtomicInteger y = new AtomicInteger(-20);
        for (ItemStack itemInput : itemInputs) {
            ItemStackHandler handler = new ItemStackHandler(1);
            handler.setStackInSlot(0, itemInput);
            Widget widget = new SlotWidget(handler, 0, 0, y.addAndGet(20), false, false) {
                @Override
                public boolean mouseClicked(int mouseX, int mouseY, int button) {
                    return RGNode.this.handleTipsSlotClick(mouseX, mouseY,this, handler.getStackInSlot(0).copy());
                }
            }.setBackgroundTexture(TerminalTheme.COLOR_B_2);
            inputsGroup.addWidget(widget);
            children.put(widget, new HashSet<>());
        }
        for (FluidStack fluidInput : fluidInputs) {
            FluidTank tank = new FluidTank(fluidInput, Integer.MAX_VALUE);
            Widget widget = new TankWidget(tank, 0, y.addAndGet(20), 18, 18) {
                @Override
                public boolean mouseClicked(int mouseX, int mouseY, int button) {
                    return RGNode.this.handleTipsSlotClick(mouseX, mouseY,this, tank.getFluid().copy());
                }
            }.setAlwaysShowFull(true).setBackgroundTexture(TerminalTheme.COLOR_B_2).setClient();
            inputsGroup.addWidget(widget);
            children.put(widget, new HashSet<>());
        }
        inputsGroup.setSelfPosition(new Position(25, -(inputsGroup.widgets.size() * 20) / 2 + 8));
    }

    private boolean handleTipsSlotClick(int mouseX, int mouseY, Widget slot, Object object){
        if (slot.isMouseOverElement(mouseX, mouseY)) {
            Position position = inputsGroup.getSelfPosition();
            RGNode child = container.addNode(RGNode.this.getSelfPosition().x + 50, RGNode.this.getSelfPosition().y + position.y + slot.getSelfPosition().y, object);
            Set<RGNode> childs = RGNode.this.children.get(slot);
            childs.add(child);

            child.addParent(RGNode.this);
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
        children.forEach((widget, rgNode) -> rgNode.forEach(child -> container.addOrUpdateLine(RGNode.this, child)));
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
            super.mouseClicked(mouseX, mouseY, button);
            return false;
        } else if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else if (isSelected) {
            container.setSelectedNode(null);
        }
        return false;
    }
}
