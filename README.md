# ThemeInterfacer

ThemeInterfacer is the background service of the theme system developed by the Projekt team. It allows theme apps (including Substratum) to run system wide operations, otherwise unobtainable without root access. This also alleviates the performance stress on the theme app.

# How to add to ROM builds
If you are a ROM developer, by now you should know how to track new packages while building your ROM. However, if you don't - please follow these steps:

Add "ThemeInterfacer" in your PRODUCT_PACKAGES

    PRODUCT_PACKAGES += \
    ...\
    ...\
    ...\
    ThemeInterfacer

Don't forget to add the project path in your AOSP manifest:

    <project path="packages/apps/ThemeInterfacer" name="substratum/interfacer" remote="github" revision="n-rootless" />
