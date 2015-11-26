#!/bin/bash
PSQL="sudo -u postgres psql -d prod -A -t"
TABLES=$($PSQL -c "select tablename from pg_tables where tablename like 'tbl%'")
for table in $TABLES; do
 echo "backing up $table"
# echo -n > /tmp/db/$table
 $PSQL -c "COPY $table TO '/tmp/db/$table'"
done

exit 
