#
# Inline Aosp Makefile
#
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := masquerade
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, java)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
