package org.edumips64.utils.args;

import org.edumips64.utils.MetaInfo;
import picocli.CommandLine;

public class EduMipsVersion implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() {
        return new String[]{"EduMIPS64 version " + MetaInfo.VERSION +
                " (codename: " + MetaInfo.CODENAME +
                ", git revision " + MetaInfo.FULL_BUILDSTRING +
                ", built on " + MetaInfo.BUILD_DATE +
                ") - Ciao 'mbare."};
    }
}
