# SC600 Android9

promote permisson


```python
--- a/device/qcom/common/rootdir/etc/ueventd.qcom.rc
+++ b/device/qcom/common/rootdir/etc/ueventd.qcom.rc
@@ -60,6 +60,7 @@ firmware_directories /vendor/firmware_mnt/image/
 
 #permissions for CSVT
 /dev/smd11                0660   radio      radio
+/dev/smd8                0666   radio      radio
```


```python
--- a/device/qcom/sepolicy/vendor/common/system_app.te
+++ b/device/qcom/sepolicy/vendor/common/system_app.te
@@ -28,6 +28,9 @@ allow system_app fm_radio_device:chr_file r_file_perms;
r_dir_file(system_app, bluetooth_data_file);
r_dir_file(system_app, bt_firmware_file);

+#add permission to access smd device in order to send at command
+allow system_app smd_device:chr_file { open read write };
+
allow system_app {
   fm_prop
   usf_prop
```



# SC60 Android7
```python
diff --git a/device/qcom/common/rootdir/etc/ueventd.qcom.rc b/device/qcom/common/rootdir/etc/ueventd.qcom.rc
index a4785b7..0ab6d05 100644
--- a/device/qcom/common/rootdir/etc/ueventd.qcom.rc
+++ b/device/qcom/common/rootdir/etc/ueventd.qcom.rc
@@ -49,7 +49,7 @@
 /dev/dpl_ctrl             0660   usb        usb
 
 #permissions for CSVT
-/dev/smd11                0660   radio      radio
+/dev/smd11                0666   radio      radio
 
 /dev/radio0               0640   system     system
 /dev/rfcomm0              0660   bluetooth  bluetooth
diff --git a/device/qcom/msm8953_64/msm8953_64.mk b/device/qcom/msm8953_64/msm8953_64.mk
index ae2d3bb..06f878f 100755
--- a/device/qcom/msm8953_64/msm8953_64.mk
+++ b/device/qcom/msm8953_64/msm8953_64.mk
@@ -154,3 +154,7 @@ endif
 #FEATURE_OPENGLES_EXTENSION_PACK support string config file
 PRODUCT_COPY_FILES += \
         frameworks/native/data/etc/android.hardware.opengles.aep.xml:system/etc/permissions/android.hardware.opengles.aep.xml
+
+PRODUCT_PACKAGES += \
+        ModemConfigApp
+
diff --git a/device/qcom/sepolicy/common/system_app.te b/device/qcom/sepolicy/common/system_app.te
index 36e6078..3fd4261 100644
--- a/device/qcom/sepolicy/common/system_app.te
+++ b/device/qcom/sepolicy/common/system_app.te
@@ -22,6 +22,8 @@
 # OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 # IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
+allow system_app smd_device:chr_file {open read write};
+
 # fm_radio app needes  open read write on fm_radio_device
 allow system_app fm_radio_device:chr_file r_file_perms;
 r_dir_file(system_app, fm_data_file);
-- 
2.7.4
```