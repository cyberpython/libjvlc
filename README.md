# libjvlc

## Description
libjvlc is a library providing Java bindings for a subset of the `libvlc` API.

It also provides `io.github.cyberpython.swing.VlcVideoView` which is a Swing 
component that wraps an AWT Canvas component and the libvlc instance which renders
on it.

**WARNING:** Since AWT components are heavyweight a lot of Swing goodies are not
available, such as transcluency etc. The use of an AWT component is mandated 
since libvlc requires a native window handle / ID to render the video on.

The library has been minimally tested and used and currently covers basic 
playback requirements (load, play, pause, stop a video, since this is all I 
currently need). Feel free to fork and expand on it.

Has been verified to work with VLC 2.2.2 on Ubuntu 16.04 and VLC 3.0.12 on 
Windows 10 Pro.

## How to use

In your build dependencies include a dependency with the following attributes:

```
group: io.github.cyberpython
artifact: libjvlc
version: 0.0.2
```

e.g. in your `build.gradle` file under `dependencies` include:

```
  implementation group: 'io.github.cyberpython', name: 'libjvlc', version: '0.0.2'
```

## How to build

In order to build the bindings you need JDK 8 or newer.

Clone the VLC source code in a directory named `vlc` on the same level with the 
`libjvlc` root directory.

Go into the `vlc` directory and checkout the tag / branch you wish to build the
bindings for.

Go into the `libjvlc` directory and execute:

```
./gradlew build
```

This should produce the bindings using JNAerator and then modify them to work 
with JNA 5.x (no JNAerator runtime required). The resulting JAR file should
be available in `build/libs`.

## Examples
A small example (with some known - and possibly a number of unknown - issues 
such as not distinguishing properly between double and single clicks) can be
found in: `src/main/java/io/github/cyberpython/swing/MediaPlayer.java`.

You can run it after building the library with (you need to download the
dependencies, i.e. Apache commons-lang3 and jna JARs into the same directory):

```
java -cp libjvlc.jar:commons-lang3-3.12.0.jar:jna-5.8.0.jar io.github.cyberpython.libjvlc.swing.MediaPlayer <MEDIA_URI>
```

where <MEDIA_URI> is the URI for the media file / stream you want to play.

## License
libjvlc is licensed under the terms of GNU LGPL v2.1 to match the license of 
libvlc.

The text of the license can be found in `LICENSE`.
