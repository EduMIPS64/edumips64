package org.edumips64.utils.cli;

import org.edumips64.utils.MetaInfo;
import picocli.CommandLine;

public class Version implements CommandLine.IVersionProvider {

    public final static String versionInfo =
            "EduMIPS64 version " + MetaInfo.VERSION + " (codename: " + MetaInfo.CODENAME + ", git revision " + MetaInfo.FULL_BUILDSTRING + ", built on " + MetaInfo.BUILD_DATE + ") - Ciao 'mbare.";

    @Override
    public String[] getVersion() {
        return new String[]{versionInfo};
    }
}
