package cc.crochethk.compilerbau.P2;

public interface Visitable {
    <R> R accept(Visitor<R> visitor) throws Exception;
}
