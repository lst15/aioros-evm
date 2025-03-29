package com.aioros.aioros.implementations.properties;


import com.aioros.aioros.interfaces.properties.IPropertyDefinition;
import com.aioros.aioros.interfaces.properties.IPropertyType;
import com.aioros.aioros.utils.StringUtils;

public class PropertyDefinition implements IPropertyDefinition {
    private PropertyDefinitionManager manager;
    private String name;
    private String description;
    private IPropertyType type;
    private int flags;

    PropertyDefinition(PropertyDefinitionManager var1, String var2, String var3, IPropertyType var4, int var5) {
        this.manager = var1;
        this.name = var2;
        this.description = var3;
        this.type = var4;
        this.flags = var5;
    }

    public PropertyDefinitionManager getManager() {
        return this.manager;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return StringUtils.safe(this.description);
    }

    public IPropertyType getType() {
        return this.type;
    }

    public int getFlags() {
        return this.flags;
    }

    public String toString() {
        return StringUtils.ff("%s(%s)", new Object[]{this.name, this.type});
    }
}

