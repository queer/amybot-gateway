package chat.amy;

import chat.amy.discord.WrappedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * @author amy
 * @since 9/17/17.
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public final class Gateway {
    public static final String DISCORD_FRONTEND_QUEUE_FORMAT = "discord-shard-%s-%s";
    private static final String GATEWAY_QUEUE = Optional.of(System.getenv("GATEWAY_QUEUE")).orElse("gateway");
    private static final String DISCORD_BACKEND_QUEUE = Optional.of(System.getenv("DISCORD_BACKEND_QUEUE"))
            .orElse("discord-backend");
    @Getter
    private final Logger logger = LoggerFactory.getLogger("amybot-gateway");
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    
    private Gateway() {
    
    }
    
    public static void main(final String[] args) {
        new Gateway().start();
    }
    
    private void start() {
        logger.info("Starting gateway...");
        connectQueues();
    }
    
    public void process(final WrappedEvent event) {
        // Realistically, for now, we can just toss the event at the backend and let that have fun figuring it out
        // There's not much of a need for the gateway to be responsible for filtering the events that the backend
        // receives; so why not?
        // Note: This method won't just take in raw event bytes, as it probably isn't too much of a performance hit,
        // and it allows us to stick in any other processing we might want / need here later.
        try {
            channel.basicPublish("", DISCORD_BACKEND_QUEUE, null, new ObjectMapper().writeValueAsString(event).getBytes());
        } catch(final IOException e) {
            e.printStackTrace();
        }
    }
    
    private void connectQueues() {
        logger.info("Preparing initial RMQ consumers...");
        factory = new ConnectionFactory();
        factory.setHost(Optional.of(System.getenv("RABBITMQ_HOST")).orElse("rabbitmq"));
        try {
            // Connect
            connection = factory.newConnection();
            channel = connection.createChannel();
            
            channel.queueDeclare(GATEWAY_QUEUE, false, false, false, null);
            channel.queueDeclare(DISCORD_BACKEND_QUEUE, false, false, false, null);
            // Only consume on the gateway queue
            // TODO: Figure out proper routing
            channel.basicConsume(GATEWAY_QUEUE, true, new GatewayQueueConsumer(this, channel));
            
            // Attempt to cleanly shut down
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    channel.close();
                    connection.close();
                } catch(final IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }));
            logger.info("Created consumers!");
        } catch(final IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
