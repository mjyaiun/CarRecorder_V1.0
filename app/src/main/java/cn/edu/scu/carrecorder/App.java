/*
 * Copyright 2016 Yan Zhenjie
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
package cn.edu.scu.carrecorder;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import cn.edu.scu.carrecorder.util.PublicDate;

/**
 * Created by Yan Zhenjie on 2016/7/27.
 */
public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();

        if (instance == null) {
            instance = this;
            SharedPreferences sp = getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
            if (! sp.contains("Quality")) {
                SharedPreferences.Editor spEditor = sp.edit();
                spEditor.putInt("Quality", PublicDate.defaultQuality);
                spEditor.putInt("MaxDuration", PublicDate.defaultDuration);
                spEditor.putLong("MaxFileSize", PublicDate.defaultFileSize);
                spEditor.putBoolean("AudioOn", true);
                spEditor.putBoolean("PowerSaving", true);
                spEditor.putBoolean("PathRecOn", true);
                spEditor.putBoolean("AutoStopOn", true);
                spEditor.putInt("AutoStopInterval", PublicDate.defaultInterval);
                spEditor.commit();
            }
        }
    }

    public static App getInstance() {
        return instance;
    }
}
