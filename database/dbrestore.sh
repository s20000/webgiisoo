#!/bin/bash
PSQL="sudo -u postgres psql -d demo -A -t"
for file in /tmp/db/tbl*; do
 table=$(basename $file)
 echo "restore $table"
 $PSQL -c "truncate table $table"
 $PSQL -c "COPY $table from '$file'"
done

exit 
