![Trux](../master/Docs/images/image00.png "Trux")<br>

## Group members
* Ersare, Jerker - <jerker@soundandvision.se> - tville
* Ghezai, Aman - <gusghezam@student.gu.se> - amuhotmax
* Kahler, Jonas - <jonas.kahler@icloud.com> - TacoVox
* Karlberg, Dennis - <realdkk@gmail.com> - DKarlberg
* le Comte, Niklas - <niklas.lecomte@hotmail.com> - salkin91
* Vryashkov, Ivo - <ivo.vryashkov@gmail.com> - vryashkov

##Last Week Changes (After final presentation)
### Map
* Scroling on the map is not possible while distracted
* Profile button disappears while distracted
* Toasts for telling the user if he follows himself/friends and how to quit, and when the user has clicked to stop following.

### General system changes
* Push notifications added for messages and new friends
* Application handles the android garbage collection better and doesn't crash after being in the background for a while

## General information
This repository was created after the alpha presentation bacause we had problems with .gitignore and for that reason we dont have any commits before the 24th of march in this repo. All the things we had were added to this repo the 24th of march or later.

## Download
* [Latest Stable - v1.0](../master/Trux/app/app-Trux.apk)

### A Sample User for Trux
* Username: alfons
* Password: 1234

## [Documentation](../master/Docs)
* [Trux - Report](../master/Docs/TruxReport.MD)
* [Trux - Backlog](../master/Docs/Backlog.xlsx) (Not 100% accurate but provided for reference)
* [Trux - Developer's Manual](../master/Docs/DeveloperManual.MD)
* [Trux - Client/User Manual](../master/Docs/UserManual.MD)
* [Trux - JavaDoc](http://derkahler.de/trux/appjdoc/)
* [TruxServer - Setup Manual](../master/Docs/ServerManual.MD)
* [TruxServer - JavaDoc](http://derkahler.de/trux/srvjdoc/)
* [Semester 2 - General Documentation + Files](https://github.com/TacoVox/Tux)

## Automatic Build + Technical Debt
* Jenkins [Trux App](http://derkahler.de:8080/job/Trux/) // [Trux Server](http://derkahler.de:8080/job/TruxServer/)
* SonarQube [Trux Server](http://www.derkahler.de:9000/dashboard/index/1)

## Short description how this repository works.
We are working in three different main branches:
* Master --> NO CHANGES IN THIS BRANCH (Edit: only when handing in...)
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
