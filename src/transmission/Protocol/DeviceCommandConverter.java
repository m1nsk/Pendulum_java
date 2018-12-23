package transmission.Protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

public class DeviceCommandConverter {
    private ObjectMapper objectMapper = new ObjectMapper();

    public DeviceCommandConverter(){
    }

    public byte[] deviceCommandToBytes(Command command) throws JsonProcessingException {

       Map<String, Object> bundleMap = new HashMap<>();

        bundleMap.put("type", command.getType());
        bundleMap.put("args", command.getArgs());

        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        byte[] body = writer.writeValueAsBytes(bundleMap);

        String header = DataType.DATA + ":" + body.length + ":" + Long.BYTES + '\n';

        byte[] data = ConvertorUtils.concatArrays(Arrays.asList(header.getBytes(), body));
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        return ConvertorUtils.concatArrays(Arrays.asList(data, ConvertorUtils.longToBytes(crc32.getValue())));
    }


    public Command bytesToCommand(byte[] bytes) throws Exception {
        if(checkCRC(bytes)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
            String paramsString = br.readLine();
            List<Integer> params = Arrays.stream(paramsString.split(":"))
                    .map(ConvertorUtils::tryParseInt)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Integer offset = paramsString.getBytes().length;

            Map<String, Object> bundle = getBundle(bytes, params, offset);
            Command command = new Command();
            command.setType(CommandType.getTypeByString((String) bundle.get("type")));
            command.setArgs((Map)bundle.get("args"));
            return command;
        }
        return null;
    }


    private static boolean checkCRC(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        byte[] crcBytes = new byte[Long.BYTES];
        System.arraycopy(bytes, bytes.length - crcBytes.length, crcBytes, 0 , crcBytes.length);
        crc32.update(bytes, 0, bytes.length - crcBytes.length);
        return ConvertorUtils.bytesToLong(crcBytes) == crc32.getValue();
    }

    private Map<String, Object> getBundle(byte[] bytes, List<Integer> params, Integer offset) throws IOException {
        byte[] infoBytes = new byte[params.get(0)];
        System.arraycopy(bytes, offset + 1, infoBytes, 0, params.get(0));
        return objectMapper.readValue(infoBytes, new TypeReference<Map<String,Object>>(){});
    }
}

