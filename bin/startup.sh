#!/bin/sh

PRG="$0" 
while [ -h "$PRG" ] ; do
	ls=`ls -ld "$PRG"`
	link=`expr "$ls" : '.*-> \(.*\)$'`
	if expr "$link" : '/.*' > /dev/null; then
		PRG="$link"
	else
		PRG=`dirname "$PRG"`/"$link"
	fi
done

PRGDIR=`dirname "$PRG"`

export MMS_HOME=`cd "$PRGDIR/.." ; pwd`

echo MMS_HOME=$MMS_HOME

os=`uname -s`
startup=com.giisoo.startup.Startup

sys="-Xmx1g -Xms128M"
sys="$sys -XX:-UseParallelGC -verbose:gc -Xloggc:/opt/doogoo_1.0.1/logs/gc.log"
#sys="$sys -XX::CompileThreshold=1500"

sys="$sys -Duser.language=en -Duser.country=US -Dfile.encoding=utf-8"
sys="$sys -Dtcpnodelay"

cmd="java -jar $sys $MMS_HOME/bin/bootstrap_1.0.1.jar $startup"

getpid() {
	case $os in
	Linux)
		pid=`ps fux |grep java | grep $1 |grep -v grep | awk '{print $2}'` 
		;;
	SunOs)
		pid=`ps -gxww |grep java |grep $1 |grep -v grep | awk '{print $1}'`
		;;
	*)
		return 2
		;;
	esac

	return 0
}

start() {
	getpid $startup
	if [ -n "$pid" ]
	then
		echo ERROR: Appliction pid=$pid is still running
		return 1
	fi	
	echo "Starting SE ... "
	
	ulimit -n 10240

	if [ "$console" = "true" ]
	then
		exec  $cmd start
	else
		exec $cmd start >/dev/null &
	fi
	return 0
}

stop() {
	getpid $startup
	if [ -z "$pid" ]
	then
		echo WARN: Application is not running
		return 0
	fi

	exec  kill $pid

	if [ -n "$pid" ]
	then
		echo "ERROR: Can't stop the process pid=$pid"
		return 1
	fi
	return 0
}

restart() {
	stop
	rc=$?
	start
	rc=$?
	return $rc
}

case $1 in 
start)
	while [ "$1" ]
	do
		case $1 in
		-console)
			console="true"
			echo Console is enabled
			;;
		-quiet)
			log_props="log4j_quiet.properties"
			echo Quiet logging is enabled
			;;
		esac
		shift
	done

	start
	;;
stop)
	stop
	;;
restart)
	restart
	;;
*)
	echo "[Usage] $0 [start|stop|restart] -console"
	;;
esac

