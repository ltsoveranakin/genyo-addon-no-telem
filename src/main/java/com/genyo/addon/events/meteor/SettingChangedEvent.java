package com.genyo.addon.events.meteor;

import meteordevelopment.meteorclient.settings.Setting;

public class SettingChangedEvent {

    private static final SettingChangedEvent INSTANCE = new SettingChangedEvent();

    public Setting<?> setting;

    public static SettingChangedEvent get(Setting<?> setting) {
        INSTANCE.setting = setting;

        return INSTANCE;
    }

}
