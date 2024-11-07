package cc.crochethk.compilerbau.p3;

public interface Visitable {
    <R> R accept(Visitor<R> visitor);

    /* Add this boilerplate for each Visitable:
    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visit(this);
    }
    */
}