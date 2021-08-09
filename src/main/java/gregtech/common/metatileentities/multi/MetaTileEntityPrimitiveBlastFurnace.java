package gregtech.common.metatileentities.multi;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.ItemHandlerProxy;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.recipes.PrimitiveBlastFurnaceRecipe;
import gregtech.api.render.ICubeRenderer;
import gregtech.api.render.Textures;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static gregtech.api.util.InventoryUtils.simulateItemStackMerge;

public class MetaTileEntityPrimitiveBlastFurnace extends MultiblockControllerBase {

    private static final List<OrePrefix> FUEL_DISPLAY_PREFIXES = ImmutableList.of(
            OrePrefix.gem, OrePrefix.dust, OrePrefix.block);

    private static final Map<String, MaterialStack> ALTERNATIVE_MATERIAL_NAMES = ImmutableMap.of(
            "fuelCoke", new MaterialStack(Materials.Coke, GTValues.M),
            "blockFuelCoke", new MaterialStack(Materials.Coke, GTValues.M * 9L));

    private static final Map<Material, Float> MATERIAL_FUEL_MAP = ImmutableMap.of(
            Materials.Lignite, 0.7f,
            Materials.Charcoal, 1.0f,
            Materials.Coal, 1.0f,
            Materials.Coke, 2.0f);

    private int maxProgressDuration;
    private int currentProgress;
    private NonNullList<ItemStack> outputsList;
    private float fuelUnitsLeft;
    private boolean isActive;
    private boolean wasActiveAndNeedUpdate;

    private ItemStack lastInputStack = ItemStack.EMPTY;
    private PrimitiveBlastFurnaceRecipe previousRecipe;

    public MetaTileEntityPrimitiveBlastFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected void updateFormedValid() {
        if (maxProgressDuration == 0) {
            if (tryPickNewRecipe()) {
                if (wasActiveAndNeedUpdate) {
                    this.wasActiveAndNeedUpdate = false;
                } else setActive(true);
            }
        } else if (++currentProgress >= maxProgressDuration) {
            finishCurrentRecipe();
            this.wasActiveAndNeedUpdate = true;
            return;
        }
        if (wasActiveAndNeedUpdate) {
            this.wasActiveAndNeedUpdate = false;
            setActive(false);
        }
    }

    private void finishCurrentRecipe() {
        this.maxProgressDuration = 0;
        this.currentProgress = 0;
        MetaTileEntity.addItemsToItemHandler(exportItems, false, outputsList);
        this.outputsList = null;
        markDirty();
    }

    private PrimitiveBlastFurnaceRecipe getOrRefreshRecipe(ItemStack inputStack) {
        PrimitiveBlastFurnaceRecipe currentRecipe = null;
        if (previousRecipe != null &&
                previousRecipe.getInput().getIngredient().apply(inputStack)) {
            currentRecipe = previousRecipe;
        } else if (!areItemStacksEqual(inputStack, lastInputStack)) {
            this.lastInputStack = inputStack.isEmpty() ? ItemStack.EMPTY : inputStack.copy();
            currentRecipe = RecipeMaps.PRIMITIVE_BLAST_FURNACE_RECIPES.stream()
                    .filter(it -> it.getInput().getIngredient().test(inputStack))
                    .findFirst().orElse(null);
            if (currentRecipe != null) {
                this.previousRecipe = currentRecipe;
            }
        }
        return currentRecipe;
    }

    private static boolean areItemStacksEqual(ItemStack stackA, ItemStack stackB) {
        return (stackA.isEmpty() && stackB.isEmpty()) ||
                (ItemStack.areItemsEqual(stackA, stackB) &&
                        ItemStack.areItemStackTagsEqual(stackA, stackB));
    }

    private boolean setupRecipe(ItemStack inputStack, int fuelAmount, PrimitiveBlastFurnaceRecipe recipe) {
        List<ItemStack> outputs = new ArrayList<>();
        outputs.add(recipe.getOutput());
        outputs.add(getAshForRecipeFuelConsumption(recipe.getFuelAmount()));
        return inputStack.getCount() >= recipe.getInput().getCount() && fuelAmount >= recipe.getFuelAmount() &&
                simulateItemStackMerge(outputs, exportItems);
    }

    private boolean tryPickNewRecipe() {
        ItemStack inputStack = importItems.getStackInSlot(0);
        ItemStack fuelStack = importItems.getStackInSlot(1);
        if (inputStack.isEmpty() || (fuelStack.isEmpty() && fuelUnitsLeft == 0)) {
            return false;
        }
        float fuelUnitsPerItem = getFuelUnits(fuelStack);
        float totalFuelUnits = fuelUnitsLeft + fuelUnitsPerItem * fuelStack.getCount();
        PrimitiveBlastFurnaceRecipe currentRecipe = getOrRefreshRecipe(inputStack);
        if (currentRecipe != null && setupRecipe(inputStack, (int) totalFuelUnits, currentRecipe)) {
            inputStack.shrink(currentRecipe.getInput().getCount());
            float fuelUnitsToConsume = currentRecipe.getFuelAmount();
            float remainderConsumed = Math.min(fuelUnitsToConsume, fuelUnitsLeft);
            fuelUnitsToConsume -= remainderConsumed;

            int fuelItemsToConsume = (int) Math.ceil(fuelUnitsToConsume / (fuelUnitsPerItem * 1.0));
            float remainderAdded = fuelItemsToConsume * fuelUnitsPerItem - fuelUnitsToConsume;

            this.fuelUnitsLeft += (remainderAdded - remainderConsumed);
            fuelStack.shrink(fuelItemsToConsume);
            this.maxProgressDuration = currentRecipe.getDuration();
            this.currentProgress = 0;
            NonNullList<ItemStack> outputs = NonNullList.create();
            outputs.add(currentRecipe.getOutput().copy());
            outputs.add(getAshForRecipeFuelConsumption(currentRecipe.getFuelAmount()));
            this.outputsList = outputs;
            markDirty();
            return true;
        }
        return false;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("Active", isActive);
        data.setBoolean("WasActive", wasActiveAndNeedUpdate);
        data.setFloat("FuelUnitsLeft", fuelUnitsLeft);
        data.setInteger("MaxProgress", maxProgressDuration);
        if (maxProgressDuration > 0) {
            data.setInteger("Progress", currentProgress);
            NBTTagList itemOutputs = new NBTTagList();
            for (ItemStack itemStack : outputsList) {
                itemOutputs.appendTag(itemStack.writeToNBT(new NBTTagCompound()));
            }
            data.setTag("Outputs", itemOutputs);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isActive = data.getBoolean("Active");
        this.wasActiveAndNeedUpdate = data.getBoolean("WasActive");
        this.fuelUnitsLeft = data.getFloat("FuelUnitsLeft");
        this.maxProgressDuration = data.getInteger("MaxProgress");
        if (maxProgressDuration > 0) {
            this.currentProgress = data.getInteger("Progress");
            NBTTagList itemOutputs = data.getTagList("Outputs", NBT.TAG_COMPOUND);
            this.outputsList = NonNullList.create();
            for (int i = 0; i < itemOutputs.tagCount(); i++) {
                this.outputsList.add(new ItemStack(itemOutputs.getCompoundTagAt(i)));
            }
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            this.isActive = buf.readBoolean();
            getWorld().checkLight(getPos());
            getHolder().scheduleChunkForRenderUpdate();
        }
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (!getWorld().isRemote) {
            writeCustomData(100, b -> b.writeBoolean(isActive));
            getWorld().checkLight(getPos());
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public double getProgressScaled() {
        return maxProgressDuration == 0 ? 0.0 : (currentProgress / (maxProgressDuration * 1.0));
    }

    @Override
    public int getLightValueForPart(IMultiblockPart sourcePart) {
        return sourcePart == null && isActive ? 15 : 0;
    }

    public static float getFuelUnits(ItemStack fuelType) {
        if (fuelType.isEmpty()) {
            return 0;
        }
        MaterialStack materialStack = OreDictUnifier.getMaterial(fuelType);
        if (materialStack == null) {
            //try alternative material names if we can't find valid standard one
            materialStack = OreDictUnifier.getOreDictionaryNames(fuelType).stream()
                    .map(ALTERNATIVE_MATERIAL_NAMES::get)
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }

        if (materialStack != null && materialStack.amount >= GTValues.M) {
            int materialAmount = (int) (materialStack.amount / GTValues.M);
            Float materialMultiplier = MATERIAL_FUEL_MAP.get(materialStack.material);
            return materialMultiplier == null ? 0 : materialAmount * materialMultiplier;
        }
        return 0;
    }

    public static List<ItemStack> getDisplayFuelsForRecipe(int recipeFuelUnits) {
        ArrayList<ItemStack> resultStacks = new ArrayList<>();

        for (OrePrefix orePrefix : FUEL_DISPLAY_PREFIXES) {
            int materialAmount = (int) (orePrefix.materialAmount / GTValues.M);
            for (Material fuelMaterial : MATERIAL_FUEL_MAP.keySet()) {
                float materialMultiplier = MATERIAL_FUEL_MAP.get(fuelMaterial);
                float materialFuelUnits = materialAmount * materialMultiplier;
                int stackSize = (int) Math.ceil(recipeFuelUnits / materialFuelUnits);

                List<ItemStack> materialStacks = OreDictUnifier.getAll(new UnificationEntry(orePrefix, fuelMaterial));
                for (ItemStack materialStack : materialStacks) {
                    materialStack.setCount(stackSize);
                    resultStacks.add(materialStack);
                }
            }
        }

        for (String specialFuelName : ALTERNATIVE_MATERIAL_NAMES.keySet()) {
            List<ItemStack> allItemStacks = OreDictUnifier.getAllWithOreDictionaryName(specialFuelName);
            MaterialStack materialStack = ALTERNATIVE_MATERIAL_NAMES.get(specialFuelName);
            int materialAmount = (int) (materialStack.amount / GTValues.M);
            float materialMultiplier = MATERIAL_FUEL_MAP.getOrDefault(materialStack.material, 0.0f);
            float materialFuelUnits = materialAmount * materialMultiplier;
            int stackSize = (int) Math.ceil(recipeFuelUnits / materialFuelUnits);

            for (ItemStack itemStack : allItemStacks) {
                itemStack.setCount(stackSize);
                resultStacks.add(itemStack);
            }
        }

        //de-duplicate items in the result list
        int currentIndex = 0;
        loop:
        while (currentIndex < resultStacks.size()) {
            ItemStack itemStack = resultStacks.get(currentIndex);

            for (int i = 0; i < currentIndex; i++) {
                ItemStack otherStack = resultStacks.get(i);
                if (ItemStack.areItemsEqual(itemStack, otherStack)) {
                    resultStacks.remove(currentIndex); //remove duplicate item
                    continue loop;
                }
            }
            //increment index only if we didn't remove item
            currentIndex++;
        }

        return resultStacks;
    }

    public static ItemStack getAshForRecipeFuelConsumption(int fuelUnitsConsumed) {
        int ashAmount = MathHelper.clamp(1 + fuelUnitsConsumed, 2, 8);
        return OreDictUnifier.get(OrePrefix.dustTiny, Materials.DarkAsh, ashAmount);
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.PRIMITIVE_BRICKS);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PRIMITIVE_BRICKS;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.PRIMITIVE_BLAST_FURNACE_OVERLAY.render(renderState, translation, pipeline, getFrontFacing(), isActive());
        if (isActive() && isStructureFormed()) {
            EnumFacing back = getFrontFacing().getOpposite();
            Matrix4 offset = translation.copy().translate(back.getXOffset(), -0.3, back.getZOffset());
            TextureAtlasSprite sprite = TextureUtils.getBlockTexture("lava_still");
            renderState.brightness = 0xF000F0;
            renderState.colour = 0xFFFFFFFF;
            Textures.renderFace(renderState, offset, new IVertexOperation[0], EnumFacing.UP, Cuboid6.full, sprite);
        }
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        ItemStackHandler emptyHandler = new ItemStackHandler(0);
        this.itemInventory = new ItemHandlerProxy(emptyHandler, emptyHandler);
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(2) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (slot == 1 && getFuelUnits(stack) == 0)
                    return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(2);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "XXX", "XXX", "XXX")
                .aisle("XXX", "X#X", "X#X", "X#X")
                .aisle("XXX", "XYX", "XXX", "XXX")
                .where('X', statePredicate(getCasingState()))
                .where('#', isAirPredicate())
                .where('Y', selfPredicate())
                .build();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityPrimitiveBlastFurnace(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .widget(new SlotWidget(importItems, 0, 33, 15, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.INGOT_OVERLAY))
                .widget(new SlotWidget(importItems, 1, 33, 33, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.FURNACE_OVERLAY))
                .progressBar(this::getProgressScaled, 58, 24, 20, 15, GuiTextures.BRONZE_BLAST_FURNACE_PROGRESS_BAR, MoveType.HORIZONTAL)
                .widget(new SlotWidget(exportItems, 0, 85, 24, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.INGOT_OVERLAY))
                .widget(new SlotWidget(exportItems, 1, 103, 24, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.DUST_OVERLAY))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 0)
                .build(getHolder(), entityPlayer);
    }
}
