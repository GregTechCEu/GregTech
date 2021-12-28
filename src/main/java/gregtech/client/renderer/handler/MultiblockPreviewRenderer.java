package gregtech.client.renderer.handler;

import gregtech.api.GTValues;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.scene.WorldSceneRenderer;
import gregtech.client.utils.RenderBufferHelper;
import gregtech.client.utils.TrackedDummyWorld;
import gregtech.integration.jei.JEIOptional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class MultiblockPreviewRenderer {

    public static void renderWorldLastEvent(RenderWorldLastEvent evt) {
        drawMultiBlockPreview(evt);
    }


    public static void renderMultiBlockPreview(MultiblockControllerBase controller, long durTimeMillis) {
        if (!controller.getPos().equals(mbpPos)) {
            layer = -1;
        }
        if (hasRendered) {
            resetMultiblockRender();
            posHighLight = null;
            hlEndTime = 0;
        }
        controllerBase = controller;
        mbpEndTime = durTimeMillis;
    }

}
