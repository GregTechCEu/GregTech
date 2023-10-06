package gregtech.common.metatileentities.miner;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.util.GTLog;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MinerUtil {

    private MinerUtil() {}

    /**
     * Maximum amount of blocks individual miners can scan in one tick
     */
    public static final int MAX_BLOCK_SCAN = 200;

    public static final String DISPLAY_CLICK_AREA_PREVIEW = "preview_area";
    public static final String DISPLAY_CLICK_AREA_PREVIEW_HIDE = "hide_preview_area";
    public static final String DISPLAY_CLICK_Y_LIMIT_DECR = "decr_y_limit";
    public static final String DISPLAY_CLICK_Y_LIMIT_INCR = "incr_y_limit";
    public static final String DISPLAY_CLICK_REPEAT_ENABLE = "enable_repeat";
    public static final String DISPLAY_CLICK_REPEAT_DISABLE = "disable_repeat";

    public static final AxisAlignedBB EMPTY_AABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    public static final ResourceLocation MINER_AREA_PREVIEW_TEXTURE = GTUtility.gregtechId("textures/fx/miner_area_preview.png");

    private static final Cuboid6 PIPE_CUBOID = new Cuboid6(4 / 16.0, 0.0, 4 / 16.0, 12 / 16.0, 1.0, 12 / 16.0);

    private static String oreReplacementConfigCache;
    private static IBlockState oreReplacement;

    @Nonnull
    @SuppressWarnings("deprecation")
    public static IBlockState getOreReplacement() {
        String config = ConfigHolder.machines.replaceMinedBlocksWith;
        if (Objects.equals(oreReplacementConfigCache, config)) {
            return oreReplacement;
        }

        oreReplacementConfigCache = config;

        String[] blockDescription = StringUtils.split(config, ":");
        String blockName = blockDescription.length <= 2 ? config : blockDescription[0] + ":" + blockDescription[1];
        Block block = Block.getBlockFromName(blockName);

        if (block == null) {
            GTLog.logger.error("Invalid configuration on entry 'machines/replaceMinedBlocksWith': Cannot find block with name '{}', using cobblestone as fallback.", blockName);
            return oreReplacement = Blocks.COBBLESTONE.getDefaultState();
        } else if (blockDescription.length <= 2 || blockDescription[2].isEmpty()) {
            return oreReplacement = block.getDefaultState();
        } else {
            try {
                return oreReplacement = block.getDefaultState().getBlock().getStateFromMeta(Integer.parseInt(blockDescription[2]));
            } catch (NumberFormatException ex) {
                GTLog.logger.error("Invalid configuration on entry 'machines/replaceMinedBlocksWith': Cannot parse metadata value '{}' as integer, using cobblestone as fallback.", blockDescription[2]);
                return oreReplacement = Blocks.COBBLESTONE.getDefaultState();
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static void renderPipe(@Nonnull ICubeRenderer pipeRenderer, int pipeLength,
                                  @Nonnull CCRenderState renderState, @Nonnull Matrix4 translation,
                                  @Nonnull IVertexOperation[] pipeline) {
        Textures.PIPE_IN_OVERLAY.renderSided(EnumFacing.DOWN, renderState, translation, pipeline);
        for (int i = 0; i < pipeLength; i++) {
            translation.translate(0.0, -1.0, 0.0);
            pipeRenderer.render(renderState, translation, pipeline, PIPE_CUBOID);
        }
    }
}
