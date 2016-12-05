###########################################################################
## This is a Makefile used to create the files Makefile.am               ##
## and configure.in in the individual test case directories.             ##
##                                                                       ##
## Basically, it inserts the correct name of the main source             ##
## file (which is also assumed to be the name of the program             ##
## being built and configures auxilliary native code.                    ##
##                                                                       ##
##         edit Makefile:                                                ##
##            1. set MAIN_NAME to the name of your main java file        ##
## (optional) 2. enter the rest of your java files in SOURCE_DEPENDS     ##
##            3. If you have any extra native source, set AUX_NATIVE_SRC ##
##       (ok) 4. Verify that tmplate_dir is correct                      ##
##         make                                                          ##
##                                                                       ##
###########################################################################


###########################################################################
## Change this to the name of the main file of your project           #####
###########################################################################

MAIN_NAME=Main


###########################################################################
## Source dependencies required for correct rebuild                      ##
###########################################################################

SOURCE_DEPENDS=se/lth/cs/eda040/realcamera/AxisM3006V.java


###########################################################################
## Any extra native source files required for the project                ##
###########################################################################

AUX_NATIVE_SRC=se/lth/cs/eda040/realcamera/axism3006v.c



###########################################################################
## The path to {Makefile.am,configure.in}.skel  (should be OK)           ##
###########################################################################

tmplate_dir=/usr/local/cs/rtp/tools/ljrt-rev311/J2CTranslator/test


############################################################################
## No changes should be needed below this line #############################
############################################################################

include $(tmplate_dir)/make.skel.rules
