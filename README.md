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

by this command `mvn clean intall`

then you can use this lib with maven

```xml
<dependency>
    <groupId>com.github.alorma</groupId>
    <artifactId>gitskarios-core</artifactId>
    <version>1.0.0</version>
</dependency>
```
