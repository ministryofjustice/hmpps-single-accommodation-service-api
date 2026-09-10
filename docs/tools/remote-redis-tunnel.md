# Remote Redis Tunnel

Use `remote-redis-tunnel.sh` to create a local port-forward to remote Redis in Cloud Platform.

## Prerequisites

- Access to the MoJ Cloud Platform Kubernetes cluster.
- `kubectl` configured for `live.cloud-platform.service.justice.gov.uk`.
- `jq` installed locally.
- `redis-cli` installed locally if you want to use `-c`.

The script derives a pod name from your local username and normalizes it to a Kubernetes-safe format.

## Run Script

Start a tunnel to one of `dev`, `test`, `preprod`, or `prod`:

```shell
./scripts/remote-redis-tunnel.sh dev
```

All options must be provided before the environment argument.

Useful options:

- `-d` print Redis connection details, including auth token.
- `-c` start `redis-cli` after the tunnel is ready.
- `-s <secret>` override the Redis secret name (default: `sas-elasticache-redis`).

Examples:

```shell
./scripts/remote-redis-tunnel.sh -d test
./scripts/remote-redis-tunnel.sh -d -c preprod
./scripts/remote-redis-tunnel.sh -s sas-elasticache-redis prod
```

## Connect To Redis

When the script is running, connect with:

- Host: `127.0.0.1`
- Port: `6379`
- Auth token: value printed by `-d` or from the secret

Example command:

```shell
redis-cli -h 127.0.0.1 -p 6379 --tls -a <REDIS_AUTH_TOKEN>
```

Only connect to `prod` when you have a clear operational need.

