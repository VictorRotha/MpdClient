package de.nif.data;

import java.io.IOException;
import java.util.List;

public class AckException extends IOException {

    public static final String ACK_POSTFIX_PLAYLIST_ALREADY_EXISTS = "Playlist already exists";

    private final List<String> result;

    public AckException(String message) {
        this(message, null);
    }

    public AckException(String message, List<String> result) {
        super(message);
        this.result = result;
    }

    public List<String> getResult() {
        return result;
    }
}
