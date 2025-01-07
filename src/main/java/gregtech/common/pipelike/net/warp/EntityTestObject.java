package gregtech.common.pipelike.net.warp;

import gregtech.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.entity.Entity;

import com.github.bsideup.jabel.Desugar;
import org.jetbrains.annotations.NotNull;

@Desugar
public record EntityTestObject(@NotNull Entity entity) implements IPredicateTestObject {}
