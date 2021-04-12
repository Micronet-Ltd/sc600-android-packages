/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion;

import android.support.annotation.VisibleForTesting;

import com.android.settings.R;
import android.os.SystemProperties;

public class McuVersionDialogController {

    @VisibleForTesting
    static int MCU_VERSION_VALUE_ID = R.id.mcu_version_value;

    private final FirmwareVersionDialogFragment mDialog;

    public McuVersionDialogController(FirmwareVersionDialogFragment dialog) {
        mDialog = dialog;
    }

    /**
     * Updates mcu version to the dialog.
     */
    public void initialize() {
        mDialog.setText(MCU_VERSION_VALUE_ID, getMcuVersion());
    }
    
    public String getMcuVersion(){
        String ver = null;
    	ver = SystemProperties.get("hw.build.version.mcu", "unknown");
        if(ver.startsWith("unknown"))
        	return "Unavailable";
        return ver;
    }
    
}
