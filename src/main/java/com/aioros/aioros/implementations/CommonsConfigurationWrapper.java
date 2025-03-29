package com.aioros.aioros.implementations;

import com.aioros.aioros.interfaces.IConfiguration;
import org.apache.commons.configuration2.Configuration;

public class CommonsConfigurationWrapper implements IConfiguration {
    private Configuration cfg;

    public CommonsConfigurationWrapper(Configuration var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("Provide a configuration ApacheCommons configuration object");
        } else {
            this.cfg = var1;
        }
    }
}
