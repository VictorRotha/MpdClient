package de.nif.data;

public class Result<T> {

    public T message;
    public Exception exception;
    public ResultType type;

    public enum ResultType {
        OK,
        ERROR,
        IDLE
    }

    public Result() {
        this(null, null, null);
    }

    public Result(T message, Exception exception, ResultType resultType) {
        this.message = message;
        this.exception = exception;
        this.type = resultType;
    }

    @Override
    public String toString() {
        return String.format("Result: message=%s, exception=%s, type=%s", message, (exception == null) ? "null" : exception.getClass(), type.name());
    }
}
