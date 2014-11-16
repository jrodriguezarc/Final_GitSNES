/*
 * Copyright 2014 GitSNES Inc. All Rights Reserved.
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

package itcr.gitsnes;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.FloatMath;

public class ShakeDetector implements SensorEventListener {

    /** The gForce that is necessary to register as shake.
     *  Must be greater than 1G (one earth gravity unit)
     *  Params to hadle handle shake
     */
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.5F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;
    private long mShakeTimestamp;
    private int mShakeCount;

    /*Shake listener*/
    private OnShakeListener mListener;

    public void setOnShakeListener(OnShakeListener listener) {
        this.mListener = listener;
    }

    /* Interface to OnShakeListener, to get shake intents */
    public interface OnShakeListener {
        public void onShake(int count);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        /*Activated accuracy sensor*/
    }

    /**Method to get sensorChanged event (shake move)*/
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            /* Force will be close to 1 when there is no movement. */
            float gForce = FloatMath.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                /* Ignore shake events too close to each other (500ms) */
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now ) {
                    return;
                }
                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now ) {
                    mShakeCount = 0;
                }
                mShakeTimestamp = now;
                mShakeCount++;
                mListener.onShake(mShakeCount);
            }
        }
    }
}