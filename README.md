[![Build Status](https://travis-ci.org/gitskarios/Gitskarios-core.svg)](https://travis-ci.org/gitskarios/Gitskarios-core)

[ ![Download](https://api.bintray.com/packages/alorma/maven/gitskarios-core/images/download.svg) ](https://bintray.com/alorma/maven/gitskarios-core/_latestVersion)


# How to Use

### add the android-sdk into you own maven repository

Download the latest Android SDK

Apache Maven 3.1.1 or higher is required!


```shell
git clone git@github.com:wsdjeg/maven-android-sdk-deployer.git
cd maven-android-sdk-deployer
mvn install
```
or you can use mvn install -P 4.1(5.0 e.g.)

As a result you should find the android.jar and maps.jar and a number of other libraries in your users local repository (~/.m2/repository/) and you can therefore use the following dependencies in your project

```xml
<dependency>
    <groupId>android</groupId>
    <artifactId>android</artifactId>
    <version>x.y.z</version>
    <scope>provided</scope>
</dependency>
```

https://github.com/wsdjeg/maven-android-sdk-deployer/blob/master/README.markdown


### Add this repository into you local repository

by this command `mvn clean intall` you will package this project into ~/.m2/repository/com/github/alorma/gitskarios-core/ . also you can find there are two package in this dir,apklib and jar,apklib contains all the necessary src res and so on,jar contains all the classes,you  need to use different scope for these two package.


``` groovy
    compile 'com.github.alorma:gitskarios-core:2.1.0'
```

for example you can use this lib by adding these two dependencies into your pom.xml

```xml
<dependency>
    <groupId>com.github.alorma</groupId>
    <artifactId>gitskarios-core</artifactId>
    <version>2.1.0</version>
    <type>apklib</type>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.github.alorma</groupId>
    <artifactId>gitskarios-core</artifactId>
    <version>2.1.0</version>
    <type>jar</type>
    <scope>provided</scope>
</dependency>
```
