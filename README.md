# nuts
Network Updatable Things Services
<pre>
    _   __      __
   / | / /_  __/ /______
  /  |/ / / / / __/ ___/
 / /|  / /_/ / /_(__  )
/_/ |_/\__,_/\__/____/   version 0.3.6.0
</pre>

nuts stands for **Network Updatable Things Services** tool. It is a simple tool  for managing remote
packages, installing these  packages to the current machine and executing such  packages on need.
Each managed package  is also called a **nuts** which  is a **Network Updatable Thing Service** .
Nuts packages are  stored  into repositories. A  *repository*  may be local for  storing local Nuts
or remote for accessing  remote packages (good examples  are  remote maven  repositories). It may
also be a proxy repository so that remote packages are fetched and cached locally to save network
resources.
One manages a set of repositories called a  workspace. Managed **nuts**  (packages)  have descriptors
that depicts dependencies between them. This dependency is seamlessly handled by  **nuts**  (tool) to
resolve and download on-need dependencies over the wire.

**nuts** is a swiss army knife tool as it acts like (and supports) *maven* build tool to have an abstract
view of the the  packages dependency, like  *zypper/apt-get*  package manager tools  to  install and
uninstall packages allowing multiple versions of the very same package to  be installed, and like
*git/svn* source version tools to support package (re)-building and deploying.

## COMMON VERBS:
+ deploy,undeploy   : to handle packages (package installers) on the local repositories
+ install,uninstall : to install/uninstall a package (using its fetched/deployed installer)
+ checkout,commit   : create new versions of the packages
+ fetch,push        : download, upload to remote repositories
+ find              : searches for existing/installable packages

## Download Latest stable version

+ Linux Installer (sh)  :: [nuts.sh](https://github.com/thevpc/nuts/raw/master/nuts-bootstrap/nuts)
+ Linux Bundle    :: [nuts-bundle.tar.gz](https://github.com/thevpc/nuts/raw/master/nuts-bootstrap/nuts-bundle.tar.gz)
+ Java or any Java enabled OS : Windows,iOS, ... :: [nuts.jar](https://github.com/thevpc/nuts/raw/master/nuts-bootstrap/nuts.jar)

## Requirements
Java Runtime Environment (JRE) or Java Development Kit (JDK) version 8
## Run
### Linux
if you have the appropriate shell file "nuts", just put it under $HOME/bin or /local/bin
```bash
nuts console
```
or, just run the java jar file :
```bash
java -jar nuts.jar console
```
### Any other platform (Windows, iOS, ...)
```bash
java -jar nuts.jar console
```

### Running a local jar with external dependencies
Let's suppose that my-app.jar is a maven created jar (contains META-INF/maven files) with a number of dependencies. Nuts 
is able to download on the fly needed dependencies, detect the Main class (no need for MANIFEST.MF) and run the 
application. If more than one class have been detected with main method, nuts will ask for the current class to run.

#### Running installed nuts
Before running an application you have to install it. It will be downloaded along with all of its dependencies. Then you can call the exec command.

```bash
nuts install my-app
nuts exec my-app some-argument-of-my-app
```

Alternatively, the 'exec' command can be omitted

```bash
nuts my-app some-argument-of-my-app
```

#### Running local file
You also may run a local file, nuts will behave as if the app is installed (in the given path, an no need to invoke install command). 
Dependencies will be downloaded as well (and cached in the workspace ~/.nuts/default-workspace)

```bash
nuts my-app.jar some-argument-of-my-app
```

#### Passing VM arguments
If you need to pass JVM arguments you have to prefix them with "--nuts" so if you want to fix maximum heap size use 
-J-Xmx2G instead of -Xmx2G

```bash
nuts -J-Xms1G -J-Xmx2G my-app.jar -Janother-vm-arg=3 some-argument-of-my-app some-app-argument
```




