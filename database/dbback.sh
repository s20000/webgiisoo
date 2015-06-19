#!/bin/bash
PSQL="psql -d doogoo -A -t"
TABLES=$($PSQL -c "select tablename from pg_tables where tablename like 'tbl%'")
for table in $TABLES; do
 echo "backing up $table"
# echo -n > /home/postgres/db/$table
 $PSQL -c "COPY $table TO '/var/lib/pgsql/doogooback/$table'"
done

exit 
