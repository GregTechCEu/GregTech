package gregtech.api.pollution;

import gregtech.api.GregTechAPI;
import gregtech.api.util.GTUtility;
import gregtech.api.util.XSTR;
import gregtech.common.ConfigHolder;
import gregtech.core.network.packets.PacketPollution;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockStem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockProperties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

public final class PollutionManager {

    private static final int CYCLE_LENGTH = 1200;
    private static final float REDUCTION_FACTOR = 0.9945F;
    private static final int POLLUTION_SPREAD_THRESHOLD = 400_000;
    private static final int PACKET_THRESHOLD = 1000;

    private static final Random RANDOM = new XSTR();

    private final PollutionSaveData saveData;
    private final Int2ObjectMap<Deque<PollutionData>> cycleQueues = new Int2ObjectOpenHashMap<>();
    private final Int2IntFunction operationsCount = new Int2IntOpenHashMap();
    private final Int2ObjectMap<PollutionClientHandler> clientHandlers = new Int2ObjectOpenHashMap<>();

    /**
     * @param saveData the save data this manager will manage
     */
    public PollutionManager(@NotNull PollutionSaveData saveData) {
        this.saveData = saveData;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void reset() {
        MinecraftForge.EVENT_BUS.unregister(this);
        this.cycleQueues.clear();
        this.operationsCount.clear();
    }

    /**
     * @param dimension the dimension containing the chunk
     * @param chunkX    the x coordinate of the chunk
     * @param chunkZ    the z coordinate of the chunk
     * @return the pollution in the chunk
     */
    public int getPollution(int dimension, int chunkX, int chunkZ) {
        return saveData.get(dimension, chunkX, chunkZ).pollution();
    }

    /**
     * @param dimension the dimension containing the chunk
     * @param chunkX    the x coordinate of the chunk
     * @param chunkZ    the z coordinate of the chunk
     * @param amount    the amount to set
     */
    public void setPollution(int dimension, int chunkX, int chunkZ, int amount) {
        PollutionData data = saveData.get(dimension, chunkX, chunkZ);
        if (data.pollution() != amount) {
            data.setPollution(amount);
            saveData.markDirty();
        }
    }

    /**
     * @param dimension the dimension containing the chunk
     * @param chunkX    the x coordinate of the chunk
     * @param chunkZ    the z coordinate of the chunk
     * @param amount    the amount to change by
     */
    public void changePollution(int dimension, int chunkX, int chunkZ, int amount) {
        PollutionData data = saveData.get(dimension, chunkX, chunkZ);
        amount = GTUtility.safeCastLongToInt(((long) amount) + data.pollution());
        amount = MathHelper.clamp(amount, 0, Integer.MAX_VALUE);
        if (data.pollution() != amount) {
            data.setPollution(amount);
            saveData.markDirty();
        }
    }

    /**
     * Handle a Server -> Client Pollution Packet.
     * <p>
     * Should only be called on the <strong>Client Side</strong>.
     *
     * @param pos       the position of the pollution updated
     * @param dimension the dimension containing the pollution
     * @param pollution the pollution amount
     */
    @ApiStatus.Internal
    public void processPollutionPacket(long pos, int dimension, int pollution) {
        PollutionClientHandler handler = clientHandlers.get(dimension);
        if (handler == null) {
            handler = new PollutionClientHandler();
            clientHandlers.put(dimension, handler);
        }
        handler.handlePacket(pos, pollution);
    }

    @SubscribeEvent
    @ApiStatus.Internal
    public void onWorldTick(@NotNull TickEvent.WorldTickEvent event) {
        World world = event.world;
        if (world.isRemote || event.phase == TickEvent.Phase.START) return;

        final int dimension = world.provider.getDimension();
        if (world.getTotalWorldTime() % CYCLE_LENGTH == 0) {
            startNewCycle(dimension);
        }
        runCycle(world, cycleQueues.get(dimension), operationsCount.get(dimension));
    }

    @SubscribeEvent
    public void onWorldUnload(@NotNull WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            this.clientHandlers.values().forEach(MinecraftForge.EVENT_BUS::unregister);
            this.clientHandlers.clear();
        }
    }

    @SubscribeEvent
    public void onChunkWatch(@NotNull ChunkWatchEvent.Watch event) {
        Chunk chunk = event.getChunkInstance();
        if (chunk == null) return;

        EntityPlayerMP player = event.getPlayer();
        ChunkPos pos = chunk.getPos();
        int pollution = getPollution(player.dimension, pos.x, pos.z);
        if (pollution > PACKET_THRESHOLD) {
            GregTechAPI.networkHandler.sendTo(new PacketPollution(pos.x, pos.z, player.dimension, pollution), event.getPlayer());
        }
    }

    private void startNewCycle(int dimension) {
        var data = saveData.getData(dimension);
        if (data != null && !data.isEmpty()) {
            var queue = cycleQueues.get(dimension);
            if (queue == null) {
                queue = new ArrayDeque<>(data.values());
                cycleQueues.put(dimension, queue);
            } else {
                queue.clear();
                queue.addAll(data.values());
            }

            operationsCount.put(dimension, Math.max(1, data.size() / CYCLE_LENGTH));
        } else {
            // ensure operation count is reset
            operationsCount.remove(dimension);
        }
    }

    private void runCycle(@NotNull World world, @Nullable Deque<@NotNull PollutionData> queue, int operationsPerTick) {
        if (queue == null || queue.isEmpty()) return;
        for (int op = 0; op < operationsPerTick; op++) {
            if (queue.isEmpty()) return;

            PollutionData data = queue.pollLast();
            int pollution = (int) (REDUCTION_FACTOR * data.pollution());

            final int dimension = data.dimension();
            final int chunkX = (int) (data.pos() & 0xFFFFFFFFL);
            final int chunkZ = (int) ((data.pos() >> 32) & 0xFFFFFFFFL);
            if (pollution > POLLUTION_SPREAD_THRESHOLD) {
                pollution = spreadPollution(dimension, chunkX, chunkZ, pollution);
            }

            if (pollution > ConfigHolder.pollution.basicThreshold) {
                smogEffects(world, chunkX, chunkZ, pollution);
            }

            if (ConfigHolder.pollution.doInWorldEffects && pollution > ConfigHolder.pollution.seriousThreshold) {
                inWorld(world, chunkX, chunkZ, pollution);
            }

            data.setPollution(pollution);
            saveData.markDirty();

            if (pollution > PACKET_THRESHOLD) {
                GregTechAPI.networkHandler.sendToAllAround(new PacketPollution(chunkX, chunkZ, dimension, pollution),
                        new NetworkRegistry.TargetPoint(dimension, chunkX << 4,
                                world.getHeight(chunkX, chunkZ), chunkZ << 4, 256));
            }
        }
    }

    private int spreadPollution(int dimension, int chunkX, int chunkZ, int pollution) {
        for (int zOffset = 0; zOffset <= 1; zOffset++) {
            int z = chunkZ + zOffset;
            for (int xOffset = 0; xOffset <= 1; xOffset++) {
                int x = chunkX + xOffset;
                PollutionData neighbor = saveData.get(dimension, ChunkPos.asLong(x, z));
                int neighborPollution = neighbor.pollution();
                if (neighborPollution * 6 < pollution * 5) { // if the neighbor has < 5/6 the current chunk's pollution
                    int diff = (pollution - neighborPollution) / 20;
                    neighbor.setPollution(GTUtility.safeCastLongToInt((long) neighborPollution + diff));
                    pollution -= diff;
                }
            }
        }
        saveData.markDirty();

        return pollution;
    }

    private static void smogEffects(@NotNull World world, int chunkX, int chunkZ, int pollution) {
        final int basicDuration = Math.min(pollution / 1000, pollution);
        final int basicAmplifier = pollution / 400_000;
        final int seriousDuration = Math.min(pollution / 2000, pollution);
        final int seriousAmplifier = 1;
        final int seriousAltDuration = Math.min(pollution / 2000, pollution);
        final int seriousAltAmplifier = pollution / 500_000;

        boolean doSeriousEffects = pollution > ConfigHolder.pollution.seriousThreshold;

        AxisAlignedBB bb = new AxisAlignedBB(chunkX << 4, 0, chunkZ << 4, (chunkX << 4) + 15, world.getHeight(),
                (chunkZ << 4) + 15);
        List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, bb);
        for (EntityLivingBase entity : list) {
            if (entity instanceof EntityPlayerMP playerMP && playerMP.capabilities.isCreativeMode) {
                continue;
            }

            // todo check protection suits

            int amplifier = Math.min(3, basicAmplifier);

            int value = RANDOM.nextInt(3);
            Potion potion;
            if (value == 0) {
                potion = MobEffects.WEAKNESS;
            } else if (value == 1) {
                potion = MobEffects.SLOWNESS;
            } else {
                potion = MobEffects.MINING_FATIGUE;
            }

            entity.addPotionEffect(new PotionEffect(potion, basicDuration, amplifier));

            int duration;

            if (doSeriousEffects) {
                value = RANDOM.nextInt(4);
                if (value == 0) {
                    potion = MobEffects.HUNGER;
                    duration = seriousDuration;
                    amplifier = 0;
                } else if (value == 1) {
                    if (ConfigHolder.pollution.doNausea) {
                        potion = MobEffects.NAUSEA;
                        duration = seriousDuration;
                        amplifier = seriousAmplifier;
                    } else {
                        continue;
                    }
                } else if (value == 2) {
                    potion = MobEffects.POISON;
                    duration = seriousAltDuration;
                    amplifier = seriousAltAmplifier;
                } else {
                    potion = MobEffects.BLINDNESS;
                    duration = seriousDuration;
                    amplifier = seriousAmplifier;
                }

                entity.addPotionEffect(new PotionEffect(potion, duration, amplifier));
            }
        }
    }

    private static void inWorld(@NotNull World world, int chunkX, int chunkZ, int pollution) {
        final boolean isSourRain = pollution > ConfigHolder.pollution.sourRainThreshold;
        final int blockX = chunkX << 4;
        final int blockZ = chunkZ << 4;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 20; i < pollution / 25_000; i++) {
            int x = blockX + RANDOM.nextInt(16);
            int y = 60 - (i + RANDOM.nextInt(i * 2 + 1));
            int z = blockZ + RANDOM.nextInt(16);
            pos.setPos(x, y, z);
            damageBlock(world, pos, isSourRain);
        }
    }

    private static void damageBlock(@NotNull World world, @NotNull BlockPos pos, boolean isSourRain) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos) || block == Blocks.STONE || block == Blocks.SAND || block == Blocks.DEADBUSH) {
            return;
        }

        // kill plants
        if (block.isLeaves(state, world, pos)) {
            world.setBlockToAir(pos);
        } else if (shouldDestroyAndDrop(block, state)) {
            block.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        } else if (block == Blocks.TALLGRASS || block instanceof BlockSapling) {
            // double bushes
            world.setBlockState(pos, Blocks.DEADBUSH.getDefaultState(), 3);
        } else if (block == Blocks.MOSSY_COBBLESTONE) {
            world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
        } else if (state.getMaterial() == Material.GRASS) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState());
        } else if (block == Blocks.FARMLAND) {
            world.setBlockState(pos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT));
        } else if (state == Blocks.DIRT.getDefaultState()) {
            world.setBlockState(pos, Blocks.SAND.getDefaultState());
        }

        if (isSourRain && (block == Blocks.COBBLESTONE || block == Blocks.GRAVEL) && world.isRainingAt(pos)) {
            if (block == Blocks.COBBLESTONE) {
                world.setBlockState(pos, Blocks.GRAVEL.getDefaultState());
            } else {
                world.setBlockState(pos, Blocks.SAND.getDefaultState());
            }
        }
    }

    private static boolean shouldDestroyAndDrop(@NotNull Block block, @NotNull IBlockProperties props) {
        return block == Blocks.REEDS || block == Blocks.VINE || block == Blocks.WATERLILY || block == Blocks.WHEAT ||
                props.getMaterial() == Material.CACTUS || props.getMaterial() == Material.GOURD ||
                block instanceof BlockStem || block instanceof BlockFlower || block instanceof BlockCocoa;
    }

    /**
     * @param dimension the dimension to retrieve with
     * @return the client handler for the dimension
     */
    @Nullable PollutionClientHandler getClientHandler(int dimension) {
        return clientHandlers.get(dimension);
    }
}
