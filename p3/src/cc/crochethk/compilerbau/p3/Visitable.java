package cc.crochethk.compilerbau.p3;

public interface Visitable {
    <R> R accept(Visitor<R> visitor) throws Exception;
}
