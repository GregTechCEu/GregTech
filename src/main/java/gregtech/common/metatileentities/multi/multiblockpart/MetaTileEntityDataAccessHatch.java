package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.impl.NotifiableFilteredItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.IDataInfoProvider;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.machines.IResearchRecipeMap;
import gregtech.api.util.LocalizationUtils;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleOverlayRenderer;
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MetaTileEntityDataAccessHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IDataAccessHatch>, IDataAccessHatch, IDataInfoProvider {

    private final Map<GTRecipeInput, Set<Recipe>> dataMap = new Object2ObjectOpenHashMap<>();

    private final boolean isCreative;

    public MetaTileEntityDataAccessHatch(ResourceLocation metaTileEntityId, int tier, boolean isCreative) {
        super(metaTileEntityId, tier, false);
        this.isCreative = isCreative;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDataAccessHatch(metaTileEntityId, getTier(), this.isCreative);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            SimpleOverlayRenderer renderer = this.isCreative ? Textures.CREATIVE_DATA_ACCESS_HATCH : Textures.DATA_ACCESS_HATCH;
            renderer.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(0);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableFilteredItemStackHandler(getInventorySize(), getController(), false)
                .setFillPredicate(stack -> {
                    if (stack.isEmpty() || stack.isItemEqual(MetaItems.TOOL_DATA_STICK.getStackForm()) ||
                            stack.isItemEqual(MetaItems.TOOL_DATA_ORB.getStackForm())) {

                        addDataNBT(stack);
                        return true;
                    }
                    return false;
                })
                .setExtractPredicate(stack -> {
                    rebuildData();
                    return true;
                });
    }

    protected int getInventorySize() {
        return isCreative ? 0 : getTier() == GTValues.ZPM ? 16 : 9;
    }

    @Override
    public MultiblockAbility<IDataAccessHatch> getAbility() {
        return MultiblockAbility.DATA_ACCESS_HATCH;
    }

    @Override
    public void registerAbilities(@Nonnull List<IDataAccessHatch> abilityList) {
        abilityList.add(this);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(getInventorySize());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 18 + 18 * rowSize + 94)
                .label(6, 6, getMetaFullName());

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(isExportHatch ? exportItems : importItems, index,
                        88 - rowSize * 9 + x * 18, 18 + y * 18, true, !isExportHatch)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12)
                .build(getHolder(), entityPlayer);
    }

    private void rebuildData() {
        dataMap.clear();
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            ItemStack stack = this.importItems.getStackInSlot(i);
            addDataNBT(stack);
        }
    }

    private void addDataNBT(@Nonnull ItemStack stack) {
        NBTTagCompound researchItemNBT = stack.getSubCompound(IResearchRecipeMap.RESEARCH_NBT_TAG);
        if (researchItemNBT != null) {
            String researchId = researchItemNBT.getString(IResearchRecipeMap.RESEARCH_ID_NBT_TAG);
            if (researchId.isEmpty()) return;
            dataMap.put(GTRecipeItemInput.getOrCreate(stack), ((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        rebuildData();
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        rebuildData();
    }

    @Nonnull
    @Override
    public Set<Recipe> getAvailableRecipes() {
        Set<Recipe> recipes = new ObjectOpenHashSet<>();
        recipes.clear();
        for (Set<Recipe> recipeSet : dataMap.values()) {
            recipes.addAll(recipeSet);
        }
        return recipes;
    }

    @Override
    public boolean isCreative() {
        return this.isCreative;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return super.openGUIOnRightClick() && !this.isCreative;
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        if (ConfigHolder.machines.enableResearch) {
            super.getSubItems(creativeTab, subItems);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.data_access_hatch.tooltip.1"));
        if (isCreative) tooltip.add(I18n.format("gregtech.machine.data_access_hatch.creative.tooltip.1"));
        else tooltip.add(I18n.format("gregtech.machine.data_access_hatch.tooltip.2", getInventorySize()));
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ObjectArrayList<>();
        rebuildData();
        Set<Recipe> recipes = getAvailableRecipes();
        if (recipes.isEmpty()) return list;

        list.add(new TextComponentTranslation("behavior.data_item.assemblyline.title"));
        list.add(new TextComponentString(""));
        Set<GTRecipeInput> itemsAdded = new ObjectOpenHashSet<>();
        for (Recipe recipe : recipes) {
            ItemStack stack = recipe.getOutputs().get(0);
            GTRecipeInput gtRecipeInput = GTRecipeItemInput.getOrCreate(stack);
            if (!itemsAdded.contains(gtRecipeInput)) {
                itemsAdded.add(gtRecipeInput);
                list.add(new TextComponentTranslation("behavior.data_item.assemblyline.data", LocalizationUtils.format(stack.getTranslationKey())));
            }
        }
        return list;
    }
}
