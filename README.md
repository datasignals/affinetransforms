# affinetransforms
contains affine transform approach to secret sharing

#### To Run:
``` shell
sbt -J--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED -Djava.library.path=/Users/og_pixel/workspace/transforms/blumamba-splitter-native/src/native/build
```
Mind you, you have to change path for your native library
Also in few locations there are hardcoded locations (mostly keystore)
