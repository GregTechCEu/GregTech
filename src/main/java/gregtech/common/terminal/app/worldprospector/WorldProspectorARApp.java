package gregtech.common.terminal.app.worldprospector;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.resources.ColorRectTexture;
import gregtech.api.gui.resources.ItemStackTexture;
import gregtech.api.gui.resources.ShaderTexture;
import gregtech.api.gui.widgets.ImageWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.PhantomSlotWidget;
import gregtech.client.utils.DepthTextureUtil;
import gregtech.client.shader.Shaders;
import gregtech.api.terminal.app.ARApplication;
import gregtech.api.terminal.app.AbstractApplication;
import gregtech.api.terminal.gui.widgets.CircleButtonWidget;
import gregtech.api.terminal.gui.widgets.RectButtonWidget;
import gregtech.api.terminal.os.TerminalDialogWidget;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.common.inventory.handlers.SingleItemStackHandler;
import gregtech.common.items.MetaItems;
import gregtech.common.terminal.app.worldprospector.matcher.BlockStateMatcher;
import gregtech.common.terminal.app.worldprospector.matcher.IMatcher;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class WorldProspectorARApp extends ARApplication {

    private SingleItemStackHandler[] handlers;
    private int[] colors;

    public WorldProspectorARApp() {
        super("world_prospector");
    }

    @Override
    public AbstractApplication initApp() {
        addWidget(new ImageWidget(10, 10, 313, 212, new ColorRectTexture(TerminalTheme.COLOR_B_2.getColor())));
        addWidget(new LabelWidget(15 + 150 / 2, 232 / 2, "terminal.world_prospector.radius", -1, new Object[]{getMaxRadius()})
                .setShadow(true)
                .setYCentered(true)
                .setXCentered(true));
        int slotSize = (int) Math.pow(2, getAppTier());
        int x = 250 - slotSize * 12;
        int y = 232 / 2 - 18;
        handlers = new SingleItemStackHandler[slotSize];
        colors = new int[slotSize];
        for (int i = 0; i < slotSize; i++) {
            int index = i;
            Tuple<ItemStack, Integer> stack = getSlotStack(i);
            if (stack == null) {
                handlers[i] = new SingleItemStackHandler(ItemStack.EMPTY);
                colors[i] = 0;
            } else {
                handlers[i] = new SingleItemStackHandler(stack.getFirst());
                colors[i] = stack.getSecond();
            }
            RectButtonWidget buttonWidget = new RectButtonWidget(x + i * 24, y + 18, 18, 18, 1);
            addWidget(new PhantomSlotWidget(handlers[i], 0, x + i * 24, y) {
                @Override
                public boolean mouseClicked(int mouseX, int mouseY, int button) {
                    if (handlers[index].getStackInSlot(0).isEmpty() && isMouseOverElement(mouseX, mouseY)) {
                        writeClientAction(-1, buffer -> {});
                        selectReference(index, buttonWidget);
                        return true;
                    }
                    return super.mouseClicked(mouseX, mouseY, button);
                }

                @Override
                public void handleClientAction(int id, PacketBuffer buffer) {
                    if (id == -1) {
                        selectReference(index, buttonWidget);
                    } else {
                        super.handleClientAction(id, buffer);
                    }
                }
            }.setBackgroundTexture(new ColorRectTexture(0x4fffffff)));
            addWidget(buttonWidget
                    .setHoverText("terminal.world_prospector.color")
                    .setColors(0x4fffffff, -1, colors[i])
                    .setClickListener(cd -> TerminalDialogWidget.showColorDialog(getOs(), "terminal.world_prospector.color", res->{
                        if (res != null) {
                            buttonWidget.setFill(res | 0xff000000);
                            colors[index] = res | 0xff000000;
                        }
                    }, colors[index]).open())
            );
        }
        addWidget(new CircleButtonWidget(333 / 2, 200)
                .setClickListener(cd->openAR())
                .setHoverText("terminal.ar.open")
                .setColors(0, -1, TerminalTheme.COLOR_B_3.getColor())
                .setIcon(new ItemStackTexture(MetaItems.CAMERA.getStackForm())));
        return this;
    }

    @Override
    public NBTTagCompound closeApp() {
        NBTTagCompound slots = new NBTTagCompound();
        nbt.removeTag("slots");
        for (int i = 0; i < handlers.length; i++) {
            if (!handlers[i].getStackInSlot(0).isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setTag("item", handlers[i].getStackInSlot(0).serializeNBT());
                itemTag.setInteger("color", colors[i]);
                slots.setTag("s" + i, itemTag);
            }
        }
        nbt.setTag("slots", slots);
        return nbt;
    }

    @Override
    public int getMaxTier() {
        return 2;
    }

    @Override
    protected void hookDrawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        super.hookDrawInBackground(mouseX, mouseY, partialTicks, context);
        float time = (gui.entityPlayer.ticksExisted + partialTicks) / 20f;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        if (Shaders.allowedShader()) {
            ShaderTexture.createShader("lightring.frag").draw(getPosition().x + 15, getPosition().y + (232 - 150) / 2f, 150, 150, uniformCache -> uniformCache.glUniform1F("u_time", time));
        }
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void selectReference(int index, RectButtonWidget buttonWidget) {
        TerminalDialogWidget.showItemSelector(getOs(), "terminal.world_prospector.reference", false,
            stack-> stack.getItem() instanceof ItemBlock,
            stack -> {
                if (stack.getItem() instanceof ItemBlock) {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    handlers[index].setStackInSlot(0, copy);
                    Block block = ((ItemBlock)copy.getItem()).getBlock();

                    if (block instanceof BlockFalling) {
                        colors[index] = ((BlockFalling) block).getDustColor(block.getStateFromMeta(copy.getMetadata()));
                    } else {
                        colors[index] = block.getStateFromMeta(copy.getMetadata()).getMaterial().getMaterialMapColor().colorValue;
                    }
                    if (colors[index] == 0) {
                        colors[index] = block.hashCode();
                    }
                    colors[index] = colors[index] | 0xff000000;
                    buttonWidget.setFill(colors[index]);
                }

            }).open();
    }

    private int getMaxRadius() {
        return (int) (15 * Math.pow(2, getAppTier()));
    }

    private Tuple<ItemStack, Integer> getSlotStack(int i) {
        if (nbt != null) {
            NBTTagCompound slots = nbt.getCompoundTag("slots");
            if (slots.hasKey("s" + i)) {
                NBTTagCompound itemTag = slots.getCompoundTag("s" + i);
                return new Tuple<>(new ItemStack(itemTag.getCompoundTag("item")), itemTag.getInteger("color"));
            }
        }
        return null;
    }

    private List<Tuple<ItemStack, Integer>> getAllSlotStack() {
        List<Tuple<ItemStack, Integer>> stacks = new ArrayList<>();
        if (nbt != null) {
            NBTTagCompound slots = nbt.getCompoundTag("slots");
            for (String key : slots.getKeySet()) {
                NBTTagCompound itemTag = slots.getCompoundTag(key);
                stacks.add(new Tuple<>(new ItemStack(itemTag.getCompoundTag("item")), itemTag.getInteger("color")));
            }
        }
        return stacks;
    }

    //////////////////////////////////////AR/////////////////////////////////////////

    @SideOnly(Side.CLIENT)
    private static Set<IMatcher> matchers;
    @SideOnly(Side.CLIENT)
    private static Map<IMatcher, Map<AxisAlignedBB, Set<BlockPos>>> founds;
    @SideOnly(Side.CLIENT)
    private static BlockPos lastPos;
    @SideOnly(Side.CLIENT)
    private static int radius;
    @SideOnly(Side.CLIENT)
    private static int maxRadius;

    @SideOnly(Side.CLIENT)
    @Override
    public void onAROpened() {
        founds = new HashMap<>();
        radius = 0;
        maxRadius = getMaxRadius();
        lastPos = null;
        matchers = new HashSet<>();
        for (Tuple<ItemStack, Integer> stack : getAllSlotStack()) {
            if (stack.getFirst().getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) stack.getFirst().getItem()).getBlock();
                if (block != Blocks.AIR) {
                    matchers.add(new BlockStateMatcher(block.getStateFromMeta(stack.getFirst().getMetadata()), stack.getSecond()));
                }
            }
        }
        matchers.forEach(matcher->founds.put(matcher, new HashMap<>()));
    }

    @SideOnly(Side.CLIENT)
    private List<BlockPos> bresenhamCircle(int xc , int zc , int r) {
        List<BlockPos> blockPos = new ArrayList<>();
        int x, z, d;
        x = 0;
        z = r;
        d = 3 - 2 * r;
        circlePlot(blockPos, xc, zc, x, z);
        while(x < z) {
            if(d < 0) {
                d = d + 4 * x + 6;
            } else {
                d = d + 4 * ( x - z ) + 10;
                z--;
            }
            x++;
            circlePlot(blockPos, xc, zc, x, z);
        }
        return  blockPos;
    }

    @SideOnly(Side.CLIENT)
    private void circlePlot(List<BlockPos> blockPos, int xc, int zc, int x, int z) {
        blockPos.add(new BlockPos(xc + x, 0, zc + z));
        blockPos.add(new BlockPos(xc - x, 0, zc + z));
        blockPos.add(new BlockPos(xc + x, 0, zc - z));
        blockPos.add(new BlockPos(xc - x, 0, zc - z));
        blockPos.add(new BlockPos(xc + z, 0, zc + x));
        blockPos.add(new BlockPos(xc - z, 0, zc + x));
        blockPos.add(new BlockPos(xc + z, 0, zc - x));
        blockPos.add(new BlockPos(xc - z, 0, zc - x));
    }

    @SideOnly(Side.CLIENT)
    private void addCluster(BlockPos pos, Map<AxisAlignedBB, Set<BlockPos>> found) {
        final BlockPos min = pos.add(-1, -1, -1);
        final BlockPos max = pos.add(1, 1, 1);

        AxisAlignedBB root = null;
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    Vec3d clusterPos = new Vec3d(x + 0.5, y + 0.5, z + 0.5);
                    AxisAlignedBB find = null;
                    for (AxisAlignedBB bb : found.keySet()) {
                        if (bb != root && bb.contains(clusterPos)) {
                            find = bb;
                            break;
                        }
                    }
                    if (find != null) {
                        AxisAlignedBB union;
                        Set<BlockPos> blocks;
                        if (root == null) {
                            union = new AxisAlignedBB(pos).union(find);
                            blocks = found.get(find);
                            found.remove(find);
                        } else {
                            union = root.union(find);
                            blocks = new HashSet<>();
                            blocks.addAll(found.get(find));
                            blocks.addAll(found.get(root));
                            found.remove(find);
                            found.remove(root);
                        }
                        found.put(union, blocks);
                        root = union;
                        found.get(root).add(pos);
                    }
                }
            }
        }
        if (root == null) {
            Set<BlockPos> blocks = new HashSet<>();
            blocks.add(pos);
            found.put(new AxisAlignedBB(pos), blocks);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void tickAR(EntityPlayer player) {
        World world = player.world;
        if (radius == 0 || lastPos == null) {
            lastPos = player.getPosition();
        }

        int maxY = Math.min(256, maxRadius + player.getPosition().getY());
        int minY = Math.max(0, -maxRadius + player.getPosition().getY());
        for (BlockPos pos : bresenhamCircle(lastPos.getX(), lastPos.getZ(), radius)) {
            for (int y = minY; y <= maxY; y++) {
                for (IMatcher matcher : matchers) {
                    BlockPos blockPos =new BlockPos(pos.getX(), y, pos.getZ());
                    if (matcher.match(world.getBlockState(blockPos))) {
                        addCluster(blockPos, founds.get(matcher));
                    }
                }

            }
        }

        if (radius == maxRadius) {
            radius = 0;
            for (IMatcher matcher : matchers) {
                Iterator<Map.Entry<AxisAlignedBB, Set<BlockPos>>> it = founds.get(matcher).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<AxisAlignedBB, Set<BlockPos>> entry = it.next();
                    entry.getValue().removeIf(pos -> !matcher.match(world.getBlockState(pos)));
                    if (entry.getValue().isEmpty()) {
                        it.remove();
                    }
                }
            }
        } else {
            radius++;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawARScreen(RenderWorldLastEvent event) {
        renderScan(event.getPartialTicks());
        renderAxisAlignedBB(event.getPartialTicks());
    }

    @SideOnly(Side.CLIENT)
    private void renderAxisAlignedBB(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return;
        final double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        final double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        final double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-posX, -posY, -posZ);

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (IMatcher matcher : matchers) {
            int color = matcher.getColor();
            final float r = ((color >> 16) & 0xFF) / 255f;
            final float g = ((color >> 8) & 0xFF) / 255f;
            final float b = (color & 0xFF) / 255f;
            final float a = 1;
            for (AxisAlignedBB bound : founds.get(matcher).keySet()) {
                RenderBufferHelper.renderCubeFace(buffer, bound.minX, bound.minY, bound.minZ, bound.maxX, bound.maxY, bound.maxZ, r, g, b, a);
            }
        }

        tessellator.draw();

        GlStateManager.popMatrix();

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
    }

    @SideOnly(Side.CLIENT)
    private void renderScan(float getPartialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        Entity viewer = mc.getRenderViewEntity();
        if (world != null && viewer != null && !Shaders.isOptiFineShaderPackLoaded()) {

            Framebuffer fbo = mc.getFramebuffer();

            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            DepthTextureUtil.bindDepthTexture();

            float time = (viewer.ticksExisted + getPartialTicks) / 20;

            Shaders.renderFullImageInFBO(fbo, Shaders.SCANNING, uniformCache -> {
                uniformCache.glUniform1F("u_time", time);
                uniformCache.glUniform1F("radius", radius + getPartialTicks);
                uniformCache.glUniform1F("u_zFar", mc.gameSettings.renderDistanceChunks * 16 * MathHelper.SQRT_2);
                uniformCache.glUniform1F("u_FOV", mc.gameSettings.fovSetting);
            });

            DepthTextureUtil.unBindDepthTexture();

            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);

        }
    }
}
