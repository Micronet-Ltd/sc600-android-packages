/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.car.settings.testutils;

import static org.mockito.Mockito.mock;

import android.car.Car;
import android.content.Context;
import android.content.ServiceConnection;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow class for {@link Car}. Components in car support library expects
 * this class to be available at run time.
 */
@Implements(Car.class)
public class ShadowCar {

    @Implementation
    public static Car createCar(Context context, ServiceConnection serviceConnection) {
        return mock(Car.class);
    }
}
