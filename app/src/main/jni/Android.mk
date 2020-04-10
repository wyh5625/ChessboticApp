LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := chess-jni
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_SRC_FILES := \
	chess-jni.cpp \
	user_interface.cpp\
	game_control.cpp\
	chess.cpp

LOCAL_CPP_FEATURES += exceptions

LOCAL_C_INCLUDES += .
LOCAL_C_INCLUDES += C:\Users\wyh56\AndroidStudioProjects\android-fundamentals-starter-apps-v2-master\StartBLE\app\src\debug\jni

include $(BUILD_SHARED_LIBRARY)
