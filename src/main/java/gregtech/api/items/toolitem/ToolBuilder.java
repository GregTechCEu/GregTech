package gregtech.api.items.toolitem;

import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class ToolBuilder<T extends IGTTool> {

    protected final String domain, id;

    protected final Set<String> toolClasses = new ObjectArraySet<>();
    protected String oreDict;
    protected List<String> secondaryOreDicts = new ArrayList<>();

    protected int tier = -1;
    protected IGTToolDefinition toolStats;
    protected SoundEvent sound;
    protected boolean playSoundOnBlockDestroy;
    protected Character symbol = null;
    protected Supplier<ItemStack> markerItem;

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
        return sound(sound, false);
    }

    public ToolBuilder<T> sound(SoundEvent sound, boolean playSoundOnBlockDestroy) {
        this.sound = sound;
        this.playSoundOnBlockDestroy = playSoundOnBlockDestroy;
        return this;
    }

    public ToolBuilder<T> toolClasses(String... tools) {
        Collections.addAll(toolClasses, tools);
        return this;
    }

    public ToolBuilder<T> toolClasses(Set<String> tools) {
        toolClasses.addAll(tools);
        return this;
    }

    public ToolBuilder<T> oreDict(@NotNull String oreDict) {
        this.oreDict = oreDict;
        return this;
    }

    public ToolBuilder<T> oreDict(@NotNull Enum<?> oreDict) {
        this.oreDict = oreDict.name();
        return this;
    }

    public ToolBuilder<T> secondaryOreDicts(@NotNull Enum<?>... oreDicts) {
        Arrays.stream(oreDicts).map(Enum::name).forEach(this.secondaryOreDicts::add);
        return this;
    }

    public ToolBuilder<T> secondaryOreDicts(@NotNull String... oreDicts) {
        this.secondaryOreDicts.addAll(Arrays.asList(oreDicts));
        return this;
    }

    public ToolBuilder<T> symbol(char symbol) {
        this.symbol = symbol;
        return this;
    }

    public ToolBuilder<T> markerItem(Supplier<ItemStack> markerItem) {
        this.markerItem = markerItem;
        return this;
    }

    public abstract Supplier<T> supply();

    public T build() {
        if (this.symbol == null) {
            return supply().get();
        }
        IGTTool existing = ToolHelper.getToolFromSymbol(this.symbol);
        if (existing != null) {
            throw new IllegalArgumentException(
                    String.format("Symbol %s has been taken by %s already!", symbol, existing));
        }
        T supplied = supply().get();
        ToolHelper.registerToolSymbol(this.symbol, supplied);
        return supplied;
    }
}
