package UsbReader;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import pendulum.Loader.HDDLoader;
import pendulum.Loader.ImageUtils;
import pendulum.PendulumParams;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FlashReader extends TimerTask {
    private final static String USB_FOLDER_PATH = "/media/";
    private static String hash;

    private PendulumParams params = PendulumParams.getInstance();

    private HDDLoader hddLoader;

    public FlashReader(HDDLoader hddLoader) {
        this.hddLoader = hddLoader;
    }

    @Override
    public void run() {
        try {
            if(inspectUsbFolder())
                hddLoader.Load();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     images data structure
     -usb_folder
     -pendulum**.properties   //properties file.
                              //here we store additional properties.
                              //1.hash,to know is this instructions are new
     -images                  //store images
     -instructions            //store instructions
     **/

    private boolean inspectUsbFolder() throws IOException {
        File usbDir = getUsbFolder();
        if (usbDir.isDirectory()){
            List<File> usbs = Arrays.stream(usbDir.listFiles()).filter(File::isDirectory).collect(Collectors.toList());
            for (File usb : usbs) {
                File[] files = usb.listFiles((file1, s) -> s.endsWith(".properties") && s.startsWith("pendulum"));
                for (File file : files) {
                    if(validateConfigData(file)) {
                        File instructions = new File(file.getParentFile().getPath() + "/instructions");
                        File images = new File(file.getParentFile().getPath() + "/images");
                        if (validateInstructions(instructions) && validateImages(images)) {
                            copyFiles(file, instructions, images);
                            return true;
                        }
                    }
                }
            }
        } else {
            throw new IOException("No usb media folder");
        }
        return false;
    }

    private File getUsbFolder() {
        return new File(USB_FOLDER_PATH);
    }

    private void copyFiles(File properties, File instructions, File images) throws IOException {
        if(instructions.exists())
            copyInstructionsToPi(instructions);
        copyImagesToPi(images);
        copyConfigToPi(properties);
    }

    private boolean validateConfigData(File file) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        if (properties.containsKey("hash")){
            if (hash == null || !hash.equals(properties.getProperty("hash"))) {
                hash = properties.getProperty("hash");
                return true;
            }
        }
        return false;
    }

    private static boolean validateImages(File file) {
        if (file.isDirectory() && Objects.requireNonNull(file.listFiles()).length > 0){
            File[] images = file.listFiles();
            Set<String> imgSet = new HashSet<>();
            for (File image : images) {
                String name = ImageUtils.getFileName(image);
                if (ImageUtils.IMAGE_EXTENSION_SET.contains(ImageUtils.getFileExtension(image)) && isIntegerString(name)){
                    if(imgSet.contains(name)) {
                        log.error("duplicate images naming");
                        return false;
                    }
                    imgSet.add(name);
                } else {
                    image.delete();
                }
            }
            if(imgSet.isEmpty())
                return false;
            return true;
        }
        log.error("invalid images file");
        return false;
    }

    private static boolean validateInstructions(File file) {
        if(!file.exists()){
            return true;
        }
        if(!file.isFile()){
            log.error("instructions not a file");
            return false;
        }
        JSONObject instructions = readInstructionsFile(file);
        if (instructions == null) {
            log.error("invalid instructions file");
            return false;
        }
        return true;
    }

    private void copyInstructionsToPi(File newInstructions) throws IOException {
        File instructions = params.getInstructionsStorageFolder();
        if(instructions.exists()) {
            FileUtils.forceDelete(instructions);
        }
        FileUtils.copyFile(newInstructions, instructions);
    }

    private void copyImagesToPi(File newImages) throws IOException {
        File images = params.getImagesStorageFolder();
        if(images.isDirectory()) {
            FileUtils.cleanDirectory(images);
            FileUtils.copyDirectory(newImages, images);
        } else {
            FileUtils.forceMkdir(images);
            FileUtils.copyDirectory(newImages, images);
        }
    }

    private void copyConfigToPi(File newConfig) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(newConfig));
        params.updateParams(properties);
    }

    private static JSONObject readInstructionsFile(File config) {
        JSONObject object;
        try {
            object = new JSONObject(Objects.requireNonNull(getResourceFileAsString(config)));
        } catch (JSONException | FileNotFoundException e){
            return null;
        }
        return object;
    }

    private static String getResourceFileAsString(File file) throws FileNotFoundException {
        InputStream is = new FileInputStream(file.getPath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    private static boolean isIntegerString(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
