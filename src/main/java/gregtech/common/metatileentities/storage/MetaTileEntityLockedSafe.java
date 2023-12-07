package gregtech.common.metatileentities.storage;

import gregtech.api.GTValues;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.ProgressWidget;
import gregtech.api.gui.widgets.ProgressWidget.MoveType;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ingredients.GTRecipeInput;
import gregtech.api.recipes.ingredients.GTRecipeItemInput;
import gregtech.api.recipes.ingredients.GTRecipeOreInput;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.worldgen.LootTableHelper;
import gregtech.loaders.recipe.CraftingComponent;
import gregtech.loaders.recipe.CraftingComponent.Component;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.oredict.OreDictionary;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.function.DoubleSupplier;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_CONTENTS_SEED;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_LOCKED_STATE;

public class MetaTileEntityLockedSafe extends MetaTileEntity implements IFastRenderMetaTileEntity {

    private static final int MAX_UNLOCK_PROGRESS = 100;
    private static Component[] ALLOWED_COMPONENTS;
    private static final IndexedCuboid6 COLLISION_BOX = new IndexedCuboid6(null,
            new Cuboid6(3 / 16.0, 0 / 16.0, 3 / 16.0, 13 / 16.0, 14 / 16.0, 13 / 16.0));

    private int unlockProgress = -1;
    private int unlockComponentTier = 1;
    private boolean isSafeUnlocked = false;

    private long unlockComponentsSeed = 0L;
    private final ItemStackHandler unlockComponents = new GTItemStackHandler(this, 2);
    private final ItemStackHandler unlockInventory = new GTItemStackHandler(this, 2) {

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            int maxStackSize = canPutUnlockItemInSlot(slot, stack);
            if (maxStackSize == 0) return stack;
            int maxAmount = Math.min(maxStackSize, stack.getCount());
            int remainder = stack.getCount() - maxAmount;
            stack = stack.copy();
            stack.setCount(maxAmount);
            int addAmount = super.insertItem(slot, stack, simulate).getCount();
            int totalAmount = remainder + addAmount;
            return totalAmount == 0 ? ItemStack.EMPTY : GTUtility.copy(totalAmount, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            recheckUnlockItemsAndUnlock();
        }
    };
    private final ItemStackHandler safeLootInventory = new GTItemStackHandler(this, 27);
    private float doorAngle = 0.0f;
    private float prevDoorAngle = 0.0f;

    public MetaTileEntityLockedSafe(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        if (isSafeUnlocked()) {
            clearInventory(itemBuffer, safeLootInventory);
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLockedSafe(metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();
        updateOpenVisualAnimation();
        updateOpenProgress();
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        generateUnlockComponents();
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(COLLISION_BOX);
    }

    @Override
    public boolean shouldDropWhenDestroyed() {
        return false;
    }

    @Override
    public float getBlockHardness() {
        return isSafeUnlocked() ? 6.0f : -1.0f;
    }

    @Override
    public float getBlockResistance() {
        return 6000000.0F;
    }

    private void generateUnlockComponents() {
        if (!getWorld().isRemote && unlockComponentsSeed == 0L) {
            setUnlockComponentsSeed(new Random().nextLong());
        }
    }

    private void updateDisplayUnlockComponents() {
        GTRecipeInput[] unlockComponents = getUnlockComponents();
        for (int i = 0; i < Math.min(this.unlockComponents.getSlots(), unlockComponents.length); i++) {
            if (unlockComponents[i].isOreDict()) {
                this.unlockComponents.setStackInSlot(i,
                        OreDictionary.getOres(OreDictionary.getOreName(unlockComponents[i].getOreDict())).get(0));
            } else {
                this.unlockComponents.setStackInSlot(i, (unlockComponents[i].getInputStacks()[0]));
            }
        }
    }

    private GTRecipeInput[] getUnlockComponents() {
        if (ALLOWED_COMPONENTS == null)
            ALLOWED_COMPONENTS = new Component[] { CraftingComponent.PUMP, CraftingComponent.CONVEYOR,
                    CraftingComponent.EMITTER, CraftingComponent.SENSOR };

        Random random = new Random(unlockComponentsSeed);
        return new GTRecipeInput[] {
                new GTRecipeOreInput(CraftingComponent.CIRCUIT.getIngredient(unlockComponentTier).toString()),
                new GTRecipeItemInput((ItemStack) ALLOWED_COMPONENTS[random.nextInt(ALLOWED_COMPONENTS.length)]
                        .getIngredient(unlockComponentTier)),
        };
    }

    private void recheckUnlockItemsAndUnlock() {
        if (getWorld() != null && !getWorld().isRemote && !isSafeUnlocked() &&
                checkUnlockedItems() && unlockProgress == -1) {
            this.unlockProgress++;
            markDirty();
        }
    }

    private int canPutUnlockItemInSlot(int slot, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return 0;
        }
        boolean isRequiredItem = false;
        int amountRequired = 0;
        GTRecipeInput[] unlockComponents = getUnlockComponents();
        for (GTRecipeInput stack : unlockComponents) {
            if (stack == null) continue;
            if (!stack.acceptsStack(itemStack)) continue;
            amountRequired = stack.getAmount();
            isRequiredItem = true;
            break;
        }
        if (!isRequiredItem) {
            return 0;
        }
        for (int i = 0; i < unlockInventory.getSlots(); i++) {
            ItemStack componentStack = unlockInventory.getStackInSlot(i);
            if (componentStack.isEmpty()) continue;
            if (!componentStack.isItemEqual(itemStack)) continue;
            return slot == i ? amountRequired - componentStack.getCount() : 0;
        }
        return amountRequired;
    }

    private boolean checkUnlockedItems() {
        GTRecipeInput[] unlockComponents = getUnlockComponents();
        for (GTRecipeInput stack : unlockComponents) {
            if (stack == null) continue;
            int itemLeftToCheck = stack.getAmount();
            for (int i = 0; i < unlockInventory.getSlots(); i++) {
                ItemStack otherStack = unlockInventory.getStackInSlot(i);
                if (otherStack.isEmpty()) continue;
                if (stack.acceptsStack(otherStack)) continue;
                itemLeftToCheck -= otherStack.getCount();
            }
            if (itemLeftToCheck > 0) return false;
        }
        return true;
    }

    private void updateOpenProgress() {
        if (!getWorld().isRemote && unlockProgress >= 0 && unlockProgress < MAX_UNLOCK_PROGRESS) {
            ++this.unlockProgress;
            if (unlockProgress >= MAX_UNLOCK_PROGRESS) {
                generateChestContents();
                setSafeUnlocked(true);
            }
        }
    }

    private void generateChestContents() {
        ResourceLocation lootTableLocation = GTUtility.gregtechId("chests/abandoned_safe_" + unlockComponentTier);
        WorldServer worldServer = (WorldServer) getWorld();
        LootTable lootTable = worldServer.getLootTableManager().getLootTableFromLocation(lootTableLocation);
        LootContext lootContext = new LootContext.Builder(worldServer).build();
        Random random = new Random();
        List<ItemStack> loots = lootTable.generateLootForPools(random, lootContext);
        LootTableHelper.fillInventory(safeLootInventory, random, loots);
    }

    private void updateOpenVisualAnimation() {
        this.prevDoorAngle = doorAngle;
        if ((!isSafeUnlocked && this.doorAngle > 0.0F) ||
                (isSafeUnlocked && this.doorAngle < 1.0F)) {
            if (isSafeUnlocked) {
                this.doorAngle += 0.1F;
            } else {
                this.doorAngle -= 0.1F;
            }
            if (this.doorAngle > 1.0F) {
                this.doorAngle = 1.0F;
            } else if (this.doorAngle < 0.0F) {
                this.doorAngle = 0.0F;
            }
        }
    }

    public boolean isSafeUnlocked() {
        return isSafeUnlocked;
    }

    public void setSafeUnlocked(boolean safeUnlocked) {
        this.isSafeUnlocked = safeUnlocked;
        if (getWorld() != null && !getWorld().isRemote) {
            writeCustomData(UPDATE_LOCKED_STATE, buf -> buf.writeBoolean(safeUnlocked));
            notifyBlockUpdate();
            markDirty();
        }
    }

    protected void setUnlockComponentsSeed(long unlockComponentsSeed) {
        this.unlockComponentsSeed = unlockComponentsSeed;
        if (getWorld() != null && !getWorld().isRemote) {
            updateDisplayUnlockComponents();
            writeCustomData(UPDATE_CONTENTS_SEED, buf -> buf.writeVarLong(unlockComponentsSeed));
            markDirty();
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(unlockComponentTier);
        buf.writeBoolean(isSafeUnlocked);
        buf.writeFloat(doorAngle);
        buf.writeVarLong(unlockComponentsSeed);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.unlockComponentTier = buf.readVarInt();
        this.isSafeUnlocked = buf.readBoolean();
        this.doorAngle = buf.readFloat();
        this.prevDoorAngle = doorAngle;
        this.unlockComponentsSeed = buf.readVarLong();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_LOCKED_STATE) {
            this.isSafeUnlocked = buf.readBoolean();
        } else if (dataId == UPDATE_CONTENTS_SEED) {
            this.unlockComponentsSeed = buf.readVarLong();
        }
    }

    @Override
    public void initFromItemStackData(NBTTagCompound itemStack) {
        super.initFromItemStackData(itemStack);
        if (itemStack.hasKey("ComponentTier", NBT.TAG_ANY_NUMERIC)) {
            this.unlockComponentTier = itemStack.getInteger("ComponentTier");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data = super.writeToNBT(data);
        data.setInteger("UnlockProgress", unlockProgress);
        data.setInteger("ComponentTier", unlockComponentTier);
        data.setBoolean("Unlocked", isSafeUnlocked);

        data.setLong("UnlockComponentsSeed", unlockComponentsSeed);
        data.setTag("UnlockInventory", unlockInventory.serializeNBT());
        data.setTag("LootInventory", safeLootInventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.unlockProgress = data.getInteger("UnlockProgress");
        this.unlockComponentTier = data.getInteger("ComponentTier");
        this.isSafeUnlocked = data.getBoolean("Unlocked");

        this.unlockComponentsSeed = data.getLong("UnlockComponentsSeed");
        this.unlockInventory.deserializeNBT(data.getCompoundTag("UnlockInventory"));
        this.safeLootInventory.deserializeNBT(data.getCompoundTag("LootInventory"));
        updateDisplayUnlockComponents();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {}

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        ColourMultiplier colourMultiplier = new ColourMultiplier(
                GTUtility.convertRGBtoOpaqueRGBA_CL(GTValues.VC[unlockComponentTier]));
        float angle = prevDoorAngle + (doorAngle - prevDoorAngle) * partialTicks;
        angle = 1.0f - (1.0f - angle) * (1.0f - angle) * (1.0f - angle);
        float resultDoorAngle = angle * 120.0f;
        Textures.SAFE.render(renderState, translation, new IVertexOperation[] { colourMultiplier }, getFrontFacing(),
                resultDoorAngle);
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(Textures.SAFE.getParticleTexture(), GTValues.VC[unlockComponentTier]);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getLightOpacity() {
        return 1;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 1, 2));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        DoubleSupplier supplier = () -> 0.2 + (unlockProgress / (MAX_UNLOCK_PROGRESS * 1.0)) * 0.8;
        ModularUI.Builder builder = ModularUI.defaultBuilder()
                .widget(new ProgressWidget(supplier, 5, 5, 166, 74,
                        GuiTextures.PROGRESS_BAR_UNLOCK,
                        MoveType.VERTICAL_INVERTED))
                .bindPlayerInventory(entityPlayer.inventory);

        ServerWidgetGroup lockedGroup = new ServerWidgetGroup(() -> !isSafeUnlocked && unlockProgress < 0);
        lockedGroup.addWidget(new LabelWidget(5, 20, "gregtech.machine.locked_safe.malfunctioning"));
        lockedGroup.addWidget(new LabelWidget(5, 30, "gregtech.machine.locked_safe.requirements"));

        lockedGroup.addWidget(
                new SlotWidget(unlockInventory, 0, 70, 40, false, true).setBackgroundTexture(GuiTextures.SLOT));
        lockedGroup.addWidget(
                new SlotWidget(unlockInventory, 1, 70 + 18, 40, false, true).setBackgroundTexture(GuiTextures.SLOT));

        lockedGroup.addWidget(new SlotWidget(unlockComponents, 0, 70, 58, false, false));
        lockedGroup.addWidget(new SlotWidget(unlockComponents, 1, 70 + 18, 58, false, false));

        ServerWidgetGroup unlockedGroup = new ServerWidgetGroup(() -> isSafeUnlocked);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                unlockedGroup.addWidget(new SlotWidget(safeLootInventory, col + row * 9,
                        8 + col * 18, 15 + row * 18, true, false));
            }
        }

        return builder.widget(unlockedGroup)
                .widget(lockedGroup)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public boolean canPlaceCoverOnSide(@NotNull EnumFacing side) {
        return false;
    }

    @Override
    public boolean canRenderMachineGrid(@NotNull ItemStack mainHandStack, @NotNull ItemStack offHandStack) {
        return false;
    }

    @Override
    public boolean showToolUsages() {
        return false;
    }
}
