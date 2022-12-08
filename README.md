# inappsell
In App Purchase Custom Library

```gradle 
Settings.gradle file

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { url "https://jitpack.io" } // add this
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io" } // add this
    }
}
rootProject.name = "inapppurchase"
include ':app'

  
  ```
  
  <br>
  
```gradle 

dependencies {
	implementation 'com.github.wdtheprovider:inappsell:1.0.2'
    }
  
  ```
  
