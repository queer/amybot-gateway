package chat.amy;

import lombok.Value;
import org.json.JSONObject;

/**
 * @author amy
 * @since 9/17/17.
 */
@Value
public class WrappedEvent {
    private final String source;
    private final String type;
    private final JSONObject data;
}
