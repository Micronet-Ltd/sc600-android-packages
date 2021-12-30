LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

src_dirs := src
res_dirs := res

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.qualcomm.qti.modemtestmode

#$(info PLATFORM_VERSION =: $(PLATFORM_VERSION))
#ifeq ($(PLATFORM_VERSION), 9)
#    LOCAL_PRIVATE_PLATFORM_APIS := true
    LOCAL_PREBUILT_JNI_LIBS := libs/arm64-v8a-smd8/libqlmodem.so
#else
#    ifeq  ($(TARGET_ARCH),arm64)
#      LOCAL_PREBUILT_JNI_LIBS := libs/arm64-v8a-smd11/libqlmodem.so
#    else
#      LOCAL_PREBUILT_JNI_LIBS := libs/armeabi-v7a-smd11/libqlmodem.so
#    endif
#endif

LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_STATIC_JAVA_LIBRARIES := mbnmanager
LOCAL_JAVA_LIBRARIES := qcrilhook telephony-common

LOCAL_DEX_PREOPT := false
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_PACKAGE_NAME := modemconfigapp
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE_PATH   := $(TARGET_OUT_APPS_PRIVILEGED)
include $(BUILD_PACKAGE)

######################################################
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
                    mbnmanager:libs/mbnmanager.jar

include $(BUILD_MULTI_PREBUILT)
