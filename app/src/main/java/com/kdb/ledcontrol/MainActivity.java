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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
    public static Context context;
    public MainActivity MainActivity;
    public int Check;
    public int lastSelected = 0;
    public int TIP_MESSAGE = 0, TIP_MESSAGE_TOAST = 1, WELCOME_MESSAGE = 2, ROOT_ERROR_MESSAGE = 3, DEVICE_ERROR_MESSAGE = 4, ABOUT_MESSAGE = 5;
    public boolean tip = true;
    public boolean firstRun;
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
    private CompoundButton mSwitch;
    private ImageView divider_extra;
    private ImageView divider_extra2;
    private ImageView divider_extra3;
    private TableRow brightnessTableRow;
    private int lastSelectedRadioButtonId;
    private boolean setOnBoot;
    private boolean isSupported;
    private boolean activityJustStarted = true;
    private String DEVICE_MOTO_X = "ghost";
    private String DEVICE_MOTO_G = "falcon";
    private String DEVICE_MOTO_E = "condor";
    private String DEVICE_NEXUS_6 = "shamu";
    private String EXTRA_INFO_SHARED_PREF = "extra_info";
    private ProgressDialog loadingDialog;
    private SetupLEDManager setupLEDManager = new SetupLEDManager();
    private TableRow textTableRow;
    private TextView aboutDialogText;
    private SeekBar brightnessBar;
    private String deviceName = Build.DEVICE;
    private FrameLayout switchBar;

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

        setOnBoot = getSharedPreferences(EXTRA_INFO_SHARED_PREF, 0).getBoolean(PREF_SET_ON_BOOT, false);
        tip = getSharedPreferences(EXTRA_INFO_SHARED_PREF, 0).getBoolean(PREF_TIP, true);
        firstRun = getSharedPreferences(EXTRA_INFO_SHARED_PREF, 0).getBoolean(PREF_FIRST_RUN, true);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSwitch = (SwitchCompat) findViewById(R.id.switch1);
        } else {
            mSwitch = (Switch) findViewById(R.id.switch2);
        }
        rBtn_Charging = (RadioButton) findViewById(R.id.rBtn_Charging);
        rBtn_Full = (RadioButton) findViewById(R.id.rBtn_Full);
        rBtn_Charging_or_full = (RadioButton) findViewById(R.id.rBtn_Charging_or_full);
        rBtn_Charging_or_full_blink = (RadioButton) findViewById(R.id.rBtn_Charging_or_full_blink);
        rBtn_USB = (RadioButton) findViewById(R.id.rBtn_USB);
        rBtn_Display = (RadioButton) findViewById(R.id.rBtn_Display);
        rBtn_Charging_adapter = (RadioButton) findViewById(R.id.rBtn_Charging_adapter);
        rBtn_DiskIO = (RadioButton) findViewById(R.id.rBtn_Disk_IO);
        rBtn_ExtIO = (RadioButton) findViewById(R.id.rBtn_Ext_IO);
        divider_extra = (ImageView) findViewById(R.id.divider_extra);
        divider_extra2 = (ImageView) findViewById(R.id.divider_extra2);
        divider_extra3 = (ImageView) findViewById(R.id.divider_extra3);
        brightnessTableRow = (TableRow) findViewById(R.id.tableRowBrightness);
        textTableRow = (TableRow) findViewById(R.id.tableRowText);
        switchBar = (FrameLayout) findViewById(R.id.SwitchBar);
        brightnessBar = (SeekBar) findViewById(R.id.SeekBarBrightness);
        aboutDialogText = (TextView) findViewById(R.id.AboutContent);
        if (aboutDialogText != null)
            aboutDialogText.setMovementMethod(LinkMovementMethod.getInstance());

        /* Directly instantiate `manager` if the activity had previously been unexpectedly destroyed */
        if (savedInstanceState != null) {
            manager = new LEDManager(MainActivity.this);
        }
        showDeviceToast();
        if (firstRun) {
            getSharedPreferences(EXTRA_INFO_SHARED_PREF, 0).edit().putBoolean(PREF_FIRST_RUN, false).commit();
            showMessage(WELCOME_MESSAGE);
        } else {
            setupLEDManager.execute();
        }

        if (deviceName.contains(DEVICE_MOTO_X)) {
            rBtn_Display.setVisibility(View.GONE);
            divider_extra2.setVisibility(View.GONE);
            rBtn_Charging_adapter.setVisibility(View.VISIBLE);
            divider_extra.setVisibility(View.VISIBLE);
        } else if (deviceName.contains(DEVICE_NEXUS_6)) {
            rBtn_Charging_adapter.setVisibility(View.VISIBLE);
            divider_extra.setVisibility(View.VISIBLE);
        } else if (deviceName.contains(DEVICE_MOTO_E)) {
            rBtn_ExtIO.setVisibility(View.VISIBLE);
            divider_extra3.setVisibility(View.VISIBLE);
        }

        if (radioGroup.isSelected())
            lastSelectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mSwitch.isChecked()) {
                    if (lastSelected != 0) {
                        manager.setChoice(lastSelected);
                        manager.Apply();
                        UpdateRadioButtons();
                    }
                    textTableRow.setVisibility(View.INVISIBLE);
                    brightnessTableRow.setVisibility(View.VISIBLE);
                    radioGroup.setVisibility(View.VISIBLE);
                } else {
                    manager.setChoice(0);
                    manager.Apply();
                    lastSelected = radioGroup.getCheckedRadioButtonId();
                    radioGroup.setVisibility(View.GONE);
                    brightnessTableRow.setVisibility(View.GONE);
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
                    if (!Charging()) {
                        showMessage(TIP_MESSAGE_TOAST);
                    } else if (tip) {
                        getSharedPreferences(EXTRA_INFO_SHARED_PREF, 0).edit().putBoolean(PREF_TIP, false).commit();
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

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
                showDeviceToast();
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
        if (deviceName.contains(DEVICE_MOTO_E)) {
            Toast.makeText(context, R.string.device_moto_e, Toast.LENGTH_SHORT).show();
        } else if (deviceName.contains(DEVICE_MOTO_G)) {
            Toast.makeText(context, R.string.device_moto_g, Toast.LENGTH_SHORT).show();
        } else if (deviceName.contains(DEVICE_MOTO_X)) {
            Toast.makeText(context, R.string.device_moto_x, Toast.LENGTH_SHORT).show();
        } else if (deviceName.contains(DEVICE_NEXUS_6)) {
            Toast.makeText(context, R.string.device_nexus_6, Toast.LENGTH_SHORT).show();
        }
    }

    private void showMessage(int id) {
        if (id == 0) {
            new MaterialDialog.Builder(context)
                    .content(R.string.dialog_content_charging_info)
                    .positiveText(R.string.dialog_button_positive_ok)
                    .show();
        } else if (id == 1) {
            Toast.makeText(context, (R.string.toast_charger_info), Toast.LENGTH_LONG).show();
        } else if (id == 2) {
            new MaterialDialog.Builder(context)
                    .title(R.string.hello_universal)
                    .content(R.string.dialog_greet)
                    .positiveText("NEXT")
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            setupLEDManager.execute();
                        }
                    })
                    .show();
        } else if (id == 3) {
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
        } else if (id == 4) {
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
        } else if (id == 5) {
            MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                    .title("About")
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
                                    .title("Licenses")
                                    .customView(R.layout.dialog_license, false)
                                    .positiveText(android.R.string.ok)
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
        }
    }

    private void UpdateRadioButtons() {
        if (manager.rooted) {
            int newCheck = manager.checkState();
            isSupported = manager.isDeviceSupported();
            RadioButton BTN = null;
            if (!isSupported) {
                showMessage(DEVICE_ERROR_MESSAGE);
            } else if (newCheck == 1) {
                BTN = rBtn_Charging;
                BTN.setChecked(true);
                BTN.setTypeface(null, Typeface.BOLD);
            } else if (newCheck == 2) {
                BTN = rBtn_Full;
                BTN.setChecked(true);
                BTN.setTypeface(null, Typeface.BOLD);
            } else if (newCheck == 3) {
                BTN = rBtn_Charging_or_full;
                BTN.setChecked(true);
                BTN.setTypeface(null, Typeface.BOLD);
            } else if (newCheck == 4) {
                BTN = rBtn_Charging_or_full_blink;
                BTN.setChecked(true);
                BTN.setTypeface(null, Typeface.BOLD);
            } else if (newCheck == 5) {
                BTN = rBtn_USB;
                BTN.setChecked(true);
                BTN.setTypeface(null, Typeface.BOLD);
            } else if (newCheck == 6) {
                if (deviceName.contains(DEVICE_MOTO_X)) {
                    BTN = rBtn_Charging_adapter;
                    BTN.setChecked(true);
                    BTN.setTypeface(null, Typeface.BOLD);
                } else {
                    BTN = rBtn_Display;
                    BTN.setChecked(true);
                    BTN.setTypeface(null, Typeface.BOLD);
                }
            } else if (newCheck == 7) {
                BTN = rBtn_DiskIO;
                BTN.setChecked(true);
                BTN.setTypeface(null, Typeface.BOLD);
            } else if (newCheck == 8) {
                BTN = rBtn_ExtIO;
                BTN.setChecked(true);
                BTN.setTypeface(null, Typeface.BOLD);
            }
            if (BTN != null) lastSelectedRadioButtonId = BTN.getId();
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

    private boolean Charging() {
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
        if (getSharedPreferences(EXTRA_INFO_SHARED_PREF, 0).getBoolean(PREF_SET_ON_BOOT, false)) {    //in English: if user has enabled the option to set on boot
            menu.findItem(R.id.action_set_on_boot).setChecked(true);
        }
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
            if (item.isChecked()) {
                item.setChecked(false);
                getSharedPreferences(EXTRA_INFO_SHARED_PREF, 0).edit().putBoolean(PREF_SET_ON_BOOT, false).commit();
            } else {
                getSharedPreferences(EXTRA_INFO_SHARED_PREF, 0).edit().putBoolean(PREF_SET_ON_BOOT, true).commit();
                item.setChecked(true);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}