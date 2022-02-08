package gregtech.api.items.toolitem;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.util.SoundEvent;

import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class ToolBuilder<T extends GTToolDefinition> {

    protected final String domain, id;

    protected final Set<String> toolClasses = new ObjectOpenHashSet<>();
    protected final Set<Block> effectiveBlocks = new ObjectOpenHashSet<>();

    protected IToolStats toolStats;
    protected SoundEvent sound;

    public ToolBuilder(String domain, String id) {
        this.domain = domain;
        this.id = id;
    }

    public ToolBuilder<T> toolStats(IToolStats toolStats) {
        this.toolStats = toolStats;
        return this;
    }

    public ToolBuilder<T> toolStats(UnaryOperator<ToolStatsBuilder> builder) {
        this.toolStats = builder.apply(new ToolStatsBuilder()).build();
        return this;
    }

    public ToolBuilder<T> sound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public ToolBuilder<T> effectiveBlocks(String... tools) {
        Collections.addAll(toolClasses, tools);
        return this;
    }

    public ToolBuilder<T> effectiveBlocks(Block... blocks) {
        Collections.addAll(effectiveBlocks, blocks);
        return this;
    }

    public abstract T build();

}
