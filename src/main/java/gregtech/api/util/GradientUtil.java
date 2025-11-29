package gregtech.api.util;

public class GradientUtil {

    public static long getGradient(int rgb, int luminanceDifference) {
        float[] hsl = RGBtoHSL(rgb);
        float[] upShade = new float[3];
        float[] downShade = new float[3];

        System.arraycopy(hsl, 0, upShade, 0, 3);
        System.arraycopy(hsl, 0, downShade, 0, 3);

        upShade[2] = Math.min(upShade[2] + luminanceDifference, 100.0F);
        downShade[2] = Math.max(downShade[2] - luminanceDifference, 0.0F);

        return ColorUtil.packTwoARGB(toRGB(downShade), toRGB(upShade));
    }

    public static float[] RGBtoHSL(int rgb) {
        float red = ColorUtil.ARGBHelper.RED.isolateAndShiftAsFloat(rgb);
        float green = ColorUtil.ARGBHelper.GREEN.isolateAndShiftAsFloat(rgb);
        float blue = ColorUtil.ARGBHelper.BLUE.isolateAndShiftAsFloat(rgb);

        // Minimum and Maximum RGB values are used in the HSL calculations
        float min = Math.min(red, Math.min(green, blue));
        float max = Math.max(red, Math.max(green, blue));

        // Calculate the Hue
        float h = 0;
        if (max == min) {
            h = 0;
        } else if (max == red) {
            h = ((60 * (green - blue) / (max - min)) + 360) % 360;
        } else if (max == green) {
            h = (60 * (blue - red) / (max - min)) + 120;
        } else if (max == blue) {
            h = (60 * (red - green) / (max - min)) + 240;
        }

        // Calculate the Luminance
        float l = (max + min) / 2;

        // Calculate the Saturation
        float s;
        if (max == min) {
            s = 0;
        } else if (l <= 0.5F) {
            s = (max - min) / (max + min);
        } else {
            s = (max - min) / (2 - max - min);
        }

        return new float[] { h, s * 100, l * 100 };
    }

    public static int toRGB(float[] hsl) {
        return toRGB(hsl[0], hsl[1], hsl[2]);
    }

    public static int toRGB(float h, float s, float l) {
        // Formula needs all values between 0 - 1
        h = h % 360.0F;
        h /= 360.0F;
        s /= 100.0F;
        l /= 100.0F;

        float q;
        if (l < 0.5F) {
            q = l * (1 + s);
        } else {
            q = (l + s) - (s * l);
        }

        float p = 2 * l - q;

        float red = Math.max(0, hueToRGB(p, q, h + (1.0F / 3.0F)));
        float green = Math.max(0, hueToRGB(p, q, h));
        float blue = Math.max(0, hueToRGB(p, q, h - (1.0F / 3.0F)));

        red = Math.min(red, 1.0F);
        green = Math.min(green, 1.0F);
        blue = Math.min(blue, 1.0F);

        return ColorUtil.combineRGBFullAlpha((int) red * 255, (int) green * 255, (int) blue * 255);
    }

    private static float hueToRGB(float p, float q, float h) {
        if (h < 0) {
            h += 1;
        }
        if (h > 1) {
            h -= 1;
        }
        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }
        if (2 * h < 1) {
            return q;
        }
        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0F / 3.0F) - h));
        }
        return p;
    }
}
