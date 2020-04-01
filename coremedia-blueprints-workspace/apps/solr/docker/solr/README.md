Solr Master Slave Replication
=============================

To enable Solr master / slave replication, all you need to do is add
another solr container service and set some environment variables.

* `SOLR_MASTER` - set this to `false` on the slave
* `SOLR_SLAVE` - set this to `true` on the slave
* `SOLR_MASTER_URL` - set this to the solr master url or keep it empty if
  started with the default docker slave setup.
* `SOLR_SLAVE_AUTOCREATE_CORES` - set this to `true` to automatically
  create the cores for the slave.
* `SOLR_SLAVE_AUTOCREATE_CORES_LIST` - set this to a space separated list of core names to control
  which cores should be replicated by this slave
* `SOLR_SLAVE_AUTOCREATE_THRESHOLD` - set this to a numerical value to wait for at least that number of cores
  to be available before creating all found cores and start solr. This is only
  effective if `SOLR_SLAVE_AUTOCREATE_CORES_LIST` is empty.

In the docker-compose setup simply add the following service definition:

```yaml
  solr-slave:
    image: ${REPOSITORY_PREFIX:-coremedia}/solr
    networks:
      - backend
    restart: always
    logging:
      driver: json-file
      options:
        max-size: "5m"
        max-file: "3"
    environment:
      SOLR_MASTER: "false"
      SOLR_SLAVE: "true"
      SOLR_SLAVE_AUTOCREATE_CORES: "true"
      SOLR_SLAVE_AUTOCREATE_CORES_LIST: "preview studio"
```

Now all you need is to configure the slave in your consuming services:
* `cae-preview`
* `cae-live`
* `studio-server`

You can achieve this by overwriting the corresponding systemproperties
using environment variables, i.e.:

```yaml
  cae-preview:
    environment:
      ELASTIC_SOLR_URL: http://solr-slave:8983/solr
      SOLR_URL: http://solr-slave:8983/solr
```

Scaling the solr slaves in docker-compose
-----------------------------------------

If you use docker-compose or docker-swarm, there is a poor mans load balancing
built in by default. Since Docker 1.11, you can define network aliases and
multiple services can share the same alias. If that is the case there will
be a round robin dispatching from the alias to one of the containers with that alias.
To configure this, simply add a network alias in the `solr-slave` service.

In the `solr-slave` definition add the following labels:
```yaml
  solr-slave:
    networks:
      backend:
        aliases:
          - solr-slave
```

Then you can start up the compose stack with the scale option, i.e.:

`docker-compose up --scale solr-slave=3 -d` to scale three solr-slaves.
