package com.genyo.systems.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.util.Util;

public class ChangelogScreen extends WindowScreen {

    private int statusCode;

    public ChangelogScreen(GuiTheme theme) {
        super(theme, "Genyo Changelog");

        locked = true;
        lockedAllowClose = true;

        MeteorExecutor.execute(() -> {

        });
    }

    @Override
    public void initWidgets() {
        // jwehfwejkfhewkjfjkwehfhewkjfhjwe
    }

    private void populateHeader(String headerMessage) {
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        l.add(theme.label(headerMessage)).expandX();

        String website = "https://genyo.dev";
        l.add(theme.button("Website")).widget().action = () -> Util.getOperatingSystem().open(website);

        l.add(theme.button("GitHub")).widget().action = () -> Util.getOperatingSystem().open("https://github.com/wuritz/genyo-addon");
    }

    private void populateError() {
        String errorMessage = switch (statusCode) {
            case Http.BAD_REQUEST -> "Connection dropped";
            case Http.UNAUTHORIZED -> "Unauthorized";
            case Http.FORBIDDEN -> "Rate-limited";
            case Http.NOT_FOUND -> "Invalid commit hash";
            default -> "Error Code: " + statusCode;
        };

        populateHeader("There was an error fetching commits: " + errorMessage);

        if (statusCode == Http.UNAUTHORIZED) {
            add(theme.horizontalSeparator()).padVertical(theme.scale(8)).expandX();
            WHorizontalList l = add(theme.horizontalList()).expandX().widget();

            l.add(theme.label("Consider using an authentication token: ")).expandX();
            l.add(theme.button("Authorization Guide")).widget().action = () -> {
                Util.getOperatingSystem().open("https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens");
            };
        }

        locked = false;
    }
}
