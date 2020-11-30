LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_STATIC_JAVA_LIBRARIES := \
    lib-commons-net \
    lib-ftp4j

LOCAL_PACKAGE_NAME := Otademo
LOCAL_CERTIFICATE := platform
LOCAL_PRIVATE_PLATFORM_APIS:=true

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
   lib-commons-net:libs/commons-net-3.3.jar

include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
   lib-ftp4j:libs/ftp4j-1.7.2.jar

include $(BUILD_MULTI_PREBUILT)

