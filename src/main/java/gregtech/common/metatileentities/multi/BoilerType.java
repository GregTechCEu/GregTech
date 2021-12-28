package gregtech.common.metatileentities.multi;

import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;

import static gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType.*;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.*;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.*;
import static gregtech.common.blocks.MetaBlocks.*;

public enum BoilerType {

    BRONZE(800, 1200,
            METAL_CASING.getState(BRONZE_BRICKS),
            BOILER_FIREBOX_CASING.getState(BRONZE_FIREBOX),
            BOILER_CASING.getState(BRONZE_PIPE),
            Textures.BRONZE_PLATED_BRICKS,
            Textures.BRONZE_FIREBOX,
            Textures.BRONZE_FIREBOX_ACTIVE,
            Textures.LARGE_BRONZE_BOILER),

    STEEL(1800, 1800,
            METAL_CASING.getState(STEEL_SOLID),
            BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX),
            BOILER_CASING.getState(STEEL_PIPE),
            Textures.SOLID_STEEL_CASING,
            Textures.STEEL_FIREBOX,
            Textures.STEEL_FIREBOX_ACTIVE,
            Textures.LARGE_STEEL_BOILER),

    TITANIUM(3200, 2400,
            METAL_CASING.getState(TITANIUM_STABLE),
            BOILER_FIREBOX_CASING.getState(TITANIUM_FIREBOX),
            BOILER_CASING.getState(TITANIUM_PIPE),
            Textures.STABLE_TITANIUM_CASING,
            Textures.TITANIUM_FIREBOX,
            Textures.TITANIUM_FIREBOX_ACTIVE,
            Textures.LARGE_TITANIUM_BOILER),

    TUNGSTENSTEEL(6400, 3000,
            METAL_CASING.getState(TUNGSTENSTEEL_ROBUST),
            BOILER_FIREBOX_CASING.getState(TUNGSTENSTEEL_FIREBOX),
            BOILER_CASING.getState(TUNGSTENSTEEL_PIPE),
            Textures.ROBUST_TUNGSTENSTEEL_CASING,
            Textures.TUNGSTENSTEEL_FIREBOX,
            Textures.TUNGSTENSTEEL_FIREBOX_ACTIVE,
            Textures.LARGE_TUNGSTENSTEEL_BOILER);

    // Workable Data
    private final int steamPerTick;
    private final int ticksToBoiling;

    // Structure Data
    public final IBlockState casingState;
    public final IBlockState fireboxState;
    public final IBlockState pipeState;

    // Rendering Data
    public final ICubeRenderer casingRenderer;
    public final ICubeRenderer fireboxIdleRenderer;
    public final ICubeRenderer fireboxActiveRenderer;
    public final ICubeRenderer frontOverlay;

    BoilerType(int steamPerTick, int ticksToBoiling,
               IBlockState casingState,
               IBlockState fireboxState,
               IBlockState pipeState,
               ICubeRenderer casingRenderer,
               ICubeRenderer fireboxIdleRenderer,
               ICubeRenderer fireboxActiveRenderer,
               ICubeRenderer frontOverlay) {

        this.steamPerTick = steamPerTick;
        this.ticksToBoiling = ticksToBoiling;

        this.casingState = casingState;
        this.fireboxState = fireboxState;
        this.pipeState = pipeState;

        this.casingRenderer = casingRenderer;
        this.fireboxIdleRenderer = fireboxIdleRenderer;
        this.fireboxActiveRenderer = fireboxActiveRenderer;
        this.frontOverlay = frontOverlay;
    }

    public int steamPerTick() {
        return steamPerTick;
    }

    public int getTicksToBoiling() {
        return ticksToBoiling;
    }

    public int runtimeBoost(int ticks) {
        switch(this) {
            case BRONZE:        return ticks * 2;
            case STEEL:         return ticks * 150 / 100;
            case TITANIUM:      return ticks * 120 / 100;
            case TUNGSTENSTEEL: return ticks;
        }
        return 0;
    }
}
