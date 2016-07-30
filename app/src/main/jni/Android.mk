LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := boxblur
LOCAL_SRC_FILES := BoxBlur.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)