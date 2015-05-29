# Manual: Trux Server v2.0 [14-05-29]

## General
The Trux Server application is command line based. That means that there is no GUI for the administrator managing it. Due to the fact that this application is written in Java it is possible to run it on every operating system capable of running a Java virtual machine (JVM/JRE). When porting a running environment to another environment it is necessary to modify the database (file paths) - changing this is on the agenda for the next server version (v3.0).

## Compilation
To compile the server it is necessary to have a working maven as well as a JDK in version 8 installation (a standard Netbeans or Eclipse installation includes this).
It is also very important to create a symlink from the TruxServer project folder to the datastructure folder in the Trux application. This is necessary because Trux and TruxServer are sharing these files as their “protocol”. A sample command for Linux and Unix machines to create the symlink (with the ln command): 
<br>`ln -s /home/jonas/Repos/Trux/Trux/app/src/main/java/se/gu/tux/trux/datastructure/ /home/jonas/Repos/Trux/TruxServer/src/main/java/se/gu/tux/trux/datastructure`
<br>After performing a clean build you will have most likely two executable .jar files in a `/target` directory. One without dependencies and one with them included. The latter file is the file you want to use.

## Run-time
To start the server simply run java -jar TruxServer-2-0-jar-with-dependencies.jar.
The server will start to execute and will create - if not available yet - a folder in the same directory where your executable .jar is running - `/config` - including a `server.conf` file. The application will create it with asking you questions about the database configuration, the GMail account you want to send the eMails from as well as different interval times. 
To prevent this behaviour simply take the config file provided in our GitHub repository and put it in the `/config` folder (`TruxServer/config/server.conf`).
It is even possible to run the server with some extra commands. Try to add a command line argument like -h to the execute command.
To stop the server simply type q followed by enter into the command line. The server will shut down.
For a long time server session it is necessary to install screen on your Unix server and run a TruxServer instance in a screen.
