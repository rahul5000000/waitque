package com.rrsgroup.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Data
@AllArgsConstructor
public class ImageWrapper {
    private BufferedImage image;

    public byte[] toByteArray(String formatName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, formatName, baos);

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert BufferedImage to byte array", e);
        }
    }
}
