package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IDataAccessHatch;
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
import gregtech.common.ConfigHolder;
import gregtech.common.items.MetaItems;
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
import java.util.Set;

public class MetaTileEntityDataAccessHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IDataAccessHatch>, IDataAccessHatch, IDataInfoProvider {

    private final Set<Recipe> recipes = new ObjectOpenHashSet<>();

    public MetaTileEntityDataAccessHatch(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier, false);
        rebuildData();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityDataAccessHatch(metaTileEntityId, getTier());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(getInventorySize()) {
            @Override
            protected void onContentsChanged(int slot) {
                rebuildData();
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (stack.isItemEqual(MetaItems.TOOL_DATA_STICK.getStackForm()) ||
                            stack.isItemEqual(MetaItems.TOOL_DATA_ORB.getStackForm())) {
                    if (stack.getSubCompound(IResearchRecipeMap.RESEARCH_NBT_TAG) != null) {
                        return super.insertItem(slot, stack, simulate);
                    }
                }
                return stack;
            }
        };
    }

    protected int getInventorySize() {
        return getTier() == GTValues.ZPM ? 16 : 9;
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
        recipes.clear();
        for (int i = 0; i < this.importItems.getSlots(); i++) {
            ItemStack stack = this.importItems.getStackInSlot(i);
            NBTTagCompound researchItemNBT = stack.getSubCompound(IResearchRecipeMap.RESEARCH_NBT_TAG);
            if (researchItemNBT != null) {
                String researchId = researchItemNBT.getString(IResearchRecipeMap.RESEARCH_ID_NBT_TAG);
                if (researchId.isEmpty()) return;
                recipes.addAll(((IResearchRecipeMap) RecipeMaps.ASSEMBLY_LINE_RECIPES).getDataStickEntry(researchId));
            }
        }
    }

    @Nonnull
    @Override
    public Set<Recipe> getAvailableRecipes() {
        return recipes;
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
        tooltip.add(I18n.format("gregtech.machine.data_access_hatch.tooltip.2", getInventorySize()));
    }

    @Nonnull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = new ObjectArrayList<>();
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
