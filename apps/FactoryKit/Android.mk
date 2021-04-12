
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += src/com/lovdream/factorykit/services/ISpeakerAndStorageHelper.aidl
LOCAL_SRC_FILES += ../../../frameworks/base/core/java/com/lovdream/ILovdreamDevice.aidl

LOCAL_PACKAGE_NAME := FactoryKit
LOCAL_CERTIFICATE := platform

#LOCAL_JAVA_LIBRARIES := qcom.fmradio
#LOCAL_JNI_SHARED_LIBRARIES := libqcomfm_jni 
#libfpjni_sw

LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))

