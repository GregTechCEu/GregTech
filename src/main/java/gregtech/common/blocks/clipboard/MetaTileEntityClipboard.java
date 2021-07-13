package gregtech.common.blocks.clipboard;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandler;

public class MetaTileEntityClipboard extends MetaTileEntity {
    public int button0state = 0;

    public int button1state = 0;

    public int button2state = 0;

    public int button3state = 0;

    public int button4state = 0;

    public int button5state = 0;

    public int button6state = 0;

    public int button7state = 0;

    public String button0text = " ";

    public String button1text = " ";

    public String button2text = " ";

    public String button3text = " ";

    public String button4text = " ";

    public String button5text = " ";

    public String button6text = " ";

    public String button7text = " ";

    public String titletext = " ";

    public int currentPage = 1;

    public int totalPages = 1;

    public MetaTileEntityClipboard(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    public void updateClipboardFromPlayerSelection(int selection) {
        if (selection >= 0 && selection <= 8) {
            checkCheckBox(selection);
        } else if (selection == 10) {
            changePage(false);
        } else if (selection == 11) {
            changePage(true);
        }
        getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
    }

    public void checkCheckBox(int box) {
        switch (box) {
            case 0:
                if (this.button0state >= 2) {
                    this.button0state = 0;
                    break;
                }
                this.button0state++;
                break;
            case 1:
                if (this.button1state >= 2) {
                    this.button1state = 0;
                    break;
                }
                this.button1state++;
                break;
            case 2:
                if (this.button2state >= 2) {
                    this.button2state = 0;
                    break;
                }
                this.button2state++;
                break;
            case 3:
                if (this.button3state >= 2) {
                    this.button3state = 0;
                    break;
                }
                this.button3state++;
                break;
            case 4:
                if (this.button4state >= 2) {
                    this.button4state = 0;
                    break;
                }
                this.button4state++;
                break;
            case 5:
                if (this.button5state >= 2) {
                    this.button5state = 0;
                    break;
                }
                this.button5state++;
                break;
            case 6:
                if (this.button6state >= 2) {
                    this.button6state = 0;
                    break;
                }
                this.button6state++;
                break;
            case 7:
                if (this.button7state >= 2) {
                    this.button7state = 0;
                    break;
                }
                this.button7state++;
                break;
        }
        ItemStack clipStack = itemInventory.getStackInSlot(0);
        if (clipStack != ItemStack.EMPTY) {
            NBTTagCompound cliptags = clipStack.getTagCompound();
            if (cliptags != null) {
                String pagenum = "page" + cliptags.getInteger("currentPage");
                NBTTagCompound pagetag = cliptags.getCompoundTag(pagenum);
                if (pagetag != null) {
                    int[] taskstat = pagetag.getIntArray("taskStates");
                    taskstat[0] = this.button0state;
                    taskstat[1] = this.button1state;
                    taskstat[2] = this.button2state;
                    taskstat[3] = this.button3state;
                    taskstat[4] = this.button4state;
                    taskstat[5] = this.button5state;
                    taskstat[6] = this.button6state;
                    taskstat[7] = this.button7state;
                    pagetag.setIntArray("taskStates", taskstat);
                    clipStack.setTagCompound(cliptags);
                    itemInventory.extractItem(0, 1, false);
                    itemInventory.insertItem(0, clipStack, false);
                    getNBTData();
                    getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
                }
            }
        }
    }


    public void changePage(boolean nextPage) {
        if (nextPage) {
            if (this.currentPage < this.totalPages)
                this.currentPage++;
        } else if (this.currentPage > 1) {
            this.currentPage--;
        }
        ItemStack clipStack = itemInventory.getStackInSlot(0);
        if (clipStack != ItemStack.EMPTY) {
            NBTTagCompound cliptags = clipStack.getTagCompound();
            if (cliptags != null) {
                String pagenum = "page" + this.currentPage;
                NBTTagCompound pagetag = cliptags.getCompoundTag(pagenum);
                if (pagetag != null) {
                    cliptags.setInteger("currentPage", this.currentPage);
                    clipStack.setTagCompound(cliptags);
                    itemInventory.extractItem(0, 1, false);
                    itemInventory.insertItem(0, clipStack, false);
                    getNBTData();
                    getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
                }
            }
        }
    }

    public void getNBTData() {
        ItemStack clipStack = itemInventory.getStackInSlot(0);
        if (clipStack != ItemStack.EMPTY) {
            NBTTagCompound cliptags = clipStack.getTagCompound();
            if (cliptags != null) {
                this.currentPage = cliptags.getInteger("currentPage");
                this.totalPages = cliptags.getInteger("totalPages");
                String pagenum = "page" + this.currentPage;
                NBTTagCompound pagetag = cliptags.getCompoundTag(pagenum);
                if (pagetag != null) {
                    int[] taskstat = pagetag.getIntArray("taskStates");
                    if (taskstat.length > 0) {
                        this.button0state = taskstat[0];
                        this.button1state = taskstat[1];
                        this.button2state = taskstat[2];
                        this.button3state = taskstat[3];
                        this.button4state = taskstat[4];
                        this.button5state = taskstat[5];
                        this.button6state = taskstat[6];
                        this.button7state = taskstat[7];
                    }
                    NBTTagCompound tasks = pagetag.getCompoundTag("tasks");
                    this.button0text = tasks.getString("task1");
                    this.button1text = tasks.getString("task2");
                    this.button2text = tasks.getString("task3");
                    this.button3text = tasks.getString("task4");
                    this.button4text = tasks.getString("task5");
                    this.button5text = tasks.getString("task6");
                    this.button6text = tasks.getString("task7");
                    this.button7text = tasks.getString("task8");
                    this.titletext = pagetag.getString("title");
                }
            }
        }
    }

    public int getInventoryStackLimit() {
        return 1;
    }

    public boolean isItemValidForSlot(int slot, ItemStack itemstack) {
        return false;
    }

    public String getName() {
        return "Clipboard";
    }

    public void setInventorySlotContentsAdditionalCommands(int slot, ItemStack stack) {}

    public void loadCustomNBTData(NBTTagCompound nbt) {
        this.button0state = nbt.getInteger("button0state");
        this.button1state = nbt.getInteger("button1state");
        this.button2state = nbt.getInteger("button2state");
        this.button3state = nbt.getInteger("button3state");
        this.button4state = nbt.getInteger("button4state");
        this.button5state = nbt.getInteger("button5state");
        this.button6state = nbt.getInteger("button6state");
        this.button7state = nbt.getInteger("button7state");
        this.button0text = nbt.getString("button0text");
        this.button1text = nbt.getString("button1text");
        this.button2text = nbt.getString("button2text");
        this.button3text = nbt.getString("button3text");
        this.button4text = nbt.getString("button4text");
        this.button5text = nbt.getString("button5text");
        this.button6text = nbt.getString("button6text");
        this.button7text = nbt.getString("button7text");
        this.currentPage = nbt.getInteger("currentPage");
        this.totalPages = nbt.getInteger("totalPages");
        this.titletext = nbt.getString("titletext");
    }

    public NBTTagCompound writeCustomNBTData(NBTTagCompound nbt) {
        nbt.setInteger("button0state", this.button0state);
        nbt.setInteger("button1state", this.button1state);
        nbt.setInteger("button2state", this.button2state);
        nbt.setInteger("button3state", this.button3state);
        nbt.setInteger("button4state", this.button4state);
        nbt.setInteger("button5state", this.button5state);
        nbt.setInteger("button6state", this.button6state);
        nbt.setInteger("button7state", this.button7state);
        nbt.setString("button0text", this.button0text);
        nbt.setString("button1text", this.button1text);
        nbt.setString("button2text", this.button2text);
        nbt.setString("button3text", this.button3text);
        nbt.setString("button4text", this.button4text);
        nbt.setString("button5text", this.button5text);
        nbt.setString("button6text", this.button6text);
        nbt.setString("button7text", this.button7text);
        nbt.setInteger("currentPage", this.currentPage);
        nbt.setInteger("totalPages", this.totalPages);
        nbt.setString("titletext", this.titletext);
        return nbt;
    }

    public ITextComponent getDisplayName() {
        return (ITextComponent)new TextComponentString(getName());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return null;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }
}
