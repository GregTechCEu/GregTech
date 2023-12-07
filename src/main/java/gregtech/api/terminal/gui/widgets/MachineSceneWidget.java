package gregtech.api.terminal.gui.widgets;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextTexture;
import gregtech.api.gui.widgets.WidgetGroup;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.terminal.os.TerminalTheme;
import gregtech.api.util.FacingPos;
import gregtech.client.renderer.scene.FBOWorldSceneRenderer;
import gregtech.client.renderer.scene.WorldSceneRenderer;
import gregtech.client.utils.RenderUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.vecmath.Vector3f;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: KilaBash
 * @Date: 2021/08/23/19:21
 * @Description:
 */
public class MachineSceneWidget extends WidgetGroup {

    private static FBOWorldSceneRenderer worldSceneRenderer;
    private boolean dragging;
    private int lastMouseX;
    private int lastMouseY;
    private int currentMouseX;
    private int currentMouseY;
    private Vector3f center;
    private float rotationYaw = 45;
    private float rotationPitch;
    private float zoom = 5;
    private float alpha = 1f;
    private boolean blendColor = true;
    private Set<BlockPos> cores;
    private Set<BlockPos> around;
    private FacingPos hoveredFacingPos;
    private FacingPos selectedFacingPos;
    private BiConsumer<BlockPos, EnumFacing> onSelected;

    protected MetaTileEntity mte;
    protected final BlockPos pos;

    public MachineSceneWidget(int x, int y, int width, int height, MetaTileEntity mte) {
        this(x, y, width, height, mte.getPos());
        this.mte = mte;
        updateScene();
    }

    public MachineSceneWidget(int x, int y, int width, int height, BlockPos pos) {
        super(x, y, width, height);
        this.pos = pos;
        this.addWidget(new ScrollBarWidget(5, height - 13, width - 50, 8, 0, 1, 0.05f)
                .setOnChanged(value -> alpha = value, true).setInitValue(1f));
        this.addWidget(new RectButtonWidget(width - 40, height - 15, 35, 12, 1)
                .setToggleButton(new TextTexture("COLOR", -1), (c, b) -> blendColor = b)
                .setValueSupplier(true, () -> blendColor)
                .setColors(TerminalTheme.COLOR_7.getColor(), TerminalTheme.COLOR_F_1.getColor(), 0)
                .setIcon(new TextTexture("ALPHA", -1)));
        if (worldSceneRenderer != null) {
            worldSceneRenderer.releaseFBO();
            worldSceneRenderer = null;
        }
    }

    public Set<BlockPos> getCores() {
        return cores;
    }

    public Set<BlockPos> getAround() {
        return around;
    }

    public static FBOWorldSceneRenderer getWorldSceneRenderer() {
        return worldSceneRenderer;
    }

    public MachineSceneWidget setOnSelected(BiConsumer<BlockPos, EnumFacing> onSelected) {
        this.onSelected = onSelected;
        return this;
    }

    @SideOnly(Side.CLIENT)
    private void renderBlockOverLay(WorldSceneRenderer renderer) {
        hoveredFacingPos = null;
        if (isMouseOverElement(currentMouseX, currentMouseY)) {
            int x = getPosition().x;
            int y = getPosition().y;
            int width = getSize().width;
            int height = getSize().height;
            int resolutionWidth = worldSceneRenderer.getResolutionWidth();
            int resolutionHeight = worldSceneRenderer.getResolutionHeight();
            int mouseX = resolutionWidth * (currentMouseX - x) / width;
            int mouseY = (int) (resolutionHeight * (1 - (currentMouseY - y) / (float) height));
            Vector3f hitPos = WorldSceneRenderer.unProject(mouseX, mouseY);
            World world = renderer.world;
            Vec3d eyePos = new Vec3d(renderer.getEyePos().x, renderer.getEyePos().y, renderer.getEyePos().z);
            hitPos.scale(2); // Double view range to ensure pos can be seen.
            Vec3d endPos = new Vec3d((hitPos.x - eyePos.x), (hitPos.y - eyePos.y), (hitPos.z - eyePos.z));
            double min = Float.MAX_VALUE;
            for (BlockPos core : cores) {
                IBlockState blockState = world.getBlockState(core);
                if (blockState.getBlock() == Blocks.AIR) {
                    continue;
                }
                RayTraceResult hit = blockState.collisionRayTrace(world, core, eyePos, endPos);
                if (hit != null && hit.typeOfHit != RayTraceResult.Type.MISS) {
                    double dist = eyePos.distanceTo(new Vec3d(hit.getBlockPos()));
                    if (dist < min) {
                        min = dist;
                        hoveredFacingPos = new FacingPos(hit.getBlockPos(), hit.sideHit);
                    }
                }
            }
        }
        if (selectedFacingPos != null || hoveredFacingPos != null) {
            GlStateManager.pushMatrix();
            RenderUtil.useLightMap(240, 240, () -> {
                GlStateManager.disableDepth();
                if (selectedFacingPos != null) {
                    drawFacingBorder(selectedFacingPos, 0xff00ff00);
                }
                if (hoveredFacingPos != null && !hoveredFacingPos.equals(selectedFacingPos)) {
                    drawFacingBorder(hoveredFacingPos, 0xffffffff);
                }
                GlStateManager.enableDepth();
            });
            GlStateManager.popMatrix();
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
    }

    private static void drawFacingBorder(FacingPos posFace, int color) {
        GlStateManager.pushMatrix();
        RenderUtil.moveToFace(posFace.getPos().getX(), posFace.getPos().getY(), posFace.getPos().getZ(),
                posFace.getFacing());
        RenderUtil.rotateToFace(posFace.getFacing(), null);
        GlStateManager.scale(1f / 16, 1f / 16, 0);
        GlStateManager.translate(-8, -8, 0);
        Widget.drawBorder(1, 1, 14, 14, color, 1);
        GlStateManager.popMatrix();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (mte == null) {
            World world = this.gui.entityPlayer.world;
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof IGregTechTileEntity &&
                    ((IGregTechTileEntity) tileEntity).getMetaTileEntity() != null) {
                mte = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
                updateScene();
            }
        } else if (!mte.isValid()) {
            worldSceneRenderer.releaseFBO();
            worldSceneRenderer = null;
            mte = null;
        }
    }

    private void updateScene() {
        if (!mte.isValid()) return;
        World world = mte.getWorld();
        if (worldSceneRenderer != null) {
            worldSceneRenderer.releaseFBO();
        }
        worldSceneRenderer = new FBOWorldSceneRenderer(world, 1080, 1080);
        worldSceneRenderer.setAfterWorldRender(this::renderBlockOverLay);
        cores = new HashSet<>();
        around = new HashSet<>();
        cores.add(pos);
        if (mte instanceof MultiblockControllerBase multi && multi.isStructureFormed()) {
            PatternMatchContext context = multi.structurePattern.checkPatternFastAt(
                    world, pos, mte.getFrontFacing().getOpposite(), multi.getUpwardsFacing(), multi.allowsFlip());
            if (context != null) {
                List<BlockPos> validPos = multi.structurePattern.cache.keySet().stream().map(BlockPos::fromLong)
                        .collect(Collectors.toList());
                Set<IMultiblockPart> parts = context.getOrCreate("MultiblockParts", HashSet::new);
                for (IMultiblockPart part : parts) {
                    if (part instanceof MetaTileEntity) {
                        cores.add(((MetaTileEntity) part).getPos());
                    }
                }
                for (EnumFacing facing : EnumFacing.VALUES) {
                    cores.forEach(pos -> around.add(pos.offset(facing)));
                }
                int minX = Integer.MAX_VALUE;
                int minY = Integer.MAX_VALUE;
                int minZ = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                int maxY = Integer.MIN_VALUE;
                int maxZ = Integer.MIN_VALUE;
                for (BlockPos vPos : validPos) {
                    around.add(vPos);
                    minX = Math.min(minX, vPos.getX());
                    minY = Math.min(minY, vPos.getY());
                    minZ = Math.min(minZ, vPos.getZ());
                    maxX = Math.max(maxX, vPos.getX());
                    maxY = Math.max(maxY, vPos.getY());
                    maxZ = Math.max(maxZ, vPos.getZ());
                }
                around.removeAll(cores);
                center = new Vector3f((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f);
            } else {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    around.add(pos.offset(facing));
                }
                center = new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
            }
        } else {
            for (EnumFacing facing : EnumFacing.VALUES) {
                around.add(pos.offset(facing));
            }
            center = new Vector3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        }
        worldSceneRenderer.addRenderedBlocks(cores, null);
        worldSceneRenderer.addRenderedBlocks(around, this::aroundBlocksRenderHook);
        worldSceneRenderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch), Math.toRadians(rotationYaw));
    }

    private void aroundBlocksRenderHook(boolean isTESR, int pass, BlockRenderLayer layer) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        if (blendColor) {
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.CONSTANT_COLOR,
                    GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
        } else {
            GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        }
        GL14.glBlendColor(1, 1, 1, alpha);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (isMouseOverElement(mouseX, mouseY)) {
            dragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            if (hoveredFacingPos != null && !hoveredFacingPos.equals(selectedFacingPos)) {
                selectedFacingPos = hoveredFacingPos;
                if (onSelected != null) {
                    onSelected.accept(selectedFacingPos.getPos(), selectedFacingPos.getFacing());
                }
            }
            return true;
        }
        dragging = false;
        return false;
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            zoom = (float) MathHelper.clamp(zoom + (wheelDelta < 0 ? 0.5 : -0.5), 3, 999);
            if (worldSceneRenderer != null) {
                worldSceneRenderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch),
                        Math.toRadians(rotationYaw));
            }
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (dragging) {
            rotationPitch += mouseX - lastMouseX + 360;
            rotationPitch = rotationPitch % 360;
            rotationYaw = (float) MathHelper.clamp(rotationYaw + (mouseY - lastMouseY), -89.9, 89.9);
            lastMouseY = mouseY;
            lastMouseX = mouseX;
            if (worldSceneRenderer != null) {
                worldSceneRenderer.setCameraLookAt(center, zoom, Math.toRadians(rotationPitch),
                        Math.toRadians(rotationYaw));
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, timeDragged);
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        int x = getPosition().x;
        int y = getPosition().y;
        int width = getSize().width;
        int height = getSize().height;
        drawSolidRect(x, y, width, height, 0xaf000000);
        if (worldSceneRenderer != null) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            worldSceneRenderer.render(x, y, width, height, mouseX - x, mouseY - y);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }
        drawBorder(x + 1, y + 1, width - 2, height - 2, 0xff000000, 1);
        if (mte != null) {
            drawStringSized(I18n.format(mte.getMetaFullName()), x + width / 2f, y + 10, -1, true, 1, true);
        }
        super.drawInBackground(mouseX, mouseY, partialTicks, context);
        currentMouseX = mouseX;
        currentMouseY = mouseY;
    }
}
