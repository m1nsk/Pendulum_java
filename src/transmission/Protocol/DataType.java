package transmission.Protocol;

public enum DataType {
    DATA, COMMAND;

    public static DataType getTypeByString(String type) {
        switch (type) {
            case "DATA":
                return DATA;
            case "COMMAND":
                return COMMAND;
            default:
                return null;
        }
    }
}