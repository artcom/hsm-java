# Hierarchical Statemachine for Java

[![Build Status](https://img.shields.io/travis/artcom/hsm-java/master.svg?style=flat)](https://travis-ci.org/artcom/hsm-java)

This Project is a hierarchical state machine framework for Java.
The state machine specification is based on the [UML statemachine](http://en.wikipedia.org/wiki/UML_state_machine).
The implementation is based on this project: <https://github.com/Mask/hsm-js>.

### Published artifacts

The artifacts of this project are intended to be received via [Jitpack.io](https://jitpack.io/)

Gradle dependency example:
<pre>
compile 'com.github.artcom:hsm-java:0.0.3'
</pre>

### Publish to Maven Local

In order to publish to the local Maven repository please use this command:
<pre>
./gradlew -Pgroup=com.github.artcom publishToMavenLocal
</pre>
It is necessary to provide the same groupId as used by jitpack in order to be able
to force the usage of the version in the local maven repository.

### License

Copyright &copy; 2015 [Art+Com AG](http://www.artcom.de/).
Distributed under the MIT License.
Please see [License File](LICENSE) for more information.
