package chat.amy;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author amy
 * @since 9/22/17.
 */
public class QueueProcessor extends AbstractScheduledService {
    private final Gateway gateway;
    private final String queue;
    private final RedissonClient redis;
    private final Logger logger;
    
    public QueueProcessor(final Gateway gateway, final String queue, final int idx) {
        this.gateway = gateway;
        this.queue = queue;
        logger = LoggerFactory.getLogger("Gateway " + queue + " Processor " + idx);
        
        final Config config = new Config();
        config.useSingleServer().setAddress(Optional.of(System.getenv("REDIS_HOST")).orElse("redis://redis:6379"))
                // Based on my bot heavily abusing redis as it is, high connection pool size is not a terrible idea.
                // NOTE: Current live implementation uses like 500 connections in the pool, so TEST TEST TEST
                // TODO: Determine better sizing
                .setConnectionPoolSize(128);
        redis = Redisson.create(config);
    }
    
    @Override
    protected void runOneIteration() throws Exception {
        logger.debug("Getting next event from " + queue + "...");
        final RBlockingQueue<WrappedEvent> blockingQueue = redis.getBlockingQueue(queue);
        final WrappedEvent event = blockingQueue.take();
        // Any preprocessing or logging or etc. goes here
        redis.getBlockingQueue(queue.replace("intake", "backend")).add(event);
    }
    
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0, Long.parseLong(Optional.of(System.getenv("POLL_DELAY")).orElse("50")),
                TimeUnit.MILLISECONDS);
    }
}
