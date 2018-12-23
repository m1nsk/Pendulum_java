package transmission.Protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class BundleInfo {
    Integer fullSize;
    DataType type;
}
