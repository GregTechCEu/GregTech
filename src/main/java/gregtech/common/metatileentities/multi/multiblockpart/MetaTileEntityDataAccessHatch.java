package gregtech.common.metatileentities.multi.multiblockpart;

import gregtech.api.GTValues;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.AbilityInstances;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.mui.GTGuiTextures;
import gregtech.api.mui.GTGuis;
import gregtech.api.mui.IMetaTileEntityGuiHolder;
import gregtech.api.mui.MetaTileEntityGuiData;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.AssemblyLineManager;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityDataBank;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaTileEntityDataAccessHatch extends MetaTileEntityMultiblockNotifiablePart
                                           implements IMultiblockAbilityPart<IDataAccessHatch>, IDataAccessHatch,
                                           IDataInfoProvider, IMetaTileEntityGuiHolder {

    private final Set<Recipe> recipes;
    private final boolean isCreative;

    public MetaTileEntityDataAccessHatch(ResourceLocation metaTileEntityId, int tier, boolean isCreative) {
        super(metaTileEntityId, tier, false);
        this.isCreative = isCreative;
        this.recipes = isCreative ? Collections.emptySet() : new ObjectOpenHashSet<>(importItems.getSlots());
        rebuildData(getController() instanceof MetaTileEntityDataBank);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDataAccessHatch(metaTileEntityId, getTier(), isCreative());
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        if (isCreative) return super.createImportItemHandler();
        return new NotifiableItemStackHandler(this, getInventorySize(), getController(), false) {

            @Override
            public void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                rebuildData(getController() instanceof MetaTileEntityDataBank);
            }

            @NotNull
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                var controller = MetaTileEntityDataAccessHatch.this.getController();
                boolean isDataBank = controller instanceof MetaTileEntityDataBank;
                if (AssemblyLineManager.isStackDataItem(stack, isDataBank) &&
                        AssemblyLineManager.hasResearchTag(stack)) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        };
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            if (isCreative) {
                Textures.CREATIVE_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            } else {
                Textures.DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    @Override
    public boolean shouldOpenUI() {
        return !isCreative;
    }

    @Override
    public @NotNull ModularPanel buildUI(MetaTileEntityGuiData guiData, PanelSyncManager panelSyncManager,
                                         UISettings settings) {
        int rowSize = (int) Math.sqrt(getInventorySize());
        panelSyncManager.registerSlotGroup("slots", rowSize);

        Widget<?> recipeLogo = GTGuiTextures.getLogo(getUITheme())
                .asWidget()
                .align(Alignment.BottomRight)
                .tooltipBuilder(tooltip -> {
                    if (recipes.isEmpty()) {
                        tooltip.addLine(IKey.lang("gregtech.machine.data_access_hatch.no_recipes"));
                    } else {
                        tooltip.addLine(IKey.lang("gregtech.machine.data_access_hatch.recipes"));
                        tooltip.spaceLine(2);
                    }

                    Set<ItemStack> itemsAdded = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAll());
                    for (Recipe recipe : recipes) {
                        ItemStack output = recipe.getOutputs().get(0);
                        if (itemsAdded.add(output)) {
                            tooltip.add(new ItemDrawable(output));
                            tooltip.space();
                            tooltip.addLine(IKey.str(output.getDisplayName()));
                        }
                    }
                });

        return GTGuis.createPanel(this, 176, 18 + 18 * rowSize + 94)
                .child(IKey.lang(getMetaFullName())
                        .asWidget()
                        .pos(5, 5))
                .child(Flow.row()
                        .top(18)
                        .margin(7, 0)
                        .coverChildrenHeight()
                        .child(new Grid()
                                .height(rowSize * 18)
                                .minElementMargin(0, 0)
                                .minColWidth(18).minRowHeight(18)
                                .alignX(0.5f)
                                .mapTo(rowSize, rowSize * rowSize, index -> new ItemSlot()
                                        .slot(SyncHandlers.itemSlot(importItems, index)
                                                .slotGroup("slots")
                                                .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                                    recipeLogo.markTooltipDirty();
                                                    if (onlyAmountChanged &&
                                                            importItems instanceof GTItemStackHandler gtHandler) {
                                                        gtHandler.onContentsChanged(index);
                                                    }
                                                }))))
                        .child(recipeLogo))
                .child(SlotGroupWidget.playerInventory(false)
                        .bottom(7)
                        .left(7));
    }

    protected int getInventorySize() {
        return getTier() == GTValues.LuV ? 16 : 9;
    }

    private void rebuildData(boolean isDataBank) {
        if (isCreative || getWorld() == null) return;
        recipes.clear();
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            ItemStack stack = this.importItems.getStackInSlot(i);
            String researchId = AssemblyLineManager.readResearchId(stack);
            boolean isValid = AssemblyLineManager.isStackDataItem(stack, isDataBank);
            if (researchId != null && isValid) {
                Collection<Recipe> collection = ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES)
                        .getDataStickEntry(researchId);
                if (collection != null) {
                    recipes.addAll(collection);
                }
            }
        }
    }

    @Override
    public boolean isRecipeAvailable(@NotNull Recipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        return recipes.contains(recipe);
    }

    @Override
    public boolean isCreative() {
        return this.isCreative;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableResearch) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.data_access_hatch.tooltip.1"));
        if (isCreative) {
            tooltip.add(I18n.format("gregtech.creative_tooltip.1") + TooltipHelper.RAINBOW +
                    I18n.format("gregtech.creative_tooltip.2") + I18n.format("gregtech.creative_tooltip.3"));
        } else {
            tooltip.add(I18n.format("gregtech.machine.data_access_hatch.tooltip.2", getInventorySize()));
        }
        if (canPartShare()) {
            tooltip.add(I18n.format("gregtech.universal.enabled"));
        } else {
            tooltip.add(I18n.format("gregtech.universal.disabled"));
        }
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        if (recipes.isEmpty()) return Collections.emptyList();
        List<ITextComponent> list = new ArrayList<>();

        list.add(new TextComponentTranslation("behavior.data_item.assemblyline.title"));
        list.add(new TextComponentString(""));
        Collection<ItemStack> itemsAdded = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAll());
        for (Recipe recipe : recipes) {
            ItemStack stack = recipe.getOutputs().get(0);
            if (!itemsAdded.contains(stack)) {
                itemsAdded.add(stack);
                list.add(new TextComponentTranslation("behavior.data_item.assemblyline.data",
                        LocalizationUtils.format(stack.getTranslationKey())));
            }
        }
        return list;
    }

    @Override
    public boolean canPartShare() {
        return isCreative;
    }

    @Override
    public MultiblockAbility<IDataAccessHatch> getAbility() {
        return MultiblockAbility.DATA_ACCESS_HATCH;
    }

    @Override
    public void registerAbilities(@NotNull AbilityInstances abilityInstances) {
        abilityInstances.add(this);
    }

    @Override
    public void addToMultiBlock(MultiblockControllerBase controllerBase) {
        rebuildData(controllerBase instanceof MetaTileEntityDataBank);
        super.addToMultiBlock(controllerBase);
    }
}
