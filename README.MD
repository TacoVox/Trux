# Trux - [Documentation Here](https://github.com/TacoVox/Trux/tree/master/Docs)
Our Project - We will rock this course.

* Jenkins [Trux App](http://derkahler.de:8080/job/Trux/) // [Trux Server](http://derkahler.de:8080/job/TruxServer/)
* SonarQube [Trux Server](http://www.derkahler.de:9000/dashboard/index/1)

##Build - Status on devel
* Trux App <img src="http://derkahler.de:8080/job/Trux/badge/icon">
* TruxServer <img src="http://derkahler.de:8080/job/TruxServer/1/badge/icon">

## TODO - Client
* Serverconnector should ignore null objects OR DataPoller should not send them. We need to know if connection to AGA is interrupted. Worst-case solution just let DataPoller check if current set of metrics is identical to last set, then don't send it.
* Fragments for speed, distance, fuel
* A way to determine if moving (display simple interface) or not
* Client should reconnect if for example server is restarted or connection temporarily interrupted

## TODO - Server
* Main server thread pool
* Different tables for different metrics since for example distance is long, not double

## Short description how this repository works.
We are working in three different main branches:
* Master --> NO CHANGES IN THIS BRANCH
* Release --> We will merge down in this just as soon as we will release a version (for example BETA)
* Devel --> This is our main development branch. This branch is there to merge down each features - it will help us to synchronize the work.
* Feature --> For each feature we need to create a new feature. After the work is done we need to merge this branch down to devel.

To work with a lot of branches we need to have a "branching discipline":
* Before starting to work on a feature run <b>git checkout devel</b> to go to the devel branch.
* Fetch the current devel branch with <b>git pull origin devel</b>
* After this is done merge out with your new feature branch: <b>git branch YOUR_BRANCHNAME</b>
* Go to that branch and make your changes: <b>git checkout YOUR_BRANCHNAME</b>
* Initial commit to our server with: <b>git push origin YOUR_BRANCHNAME</b>
* Do your usual stuff: <b>git add FILES</b> and <b>git commit -m "YOUR MESSAGE"</b>
* Finally push everything to the server: <b>git push origin YOUR_BRANCHNAME</b>
* <b>After you have done your work</b> you want to merge down to devel. Go down to devel: <b>git checkout devel</b>
* Then it's time for your first merge. Be careful and propably talk to the others... Avoid working on the same files
* <b>git merge YOUR_BRANCHNAME</b> will merge devel with your branch.
* <b>git push origin devel</b> to finish the successful working day.

If you want to work on that feature the next day you need to do one more step: <b>git branch -d YOUR_BRANCHNAME</b>
After that continue with the steps described above.

<br>Cheers. Jonas :)

<img src="http://fc01.deviantart.net/fs71/f/2011/060/3/a/tux_in_android_robot_costume_2_by_whidden-d3aq9k0.png" alt="Tux hiding in an Android costume"><br>
