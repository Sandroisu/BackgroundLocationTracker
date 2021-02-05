package ru.slatinin.backgroundlocationtracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static ru.slatinin.backgroundlocationtracker.LocationService.IS_NOTIFIED_ABOUT_BATTERY_SAVE;
import static ru.slatinin.backgroundlocationtracker.LocationService.LOCATION_SLA;
import static ru.slatinin.backgroundlocationtracker.LocationService.SHARED_PREFS;
import static ru.slatinin.backgroundlocationtracker.LocationService.formatMillisToHours;

public class MainActivity extends AppCompatActivity {

    private TextView locations;
    private LocationService locationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App app = (App) getApplication();
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                locationService = ((LocationService.Binder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                locationService = null;
            }
        };
        Button startService = findViewById(R.id.start_service);
        Button stopService = findViewById(R.id.stop_service);
        locations = findViewById(R.id.location_text);


        startService.setOnClickListener((v) -> {
            if (locationService == null) {
                Intent intent = new Intent();
                intent.setClass(this, LocationService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
                bindService(new Intent(getApplicationContext(), LocationService.class)
                        , serviceConnection, BIND_AUTO_CREATE);
            } else {
                Toast.makeText(app, "Сервис уже запущен", Toast.LENGTH_SHORT).show();
            }
        });
        stopService.setOnClickListener((v) -> {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            String prefAddress = sharedPreferences.getString(LOCATION_SLA, "");
            prefAddress = formatMillisToHours(System.currentTimeMillis()) + ": очищено";
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(LOCATION_SLA, prefAddress);
            editor.apply();
        });
        String manufacturer = android.os.Build.MANUFACTURER;
        if ("xiaomi".equalsIgnoreCase(manufacturer)) {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            boolean notifiedAboutBatterySave = sharedPreferences.getBoolean(IS_NOTIFIED_ABOUT_BATTERY_SAVE, false);
            if (!notifiedAboutBatterySave) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setPositiveButton("Понятно", (dialog, which) -> {
                    try {
                        Intent intent = new Intent();
                        ComponentName componentName = new ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity");
                        intent.setComponent(componentName);
                        intent.putExtra("package_name", getPackageName());
                        intent.putExtra("package_label", getText(R.string.app_name));
                        startActivityForResult(intent, 1411);
                    } catch (ActivityNotFoundException anfe) {
                    }
                });
                AlertDialog alert = adb.create();
                alert.setTitle("ВНИМАНИЕ");
                alert.setMessage("Обнаружено что на вашем устройстве присутствует режим энергосбережения. Необходимо отключить его для нашего приложения");
                alert.setCancelable(false);
                alert.show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1411) {
            if (resultCode == RESULT_OK) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(IS_NOTIFIED_ABOUT_BATTERY_SAVE, true);
                editor.apply();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String prefAddress = sharedPreferences.getString(LOCATION_SLA, "");
        locations.setText(prefAddress);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


}