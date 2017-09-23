package chat.amy;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author amy
 * @since 9/17/17.
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public final class Gateway {
    @Getter
    private final Logger logger = LoggerFactory.getLogger("amybot-gateway");
    
    private Gateway() {
    }
    
    public static void main(final String[] args) {
        new Gateway().start();
    }
    
    private void start() {
        logger.info("Starting gateway...");
        connectQueues();
    }
    
    private void connectQueues() {
        final Thread intake = new Thread(new QueueProcessor(this, "discord-intake", 0));
        intake.start();
    }
}
