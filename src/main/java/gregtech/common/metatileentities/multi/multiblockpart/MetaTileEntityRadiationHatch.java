package gregtech.common.metatileentities.multi.multiblockpart;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IRadiationHatch;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.util.ItemStackHashStrategy;
import gregtech.client.renderer.texture.Textures;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;
import java.util.Map;

public class MetaTileEntityRadiationHatch extends MetaTileEntityMultiblockNotifiablePart implements IMultiblockAbilityPart<IRadiationHatch>, IRadiationHatch {

    private final boolean isCreative;
    private float radValue;
    private static final Map<ItemStack, Float> radiationValues = new Object2FloatOpenCustomHashMap<>(ItemStackHashStrategy.builder()
            .compareCount(false)
            .compareDamage(true)
            .compareItem(true)
            .compareTag(false)
            .build());

    public MetaTileEntityRadiationHatch(ResourceLocation metaTileEntityId, int tier, boolean isCreative) {
        super(metaTileEntityId, tier, false);
        this.isCreative = isCreative;
        this.radValue = 0;
    }

    @Override
    public float getRadValue() {
        return radValue;
    }

    @Override
    public boolean isCreative() {
        return isCreative;
    }

    public static void addRadiationItem(GTRecipeInput item, float rads) {
        for (ItemStack stack : item.getInputStacks()) {
            radiationValues.put(stack, rads);
        }
    }

    private void recalculateRadValue() {
        radValue = 0.0F;
        for (int i = 0; i < importItems.getSlots(); i++) {
            ItemStack stack = importItems.getStackInSlot(i);
            if (stack != null && !stack.isEmpty()) {
                radValue += radiationValues.get(stack) * stack.getCount();
            }
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityRadiationHatch(metaTileEntityId, getTier(), isCreative);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(getInventorySize(), getController(), false) {
            @Override
            public void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                recalculateRadValue();
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler();
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

    private int getInventorySize() {
        return 9;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        if (shouldRenderOverlay()) {
            Textures.PIPE_IN_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        }
    }

    @Override
    public MultiblockAbility<IRadiationHatch> getAbility() {
        return MultiblockAbility.RADIATION_HATCH;
    }

    @Override
    public void registerAbilities(List<IRadiationHatch> abilityList) {
        abilityList.add(this);
    }
}
