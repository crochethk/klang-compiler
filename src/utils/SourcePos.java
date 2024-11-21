package utils;

public record SourcePos(int line, int column) {
    @Override
    public String toString() {
        return "L" + line + ":" + column;
    }
}