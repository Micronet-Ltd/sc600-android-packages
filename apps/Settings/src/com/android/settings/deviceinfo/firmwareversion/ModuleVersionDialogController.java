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

public class ModuleVersionDialogController {

    @VisibleForTesting
    static int MODULE_VERSION_VALUE_ID = R.id.module_version_value;

    private final FirmwareVersionDialogFragment mDialog;

    public ModuleVersionDialogController(FirmwareVersionDialogFragment dialog) {
        mDialog = dialog;
    }

    /**
     * Updates Module version to the dialog.
     */
    public void initialize() {
        mDialog.setText(MODULE_VERSION_VALUE_ID, "MSCAM_10.2.0.0-Q");
    }
    
}
