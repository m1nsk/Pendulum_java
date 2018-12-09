package transmission.Protocol;

public enum DataType {
    DATA("DATA"), COMMAND("COMMAND");

    private final String name;

    DataType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

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