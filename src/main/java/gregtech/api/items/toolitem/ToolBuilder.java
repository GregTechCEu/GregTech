package gregtech.api.items.toolitem;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.util.SoundEvent;

import java.util.Collections;
import java.util.Set;
import java.util.function.UnaryOperator;

public abstract class ToolBuilder<T extends IGTTool> {

    protected final String domain, id;

    protected final Set<String> toolClasses = new ObjectArraySet<>();
    protected final Set<String> oreDicts = new ObjectArraySet<>();
    protected final Set<Block> effectiveBlocks = new ObjectOpenHashSet<>();

    protected int tier = -1;
    protected IGTToolDefinition toolStats;
    protected SoundEvent sound;
    protected Character craftingSymbol = null;

    public ToolBuilder(String domain, String id) {
        this.domain = domain;
        this.id = id;
    }

    public ToolBuilder<T> electric(int tier) {
        this.tier = tier;
        return this;
    }

    public ToolBuilder<T> toolStats(IGTToolDefinition toolStats) {
        this.toolStats = toolStats;
        return this;
    }

    public ToolBuilder<T> toolStats(UnaryOperator<ToolDefinitionBuilder> builder) {
        this.toolStats = builder.apply(new ToolDefinitionBuilder()).build();
        return this;
    }

    public ToolBuilder<T> sound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public ToolBuilder<T> toolClasses(String... tools) {
        Collections.addAll(toolClasses, tools);
        return this;
    }

    public ToolBuilder<T> oreDicts(String... ores) {
        Collections.addAll(oreDicts, ores);
        return this;
    }

    public ToolBuilder<T> effectiveBlocks(Block... blocks) {
        Collections.addAll(effectiveBlocks, blocks);
        return this;
    }

    public ToolBuilder<T> craftingSymbol(char craftingSymbol) {
        this.craftingSymbol = craftingSymbol;
        return this;
    }

    public abstract T build();

}
