LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ImageBlur
LOCAL_C_INCLUDES := include
LOCAL_SRC_FILES := BoxBlurFilter.cpp StackBlurFilter.cpp GaussianBlurFilter.cpp
LOCAL_LDLIBS := -llog -lm
include $(BUILD_SHARED_LIBRARY)