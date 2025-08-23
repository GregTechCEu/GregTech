package gtqt.api.util;

import appeng.api.implementations.ICraftingPatternItem;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.ArrayList;
import java.util.List;

public class PatternUtils {
    public static void adjustPatternMultipliers(ItemStack patternStack, int divideFactor, int multiplyFactor) {
        if (patternStack.isEmpty() || !(patternStack.getItem() instanceof ICraftingPatternItem)) return;

        NBTTagCompound nbt = patternStack.getTagCompound();
        if (nbt == null) return;

        // 统一收集需要处理的NBT列表
        List<NBTTagList> processingLists = new ArrayList<>(4);
        final boolean isAE2FC = patternStack.getItem() instanceof ItemFluidEncodedPattern;
        final String countTag = isAE2FC ? "Cnt" : "Count";

        // 添加基础标签
        processingLists.add(nbt.getTagList("in", 10));
        processingLists.add(nbt.getTagList("out", 10));

        // 添加AE2FC专用标签
        if (isAE2FC) {
            processingLists.add(nbt.getTagList("Inputs", 10));
            processingLists.add(nbt.getTagList("Outputs", 10));
        }

        // 执行倍除操作
        if (divideFactor > 1) {
            boolean canDivide = processingLists.stream()
                    .allMatch(list -> canSafelyDivide(list, divideFactor, countTag));

            if (canDivide) {
                processingLists.forEach(list ->
                        modifyPatternCount(list, divideFactor, Operation.DIVIDE, countTag));
            }
        }

        // 执行倍乘操作
        if (multiplyFactor > 1) {
            processingLists.forEach(list ->
                    modifyPatternCount(list, multiplyFactor, Operation.MULTIPLY, countTag));
        }

        // 更新所有stackSize标签
        processingLists.forEach(list -> updateStackSizeTags(list, countTag));
    }
    // 检查列表是否可以安全倍除
    private static boolean canSafelyDivide(NBTTagList list, int divisor, String countTag) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int count = tag.getInteger(countTag);
            if (count % divisor != 0) {
                return false;
            }
        }
        return true;
    }

    // 修改模式数量
    private static void modifyPatternCount(NBTTagList list, int factor, Operation op, String countTag) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int original = tag.getInteger(countTag);

            switch (op) {
                case DIVIDE:
                    if (original >= factor) {
                        tag.setInteger(countTag, Math.max(1, original / factor));
                    }
                    break;
                case MULTIPLY:
                    if (original > 0 && original <= Integer.MAX_VALUE / factor) {
                        tag.setInteger(countTag, original * factor);
                    }
                    break;
            }
        }
    }

    // 更新stackSize标签
    private static void updateStackSizeTags(NBTTagList list, String countTag) {
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int count = tag.getInteger(countTag);
            if (count > 64) {
                tag.setInteger("stackSize", count);
            } else {
                tag.removeTag("stackSize");
            }
        }
    }

    enum Operation {
        DIVIDE,
        MULTIPLY
    }
}
