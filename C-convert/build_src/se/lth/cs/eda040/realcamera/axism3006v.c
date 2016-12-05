#include <gc.h>
#include <gci_common.h>
#include <exception.h>
#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <time.h>
#include <sys/sysinfo.h>
#include <javatypes.h>
#include <sys/time.h>
#include "capture.h" 

#define TEN_TO_NINE 1000000000
#define TEN_TO_SIX 1000000
#define TIMESTAMP_POS 25
#define TIME_ARRAY_SIZE 8

/*
 * This is the c wrapper for the hardware interface to the Axis camera M3006V and is closely tied to
 * the java class with the same name. It can open a stream, fetch images from it and close the stream.
 * It assumes that a char is 8 bit and long long is 64 bit. The captures pictures have a timestamp that
 * works well for internal use but we wish to use these pictures clienstide so the timestamp is estimated.
 */

static struct media_stream *stream;
static char cap_time[TIME_ARRAY_SIZE];
/*
 * boolean nativeConnect()
 * Opens the stream and initializes the stream variable. 
 * return true if success or false if not.
 */
GC_VAR_FUNC_BEGIN(JBoolean, se_lth_cs_eda040_realcamera_AxisM3006V_nativeConnect , GC_PARAM(se_lth_cs_eda040_realcamera_AxisM3006V, this))
GC_PARAM_REF(se_lth_cs_eda040_realcamera_AxisM3006V, this);
GC_PUSH_PARAM(this);
GC_FUNC_ENTER
{	
  stream = capture_open_stream(IMAGE_JPEG, "fps=25&resolution=640x480");
  if (!stream){ // Check if null
    printf("axism3006v: Stream is null, can't connect to camera");
    GC_RETURN_VAR(false);
  } else{
    GC_RETURN_VAR(true);
  }
	
	
}
GC_FUNC_LEAVE
GC_POP_PARAM(this);
GC_VAR_FUNC_END(JBoolean,se_lth_cs_eda040_realcamera_AxisM3006V_nativeConnect)


/* int nativeGetJPEG(byte[] target, int offset) 
 * Reads an image from the stream and puts it in the supplied byte array starting at offset.
 *
 * It also modifies the timestamp of the image because it is from CLOCK_MONOTONIC, which can only
 * be used internally in the camera and we wish to read time client side.
 * The capture time since the epoch (CLOCK_REALTIME) is estimated by subtracting the original image
 * timestamp from the current time from the same clock (CLOCK_MONOTONIC) and then subtracting that 
 * from the current time since the epoch (CLOCK_REALTIME). The old timestamp is then overwritten 
 * with the new one.
 * return image size, zero if no image was captured
 */
GC_VAR_FUNC_BEGIN(JInt,se_lth_cs_eda040_realcamera_AxisM3006V_nativeGetJPEG_byteA_int, GC_PARAM(se_lth_cs_eda040_realcamera_AxisM3006V, this), GC_PARAM(JByteArray, target), 	JInt offset)
GC_PARAM_REF(se_lth_cs_eda040_realcamera_AxisM3006V, this);
GC_PARAM_REF(JByteArray, target);
GC_PUSH_PARAM(this);
GC_PUSH_PARAM(target);
GC_FUNC_ENTER
{	

  /* &GC_POINTER creates a pointer to target, the data member of the JByteArray is then accessed and
     target_p is now pointing to where we want to insert JPEG data. */
  char *target_p = (char *) &GC___PTR(target->ref)->data[offset]; 
	
  struct media_frame *frame = NULL;
  frame = capture_get_frame(stream); 

  size_t image_size;
  if(frame){
    char *data_p = (char *) capture_frame_data(frame);
    image_size = capture_frame_size(frame);
    memcpy(target_p, data_p, image_size); // Copies all the bytes (all data) into target
    capture_time timestamp = capture_frame_timestamp(frame);
    struct timespec real_current_time;
    clock_gettime(CLOCK_REALTIME, &real_current_time); // get realtime clock 
    struct timespec mono_current_time;
    clock_gettime(CLOCK_MONOTONIC, &mono_current_time); // get  monotic clock 
    unsigned long long conv_mono= ((unsigned long long) mono_current_time.tv_sec) * TEN_TO_NINE + mono_current_time.tv_nsec; // Convert to ns
    unsigned long long conv_real = ((unsigned long long) real_current_time.tv_sec) * TEN_TO_NINE + real_current_time.tv_nsec; // Convert to ns
    unsigned long long real_timestamp = conv_real - (conv_mono - timestamp); // Calculate diff, real_timestamp is in ns since epoch
    long long real_ms = real_timestamp/TEN_TO_SIX;
    int index = 0;
    cap_time[index++] = (unsigned char) ((real_ms & 0xff00000000000000LL)>>56);
    cap_time[index++] = (unsigned char) ((real_ms & 0x00ff000000000000LL)>>48);
    cap_time[index++] = (unsigned char) ((real_ms & 0x0000ff0000000000LL)>>40);
    cap_time[index++] = (unsigned char) ((real_ms & 0x000000ff00000000LL)>>32);
    cap_time[index++] = (unsigned char) ((real_ms & 0x00000000ff000000LL)>>24);
    cap_time[index++] = (unsigned char) ((real_ms & 0x0000000000ff0000LL)>>16);
    cap_time[index++] = (unsigned char) ((real_ms & 0x000000000000ff00LL)>>8);
    cap_time[index++] = (unsigned char) ((real_ms & 0x00000000000000ffLL));

    /* DEBUG	
       printf("Size %u \n",image_size); 
       time_t printSeconds = real_timestamp/TEN_TO_NINE; 
       char buff[20]; 
       struct tm * timeinfo;
       timeinfo = localtime(&printSeconds); 
       strftime(buff, 20, "%F %H:%M:%S", timeinfo); 
       printf("Timestamp realtime: %s, %d \n",buff, (conv_mono - timestamp));
    */
		
	
  }else{
    image_size = 0;
  }
  capture_frame_free(frame);
  GC_RETURN_VAR(image_size);
}
GC_FUNC_LEAVE
GC_POP_PARAM(target);
GC_POP_PARAM(this);
GC_VAR_FUNC_END(JInt,se_lth_cs_eda040_realcamera_AxisM3006V_nativeGetJPEG_byteA_int)


/** void getTime(byte[] target, int offset)
 * Puts the system time in the specified target byte array, starting at offset.
 * The resolution is milliseconds. Returns -1 if length of target is too short. 
 * 
 * @param target the array to be written into
 * @param offset the starting position
 */
	
GC_PROC_BEGIN(se_lth_cs_eda040_realcamera_AxisM3006V_nativeGetTime_byteA_int, GC_PARAM(se_lth_cs_eda040_realcamera_AxisM3006V, this), GC_PARAM(JByteArray, target), 	JInt offset)
GC_PARAM_REF(se_lth_cs_eda040_realcamera_AxisM3006V, this);
GC_PARAM_REF(JByteArray, target);
GC_PUSH_PARAM(this);
GC_PUSH_PARAM(target);
GC_FUNC_ENTER
{	
  char *target_p = (char *) &GC___PTR(target->ref)->data[offset];
  memcpy(target_p, cap_time, TIME_ARRAY_SIZE);
}
GC_FUNC_LEAVE
GC_POP_PARAM(target);
GC_POP_PARAM(this);
GC_PROC_END(se_lth_cs_eda040_realcamera_AxisM3006V_nativeGetTime_byteA_int)
/* void nativeClose()
 * Closes the stream.
 */ 
GC_PROC_BEGIN(se_lth_cs_eda040_realcamera_AxisM3006V_nativeClose, GC_PARAM(se_lth_cs_eda040_realcamera_AxisM3006V, this))
GC_PARAM_REF(se_lth_cs_eda040_realcamera_AxisM3006V, this);
GC_PUSH_PARAM(this);
GC_FUNC_ENTER
capture_close_stream(stream);
GC_FUNC_LEAVE
GC_POP_PARAM(this);
GC_PROC_END(se_lth_cs_eda040_realcamera_AxisM3006V_nativeClose)

