#!/bin/bash

# Script for building the CameraServer program. Based on build_camera.sh for axis camera cross compiling by Jesper Öqvist.
# Author: Niklas Jonsson 2014

# Set this to the path of ljrt directory.
export J2C_HOME=/usr/local/cs/rtp/tools/ljrt-rev311

# Set this to the path of the compiler directory. 
COMPILER_DIR=/usr/local/cs/rtp/tools/comptools-mips-r12_1.2-0/comptools-mips-r12_1.2-0_amd64/mipsisa32r2el/r12

# Absolute path to the Axis Software Development Kit containing camera specific libs and headers, used for capture.h and its corresponding object file (i.e. libcapture)
AXIS_PATH=/usr/local/cs/rtp/tools/emb-app-sdk_1_4/target/mipsisa32r2el-axis-linux-gnu/usr

# Set PATH to ensure J2C finds the right compiler. 
export PATH=""$COMPILER_DIR"/bin:"$PATH"" 
# Set to ensure compiler can find platform specific standard libs
export LIBRARY_PATH=""$COMPILER_DIR"/lib:"$LIBRARY_PATH""

# Inform compiler of this lib path as well
export LIBRARY_PATH=""$AXIS_PATH"/lib:"$LIBRARY_PATH""

# Tell the compiler where to look for header files used in the auxilliary native methods for the camera classes
export CPPFLAGS=" -I"$AXIS_PATH"/include "$CPPFLAGS""
AXIS_LIBCAP="-lcapture" # This is needed for the native camera classes to fetch images
AXIS_LIBCAP_DEP="-ldbus-1 -lgobject-2.0 -ldbus-glib-1 -lgthread-2.0 -lglib-2.0 -lrapp" # Dependencies of libcapture
export LDFLAGS=" "$AXIS_LIBCAP_DEP" "$AXIS_LIBCAP" -L"$AXIS_PATH"/lib "$LDFLAGS"" # Specify which libs are used and set search path for them

HOST_NAME="mipsisa32r2el-axis-linux-gnu" 

# --------------------------------------------- 	
BIN_DIR="build_bin"
SRC_DIR="build_src"

echo "Setting up environment."
#Clearing old build
if [ -d "$BIN_DIR" ]; then
	echo "Clearing old build." 1>&2
	rm -r "$BIN_DIR"

fi
mkdir "$BIN_DIR"
#Copying source files and make file
cp -a "$SRC_DIR"/. "$BIN_DIR"/
cd  "$BIN_DIR"
echo "Starting build, first Makefile"
if ! (make) 1> build.log 2> build.err; then
	echo "Build failed; look in "$BIN_DIR"/build.err and "$BIN_DIR"/build.log" 1>&2
	exit 1;
fi
mkdir build
cd build
echo "Configuring with: --with-thread=pthread --with-gc=nonmoving --host="$HOST_NAME""
if ! (../configure --with-thread=pthread --with-gc=nonmoving --host=$HOST_NAME) 1>> build.log 2>> build.err; then
	echo "Build failed; look in "$BIN_DIR"/build/build.err and "$BIN_DIR"/build/build.log" 1>&2
	exit 1;
fi
# Compiling
echo "Cross compiling, second Makefile"
if ! (make) 1>> build.log 2>> build.err; then
	echo "Build failed; look in build.err and build.log" 1>&2
	exit 1;
fi
echo "Build done! Binary is in "$BIN_DIR"/build/"
echo "Exiting"
