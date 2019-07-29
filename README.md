fluentd-standalone [![Build Status](https://travis-ci.org/xerial/fluentd-standalone.svg?branch=develop)](https://travis-ci.org/xerial/fluentd-standalone) ![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.xerial/fluentd-standalone_2.12/badge.svg) [![Scaladoc](http://javadoc-badge.appspot.com/org.xerial/fluentd-standalone_2.12.svg?label=scaladoc)](http://javadoc-badge.appspot.com/org.xerial/fluentd-standalone_2.12)
=========

Standalone fluentd (http://fluentd.org) server for Java and Scala.

This library launches a new fluentd server using a random port when fluentd is not started in the node. 

Use cases:
  * Running test codes that use fluentd (logger) in a build server (e.g., Travis CI, Jenkins, etc.), in which fluentd is not running.
  

## Usage (Java/Scala)

fluentd depends on some ruby modules. You need to install these dependencies first.

The simplest way is just install fluentd.
```
$ gem install fluentd -v 1.6.2
```

See also http://docs.fluentd.org/articles/quickstart.

### Maven
```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.xerial</groupId>
    <artifactId>fluentd-standalone_2.12</artifactId>
    <version>1.2.6</version>
  </dependency>
  ...
</dependencies>
```

### sbt
```
libraryDependencies += "org.xerial" %% "fluentd-standalone" % "1.6.2"
```

## Sample code

```java
import xerial.fluentd.FluentdStandalone;

FluentdStandalone s = new FluentdStandalone();
// Start new fluentd server
s.start();

// Send log messsages to fluentd
int port = s.port(); // fluentd port

// Terminate the fluentd
s.stop();
```


## For developers

```
$ git clone https://github.com/xerial/fluentd-standalone.git
$ cd fluentd-standalone
$ git submodule init          # Only for the first time
$ git submodule update        # Fetch fluentd 

# Run tests
$ ./sbt test 
```
