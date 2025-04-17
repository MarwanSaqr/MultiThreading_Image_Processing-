/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package javaapplication1;
import java.awt.image.BufferedImage;

/**
 *
 * @author Saqr
 */
public class BrightnessAdjuster {
    public static BufferedImage process(BufferedImage src, float factor) {
        BufferedImage output = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int rgb = src.getRGB(x, y);
                int r = Math.min(255, (int)(((rgb >> 16) & 0xFF) * factor));
                int g = Math.min(255, (int)(((rgb >> 8) & 0xFF) * factor));
                int b = Math.min(255, (int)((rgb & 0xFF) * factor));
                int newRGB = (r << 16) | (g << 8) | b;
                output.setRGB(x, y, newRGB);
            }
        }
        return output;
    }
}

