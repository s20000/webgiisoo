#!/bin/sh

if [ -f "/opt/data/db/mongod.lock" ]
then
	rm -f /opt/data/db/mongod.lock
#	/opt/mongo/bin/mongod --dbpath /opt/data/db --repair
#	/opt/mongo/bin/mongod --dbpath /opt/data/db --repair --replSet doogoo --oplogSize=4000
fi

/opt/mongo/bin/mongod --dbpath /opt/data/db --port 27018 --bind_ip 0.0.0.0 &
