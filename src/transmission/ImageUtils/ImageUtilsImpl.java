package transmission.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageUtilsImpl {

    public static File resizeImageAndConvertToFile(File file, Integer width, Integer height) throws IOException {
        BufferedImage originalImage = ImageIO.read(file);
        int imageType = BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(width, height, imageType);
        Graphics2D g = scaledBI.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        File outputFile = new File(file.getParent() + "/_" + file.getName());
        ImageIO.write(scaledBI, "png", Files.newOutputStream(Paths.get(outputFile.getPath())));
        return outputFile;
    }
}