/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.lineageos.device.DeviceSettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SELinux;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

import org.lineageos.device.DeviceSettings.FileUtils;
import org.lineageos.device.DeviceSettings.Constants;
import org.lineageos.device.DeviceSettings.R;
import org.lineageos.device.DeviceSettings.SuShell;
import org.lineageos.device.DeviceSettings.SuTask;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_CATEGORY_GRAPHICS = "graphics";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_AUTO_HBM_SWITCH = "auto_hbm";
    public static final String KEY_AUTO_HBM_THRESHOLD = "auto_hbm_threshold";
    public static final String KEY_DC_SWITCH = "dc";
    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_DCI_SWITCH = "dci";
    public static final String KEY_WIDE_SWITCH = "wide";

    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_CALL_VIBSTRENGTH = "vib_call_strength";
    public static final String KEY_NOTIF_VIBSTRENGTH = "vib_notif_strength";

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";

    private static final String SELINUX_CATEGORY = "selinux";
    private static final String PREF_SELINUX_MODE = "selinux_mode";
    private static final String PREF_SELINUX_PERSISTENCE = "selinux_persistence";

    private static TwoStatePreference mHBMModeSwitch;
    private static TwoStatePreference mAutoHBMSwitch;
    private static TwoStatePreference mDCModeSwitch;
    private ListPreference mTopKeyPref;
    private ListPreference mMiddleKeyPref;
    private ListPreference mBottomKeyPref;
    private VibratorStrengthPreference mVibratorStrength;
    private VibratorCallStrengthPreference mVibratorCallStrength;
    private VibratorNotifStrengthPreference mVibratorNotifStrength;
    private SwitchPreference mSelinuxMode;
    private SwitchPreference mSelinuxPersistence;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        addPreferencesFromResource(R.xml.main);

        Resources res = getResources();
        Window win = getActivity().getWindow();

        win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        win.setNavigationBarColor(res.getColor(R.color.primary_color));
        win.setNavigationBarDividerColor(res.getColor(R.color.primary_color));

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength != null)
            mVibratorStrength.setEnabled(VibratorStrengthPreference.isSupported());
        mVibratorCallStrength = (VibratorCallStrengthPreference) findPreference(KEY_CALL_VIBSTRENGTH);
        if (mVibratorCallStrength != null)
            mVibratorCallStrength.setEnabled(VibratorCallStrengthPreference.isSupported());
        mVibratorNotifStrength = (VibratorNotifStrengthPreference) findPreference(KEY_NOTIF_VIBSTRENGTH);
        if (mVibratorNotifStrength != null)
            mVibratorNotifStrength.setEnabled(VibratorNotifStrengthPreference.isSupported());

        mTopKeyPref = (ListPreference) findPreference(Constants.NOTIF_SLIDER_TOP_KEY);
        mTopKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_TOP_KEY));
        mTopKeyPref.setOnPreferenceChangeListener(this);
        mMiddleKeyPref = (ListPreference) findPreference(Constants.NOTIF_SLIDER_MIDDLE_KEY);
        mMiddleKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_MIDDLE_KEY));
        mMiddleKeyPref.setOnPreferenceChangeListener(this);
        mBottomKeyPref = (ListPreference) findPreference(Constants.NOTIF_SLIDER_BOTTOM_KEY);
        mBottomKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_BOTTOM_KEY));
        mBottomKeyPref.setOnPreferenceChangeListener(this);

        mDCModeSwitch = (TwoStatePreference) findPreference(KEY_DC_SWITCH);
        mDCModeSwitch.setEnabled(DCModeSwitch.isSupported());
        mDCModeSwitch.setChecked(DCModeSwitch.isCurrentlyEnabled(this.getContext()));
        mDCModeSwitch.setOnPreferenceChangeListener(new DCModeSwitch());

        mHBMModeSwitch = (TwoStatePreference) findPreference(KEY_HBM_SWITCH);
        mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported());
        mHBMModeSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_HBM_SWITCH, false));
        mHBMModeSwitch.setOnPreferenceChangeListener(this);

        mAutoHBMSwitch = (TwoStatePreference) findPreference(KEY_AUTO_HBM_SWITCH);
        mAutoHBMSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_AUTO_HBM_SWITCH, false));
        mAutoHBMSwitch.setOnPreferenceChangeListener(this);

        // SELinux
        boolean isRooted = SuShell.detectValidSuInPath();
        Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
        mSelinuxMode = (SwitchPreference) findPreference(PREF_SELINUX_MODE);
        mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());
        mSelinuxMode.setOnPreferenceChangeListener(this);
        mSelinuxMode.setEnabled(isRooted);

        mSelinuxPersistence =
        (SwitchPreference) findPreference(PREF_SELINUX_PERSISTENCE);
        mSelinuxPersistence.setOnPreferenceChangeListener(this);
        mSelinuxPersistence.setChecked(getContext()
        .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
        .contains(PREF_SELINUX_MODE));
        mSelinuxPersistence.setEnabled(isRooted);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAutoHBMSwitch) {
            Boolean enabled = (Boolean) newValue;
            SharedPreferences.Editor prefChange = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefChange.putBoolean(KEY_AUTO_HBM_SWITCH, enabled).commit();
            Utils.enableService(getContext());
        } else if (preference == mSelinuxMode) {
            boolean enabled = (Boolean) newValue;
            new SwitchSelinuxTask(getActivity()).execute(enabled);
            setSelinuxEnabled(enabled, mSelinuxPersistence.isChecked());
        } else if (preference == mSelinuxPersistence) {
            setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) newValue);
        } else if (preference == mHBMModeSwitch) {
            Boolean enabled = (Boolean) newValue;
            Utils.writeValue(HBMModeSwitch.getFile(), enabled ? "5" : "0");
            Intent hbmIntent = new Intent(this.getContext(),
                    org.lineageos.device.DeviceSettings.HBMModeService.class);
            if (enabled) {
                this.getContext().startService(hbmIntent);
            } else {
                this.getContext().stopService(hbmIntent);
            }
        } else {
            Constants.setPreferenceInt(getContext(), preference.getKey(),
                    Integer.parseInt((String) newValue));
        }
        return true;
    }

    public static boolean isHBMModeService(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_HBM_SWITCH, false);
    }

    public static boolean isAUTOHBMEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_AUTO_HBM_SWITCH, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSelinuxEnabled(boolean status, boolean persistent) {
      SharedPreferences.Editor editor = getContext()
          .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
      if (persistent) {
        editor.putBoolean(PREF_SELINUX_MODE, status);
      } else {
        editor.remove(PREF_SELINUX_MODE);
      }
      editor.apply();
      mSelinuxMode.setChecked(status);
    }

    private class SwitchSelinuxTask extends SuTask<Boolean> {
      public SwitchSelinuxTask(Context context) {
        super(context);
      }
      @Override
      protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
        if (params.length != 1) {
          return;
        }
        if (params[0]) {
          SuShell.runWithSuCheck("setenforce 1");
        } else {
          SuShell.runWithSuCheck("setenforce 0");
        }
      }

      @Override
      protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (!result) {
          // Did not work, so restore actual value
          setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
        }
      }
    }
}
