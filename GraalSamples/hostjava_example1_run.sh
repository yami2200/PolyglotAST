#!/usr/bin/env bash
set -ex
"$GRAALVM_HOME"/bin/javac hostjava_example1.java
"$GRAALVM_HOME"/bin/java HostJavaExample1