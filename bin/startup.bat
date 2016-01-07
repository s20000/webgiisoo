@echo off

java -jar -Xmx512M -Xms128M -XX:-UseParallelGC -Dfile.encoding=utf-8 -Duser.language=en -Duser.country=US -Dtcpnodelay bootstrap_1.0.1.jar com.giisoo.startup.Startup start
