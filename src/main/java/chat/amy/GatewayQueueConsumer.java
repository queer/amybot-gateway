package chat.amy;

import chat.amy.discord.WrappedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

/**
 * @author amy
 * @since 9/17/17.
 */
@SuppressWarnings("WeakerAccess")
public class GatewayQueueConsumer extends DefaultConsumer {
    private final Gateway gateway;
    
    public GatewayQueueConsumer(final Gateway gateway, final Channel channel) {
        super(channel);
        this.gateway = gateway;
    }
    
    @Override
    public void handleDelivery(final String consumerTag, final Envelope envelope, final AMQP.BasicProperties properties, final byte[] body) throws IOException {
        final String message = new String(body);
        final ObjectMapper mapper = new ObjectMapper();
        // TODO: This is not necessarily the right thing to do...
        final WrappedEvent event = mapper.readValue(message, WrappedEvent.class);
        // Declare new shard queue if it doesn't already exist
        getChannel().queueDeclare(String.format(Gateway.DISCORD_FRONTEND_QUEUE_FORMAT, event.getShard(), event.getLimit()),
                false, false, false, null);
        // Process event
        gateway.process(event);
    }
}
