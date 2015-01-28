/*          Copyright (c) 2015 Krishanu Dutta Bhuyan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.kdb.ledcontrol;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by KDB on 17/12/2014.
 */
public class LEDManager {
    private static final String pathToLED = "/sys/class/leds/charging/trigger";
    private static final String pathToLEDdir = "/sys/class/leds/";
    private static final String pathToBrightness = "/sys/class/leds/charging/max_brightness";
    private static final String TAG = "LED Control";
    public static final String BOOT_PREF_FILE_CMD = "boot_pref_cmd";
    public static final String BOOT_PREF_FILE_BRIGHTNESS = "boot_pref_bright";
    public String cmd;
    public boolean rooted;
    private Context context;
    private Process process;
    private FileOutputStream bootPrefOutput;
    private FileInputStream bootPrefInput;
    private DataOutputStream toDevice;
    private BufferedReader fromDevice;
    private String DEVICE_MOTO_X = "ghost";
    private String DEVICE_MOTO_G = "falcon";
    private String DEVICE_MOTO_E = "condor";
    private String DEVICE_NEXUS_6 = "shamu";

    public LEDManager(Context context) {
        this.context = context;
        process = null;
        rooted = true;
        //check for root
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
            if (process != null) process.destroy();
        }
        //confirming root only if the command "id" has "root"
        if (process != null) {
            fromDevice = new BufferedReader(new InputStreamReader(process.getInputStream()));
            toDevice = new DataOutputStream(process.getOutputStream());
            String user = getUser();
            if ((user == null) || (!user.contains("root"))) {
                rooted = false;
            }
        } else {
            rooted = false;
        }
    }

    /*
     *  Returns output from executing command "id"
     */
    private String getUser() {
        if (toDevice == null || fromDevice == null) {
            return null;
        }
        String user = null;
        try {
            toDevice.writeBytes("id\n");
            toDevice.flush();
            user = fromDevice.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    /*
     *  Dynamically sets the value of cmd based on user's choice
     */
    public void setChoice(int Choice) {
        //Don't proceed further if no root access:
        if (!rooted)
            return;
        if (Choice == -2) {   //for (int) lastSelected in MainActivity
            cmd = null;
        } else if (Choice == 0) {
            cmd = "none";
        } else if (Choice == R.id.rBtn_Charging) {
            cmd = "battery-charging";
        } else if (Choice == R.id.rBtn_Full) {
            cmd = "battery-full";
        } else if (Choice == R.id.rBtn_Charging_or_full) {
            cmd = "battery-charging-or-full";
        } else if (Choice == R.id.rBtn_Charging_or_full_blink) {
            if (getDevice().equals(DEVICE_MOTO_X)) {
                cmd = "heartbeat";
            } else {
                cmd = "battery-charging-blink-full-solid";
            }
        } else if (Choice == R.id.rBtn_USB) {
            cmd = "usb-online";
        } else if (Choice == R.id.rBtn_Display) {
            if (getDevice().equals(DEVICE_NEXUS_6)) {
                cmd = "backlight";
            } else {
                cmd = "bkl-trigger";
            }
        } else if (Choice == R.id.rBtn_Disk_IO) {
            cmd = "mmc0";
        } else if (Choice == R.id.rBtn_Ext_IO) {
            cmd = "mmc1";
        } else if (Choice == R.id.rBtn_Charging_adapter) {
            if (getDevice().equals(DEVICE_NEXUS_6)) {
                cmd = "dc-online";
            } else {
                cmd = "pm89221-dc-online";
            }
        }
    }

    /*
     * Returns true if a directory called `charging` exists in /sys/class/leds/, to determine if the device is supported
     */
    public boolean isDeviceSupported() {
        File chargingDir = new File(pathToLEDdir + "charging");
        return ((chargingDir.exists()) && (chargingDir.isDirectory()));
    }

    /*
     *  Returns an integer value depending on currently set trigger
     */
    public int checkState() {
        //not aborting if not root here, since these commands can run without root access
        String output = null;
        int val = 0;
        try {
            toDevice.writeBytes("cat " + pathToLED + "\n");
            toDevice.flush();
            output = fromDevice.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (output == null)
            return -1;
        if (output.contains("[battery-charging]"))
            val = 1;
        if (output.contains("[battery-full]"))
            val = 2;
        if (output.contains("[battery-charging-or-full]"))
            val = 3;
        if (output.contains("[battery-charging-blink-full-solid]") || output.contains("[heartbeat]"))
            val = 4;
        if (output.contains("[usb-online]"))
            val = 5;
        if (output.contains("[bkl-trigger]") || output.contains("[pm8921-dc-online]"))
            val = 6;
        if (output.contains("[mmc0]"))
            val = 7;
        if (output.contains("[mmc1]"))
            val = 8;
        return val;
    }

    /*
     *  Returns an integer value depending on currently set brightness
     */
    public int checkBrightness() {
        int brightness = 0;
        try {
            toDevice.writeBytes("cat " + pathToBrightness + "\n");
            toDevice.flush();
            brightness = Integer.parseInt(fromDevice.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return brightness / 25;
    }

    /*
     *  Brings the app to life; the core methods - Apply() and ApplyBrightness()
     */
    public void Apply() {
        //again, stop if root not found:
        if (!rooted || cmd == null)
            return;
        try {
            toDevice.writeBytes("echo " + cmd + " > " + pathToLED + "\n");
            toDevice.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bootPrefOutput = context.openFileOutput(BOOT_PREF_FILE_CMD, Context.MODE_PRIVATE);
            bootPrefOutput.write(cmd.getBytes());
            bootPrefOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Successfully set trigger: " + cmd);
    }

    public void ApplyBrightness(int brightness) {
        if (!rooted) return;
        if (brightness != 10) brightness *= 25;
        else brightness = 255;
        try {
            toDevice.writeBytes("echo " + Integer.toString(brightness) + " > " + pathToBrightness + "\n");
            toDevice.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bootPrefOutput = context.openFileOutput(BOOT_PREF_FILE_BRIGHTNESS, Context.MODE_PRIVATE);
            bootPrefOutput.write(Integer.toString(brightness).getBytes());
            bootPrefOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
     *  Returns a String value based on user's device.
     *  Since the app only runs on Moto G, E and X, we need to check if the device is correct
     */
    public String getDevice() {
        if (toDevice == null || fromDevice == null) return null;
        String device = null;
        String output = Build.DEVICE;
        if (output.contains(DEVICE_MOTO_E)) device = DEVICE_MOTO_E;
        else if (output.contains(DEVICE_MOTO_G)) device = DEVICE_MOTO_G;
        else if (output.contains(DEVICE_MOTO_X)) device = DEVICE_MOTO_X;
        else if (output.contains(DEVICE_NEXUS_6)) device = DEVICE_NEXUS_6;
        return device;
    }

    public void setOnBoot() {
        String cmd = null;
        try {
            bootPrefInput = context.openFileInput(BOOT_PREF_FILE_CMD);
            BufferedReader reader = new BufferedReader(new InputStreamReader(bootPrefInput));
            cmd = reader.readLine();
            bootPrefInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int brightness = -1;
        try {
            bootPrefInput = context.openFileInput(BOOT_PREF_FILE_BRIGHTNESS);
            BufferedReader reader = new BufferedReader(new InputStreamReader(bootPrefInput));
            String brightness_string = reader.readLine();
            brightness = Integer.parseInt(brightness_string);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (brightness != -1) {
            brightness /= 25;
            ApplyBrightness(brightness);
        }
        if (cmd != null) {
            try {
                toDevice.writeBytes("echo " + cmd + " > " + pathToLED + "\n");
                toDevice.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}