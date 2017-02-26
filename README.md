# masquerade
[![Build Status](https://travis-ci.org/nicholaschum/masquerade.svg?branch=master)](https://travis-ci.org/nicholaschum/masquerade)

Masquerade is Substratum's background service that runs functions that should run on a completely separate thread. This alleviates the stress on the main app and allows for Substratum to run more smoothly.

# How to add to ROM builds
If you are a ROM developer, by now you should know how to track new packages while building your ROM. However, if you don't - please follow these steps:

Add "masquerade" under PRODUCT_PACKAGES in "vendor/config/common.mk"

    PRODUCT_PACKAGES += \
    ...\
    ...\
    ...\
    masquerade
    
Add the project path in "platform_manifest/default.xml"

    <project path="packages/apps/masquerade" name="substratum/masquerade" remote="github" revision="n-rootless" />

## An example is found here:
platform_manifest/default.xml:
https://github.com/TipsyOs/platform_manifest/commit/fbff9ea598ec04f4f4a69687a8024770fefa83de

vendor_tipsy/config/common.mk:
https://github.com/TipsyOs/vendor_tipsy/commit/2e72dc4eb3a206fad34e9c4f130e270180eab1c2
