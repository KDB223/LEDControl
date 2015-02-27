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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

public class MainActivity extends ActionBarActivity {

    private static final String PREF_FIRST_RUN = "first_run";
    private static final String PREF_SET_ON_BOOT = "set_on_boot";
    private static final String PREF_TIP = "tip";
    private static final String PREF_LAST_CHOICE = "last_choice";
    private static final String PREF_LAUNCH_COUNT = "count";
    private static final String PREF_SHOW_NOTIF = "show_notif";
    public final int TRIGGER_BATTERY_CHARGING = 1;
    public final int TRIGGER_BATTERY_FULL = 2;
    public final int TRIGGER_BATTERY_CHARGING_OR_FULL = 3;
    public final int TRIGGER_BATTERY_CHARGING_BLINKING_OR_FULL = 4;
    public final int TRIGGER_USB = 5;
    public final int TRIGGER_BKL_or_MOTOX_ADAPTER = 6;
    public final int TRIGGER_DISK_IO = 7;
    public final int TRIGGER_EXT_IO = 8;
    public final int TRIGGER_BLUETOOTH = 9;
    public final int TRIGGER_TORCH = 10;
    public final int TRIGGER_FLASH = 11;
    public final int TRIGGER_ALWAYS = 12;
    private static Context context;
    public MainActivity MainActivity;
    private int Check;
    private int lastSelected = 0;
    private int TIP_MESSAGE = 0;
    private int TIP_MESSAGE_TOAST = 1;
    private int WELCOME_MESSAGE = 2;
    private int ROOT_ERROR_MESSAGE = 3;
    private int DEVICE_ERROR_MESSAGE = 4;
    private int ABOUT_MESSAGE = 5;
    private int RATE_MESSAGE = 6;
    public boolean tip = true;
    public boolean firstRun;
    private int count;
    private LEDManager manager;
    private RadioGroup radioGroup;
    private RadioButton rBtn_Charging;
    private RadioButton rBtn_Full;
    private RadioButton rBtn_Charging_or_full;
    private RadioButton rBtn_Charging_or_full_blink;
    private RadioButton rBtn_USB;
    private RadioButton rBtn_Charging_adapter;
    private RadioButton rBtn_Display;
    private RadioButton rBtn_DiskIO;
    private RadioButton rBtn_ExtIO;
    private RadioButton rBtn_Bluetooth;
    private RadioButton rBtn_Flash;
    private RadioButton rBtn_Torch;
    private RadioButton rBtn_Always;
    private CompoundButton mSwitch;
    private ImageView divider_bluetooth, divider_flash, divider_torch, divider_always, divider_ext, divider_adapter, divider_display;
    private TableRow brightnessTableRow;
    private int lastSelectedRadioButtonId;
    private int lastBeforeCloseRadioButtonId = -1;
    private boolean setOnBoot;
    private boolean showNotif;
    private boolean isSupported;
    private boolean activityJustStarted = true;
    private String DEVICE_MOTO_X = "ghost";
    private String DEVICE_MOTO_G = "falcon";
    private String DEVICE_MOTO_E = "condor";
    private String DEVICE_NEXUS_6 = "shamu";
    private String DEVICE_MAXX = "obake-maxx";
    private String DEVICE_ULTRA = "obake";
    private String SHARED_PREF;
    private ProgressDialog loadingDialog;
    private SetupLEDManager setupLEDManager = new SetupLEDManager();
    private TableRow textTableRow;
    private TextView aboutDialogText;
    private SeekBar brightnessBar;
    private String deviceName = Build.DEVICE;
    private FrameLayout switchBar;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;
    private Menu optionsMenu;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.this;
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.action_bar_layout, null);
        getSupportActionBar().setCustomView(v);

        setContentView(R.layout.activity_main);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        rBtn_Charging = (RadioButton) findViewById(R.id.rBtn_Charging);
        rBtn_Full = (RadioButton) findViewById(R.id.rBtn_Full);
        rBtn_Charging_or_full = (RadioButton) findViewById(R.id.rBtn_Charging_or_full);
        rBtn_Charging_or_full_blink = (RadioButton) findViewById(R.id.rBtn_Charging_or_full_blink);
        rBtn_USB = (RadioButton) findViewById(R.id.rBtn_USB);
        rBtn_Display = (RadioButton) findViewById(R.id.rBtn_Display);
        rBtn_Charging_adapter = (RadioButton) findViewById(R.id.rBtn_Charging_adapter);
        rBtn_DiskIO = (RadioButton) findViewById(R.id.rBtn_Disk_IO);
        rBtn_ExtIO = (RadioButton) findViewById(R.id.rBtn_Ext_IO);
        rBtn_Always = (RadioButton) findViewById(R.id.rBtn_Always_on);
        rBtn_Flash = (RadioButton) findViewById(R.id.rBtn_Cam_flash);
        rBtn_Torch = (RadioButton) findViewById(R.id.rBtn_Torch);
        rBtn_Bluetooth = (RadioButton) findViewById(R.id.rBtn_Bluetooth);
        divider_adapter = (ImageView) findViewById(R.id.divider_below_adapter);
        divider_always = (ImageView) findViewById(R.id.divider_below_always);
        divider_bluetooth = (ImageView) findViewById(R.id.divider_below_bluetooth);
        divider_ext = (ImageView) findViewById(R.id.divider_below_ExtIO);
        divider_flash = (ImageView) findViewById(R.id.divider_below_flash);
        divider_torch = (ImageView) findViewById(R.id.divider_below_torch);
        divider_display = (ImageView) findViewById(R.id.divider_below_display);
        brightnessTableRow = (TableRow) findViewById(R.id.tableRowBrightness);
        textTableRow = (TableRow) findViewById(R.id.tableRowText);
        switchBar = (FrameLayout) findViewById(R.id.SwitchBar);
        switchBar.setLongClickable(false);
        brightnessBar = (SeekBar) findViewById(R.id.SeekBarBrightness);
        aboutDialogText = (TextView) findViewById(R.id.AboutContent);

        if (deviceName.contains(DEVICE_MOTO_X)||deviceName.contains(DEVICE_MAXX)||deviceName.contains(DEVICE_ULTRA)) {
            rBtn_Charging_adapter.setVisibility(View.VISIBLE);
            divider_adapter.setVisibility(View.VISIBLE);
        } else if (deviceName.contains(DEVICE_NEXUS_6)) {
            rBtn_Charging_adapter.setVisibility(View.VISIBLE);
            divider_adapter.setVisibility(View.VISIBLE);
            rBtn_Always.setVisibility(View.VISIBLE);
            divider_always.setVisibility(View.VISIBLE);
            rBtn_Display.setVisibility(View.VISIBLE);
            divider_display.setVisibility(View.VISIBLE);
            rBtn_Bluetooth.setVisibility(View.VISIBLE);
            divider_bluetooth.setVisibility(View.VISIBLE);
        } else if (deviceName.contains(DEVICE_MOTO_E)) {
            rBtn_Display.setVisibility(View.VISIBLE);
            divider_display.setVisibility(View.VISIBLE);
            rBtn_ExtIO.setVisibility(View.VISIBLE);
            divider_ext.setVisibility(View.VISIBLE);
        } else if (deviceName.contains(DEVICE_MOTO_G)) {
            rBtn_Display.setVisibility(View.VISIBLE);
            divider_display.setVisibility(View.VISIBLE);
            rBtn_Flash.setVisibility(View.VISIBLE);
            divider_flash.setVisibility(View.VISIBLE);
            rBtn_Torch.setVisibility(View.VISIBLE);
            divider_torch.setVisibility(View.VISIBLE);
        }


        SHARED_PREF = getPackageName() + ".prefs";
        prefs = getSharedPreferences(SHARED_PREF, 0);
        prefsEditor = prefs.edit();

        showNotif = prefs.getBoolean(PREF_SHOW_NOTIF, true);
        tip = prefs.getBoolean(PREF_TIP, true);
        firstRun = prefs.getBoolean(PREF_FIRST_RUN, true);
        lastBeforeCloseRadioButtonId = prefs.getInt(PREF_LAST_CHOICE, -1);
        setOnBoot = prefs.getBoolean(PREF_SET_ON_BOOT, false);
        count = prefs.getInt(PREF_LAUNCH_COUNT, 0);

        if (count != -1 && count > 3){
            showMessage(RATE_MESSAGE);
        } else if (count != -1) {
            count++;
            prefsEditor.putInt(PREF_LAUNCH_COUNT, count).apply();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSwitch = (SwitchCompat) findViewById(R.id.switch1);
        } else {
            mSwitch = (Switch) findViewById(R.id.switch2);
        }


        if (aboutDialogText != null)
            aboutDialogText.setMovementMethod(LinkMovementMethod.getInstance());

        /* Directly instantiate `manager` if the activity had previously been unexpectedly destroyed */
        if (savedInstanceState != null) {
            manager = new LEDManager(MainActivity.this);
        }
        if (firstRun) {
            prefsEditor.putBoolean(PREF_FIRST_RUN, false).apply();
            showMessage(WELCOME_MESSAGE);
            showDeviceToast();
        } else {
            setupLEDManager.execute();
        }

        if (radioGroup.isSelected())
            lastSelectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        if (!mSwitch.isChecked()) {
            if (lastBeforeCloseRadioButtonId != -1) {
                lastSelected = lastBeforeCloseRadioButtonId;
                radioGroup.check(lastBeforeCloseRadioButtonId);
            }
        }

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!switchBar.isPressed()) {
                    switchBar.setPressed(true);
                    switchBar.setPressed(false);
                }
                switchBar.setPressed(false);
                if (mSwitch.isChecked()) {
                    if (lastSelected != 0) {
                        manager.setChoice(lastSelected);
                        manager.Apply();
                        UpdateRadioButtons();
                    }
                    radioGroup.setVisibility(View.VISIBLE);
                    textTableRow.setVisibility(View.INVISIBLE);
                    brightnessTableRow.setVisibility(View.VISIBLE);
                } else {
                    manager.setChoice(0);
                    manager.Apply();
                    lastSelected = radioGroup.getCheckedRadioButtonId();
                    radioGroup.setVisibility(View.GONE);
                    brightnessTableRow.setVisibility(View.INVISIBLE);
                    textTableRow.setVisibility(View.VISIBLE);
                }
            }
        });

        switchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSwitch.setChecked(!mSwitch.isChecked());
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton tRadioButton;
                if (lastSelectedRadioButtonId != 0) {
                    tRadioButton = (RadioButton) findViewById(lastSelectedRadioButtonId);
                    tRadioButton.setTypeface(null, Typeface.NORMAL);
                }
                tRadioButton = (RadioButton) findViewById(checkedId);
                lastSelectedRadioButtonId = checkedId;
                if (tRadioButton != null) {
                    tRadioButton.setTypeface(null, Typeface.BOLD);
                }
                if (mSwitch.isChecked()) {
                    if (!isCharging()) {
                        showMessage(TIP_MESSAGE_TOAST);
                    } else if (tip) {
                        prefsEditor.putBoolean(PREF_TIP, false).apply();
                        tip = false;
                        showMessage(TIP_MESSAGE);
                    }
                }
                if (Check != 0 && activityJustStarted) {
                    activityJustStarted = false;
                } else {
                    manager.setChoice(checkedId);
                    manager.Apply();
                }
            }
        });

        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                manager.ApplyBrightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Nothing to do here
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Nothing to do here
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mSwitch.isChecked()) {
            lastBeforeCloseRadioButtonId = radioGroup.getCheckedRadioButtonId();
            prefsEditor.putInt(PREF_LAST_CHOICE, lastBeforeCloseRadioButtonId).apply();
        } else {
            prefsEditor.putInt(PREF_LAST_CHOICE, 0).apply();
        }
    }

    private class SetupLEDManager extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new ProgressDialog(context);
            loadingDialog.setMessage(getString(R.string.dialog_getting_root_access));
            loadingDialog.setIndeterminate(true);
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingDialog.setCancelable(false);
            loadingDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            manager = new LEDManager(context);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadingDialog.dismiss();
            if (!manager.rooted) {
                showMessage(ROOT_ERROR_MESSAGE);
            } else {
                Check = manager.checkState();
                UpdateRadioButtons();
                UpdateSwitch();
                UpdateSlider();
                if (radioGroup.isSelected())
                    lastSelectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            }
        }
    }

    private void UpdateSlider() {
        if (isSupported) {
            int currentBrightness;
            currentBrightness = manager.checkBrightness();
            brightnessBar.setProgress(currentBrightness);
        }
    }

    private void showDeviceToast() {
        Log.d("KDB", "called showDeviceToast");
        if (deviceName.contains(DEVICE_MOTO_E)) {
            Toast.makeText(context, R.string.device_moto_e, Toast.LENGTH_SHORT).show();
        } else if (deviceName.contains(DEVICE_MOTO_G)) {
            Toast.makeText(context, R.string.device_moto_g, Toast.LENGTH_SHORT).show();
        } else if (deviceName.contains(DEVICE_MOTO_X)) {
            Toast.makeText(context, R.string.device_moto_x, Toast.LENGTH_SHORT).show();
        } else if (deviceName.contains(DEVICE_NEXUS_6)) {
            Toast.makeText(context, R.string.device_nexus_6, Toast.LENGTH_SHORT).show();
        } else if (deviceName.contains(DEVICE_MAXX)) {
            Toast.makeText(context, R.string.device_maxx, Toast.LENGTH_SHORT).show();
        } else if (deviceName.contains(DEVICE_ULTRA)) {
            Toast.makeText(context, R.string.device_ultra, Toast.LENGTH_SHORT).show();
        }
    }

    private void showMessage(int id) {
        if (id == TIP_MESSAGE) {
            new MaterialDialog.Builder(context)
                    .content(R.string.dialog_content_charging_info)
                    .positiveText(R.string.dialog_button_positive_ok)
                    .show();
        } else if (id == TIP_MESSAGE_TOAST) {
            Toast.makeText(context, (R.string.toast_charger_info), Toast.LENGTH_LONG).show();
        } else if (id == WELCOME_MESSAGE) {
            new MaterialDialog.Builder(context)
                    .title(R.string.hello_universal)
                    .content(R.string.dialog_greet)
                    .positiveText("Next")
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            setupLEDManager.execute();
                        }
                    })
                    .show();
        } else if (id == ROOT_ERROR_MESSAGE) {
            new MaterialDialog.Builder(context)
                    .title(R.string.dialog_title_root_failed)
                    .content(R.string.dialog_content_root_failed)
                    .positiveText("Exit")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            finish();
                        }
                    })
                    .cancelable(false)
                    .show();
        } else if (id == DEVICE_ERROR_MESSAGE) {
            new MaterialDialog.Builder(MainActivity.this)
                    .title(R.string.dialog_title_device_error)
                    .content(R.string.dialog_content_device_error)
                    .positiveText("Exit")
                    .negativeText("Continue Anyway")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            finish();
                        }
                    })
                    .show();
        } else if (id == ABOUT_MESSAGE) {
            MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                    .title(getText(R.string.dialog_about_title_html))
                    .icon(getResources().getDrawable(R.drawable.ic_launcher))
                    .autoDismiss(false)
                    .customView(R.layout.dialog_about, true)
                    .positiveText("Rate")
                    .negativeText("Share")
                    .neutralText("Licenses")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNeutral(MaterialDialog dialog) {
                            super.onNeutral(dialog);
                            MaterialDialog licenseDialog = new MaterialDialog.Builder(context)
                                    .title("Open Source Licenses")
                                    .customView(R.layout.dialog_license, false)
                                    .positiveText("Accept")
                                    .build();
                            WebView webView = (WebView) licenseDialog.getCustomView().findViewById(R.id.webview);
                            webView.loadUrl("file:///android_asset/license.html");
                            licenseDialog.show();
                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            Uri playURL = Uri.parse("market://details?id=" + context.getPackageName());
                            startActivity(new Intent(Intent.ACTION_VIEW, playURL));
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.share_text));
                            shareIntent.setType("text/plain");
                            startActivity(Intent.createChooser(shareIntent, "Share via"));
                        }
                    })
                    .build();
            TextView body = (TextView) dialog.getCustomView().findViewById(R.id.AboutContent);
            TextView footer = (TextView) dialog.getCustomView().findViewById(R.id.AboutFooter);
            body.setMovementMethod(LinkMovementMethod.getInstance());
            footer.setMovementMethod(LinkMovementMethod.getInstance());
            dialog.show();
        } else if (id == RATE_MESSAGE) {
            new MaterialDialog.Builder(MainActivity.this)
                .title("Enjoying LED Control Pro?")
                .content("Looks like you've been using this app for quite some time now! Do you like it/dislike it?\nPlease take a moment to rate this app on Google Play and support the developer.")
                .positiveText("Rate")
                .neutralText("Not Now")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        Uri playURL = Uri.parse("market://details?id=" + context.getPackageName());
                        startActivity(new Intent(Intent.ACTION_VIEW, playURL));
                        prefsEditor.putInt(PREF_LAUNCH_COUNT, -1).apply();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        prefsEditor.putInt(PREF_LAUNCH_COUNT, 0).apply();
                    }
                })
                .show();
        }
    }

    private void UpdateRadioButtons() {
        if (manager.rooted) {
            int newCheck = manager.checkState();
            isSupported = manager.isDeviceSupported();
            RadioButton BTN = null;
            if (!isSupported) {
                showMessage(DEVICE_ERROR_MESSAGE);
            } else if (newCheck == TRIGGER_BATTERY_CHARGING) {
                BTN = rBtn_Charging;
            } else if (newCheck == TRIGGER_BATTERY_FULL) {
                BTN = rBtn_Full;
            } else if (newCheck == TRIGGER_BATTERY_CHARGING_OR_FULL) {
                BTN = rBtn_Charging_or_full;
            } else if (newCheck == TRIGGER_BATTERY_CHARGING_BLINKING_OR_FULL) {
                BTN = rBtn_Charging_or_full_blink;
            } else if (newCheck == TRIGGER_USB) {
                BTN = rBtn_USB;
            } else if (newCheck == TRIGGER_BKL_or_MOTOX_ADAPTER) {
                if (deviceName.contains(DEVICE_MOTO_X)) {
                    BTN = rBtn_Charging_adapter;
                } else {
                    BTN = rBtn_Display;
                }
            } else if (newCheck == TRIGGER_DISK_IO) {
                BTN = rBtn_DiskIO;
            } else if (newCheck == TRIGGER_EXT_IO) {
                BTN = rBtn_ExtIO;
            } else if (newCheck == TRIGGER_BLUETOOTH) {
                BTN = rBtn_Bluetooth;
            } else if (newCheck == TRIGGER_FLASH) {
                BTN = rBtn_Flash;
            } else if (newCheck == TRIGGER_ALWAYS) {
                BTN = rBtn_Always;
            } else if (newCheck == TRIGGER_TORCH) {
                BTN = rBtn_Torch;
            }
            if (BTN != null) {
                BTN.setChecked(true);
                BTN.setTypeface(null, Typeface.BOLD);
                lastSelectedRadioButtonId = BTN.getId();
            }
        }
    }

    private void UpdateSwitch() {
        if (isSupported) {
            if (Check != 0 && Check != -1) {
                mSwitch.setChecked(true);
                radioGroup.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isCharging() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, intentFilter);

        // Are we charging / charged?
        int status = 0;
        if (batteryStatus != null)
            status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem notifItem = menu.findItem(R.id.action_show_notif);
        MenuItem setOnBootItem = menu.findItem(R.id.action_set_on_boot);
        if (setOnBoot) {
            setOnBootItem.setChecked(true);
            notifItem.setEnabled(true);
        }
        if (showNotif) {
            notifItem.setChecked(true);
        } else notifItem.setChecked(false);
        this.optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            showMessage(ABOUT_MESSAGE);
            return true;
        } else if (id == R.id.action_set_on_boot) {
            optionsMenu.findItem(R.id.action_show_notif).setEnabled(!item.isChecked());
            item.setChecked(!item.isChecked());
            prefsEditor.putBoolean(PREF_SET_ON_BOOT, item.isChecked()).apply();
            return true;
        } else if (id == R.id.action_show_notif) {
            item.setChecked(!item.isChecked());
            prefsEditor.putBoolean(PREF_SHOW_NOTIF, item.isChecked()).apply();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}