package gregtech.mixins.mui2;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.core.mixin.FontRendererAccessor;
import com.cleanroommc.modularui.drawable.text.FontRenderHelper;
import com.cleanroommc.modularui.drawable.text.RichTextCompiler;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(RichTextCompiler.class)
public abstract class MixinRichTextCompiler {

    // 使用 @Shadow 映射原始类中的字段
    @Shadow(remap = false) private FontRenderer fr;
    @Shadow(remap = false) private int maxWidth;
    @Shadow(remap = false) private int x;
    @Shadow(remap = false) private int h;
    @Shadow(remap = false) private List<Object> currentLine;

    // 使用 @Shadow 映射原始类中的方法
    @Shadow(remap = false) public abstract void newLine();
    @Shadow(remap = false) public abstract void addLineElement(Object element);


    /**
     * @author MeowmelMuku
     * @reason 优化文本编译
     */
    @Overwrite(remap = false)
    private void compileString(String text) {
        int lineEndIndex = text.indexOf('\n'); // 查找换行符位置
        int startIndex = 0; // 当前子字符串的起始索引

        do {
            // 如果没有找到换行符，则使用文本长度作为结束位置
            if (lineEndIndex < 0) {
                lineEndIndex = text.length();
            }

            // 获取当前行的子字符串
            String subText = text.substring(startIndex, lineEndIndex);
            startIndex = lineEndIndex + 1; // 设置下一个子字符串的起始位置

            // 处理当前子字符串
            while (!subText.isEmpty()) {
                // 计算当前行剩余空间能容纳的字符数
                int charsThatFit = ((FontRendererAccessor) this.fr).invokeSizeStringToWidth(
                        subText,
                        this.maxWidth - this.x
                );

                // 如果当前行无法容纳任何字符
                if (charsThatFit == 0) {
                    // 如果当前行已经有内容，则换行后重新计算
                    if (this.x > 0) {
                        this.newLine();
                        charsThatFit = ((FontRendererAccessor) this.fr).invokeSizeStringToWidth(
                                subText,
                                this.maxWidth
                        );
                    }

                    // 如果换行后仍然无法容纳任何字符，则强制插入部分字符串
                    if (charsThatFit == 0) {
                        // 计算格式代码的长度
                        int formatLength = FontRenderHelper.getFormatLength(subText, 0);

                        // 确保不会超出字符串长度
                        if (formatLength < subText.length()) {
                            charsThatFit = formatLength + 1;
                        } else {
                            charsThatFit = subText.length();
                        }
                    }
                }

                // 检查是否需要换行（单词中断的情况）
                if (charsThatFit < subText.length()) {
                    char nextChar = subText.charAt(charsThatFit);

                    // 如果当前行有内容且不是在空格处换行
                    if (nextChar != ' ' && this.x > 0) {
                        int fullLineChars = ((FontRendererAccessor) this.fr).invokeSizeStringToWidth(
                                subText,
                                this.maxWidth
                        );

                        if (fullLineChars < subText.length()) {
                            nextChar = subText.charAt(fullLineChars);

                            // 如果新行中在空格处结束，则换行
                            if (fullLineChars > charsThatFit && nextChar == ' ') {
                                this.newLine();
                            }
                        } else {
                            // 整个字符串可以放在一行中，换行
                            this.newLine();
                        }
                    }
                }

                // 获取当前要添加的字符串部分
                String currentPart = (subText.length() <= charsThatFit)
                        ? subText
                        : RichTextCompiler.trimRight(subText.substring(0, charsThatFit));

                // 计算字符串宽度并添加
                int width = this.fr.getStringWidth(currentPart);
                this.addLineElement(currentPart);

                // 更新行高度和当前位置
                this.h = Math.max(this.h, this.fr.FONT_HEIGHT);
                this.x += width;

                // 如果处理完整个子字符串，则退出循环
                if (subText.length() <= charsThatFit) {
                    break;
                }

                // 换行处理剩余部分
                this.newLine();
                char nextChar = subText.charAt(charsThatFit);

                // 如果换行处是空格，则跳过空格
                if (nextChar == ' ') {
                    charsThatFit++;
                }

                // 更新剩余要处理的字符串
                subText = subText.substring(charsThatFit);
            }

            // 如果当前位置是换行符，则换行
            if (lineEndIndex < text.length() && text.charAt(lineEndIndex) == '\n') {
                this.newLine();
            }

            // 继续查找下一个换行符，直到处理完所有文本
        } while ((lineEndIndex = text.indexOf('\n', startIndex)) >= 0 || startIndex < text.length());
    }

    /**
     * @author MeowmelMuku
     * @reason 优化文本编译
     */
    @Overwrite(remap = false)
    public static String trimRight(String s) {
        int i = s.length() - 1;

        // 从右侧开始查找第一个非空格字符
        while (i >= 0 && Character.isWhitespace(s.charAt(i))) {
            i--;
        }

        // 返回去除右侧空格后的字符串
        return s.substring(0, i + 1);
    }
}
