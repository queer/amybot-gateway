package chat.amy;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author amy
 * @since 9/17/17.
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public final class Gateway {
    @Getter
    private final Logger logger = LoggerFactory.getLogger("amybot-gateway");
    
    @Getter
    private final List<QueueProcessor> processors = new ArrayList<>();
    
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
        logger.info("Preparing initial intake queue consumers...");
        final int size = 4;
        for(int i = 0; i < size; i++) {
            processors.add(new QueueProcessor(this, "discord-intake", i + 1));
        }
        logger.info("Spawned " + size + " workers.");
    }
}
