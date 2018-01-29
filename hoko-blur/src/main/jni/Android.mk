LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := hoko_blur
LOCAL_C_INCLUDES := include
LOCAL_SRC_FILES := BoxBlurFilter.cpp StackBlurFilter.cpp GaussianBlurFilter.cpp BlurDrawable.cpp DrawFunctor.cpp BlurUtil.cpp scope_jenv.cpp
LOCAL_LDLIBS := -llog -lm -ljnigraphics
include $(BUILD_SHARED_LIBRARY)