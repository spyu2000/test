#! /bin/sh
pid_file='mainserver.pid'
start()
{
        echo $"Starting main server ......"
        java  -Xms50m -Xmx512m -jar serverboot.jar testflag > /dev/null &
        echo $! > $pid_file

        echo "start ### `date`" >> mainserver.log
        echo "`ulimit -an`"  >> mainserver.log
                echo $"main server started!"
}

stop()
{
        echo $"Stopping main server ......"
        pid=`cat $pid_file`
        kill -9 $pid
        echo "stop "$pid
         mv log/day.log log/day.log.bak_`date +%m%d%H%M`
        sleep 1
}

restart()
{
        stop
        sleep 5
        start
}

case "$1" in
start)
        start
        ;;
stop)
        stop
        ;;
restart)
        restart
        ;;
*)
        echo $"Usage: $0 {start|stop|restart}"
        exit 1
esac
