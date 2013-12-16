fluentd-standalone
=========

Standalone fluentd (http://fluentd.org) server for Java and Scala.

This library launches a new fluentd server using a random port when fluentd is not started in the node. 

Use cases:
  * Running test codes that use fluentd (logger) in a build server (e.g., Travis CI, Jenkins, etc.), in which fluentd is not running.
  

## Usage (Java/Scala)

fluentd depends on some ruby modules. You need to install these dependencies first.

The simplest way is just install fluentd.
```
$ gem install fluentd
```

See also http://docs.fluentd.org/articles/quickstart.

### Maven
```xml
<dependencies>
  ...
  <dependency>
    <groupId>org.xerial</groupId>
    <artifactId>fluentd-standalone</artifactId>
    <version>0.1</version>
  </dependency>
  ...
</dependencies>
```

### sbt
```
libraryDependencies += "org.xerial" % "fluentd-standalone" % "0.1"
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
$ ./sbt test -Dloglevel=debug
```


For using Travis CI (http://travis-ci.org), add the following settings to install ruby dependencies fluentd.

.travis.yml
```
before_install: gem install fluentd
```