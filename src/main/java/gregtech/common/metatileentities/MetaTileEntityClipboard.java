package gregtech.common.metatileentities;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.itemhandlers.InaccessibleItemStackHandler;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.MetaTileEntityUIFactory;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.models.ModelCache;
import gregtech.common.items.behaviors.ClipboardBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static gregtech.api.render.Textures.CLIPBOARD_RENDERER;
import static gregtech.common.items.MetaItems.CLIPBOARD;

public class MetaTileEntityClipboard extends MetaTileEntity implements IFastRenderMetaTileEntity {
    private static final AxisAlignedBB CLIPBOARD_AABB = new AxisAlignedBB(2.75 / 16.0, 0.0, 0.0, 13.25 / 16.0, 1.0, 0.4 / 16.0);
    public static final ResourceLocation MODEL_RESOURCE_LOCATION = new ResourceLocation("gregtech", "block/clipboard");
    public static ModelCache cache = new ModelCache();


    public MetaTileEntityClipboard(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void getSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
    }


    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        this.renderMetaTileEntityFast(renderState, translation, 0);
    }

    @Override
    public int getLightOpacity() {
        return 0;
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        CLIPBOARD_RENDERER.render(renderState, translation, new IVertexOperation[]{}, getFrontFacing());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(-1, 0, -1), getPos().add(2, 2, 2));
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
    protected ModularUI createUI(EntityPlayer entityPlayer) {
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
}
