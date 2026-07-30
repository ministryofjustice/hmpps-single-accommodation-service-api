# Remote Database Tunnel

Use `remote-db-tunnel.sh` to create a local port-forward to a remote SAS database in Cloud Platform.

## Prerequisites

- Access to the MoJ Cloud Platform Kubernetes cluster.
- `kubectl` configured for `live.cloud-platform.service.justice.gov.uk`.
- `jq` installed locally.
- A unique port-forward pod name for your user.

Set a unique pod name once in your shell profile, for example:

```shell
export CAS_PORT_FORWARD_CONTAINER_NAME=port-forward-pod-yourinitials
```

The script prefixes this with `data-domain-`, so the final pod name will be unique to your tunnel session.


## Run Script

Start a tunnel to one of `dev`, `test`, `preprod`, or `prod`:

```shell
./scripts/remote-db-tunnel.sh -d dev
```

Use the environment you need:

```shell
./scripts/remote-db-tunnel.sh -d test
./scripts/remote-db-tunnel.sh -d preprod
./scripts/remote-db-tunnel.sh -d prod
```

The `-d` option prints the database connection details, including credentials. Keep the terminal open while you are connected. Press `Ctrl+C` when finished; the script will stop port-forwarding and delete the temporary pod.

If you need a custom local port:

```shell
./scripts/remote-db-tunnel.sh -d -p 5433 dev
```

## Connect To The Database

When the script is running, connect from your database client using the details printed by the script:

- Host: `localhost`
- Port: `5432`, unless you used `-p`
- Database: value printed as `Database Name`
- Username: value printed as `User`
- Password: value printed as `Password`

Only connect to `prod` when you have a clear operational need, and avoid changing data unless the change has been approved.