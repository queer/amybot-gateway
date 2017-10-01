package chat.amy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 9/22/17.
 */
@SuppressWarnings("unused")
public class QueueProcessor implements Runnable {
    @SuppressWarnings("FieldCanBeLocal")
    private final Gateway gateway;
    private final String queue;
    private final String outputQueue;
    private final JedisPool redis;
    
    private final Logger logger;
    
    @SuppressWarnings("WeakerAccess")
    public QueueProcessor(final Gateway gateway, final String queue, final int idx) {
        this.gateway = gateway;
        this.queue = queue;
        outputQueue = queue.replace("intake", "backend");
        logger = LoggerFactory.getLogger("Gateway " + queue + " Processor " + idx);
        
        final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(1024);
        jedisPoolConfig.setMaxTotal(1024);
        jedisPoolConfig.setMaxWaitMillis(500);
        redis = new JedisPool(jedisPoolConfig, Optional.ofNullable(System.getenv("REDIS_HOST")).orElse("redis://redis:6379"));
    }
    
    private void cache(final Consumer<Jedis> op) {
        try(Jedis jedis = redis.getResource()) {
            jedis.auth(System.getenv("REDIS_PASS"));
            op.accept(jedis);
        }
    }
    
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while(true) {
            logger.debug("Getting next event from " + queue + "...");
            cache(jedis -> {
                // Just block forever
                final List<String> blpop = jedis.blpop(0, queue);
                // [0] is key, [1] is value
                // sanity check
                if(blpop.get(0).equalsIgnoreCase(queue)) {
                    final String event = blpop.get(1);
                    // Any processing etc. goes here
                    logger.debug("Got next event: " + event);
                    if(Objects.nonNull(event)) {
                        jedis.rpush(outputQueue, event);
                    } else {
                        logger.debug("Ignoring null queue event");
                    }
                } else {
                    logger.warn("Somehow didn't pop from " + queue + "!?");
                }
            });
        }
    }
}
