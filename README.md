# Gateway

## TODO List

- Event filtering
- Better event buffering (multi-queue?)
- Shard (re)connect and ID management

## Notes

- Queues are of the form `service-intake` and `service-backend`. This assumption is used to provide functionality.  

## Configuration
Environment variables:
```Bash
REDIS_HOST="redis://redis:6379"
REDIS_PASS="your password here"
```