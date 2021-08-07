package gregtech.common.metatileentities;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.impl.FakeModularGui;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.itemhandlers.InaccessibleItemStackHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.*;
import gregtech.api.net.NetworkHandler;
import gregtech.api.net.PacketClipboardUIWidgetUpdate;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GregFakePlayer;
import gregtech.common.blocks.models.ModelCache;
import gregtech.common.gui.impl.FakeModularUIContainerClipboard;
import gregtech.common.items.behaviors.ClipboardBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static gregtech.api.render.Textures.CLIPBOARD_RENDERER;
import static gregtech.common.items.MetaItems.CLIPBOARD;

public class MetaTileEntityClipboard extends MetaTileEntity implements IRenderMetaTileEntity, IFastRenderMetaTileEntity {
    private static final AxisAlignedBB CLIPBOARD_AABB = new AxisAlignedBB(2.75 / 16.0, 0.0, 0.0, 13.25 / 16.0, 1.0, 0.4 / 16.0);
    public static final ResourceLocation MODEL_RESOURCE_LOCATION = new ResourceLocation("gregtech", "block/clipboard");
    public static ModelCache modelCache = new ModelCache();
    public static final float scale = 1;
    public FakeModularGui guiCache;
    public FakeModularUIContainerClipboard guiContainerCache;

    private static final int RENDER_PASS_NORMAL = 0;
    private static final NBTBase NO_CLIPBOARD_SIG = new NBTTagInt(0);


    public MetaTileEntityClipboard(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void update() {
        super.update();
        if (this.guiCache == null)
            createFakeGui();
        if (this.getWorld().isRemote) {
            if (guiCache != null)
                guiCache.updateScreen();

        } else {
            if (guiContainerCache != null) {
                guiContainerCache.detectAndSendChanges();
            }
        }
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public void renderMetaTileEntityDynamic(double x, double y, double z, float partialTicks) {
        if (this.getClipboard() != null)
            CLIPBOARD_RENDERER.renderGUI(x, y, z, this.getFrontFacing(), this, partialTicks);
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        CLIPBOARD_RENDERER.renderBoard(renderState, translation.copy(), new IVertexOperation[]{}, getFrontFacing(), this, partialTicks);
    }

    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 2, 2));
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == RENDER_PASS_NORMAL;
    }

    @Override
    public boolean isGlobalRenderer() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder holder) {
        return new MetaTileEntityClipboard(metaTileEntityId);
    }

    @Override
    public ModularUI createUI(EntityPlayer entityPlayer) {
        if (getClipboard().isItemEqual(CLIPBOARD.getStackForm())) {
            List<IItemBehaviour> behaviours = ((MetaItem<?>) getClipboard().getItem()).getBehaviours(getClipboard());
            Optional<IItemBehaviour> clipboardBehaviour = behaviours.stream().filter((x) -> x instanceof ClipboardBehaviour).findFirst();
            if (!clipboardBehaviour.isPresent())
                return null;
            if (clipboardBehaviour.get() instanceof ClipboardBehaviour) {
                return ((ClipboardBehaviour) clipboardBehaviour.get()).createUI(new PlayerInventoryHolder(entityPlayer, entityPlayer.getActiveHand(), getClipboard()), entityPlayer);
            }
        }
        return null;
    }

    public void createFakeGui() {
        // Basically just the original function from the PluginBehavior, but with a lot of now useless stuff stripped out.
        try {
            GregFakePlayer fakePlayer = new GregFakePlayer(this.getWorld());
            ModularUI ui = this.createUI(fakePlayer);

            ModularUI.Builder builder = new ModularUI.Builder(ui.backgroundPath, ui.getWidth(), ui.getHeight());

            List<Widget> widgets = new ArrayList<>(ui.guiWidgets.values());

            for (Widget widget : widgets) {
                builder.widget(widget);
            }
            ui = builder.build(ui.holder, ui.entityPlayer);
            FakeModularUIContainerClipboard fakeModularUIContainer = new FakeModularUIContainerClipboard(ui, this);
            this.guiContainerCache = fakeModularUIContainer;
            this.guiCache = new FakeModularGui(ui, fakeModularUIContainer);
            this.writeCustomData(1, buffer -> {});
        } catch (Exception e) {
            GTLog.logger.error(e);
        }
    }


    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.itemInventory = new InaccessibleItemStackHandler();
    }

    public ItemStack getClipboard() {
        if (this.itemInventory.getStackInSlot(0) == ItemStack.EMPTY) {
            ((InaccessibleItemStackHandler) this.itemInventory).setStackInSlot(0, CLIPBOARD.getStackForm());
        }
        return this.itemInventory.getStackInSlot(0);
    }

    public void setClipboard(ItemStack stack) {
        ((InaccessibleItemStackHandler) this.itemInventory).setStackInSlot(0, stack.copy());
    }

    @Override
    public void getDrops(NonNullList<ItemStack> dropsList, @Nullable EntityPlayer harvester) {
        dropsList.clear();
        dropsList.add(this.getClipboard());
    }

    @Override
    public float getBlockHardness() {
        return 10;
    }

    @Override
    public int getHarvestLevel() {
        return 10;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        if (playerIn.isSneaking()) {
            if (getWorld() != null && !getWorld().isRemote) {
                MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (EntityPlayerMP) playerIn);
            }
        } else {
            getWorld().destroyBlock(this.getPos(), true);
        }
        return true;
    }


    @Override
    public String getHarvestTool() {
        return "wrench";
    }

    @Override
    public void addCollisionBoundingBox(List<IndexedCuboid6> collisionList) {
        collisionList.add(new IndexedCuboid6(null, GTUtility.rotateAroundYAxis(CLIPBOARD_AABB, EnumFacing.NORTH, this.getFrontFacing())));
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(CLIPBOARD_RENDERER.getParticleTexture(), 0xFFFFFF);
    }

    @SideOnly(Side.CLIENT)
    public Pair<Double, Double> checkLookingAt(float partialTicks) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (this.getWorld() != null && player != null) {
            RayTraceResult rayTraceResult = player.rayTrace(Minecraft.getMinecraft().playerController.getBlockReachDistance(), partialTicks);
            if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.sideHit == this.getFrontFacing()) {
                int i = -1, j = -1;
                TileEntity tileEntity = this.getWorld().getTileEntity(rayTraceResult.getBlockPos());
                if (tileEntity instanceof MetaTileEntityHolder && ((MetaTileEntityHolder) tileEntity).getMetaTileEntity() instanceof MetaTileEntityClipboard) {
                    MetaTileEntityClipboard clipboardHit = (MetaTileEntityClipboard) ((MetaTileEntityHolder) tileEntity).getMetaTileEntity();
                    double[] pos = handleRayTraceResult(rayTraceResult, this.getFrontFacing());
                    pos[0] /= this.scale;
                    pos[1] /= this.scale;
                    if (pos[0] >= 0 && pos[0] <= 1 && pos[1] >= 0 && pos[1] <= 1)
                        return Pair.of(pos[0], pos[1]);
                }
            }
        }
        return null;
    }

    private double[] handleRayTraceResult(RayTraceResult rayTraceResult, EnumFacing spin) {
        double x = 0;
        double y = 0;
        double dX = rayTraceResult.sideHit.getAxis() == EnumFacing.Axis.X
                ? rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ()
                : rayTraceResult.hitVec.x - rayTraceResult.getBlockPos().getX();
        double dY = rayTraceResult.sideHit.getAxis() == EnumFacing.Axis.Y
                ? rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ()
                : rayTraceResult.hitVec.y - rayTraceResult.getBlockPos().getY();
        if (spin == EnumFacing.NORTH) {
            x = 1 - dX;
            y = 1 - dY;
            if (rayTraceResult.sideHit.getYOffset() < 0) {
                y = 1 - y;
            }
        } else if (spin == EnumFacing.SOUTH) {
            x = dX;
            y = dY;
            if (rayTraceResult.sideHit.getYOffset() < 0) {
                y = 1 - y;
            }
        } else if (spin == EnumFacing.EAST) {
            x = 1 - dY;
            y = dX;
            if (rayTraceResult.sideHit.getXOffset() < 0 || rayTraceResult.sideHit.getZOffset() > 0) {
                x = 1 - x;
                y = 1 - y;
            } else if (rayTraceResult.sideHit.getYOffset() < 0) {
                y = 1 - y;
            }
        } else {
            x = dY;
            y = 1 - dX;
            if (rayTraceResult.sideHit.getXOffset() < 0 || rayTraceResult.sideHit.getZOffset() > 0) {
                x = 1 - x;
                y = 1 - y;
            } else if (rayTraceResult.sideHit.getYOffset() < 0) {
                y = 1 - y;
            }
        }
        if (rayTraceResult.sideHit == EnumFacing.WEST || rayTraceResult.sideHit == EnumFacing.SOUTH) {
            x = 1 - x;
        } else if (rayTraceResult.sideHit == EnumFacing.UP) {
            x = 1 - x;
            y = 1 - y;
        }
        return new double[]{x, y};
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (this.getClipboard() != null && this.getClipboard().getTagCompound() != null)
            data.setTag("clipboardNBT", this.getClipboard().getTagCompound());
        else
            data.setTag("clipboardNBT", NO_CLIPBOARD_SIG);
        return data;
    }


    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        NBTBase clipboardNBT = data.getTag("clipboardNBT");
        if (clipboardNBT != NO_CLIPBOARD_SIG && clipboardNBT instanceof NBTTagCompound) {
            ItemStack clipboard = this.getClipboard();
            clipboard.setTagCompound((NBTTagCompound) clipboardNBT);
            this.setClipboard(clipboard);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        if (this.getClipboard() != null && this.getClipboard().getTagCompound() != null)
            buf.writeCompoundTag(this.getClipboard().getTagCompound());
        else {
            buf.writeCompoundTag(new NBTTagCompound());
        }
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        try {
            NBTTagCompound clipboardNBT = buf.readCompoundTag();
            if (clipboardNBT != new NBTTagCompound() && clipboardNBT != null) {
                ItemStack clipboard = this.getClipboard();
                clipboard.setTagCompound(clipboardNBT);
                this.setClipboard(clipboard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if(dataId == 0) {
            int windowID = buf.readVarInt();
            int widgetID = buf.readVarInt();
            if (guiCache != null)
                guiCache.handleWidgetUpdate(windowID, widgetID, buf);
        } else if (dataId == 1) {
            createFakeGui();
        }
    }



    @Override
    public void preInit(Object... data) {
        if (data.length != 0 && data[0] instanceof ItemStack)
            this.setClipboard((ItemStack) data[0]);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) { // JEI shouldn't show this
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
    }

    public void readUIAction(EntityPlayerMP player, int id, PacketBuffer buf) {
        if (id == 1) {
            if (this.guiContainerCache != null) {
                guiContainerCache.handleClientAction(buf);
            }
        }
    }
}
