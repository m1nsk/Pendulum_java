package transmission.Protocol;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class DataTypeHelper {
    public static BundleInfo getBundleInfo(byte[] bytes) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        String paramsString = br.readLine();
        List<String> params = Arrays.asList(paramsString.split(":"));
        DataType dataType = DataType.getTypeByString(params.get(0));
        Integer fullSize = params
                .subList(1, params.size())
                .stream()
                .map(ConvertorUtils::tryParseInt)
                .reduce((sum, item) -> sum + item).orElse(null);
        if(fullSize == null)
            throw new Exception("bad data request");
        fullSize = paramsString.getBytes().length + fullSize + 1;
        return new BundleInfo(fullSize, dataType);
    }
}
