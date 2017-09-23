# Gateway

## Notes

- Queues are of the form `service-intake` and `service-backend`. This assumption is used to provide functionality.  

## Configuration
Environment variables:
```Bash
REDIS_HOST="redis://redis:6379"
# ms poll period
POLL_DELAY="50"
REDIS_PASS="your password here"
```