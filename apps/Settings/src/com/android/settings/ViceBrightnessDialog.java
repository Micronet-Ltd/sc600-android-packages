/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.io.FileWriter;

/** A dialog that provides controls for adjusting the vice screen brightness. */
public class ViceBrightnessDialog extends Activity implements SeekBar.OnSeekBarChangeListener{

        private static final String TAG = "ViceBrightnessDialog";
        private final static int DEF_VICE_SCREEN_BRIGHTNESS = 245;
        private final static String STR_VICE_SCREEN = "Quectel.def.vice.screen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Window window = getWindow();

        window.setGravity(Gravity.TOP);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.quick_settings_brightness_dialog);

        final SeekBar mSeekbar = (SeekBar) findViewById(R.id.seekBar);
        mSeekbar.setOnSeekBarChangeListener(this);

        //initialize vice screen brightness
        int num = Settings.System.getInt(getContentResolver(), STR_VICE_SCREEN,DEF_VICE_SCREEN_BRIGHTNESS);
        mSeekbar.setProgress(num);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d(TAG, "seekid:"+seekBar.getId()+", progess"+progress);
        int num = progress + 10;
        bootCommand(String.valueOf(num),"/sys/devices/platform/soc/soc:qcom,dsi1_bridge/dsi1_bl_value");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(seekBar != null) {
            Settings.System.putInt(getContentResolver(), STR_VICE_SCREEN, seekBar.getProgress());
            Log.d(TAG,"seekBar.getProgress()=="+seekBar.getProgress());
        }
    }

	/* Change the value of the node
     * no return values
     * two parameter(Strvalue:value of node  path:file)
     *
     *andrrew.add
     */
    private void bootCommand(String Strvalue,String path){
        FileWriter command = null;
        try {
            command = new FileWriter(path);
               if (!Strvalue.isEmpty()) {
                    command.write(Strvalue);
                    command.write("\n");
                }
                    command.close();
            }catch (IOException e){
              Log.d(TAG,"bootCommand exception: "+e);
            }
    }
}
