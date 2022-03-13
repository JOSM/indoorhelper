# I want to use the plugin

If you want to use the plugin the best way to start is to download the [JOSM](https://josm.openstreetmap.de/), navigate to preferences window → plugins and install the indoorhelper.

# I want to work with the source code

### Setup a local git-repository

```
git clone git@github.com:JOSM/indoorhelper.git
cd indoorhelper
```

### Setup the development environment

For developing two IDEs are recommended: [IntelliJ IDEA](https://www.jetbrains.com/de-de/idea/) or [Eclipse](https://www.eclipse.org/downloads/)

**Setup with [IntelliJ IDEA](https://www.jetbrains.com/de-de/idea/):**

 First of all make sure the [Java SE Development Kit 8](https://www.oracle.com/de/java/technologies/javase/javase-jdk8-downloads.html) is installed on your local machine 
 1. Download IntelliJ IDEA
 2. Open IntelliJ and import the indoorhelper plugin: File → Open → `path/to/indoorhelper_folder`
 3. You may need to set the current SDK: File → Project Strucuture → Project → Project SDK
 4. You may need to set `indoorhelper/src` as source folder and `indoorhelper/test` as test folder. This settings can be found under File → Project Strucuture → Modules (see also [jetbrains#adding_content_root](https://www.jetbrains.com/help/idea/content-roots.html#adding_content_root))
 5. After editing the src or test folder (in step 3.) you may need to add the dependencies to this modules again. In Project Structure... → Modules ...select the module and select tab Dependencies → + → Library ...add missing libraries
 6. Build the plugin using gradle (thanks to [floschers gradle-josm-plugin](https://gitlab.com/floscher/gradle-josm-plugin/-/tree/master)!): Open the gradle toolbar under View → Tool Windows → gradle. Now you see already defined configurations. Use Tasks → build → build to build the plugin. Use Tasks → josm → runJosm to run the plugin with a clean JOSM instance. For more information see [ gradle-josm-plugin README.md](https://gitlab.com/floscher/gradle-josm-plugin/-/blob/master/README.md)

**Troubleshooting IntelliJ IDEA**
* Check if correct SDK is set (see above 3.)
* Check if folder `src` is set to src directory and if folder `test` is set to test directory (see above 4.)
* Check if all dependencies are added properly (see above 5.)

**Setup with [Eclipse](https://www.eclipse.org/downloads/):**

First of all make sure the [Java SE Development Kit 8](https://www.oracle.com/de/java/technologies/javase/javase-jdk8-downloads.html) is installed on your local machine 

* See [Howto checkout a JOSM plugin into Eclipse](https://www.youtube.com/watch?v=Z3OjG3nDvzA)
* See [How to checkout JOSM into Eclipse](https://www.youtube.com/watch?v=-LoWGf-hqiQ)

This tutorials use Ant to build the plugin. If you want to use gradle to build the plugin you will need to use a [plugin](https://docs.gradle.org/current/userguide/eclipse_plugin.html).


**Additional**
* For more information see [JOSM DevelopersGuide/DevelopingPlugins](https://josm.openstreetmap.de/wiki/DevelopersGuide/DevelopingPlugins)
* Videos to setup the IDE see [JOSM DevelopersGuide#IDEs](https://josm.openstreetmap.de/wiki/DevelopersGuide#IDEs)
