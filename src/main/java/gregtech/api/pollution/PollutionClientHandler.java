package gregtech.api.pollution;

import gregtech.common.ConfigHolder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
class PollutionClientHandler {

    private static final int MAX_PARTICLES = 100;
    private static final int PARTICLES_THRESHOLD = 400_000;
    private static final int PARTICLES_LIMIT = 3_500_000;

    private static final int FOG_THRESHOLD = 400_000;
    private static final int FOG_LIMIT = 7_000_000;
    private static final float FOG_RATIO = 0.02F;
    private static final int FOG_DISTANCE = 191;

    private static final float[] FOG_COLORS = { 0.3F, 0.25F, 0.1F };
    private static final int[] GRASS_COLORS = { 230, 180, 40 };
    private static final int[] LEAVES_COLORS = { 160, 80, 15 };
    private static final int[] LIQUID_COLORS = { 160, 200, 10 };
    private static final int[] FOLIAGE_COLORS = { 160, 80, 15 };

    private final PollutionMap pollutionMap;
    private double fogIntensityLastTick;
    private double lastUpdateTime;
    private int playerPollution;

    PollutionClientHandler() {
        this.pollutionMap = new PollutionMap();
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Handle a Server -> Client Pollution Packet
     * @param pos the position of the pollution updated
     * @param pollution the pollution amount
     */
    public void handlePacket(long pos, int pollution) {
        pollutionMap.addPollution((int) (pos & 0xFFFFFFFFL), (int) ((pos >> 32) & 0xFFFFFFFFL), pollution);
    }

    public int colorGrass(int color, int x, int z) {
        return colorize(color, pollutionMap.getPollution(x, z) / 1000, 350, 600, GRASS_COLORS);
    }

    public int colorLeaves(int color, int x, int z) {
        return colorize(color, pollutionMap.getPollution(x, z) / 1000, 300, 500, LEAVES_COLORS);
    }

    public int colorLiquid(int color, int x, int z) {
        return colorize(color, pollutionMap.getPollution(x, z) / 1000, 300, 500, LIQUID_COLORS);
    }

    public int colorFoliage(int color, int x, int z) {
        return colorize(color, pollutionMap.getPollution(x, z) / 1000, 300, 500, FOLIAGE_COLORS);
    }

    /**
     * @param color the existing color to modify
     * @param pollution the amount of pollution present
     * @param low the lower bound for pollution
     * @param high the higher bound for pollution
     * @param colors the colors to interpolate between
     * @return the new color
     */
    private static int colorize(int color, int pollution, int low, int high, int[] colors) {
        if (pollution < low) return color;

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        float ratio = ((float) (pollution - low)) / high;
        if (ratio > 1) ratio = 1;

        float complement = 1 - ratio;

        r = ((int) (r * complement + ratio * colors[0])) & 0xFF;
        g = ((int) (g * complement + ratio * colors[1])) & 0xFF;
        b = ((int) (b * complement + ratio * colors[2])) & 0xFF;

        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    @SubscribeEvent
    public void onFogColors(@NotNull EntityViewRenderEvent.FogColors event) {
        if (Minecraft.getMinecraft().player.capabilities.isCreativeMode) return;

        Block block = event.getState().getBlock();
        if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
            return;
        }

        float intensity = fogIntensityLastTick > 1 ? 1 : (float) fogIntensityLastTick;
        float complement = 1 - intensity;

        event.setRed(complement * event.getRed() + intensity * FOG_COLORS[0]);
        event.setGreen(complement * event.getGreen() + intensity * FOG_COLORS[1]);
        event.setBlue(complement * event.getBlue() + intensity * FOG_COLORS[2]);
    }

    /**
     * Tries to smooth out jump from linear to exponential for fog drawing
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderFog(@NotNull EntityViewRenderEvent.RenderFogEvent event) {
        if (!ConfigHolder.client.doPollutionFog) return;
        if (Minecraft.getMinecraft().player.capabilities.isCreativeMode) return;
        if (fogIntensityLastTick <= 0 || fogIntensityLastTick >= FOG_RATIO) return;

        if (event.getFogMode() == 0) {
            double v = 1 - fogIntensityLastTick / FOG_RATIO;
            GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
            GlStateManager.setFogStart((float) ((FOG_DISTANCE - 20) * 0.75 * v + 20));
            GlStateManager.setFogEnd((float) (FOG_DISTANCE * (0.75 + (v * 0.25))));
        }
    }

    @SubscribeEvent
    public void onFogDensity(@NotNull EntityViewRenderEvent.FogDensity event) {
        if (!ConfigHolder.client.doPollutionFog) return;
        if (Minecraft.getMinecraft().player.capabilities.isCreativeMode) return;
        if (fogIntensityLastTick < FOG_RATIO) return;
        if (event.getEntity() instanceof EntityLivingBase livingBase && livingBase.isPotionActive(MobEffects.BLINDNESS)) {
            return;
        }

        Block block = event.getState().getBlock();
        if (block instanceof IFluidBlock || block instanceof BlockLiquid) {
            return;
        }

        GlStateManager.setFog(GlStateManager.FogMode.EXP2);
        event.setDensity((float) (Math.pow(fogIntensityLastTick - FOG_RATIO, 0.75) / 5 + 0.01));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderTick(@NotNull TickEvent.RenderTickEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        if (player == null) return;

        if (event.phase == TickEvent.Phase.START) {
            if (!ConfigHolder.client.doPollutionFog) return;
            if (event.renderTickTime < lastUpdateTime) {
                lastUpdateTime -= 1;
            }

            float step = (float) ((event.renderTickTime - lastUpdateTime) / 50);
            lastUpdateTime = event.renderTickTime;

            float fogIntensity = MathHelper.clamp((playerPollution - FOG_THRESHOLD) / (float) FOG_LIMIT, 0, 1);
            double e = fogIntensity - fogIntensityLastTick;
            if (e != 0) {
                e = MathHelper.clamp(e, -0.5, 0.2);
                if (e > 0.001 || e < -0.001) {
                    fogIntensityLastTick += step * e;
                } else {
                    fogIntensityLastTick = fogIntensity;
                }
            }
        } else {
            // TODO debugging check
            drawDebug("Intensity: " + (fogIntensityLastTick * 10000), 0);
            drawDebug(
                    "Pollution: " + pollutionMap.getPollution(
                            MathHelper.floor(Minecraft.getMinecraft().player.lastTickPosX),
                            MathHelper.floor(Minecraft.getMinecraft().player.lastTickPosZ)),
                    20);
            drawDebug(
                    "Density:   "
                            + ((float) (Math.pow(fogIntensityLastTick - FOG_RATIO, 0.75F) / 5 + 0.01F) * 10000),
                    40);
        }
    }

    @SubscribeEvent
    public void onClientTick(@NotNull TickEvent.ClientTickEvent event) {
        if (!ConfigHolder.client.doPollutionParticles) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        if (player == null) return;

        World world = player.world;
        this.playerPollution = pollutionMap.getPollution(MathHelper.floor(player.lastTickPosX), MathHelper.floor(player.lastTickPosZ));

        float intensity = (playerPollution - PARTICLES_THRESHOLD) / (float) PARTICLES_LIMIT;
        if (intensity < 0) return;
        if (intensity > 1) intensity = 1;
        else intensity *= intensity;

        int x = MathHelper.floor(player.posX);
        int y = MathHelper.floor(player.posY);
        int z = MathHelper.floor(player.posZ);

        int particleAmount = Math.round(intensity * MAX_PARTICLES);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < particleAmount; i++) {
            pos.setPos(x + world.rand.nextInt(16) - world.rand.nextInt(16),
                    y + world.rand.nextInt(16) - world.rand.nextInt(16),
                    z + world.rand.nextInt(16) - world.rand.nextInt(16));
            IBlockState state = world.getBlockState(pos);

            if (state.getBlock().isAir(state, world, pos)) {
                minecraft.effectRenderer.addEffect(new PollutionParticle(world,
                        pos.getX() + world.rand.nextFloat(),
                        pos.getY() + world.rand.nextFloat(),
                        pos.getZ() + world.rand.nextFloat()));
            }
        }
    }

    private static void drawDebug(@NotNull String text, int offset) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, 0, offset, 0xFFFFFFFF);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
