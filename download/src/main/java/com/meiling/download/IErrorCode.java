package com.meiling.download;

/**
 * Created by Administrator on 2017/8/2.
 */

public interface IErrorCode {
    int NO_PERMISSION_INTERNET = 1;
    int NO_PERMISSION_READ_EX_STORAGE = 2;
    int NO_PERMISSION_INTERNET_READ_EX_STORAGE = 3;
    int NO_PERMISSION_WRITE_EX_STORAGE = 4;
    int NO_PERMISSION_INTERNET_WRITE_EX_STORAGE = 5;
    int NO_PERMISSION_READ_WRITE_EX_STORAGE = 6;
    int NO_PERMISSION_INTERNET_READ_WRITE_EX_STORAGE = 7;
    int NO_PERMISSION_READ_PHONE_STATE = 8;
    int NO_PERMISSION_WAKE_LOCK = 9;
    int NO_PERMISSION_VIBRATE = 10;
    int NO_PERMISSION_CALL_PHONE = 11;
    int NO_PERMISSION_DISABLE_KEYGUARD = 12;
    int NO_PERMISSION_RECORD_AUDIO = 13;
    int NO_PERMISSION_MODIFY_AUDIO_SETTINGS = 14;
    int NO_PERMISSION_FLASHLIGHT = 15;
    int NO_PERMISSION_CAMERA = 16;
    int NO_PERMISSION_ACCESS_NETWORK_STATE = 17;
    int NO_PERMISSION_CHANGE_NETWORK_STATE = 18;
    int NO_PERMISSION_ACCESS_WIFI_STATE = 19;
    int NO_PERMISSION_CHANGE_WIFI_STATE = 20;
    int NO_PERMISSION_MOUNT_UNMOUNT_FILESYSTEMS = 21;
    int NO_PERMISSION_SYSTEM_ALERT_WINDOW = 22;
    int NO_PERMISSION_WRITE_SETTINGS = 23;
    int NO_PERMISSION_RECEIVE_BOOT_COMPLETED = 24;
    int NO_PERMISSION_READ_CONTACTS = 25;
    int NO_PERMISSION_WRITE_CONTACTS = 26;
    int NO_PERMISSION_ACCESS_COARSE_LOCATION = 27;
    int NO_PERMISSION_ACCESS_FINE_LOCATION = 28;
    int NO_PERMISSION_ACCESS_LOCATION_EXTRA_COMMANDS = 29;



    int ERROR_SERVER_CONNECTION = 30;
    int ERROR_PROTOCOL = 31;
    int ERROR_MALFORMEDURL = 32;

    /**
     * VIBRATE
     * MOUNT_UNMOUNT_FILESYSTEMS
     *
     *
     * SYSTEM_ALERT_WINDOW
     * WRITE_SETTINGS
     *
     * CALL_PHONE
     * DISABLE_KEYGUARD
     *
     * RECORD_AUDIO
     * MODIFY_AUDIO_SETTINGS
     *
     * RECEIVE_BOOT_COMPLETED
     *
     *
     * READ_CONTACTS
     * WRITE_CONTACTS
     *
     * CHANGE_WIFI_STATE
     * ACCESS_WIFI_STATE
     *
     * CHANGE_NETWORK_STATE
     * ACCESS_NETWORK_STATE
     *
     * CAMERA
     * FLASHLIGHT
     *
     * ACCESS_FINE_LOCATION
     * ACCESS_COARSE_LOCATION
     * ACCESS_LOCATION_EXTRA_COMMANDS
     */
}
