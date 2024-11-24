package utils;

public sealed interface Result<T> permits Result.Ok, Result.Err {
    final static Result<Void> Ok = new Result.Ok<Void>(null);
    final static Result<Void> Err = new Result.Err<Void>();

    boolean isOk();

    T value();

    default boolean isErr() {
        return !isOk();
    }

    record Ok<T>(T value) implements Result<T> {
        @Override
        public boolean isOk() {
            return true;
        }
    }

    record Err<T>() implements Result<T> {
        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public T value() {
            throw new RuntimeException("attempt to unwrap Err Result");
        }

    }
}