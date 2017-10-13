package com.halite.util.haliterelay;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // RelayService가 실행되고 있는지 체크해 실행되지 않고 있으면 실행해 준다
        if (!isMyServiceRunning(RelayService.class)) {
            startService(new Intent(getBaseContext(), RelayService.class));
        }
        else {
            Toast.makeText(this, "RelayService is already running.", Toast.LENGTH_SHORT).show();
        }

        // NotificationListener가 실행되고 있는지 체크해 실행되지 않고 있으면 실행해 준다
        if (!isMyServiceRunning(NotificationListener.class)) {
            startService(new Intent(getBaseContext(), NotificationListener.class));
        }
        else {
            Toast.makeText(this, "NotificationListener is already running.", Toast.LENGTH_SHORT).show();
        }

        _loadSettings();
    }

    // check the service is already running or not
    // return true - the service is already running
    // return false - the service is not running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void restartServiceBtn_Click(View v) {
        Intent intent = new Intent(getBaseContext(), RelayService.class);
        stopService(intent);
        startService(intent);

        intent = new Intent(getBaseContext(), NotificationListener.class);
        stopService(intent);
        startService(intent);
    }

    private void _loadSettings() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this); //getPreferences(MODE_PRIVATE);

        // phone number
        EditText pnet = (EditText)findViewById(R.id.phoneNumberEditText);
        pnet.setText(sp.getString(Config.PHONE_NUMBER, ""));

        // check boxes
        CheckBox cb = (CheckBox)findViewById(R.id.phoneCallCheckBox);
        cb.setChecked(sp.getBoolean(Config.PHONE_CALL, false));
        cb = (CheckBox)findViewById(R.id.smsCheckBox);
        cb.setChecked(sp.getBoolean(Config.SMS, false));
        cb = (CheckBox)findViewById(R.id.emailCheckBox);
        cb.setChecked(sp.getBoolean(Config.EMAIL, false));
        cb = (CheckBox)findViewById(R.id.kakaoTalkCheckBox);
        cb.setChecked(sp.getBoolean(Config.KAKAOTALK, false));
    }

    public void saveSettingsBtn_Click(View v) {
        _saveSettings();

    }

    private void _saveSettings() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this); //getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();

        // phone number
        EditText pnet = (EditText)findViewById(R.id.phoneNumberEditText);
        edit.putString(Config.PHONE_NUMBER, pnet.getText().toString());

        // check boxes
        CheckBox cb = (CheckBox)findViewById(R.id.phoneCallCheckBox);
        edit.putBoolean(Config.PHONE_CALL, cb.isChecked());
        cb = (CheckBox)findViewById(R.id.smsCheckBox);
        edit.putBoolean(Config.SMS, cb.isChecked());
        cb = (CheckBox)findViewById(R.id.emailCheckBox);
        edit.putBoolean(Config.EMAIL, cb.isChecked());
        cb = (CheckBox)findViewById(R.id.kakaoTalkCheckBox);
        edit.putBoolean(Config.KAKAOTALK, cb.isChecked());

        edit.apply();

        _loadSettingsInService();
    }

    private void _loadSettingsInService() {
        Intent smsIntent = new Intent(this, RelayService.class);
        smsIntent.putExtra(RelayService.CMD_LOAD_SETTINGS, "");
        this.startService(smsIntent);
    }
}
