package gregtech.common.metatileentities.storage;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.ModularUI.Builder;
import gregtech.api.gui.widgets.SortingButtonWidget;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.recipes.ModHandler;
import gregtech.api.render.Textures;
import gregtech.api.unification.material.type.SolidMaterial;
import gregtech.api.util.GTUtility;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static gregtech.api.util.GTUtility.convertOpaqueRGBA_CLtoRGB;

public class MetaTileEntityChest extends MetaTileEntity implements IFastRenderMetaTileEntity {

    private static final IndexedCuboid6 CHEST_COLLISION = new IndexedCuboid6(null, new Cuboid6(1 / 16.0, 1 / 16.0, 1 / 16.0, 15 / 16.0, 14 / 16.0, 15 / 16.0));

    private final SolidMaterial material;
    private final int rowSize;
    private final int amountOfRows;
    private ItemStackHandler inventory;

    private float lidAngle;
    private float prevLidAngle;
    private int numPlayersUsing;

    public MetaTileEntityChest(ResourceLocation metaTileEntityId, SolidMaterial material, int rowSize, int amountOfRows) {
        super(metaTileEntityId);
        this.material = material;
        this.rowSize = rowSize;
        this.amountOfRows = amountOfRows;
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityChest(metaTileEntityId, material, rowSize, amountOfRows);
    }

    @Override
    public void update() {
        super.update();
        BlockPos blockPos = getPos();
        this.prevLidAngle = this.lidAngle;

        if (!getWorld().isRemote && this.numPlayersUsing != 0 && getOffsetTimer() % 200 == 0) {
            int lastPlayersUsing = numPlayersUsing;
            this.numPlayersUsing = GTUtility.findPlayersUsing(this, 5.0).size();
            if (lastPlayersUsing != numPlayersUsing) {
                updateNumPlayersUsing();
            }
        }

        if (this.numPlayersUsing > 0 && this.lidAngle == 0.0F) {
            double soundX = blockPos.getX() + 0.5;
            double soundZ = blockPos.getZ() + 0.5;
            double soundY = blockPos.getY() + 0.5;
            getWorld().playSound(null, soundX, soundY, soundZ, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, getWorld().rand.nextFloat() * 0.1F + 0.9F);
        }

        if ((numPlayersUsing == 0 && this.lidAngle > 0.0F) || (this.numPlayersUsing > 0 && this.lidAngle < 1.0F)) {
            float previousValue = this.lidAngle;

            if (this.numPlayersUsing > 0) {
                this.lidAngle = Math.min(this.lidAngle + 0.1F, 1.0F);
            } else {
                this.lidAngle = Math.max(this.lidAngle - 0.1F, 0.0F);
            }
            if (this.lidAngle < 0.5F && previousValue >= 0.5F) {
                double soundX = blockPos.getX() + 0.5;
                double soundZ = blockPos.getZ() + 0.5;
                double soundY = blockPos.getY() + 0.5;
                getWorld().playSound(null, soundX, soundY, soundZ, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, getWorld().rand.nextFloat() * 0.1F + 0.9F);
            }
        }
    }

    private void onContainerOpen(EntityPlayer player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }
            ++this.numPlayersUsing;
            updateNumPlayersUsing();
        }
    }

    private void onContainerClose(EntityPlayer player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
            updateNumPlayersUsing();
        }
    }

    private void updateNumPlayersUsing() {
        writeCustomData(100, buffer -> buffer.writeVarInt(numPlayersUsing));
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeVarInt(numPlayersUsing);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.numPlayersUsing = buf.readVarInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == 100) {
            this.numPlayersUsing = buf.readVarInt();
        }
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public String getHarvestTool() {
        return ModHandler.isMaterialWood(material) ? "axe" : "pickaxe";
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(CHEST_COLLISION);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.inventory = new ItemStackHandler(rowSize * amountOfRows);
        this.itemInventory = inventory;
    }

    @Override
    public int getActualComparatorValue() {
        return ItemHandlerHelper.calcRedstoneFromInventory(inventory);
    }

    @Override
    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, inventory);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        if(ModHandler.isMaterialWood(material)) {
            return Pair.of(Textures.WOODEN_CHEST.getParticleTexture(), getPaintingColor());
        } else {
            int color = ColourRGBA.multiply(
                GTUtility.convertRGBtoOpaqueRGBA_CL(material.materialRGB),
                GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColor())
            );
            color = convertOpaqueRGBA_CLtoRGB(color);
            return Pair.of(Textures.METAL_CHEST.getParticleTexture(), color);
        }
    }

    @Override
    public double getCoverPlateThickness() {
        return 1.0 / 16.0; //1/16th of the block size
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        float angle = prevLidAngle + (lidAngle - prevLidAngle) * partialTicks;
        angle = 1.0f - (1.0f - angle) * (1.0f - angle) * (1.0f - angle);
        float resultLidAngle = angle * 90.0f;
        if (ModHandler.isMaterialWood(material)) {
            ColourMultiplier multiplier = new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()));
            Textures.WOODEN_CHEST.render(renderState, translation, new IVertexOperation[]{multiplier}, getFrontFacing(), resultLidAngle);
        } else {
            ColourMultiplier multiplier = new ColourMultiplier(ColourRGBA.multiply(
                GTUtility.convertRGBtoOpaqueRGBA_CL(material.materialRGB),
                GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
            Textures.METAL_CHEST.render(renderState, translation, new IVertexOperation[]{multiplier}, getFrontFacing(), resultLidAngle);
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 2, 2));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        Builder builder = ModularUI.builder(GuiTextures.BACKGROUND,
            Math.max(176, 14 + rowSize * 18),
            18 + 18 * amountOfRows + 94)
            .label(5, 5, getMetaFullName());
        builder.widget(new SortingButtonWidget(111, 4, 60, 10, "gregtech.gui.sort",
            (info) -> sortInventorySlotContents(inventory)));

        for (int y = 0; y < amountOfRows; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.slot(inventory, index, 7 + x * 18, 18 + y * 18, GuiTextures.SLOT);
            }
        }
        int startX = (Math.max(176, 14 + rowSize * 18) - 162) / 2;
        builder.bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, startX, 18 + 18 * amountOfRows + 12);
        if (!getWorld().isRemote) {
            builder.bindOpenListener(() -> onContainerOpen(entityPlayer));
            builder.bindCloseListener(() -> onContainerClose(entityPlayer));
        }
        return builder.build(getHolder(), entityPlayer);
    }

    private static void sortInventorySlotContents(IItemHandlerModifiable inventory) {
        //stack item stacks with equal items and compounds
        for (int i = 0; i < inventory.getSlots(); i++) {
            for (int j = i + 1; j < inventory.getSlots(); j++) {
                ItemStack stack1 = inventory.getStackInSlot(i);
                ItemStack stack2 = inventory.getStackInSlot(j);
                if (!stack1.isEmpty() && ItemStack.areItemsEqual(stack1, stack2) &&
                    ItemStack.areItemStackTagsEqual(stack1, stack2)) {
                    int maxStackSize = Math.min(stack1.getMaxStackSize(), inventory.getSlotLimit(i));
                    int itemsCanAccept = Math.min(stack2.getCount(), maxStackSize - Math.min(stack1.getCount(), maxStackSize));
                    if (itemsCanAccept > 0) {
                        stack1.grow(itemsCanAccept);
                        stack2.shrink(itemsCanAccept);
                    }
                }
            }
        }
        //create itemstack pairs and sort them out by attributes
        ArrayList<ItemStack> inventoryContents = new ArrayList<>();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!itemStack.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                inventoryContents.add(itemStack);
            }
        }
        inventoryContents.sort(GTUtility.createItemStackComparator());
        for (int i = 0; i < inventoryContents.size(); i++) {
            inventory.setStackInSlot(i, inventoryContents.get(i));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("Inventory", inventory.serializeNBT());
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.inventory.deserializeNBT(data.getCompoundTag("Inventory"));
    }

    @Override
    protected boolean shouldSerializeInventories() {
        return false; //handled manually
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.universal.tooltip.item_storage_capacity", rowSize * amountOfRows));
    }
}
