package transmission.Protocol;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.CRC32;

public class ConvertorUtils {

    static byte[] concatArrays(List<byte[]> list) {
        ByteBuffer bb = ByteBuffer.allocate(list.stream().mapToInt(image -> image.length).sum());
        list.forEach(bb::put);
        return bb.array();
    }

    static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    static Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static byte[] fileToArray(File file) {
        byte[] data = new byte[(int) file.length()];
        try {
            new FileInputStream(file).read(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    static boolean checkCRC(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        byte[] crcBytes = new byte[Long.BYTES];
        System.arraycopy(bytes, bytes.length - crcBytes.length, crcBytes, 0 , crcBytes.length);
        crc32.update(bytes, 0, bytes.length - crcBytes.length);
        return bytesToLong(crcBytes) == crc32.getValue();
    }
}
