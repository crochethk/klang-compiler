package cc.crochethk.compilerbau.p2;

public interface Visitable {
    <R> R accept(Visitor<R> visitor) throws Exception;
}
