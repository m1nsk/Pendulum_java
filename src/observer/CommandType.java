package observer;

public enum CommandType {
    IMAGE("IMAGE"), PROPS("PROPS");

    private final String name;

    CommandType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static CommandType getTypeByString(String type) {
        switch (type) {
            case "IMAGE":
                return IMAGE;
            case "PROPS":
                return PROPS;
            default:
                return null;
        }
    }
}