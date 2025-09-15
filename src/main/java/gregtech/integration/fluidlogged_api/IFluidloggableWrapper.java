package gregtech.integration.fluidlogged_api;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;

import gregtech.api.util.Mods;

import net.minecraftforge.fml.common.Optional;

@Optional.Interface(modid = Mods.Names.FLUIDLOGGED_API, iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable")
public interface IFluidloggableWrapper extends IFluidloggable {}
