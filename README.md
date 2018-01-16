# JDA-Generators

JDA-Generators is a group of projects geared towards auto-generation of code for
the popular Discord Java API Wrapper: [JDA](https://github.com/Dv8FromTheWorld/JDA).

## Work in Progress

The projects in this repository are currently all a work in progress.

## Projects

### Auto-Listener

Highly simplified and intuitive annotation processor that automatically generates
`EventListener` implementations.

## Download

Downloads are hosted on the [bintray repo](https://bintray.com/kaidangustave/maven/JDA-Auto).

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'me.kgustave:jda-auto-{MODULE_NAME}:{VERSION}'
}
```

```xml
<repository>
  <id>central</id>
  <name>bintray</name>
  <url>http://jcenter.bintray.com</url>
</repository>
```

```xml
<dependency>
  <groupId>me.kgustave</groupId>
  <artifactId>jda-auto-{MODULE_NAME}</artifactId>
  <version>{VERSION}</version>
  <type>pom</type>
</dependency>
```
