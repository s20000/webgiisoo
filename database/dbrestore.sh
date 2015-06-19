#!/bin/bash
PSQL="psql -d doogoo -A -t"
for file in /var/lib/pgsql/doogooback/tbl*; do
 table=$(basename $file)
 echo "restore $table"
 $PSQL -c "truncate table $table"
 $PSQL -c "COPY $table from '$file'"
done

exit 
