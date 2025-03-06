package gregtech.api.statemachine;

import net.minecraft.nbt.NBTTagCompound;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.function.Predicate;

public class GTStateMachineLink {

    public static final GTStateMachineLink UNKNOWN_LINK = new GTStateMachineLink();

    protected final List<Predicate<NBTTagCompound>> conditions;
    protected final IntList linkIfs;
    protected final int linkElse;

    protected GTStateMachineLink() {
        this.conditions = new ObjectArrayList<>(1);
        this.linkIfs = new IntArrayList(1);
        this.linkElse = -1;
    }

    protected GTStateMachineLink(GTStateMachineLink link, int linkElse) {
        this.conditions = new ObjectArrayList<>(link.conditions.size() + 1);
        this.conditions.addAll(link.conditions);
        this.linkIfs = new IntArrayList(link.linkIfs.size() + 1);
        this.linkIfs.addAll(link.linkIfs);
        this.linkElse = linkElse;
    }

    public static GTStateMachineLink link(int link) {
        return UNKNOWN_LINK.elseLink(link);
    }

    public static GTStateMachineLink linkIf(Predicate<NBTTagCompound> predicate, int link) {
        return UNKNOWN_LINK.elseIf(predicate, link);
    }

    public static GTStateMachineLink linkIfElse(Predicate<NBTTagCompound> predicate, int link, int linkElse) {
        return UNKNOWN_LINK.elseIfElse(predicate, link, linkElse);
    }

    public GTStateMachineLink elseIf(Predicate<NBTTagCompound> predicate, int link) {
        return elseIfElse(predicate, link, this.linkElse);
    }

    public GTStateMachineLink elseIfElse(Predicate<NBTTagCompound> predicate, int link, int linkElse) {
        GTStateMachineLink created = new GTStateMachineLink(this, linkElse);
        created.conditions.add(predicate);
        created.linkIfs.add(link);
        return created;
    }

    public GTStateMachineLink elseLink(int link) {
        return new GTStateMachineLink(this, link);
    }

    public int getLink(NBTTagCompound data) {
        for (int i = 0; i < conditions.size(); i++) {
            if (conditions.get(i).test(data)) {
                return linkIfs.get(i);
            }
        }
        return linkElse;
    }
}
