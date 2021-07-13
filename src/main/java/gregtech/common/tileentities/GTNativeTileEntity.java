package gregtech.common.tileentities;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public abstract class GTNativeTileEntity extends TileEntity implements IInventory, IItemHandler {
    private EnumFacing angle = EnumFacing.NORTH;

    private EnumShiftPosition shift = EnumShiftPosition.NO_SHIFT;

    private EnumVertPosition vertPosition = EnumVertPosition.WALL;

    public NonNullList<ItemStack> inventory;

    private String customTexture = "none";

    private boolean isRetexturable;

    private boolean isLocked = false;

    private String lockee = "";

    private int renderBoxAdditionalSize = 1;

    public GTNativeTileEntity(int inventorySize, boolean canRetexture) {
        this.inventory = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
        this.isRetexturable = canRetexture;
    }

    public boolean addStackToInventoryFromWorld(ItemStack stack, int slot, EntityPlayer player) {
        if (slot == -1)
            return false;
        boolean returnValue = false;
        ItemStack currentStack = getStackInSlot(slot);
        if (stack != ItemStack.EMPTY && currentStack == ItemStack.EMPTY) {
            setInventorySlotContents(slot, stack);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            returnValue = true;
        } else if (stack != ItemStack.EMPTY && currentStack != ItemStack.EMPTY) {
            ItemStack leftStack = stack.copy();
            ItemStack rightStack = currentStack.copy();
            leftStack.setCount(1);
            rightStack.setCount(1);
            if (getIsItemStacksEqual(leftStack, rightStack)) {
                int total = stack.getCount() + currentStack.getCount();
                if (total > stack.getMaxStackSize() && currentStack.getCount() != currentStack.getMaxStackSize()) {
                    currentStack.setCount(stack.getMaxStackSize());
                    stack.setCount(total - stack.getMaxStackSize());
                    setInventorySlotContents(slot, currentStack);
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
                    returnValue = true;
                } else if (total <= stack.getMaxStackSize()) {
                    currentStack.setCount(total);
                    setInventorySlotContents(slot, currentStack);
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                    returnValue = true;
                }
            }
            if (returnValue)
                getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
        }
        return returnValue;
    }

    public boolean getIsItemStacksEqual(ItemStack stack1, ItemStack stack2) {
        boolean output = false;
        if (stack1 != ItemStack.EMPTY && stack2 != ItemStack.EMPTY)
            if (stack1.getItem() == stack2.getItem() && stack1.getItemDamage() == stack2.getItemDamage()) {
                NBTTagCompound tag1 = stack1.getTagCompound();
                NBTTagCompound tag2 = stack2.getTagCompound();
                if (tag1 == null && tag2 == null) {
                    output = true;
                } else {
                    output = tag1.equals(tag2);
                }
            }
        return output;
    }

    public boolean addStackToInventoryFromWorldSingleStackSize(ItemStack stack, int slot, EntityPlayer player) {
        boolean returnValue = false;
        ItemStack currentStack = getStackInSlot(slot);
        if (stack != ItemStack.EMPTY && currentStack == ItemStack.EMPTY) {
            if (stack.getCount() > 1) {
                ItemStack newStack = stack.copy();
                newStack.setCount(1);
                stack.setCount(stack.getCount() - 1);
                setInventorySlotContents(slot, newStack);
                player.inventory.setInventorySlotContents(player.inventory.currentItem, stack);
            } else {
                setInventorySlotContents(slot, stack);
                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            }
            returnValue = true;
        }
        if (returnValue)
            getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
        return returnValue;
    }

    public boolean removeStackFromInventoryFromWorld(int slot, EntityPlayer player, Block block) {
        boolean returnValue = false;
        ItemStack stack = getStackInSlot(slot);
        if (stack != ItemStack.EMPTY) {
            this.dropStackInSlot(this.world, slot, getPos());
            setInventorySlotContents(slot, ItemStack.EMPTY);
            getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
            returnValue = true;
        }
        return returnValue;
    }

    public void dropStackInSlot(World world, int slot, BlockPos extractPos) {
        ItemStack stack = this.getStackInSlot(slot);
        if (stack != ItemStack.EMPTY && stack.getCount() > 0) {
            EntityItem entityItem = new EntityItem(world, (extractPos.getX() + 0.5F), (extractPos.getY() + 0.5F), (extractPos.getZ() + 0.5F), new ItemStack(stack.getItem(), stack.getCount(), stack.getItemDamage()));
            if (stack.hasTagCompound())
                entityItem.getItem().setTagCompound(stack.getTagCompound().copy());
            entityItem.motionX = 0.0D;
            entityItem.motionY = 0.0D;
            entityItem.motionZ = 0.0D;
            world.spawnEntity(entityItem);
            stack.setCount(0);
        }
    }

    public abstract void setInventorySlotContentsAdditionalCommands(int paramInt, ItemStack paramItemStack);

    public abstract void loadCustomNBTData(NBTTagCompound paramNBTTagCompound);

    public abstract NBTTagCompound writeCustomNBTData(NBTTagCompound paramNBTTagCompound);

    public void setAngle(EnumFacing facing) {
        this.angle = facing;
        getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
    }

    public void setShiftPosition(EnumShiftPosition position) {
        this.shift = position;
        getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
    }

    public void setVertPosition(EnumVertPosition position) {
        this.vertPosition = position;
        getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
    }

    public EnumFacing getAngle() {
        return this.angle;
    }

    public EnumShiftPosition getShiftPosition() {
        return this.shift;
    }

    public EnumVertPosition getVertPosition() {
        return this.vertPosition;
    }

    public boolean canRetextureBlock() {
        return this.isRetexturable;
    }

    public String getCustomTextureString() {
        return this.customTexture;
    }

    public void setCustomTexureString(String tex) {
        this.customTexture = tex;
        getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
        this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
        getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
    }

    public String getLockee() {
        return this.lockee;
    }

    public void setLockee(String lockeeperson) {
        this.lockee = lockeeperson;
    }

    public int getSizeInventory() {
        return this.inventory.size();
    }

    public ItemStack getStackInSlot(int slot) {
        ItemStack output = ItemStack.EMPTY;
        if (slot >= 0 && slot < this.inventory.size())
            output = (ItemStack)this.inventory.get(slot);
        return output;
    }

    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.inventory.size()) {
            this.inventory.set(slot, stack);
            if (stack != ItemStack.EMPTY && stack.getCount() > getInventoryStackLimit())
                stack.setCount(getInventoryStackLimit());
            setInventorySlotContentsAdditionalCommands(slot, stack);
            getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
        }
    }

    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != ItemStack.EMPTY)
            if (stack.getCount() <= amount) {
                setInventorySlotContents(slot, ItemStack.EMPTY);
            } else {
                stack = stack.splitStack(amount);
                if (stack.getCount() == 0)
                    setInventorySlotContents(slot, ItemStack.EMPTY);
            }
        return stack;
    }

    public ItemStack removeStackFromSlot(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != ItemStack.EMPTY)
            setInventorySlotContents(slot, ItemStack.EMPTY);
        return stack;
    }

    public abstract int getInventoryStackLimit();

    public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        bb = new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), (this.pos.getX() + this.renderBoxAdditionalSize), (this.pos.getY() + this.renderBoxAdditionalSize), (this.pos.getZ() + this.renderBoxAdditionalSize));
        return bb;
    }

    public void setRenderBoxAdditionalSize(int size) {
        this.renderBoxAdditionalSize = size;
    }

    public boolean hasCustomName() {
        return false;
    }

    public void openInventory(EntityPlayer player) {}

    public void closeInventory(EntityPlayer player) {}

    public int getField(int id) {
        return 0;
    }

    public void setField(int id, int value) {}

    public int getFieldCount() {
        return 0;
    }

    public void clear() {}

    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        loadNBTData(nbt);
    }

    public void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet) {
        NBTTagCompound nbtData = packet.getNbtCompound();
        loadNBTData(nbtData);
        this.world.markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    private void loadNBTData(NBTTagCompound nbt) {
        NBTTagList tagList = nbt.getTagList("Inventory", 10);
        this.inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            byte slot = tag.getByte("Slot");
            if (slot >= 0 && slot < this.inventory.size())
                this.inventory.set(slot, new ItemStack(tag));
        }
        this.isLocked = nbt.getBoolean("locked");
        this.lockee = nbt.getString("lockee");
        this.angle = getFacingFromAngleID(nbt.getInteger("angle"));
        this.shift = EnumShiftPosition.getEnumFromID(nbt.getInteger("shift"));
        this.vertPosition = EnumVertPosition.getEnumFromID(nbt.getInteger("position"));
        loadCustomNBTData(nbt);
        if (this.isRetexturable)
            this.customTexture = nbt.getString("customTexture");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt = writeNBTData(nbt);
        return nbt;
    }

    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound dataTag = new NBTTagCompound();
        dataTag = writeNBTData(dataTag);
        return new SPacketUpdateTileEntity(this.pos, 1, dataTag);
    }

    public NBTTagCompound getUpdateTag() {
        NBTTagCompound tags = super.getUpdateTag();
        return writeNBTData(tags);
    }

    private NBTTagCompound writeNBTData(NBTTagCompound nbt) {
        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < this.inventory.size(); i++) {
            ItemStack stack = (ItemStack)this.inventory.get(i);
            if (stack != ItemStack.EMPTY) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)i);
                stack.writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        nbt.setTag("Inventory", itemList);
        nbt.setBoolean("locked", this.isLocked);
        nbt.setString("lockee", this.lockee);
        nbt.setInteger("angle", getAngleIDFromFacing(this.angle));
        nbt.setInteger("shift", this.shift.getID());
        nbt.setInteger("position", this.vertPosition.getID());
        nbt = writeCustomNBTData(nbt);
        if (this.isRetexturable)
            nbt.setString("customTexture", this.customTexture);
        return nbt;
    }

    private int getAngleIDFromFacing(EnumFacing facing) {
         switch (facing) {
            case WEST:
                return 1;
            case NORTH:
                return 2;
            case EAST:
                return 3;
            case DOWN:
                return 4;
            case UP:
                return 5;
        }
        return 0;
    }

    public int getAngleID() {
        return getAngleIDFromFacing(getAngle());
    }

    private EnumFacing getFacingFromAngleID(int angle) {
        EnumFacing face = EnumFacing.SOUTH;
        switch (angle) {
            case 1:
                face = EnumFacing.WEST;
                break;
            case 2:
                face = EnumFacing.NORTH;
                break;
            case 3:
                face = EnumFacing.EAST;
                break;
            case 4:
                face = EnumFacing.DOWN;
                break;
            case 5:
                face = EnumFacing.UP;
                break;
        }
        return face;
    }

    public void updateSurroundingBlocks(Block blocktype) {
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX() + 1, this.pos.getY(), this.pos.getZ()), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX() - 1, this.pos.getY(), this.pos.getZ() + 1), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 1), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY() + 1, this.pos.getZ()), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ()), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX() + 2, this.pos.getY(), this.pos.getZ()), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX() - 2, this.pos.getY(), this.pos.getZ()), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() + 2), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ() - 2), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY() + 2, this.pos.getZ()), blocktype, true);
        this.world.notifyNeighborsOfStateChange(new BlockPos(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ()), blocktype, true);
        getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
    }

    public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
        return true;
    }

    public boolean isEmpty() {
        boolean output = true;
        for (ItemStack itemStack : this.inventory) {
            if (itemStack != ItemStack.EMPTY) {
                output = false;
                break;
            }
        }
        return output;
    }

    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return (this.world.getTileEntity(this.pos) == this && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) < 64.0D);
    }

    public int getSlots() {
        return this.inventory.size();
    }

    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        ItemStack returnStack = stack;
        if (slot < this.inventory.size()) {
            ItemStack currentSlot = getStackInSlot(slot);
            if (currentSlot != ItemStack.EMPTY) {
                if (stack.getItem() == currentSlot.getItem() && currentSlot.getCount() < currentSlot.getMaxStackSize())
                    if (!simulate) {
                        int count = currentSlot.getCount() + stack.getCount();
                        if (count > stack.getMaxStackSize()) {
                            currentSlot.setCount(currentSlot.getMaxStackSize());
                            setInventorySlotContents(slot, currentSlot);
                            returnStack = stack.copy();
                            returnStack.setCount(count - currentSlot.getMaxStackSize());
                        } else {
                            stack.setCount(count);
                            setInventorySlotContents(slot, stack);
                            returnStack = ItemStack.EMPTY;
                        }
                    }
            } else if (!simulate) {
                setInventorySlotContents(slot, stack);
                returnStack = ItemStack.EMPTY;
            }
        }
        return returnStack;
    }

    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack result = ItemStack.EMPTY;
        if (slot < this.inventory.size()) {
            ItemStack slottedStack = getStackInSlot(slot);
            if (slottedStack != ItemStack.EMPTY && !simulate) {
                result = slottedStack.copy();
                if (amount >= slottedStack.getCount()) {
                    setInventorySlotContents(slot, ItemStack.EMPTY);
                } else {
                    result.setCount(amount);
                    slottedStack.setCount(slottedStack.getCount() - amount);
                    setInventorySlotContents(slot, slottedStack);
                }
            }
        }
        return result;
    }

    public int getSlotLimit(int slot) {
        return 64;
    }
}
