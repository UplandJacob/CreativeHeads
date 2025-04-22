package io.github.theluca98.creativeheads.util.exception;
  
public class PlayerNotFound extends Exception {
    public PlayerNotFound() {
        super();
    }
    public PlayerNotFound(String message) {
        super(message);
    }
    public PlayerNotFound(String message, Throwable cause) {
        super(message, cause);
    }
    public PlayerNotFound(Throwable cause) {
        super(cause);
    }
}
