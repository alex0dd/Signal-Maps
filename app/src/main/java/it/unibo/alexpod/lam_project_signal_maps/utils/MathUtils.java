package it.unibo.alexpod.lam_project_signal_maps.utils;

import android.graphics.Color;
import android.support.annotation.ColorInt;

public class MathUtils {

    public static float rescaleInInterval(float t, float a, float b, float c, float d){
        // TODO: put this method outside of this class
        return c + ((d-c)/(b-a))*(t-a);
    }

    public static int interpolateColors(@ColorInt int colorA, @ColorInt int colorB, float bAmount) {
        float aAmount = 1.0f - bAmount;
        int newRed = (int)(Color.red(colorA) * aAmount + Color.red(colorB) * bAmount);
        int newGreen = (int)(Color.green(colorA) * aAmount + Color.green(colorB) * bAmount);
        int newBlue = (int)(Color.blue(colorA) * aAmount + Color.blue(colorB) * bAmount);
        return Color.argb(100, newRed, newGreen, newBlue);
    }

}
