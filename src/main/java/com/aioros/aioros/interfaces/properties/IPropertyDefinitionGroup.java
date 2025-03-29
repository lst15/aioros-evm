package com.aioros.aioros.interfaces.properties;

import java.util.List;

public interface IPropertyDefinitionGroup {
    void removeDefinition(String var1);
    List<IPropertyDefinition> getDefinitions();
}
