package com.aioros.aioros.interfaces;

import java.util.List;

public interface IPropertyDefinitionGroup {
    void removeDefinition(String var1);
    List<IPropertyDefinition> getDefinitions();
}
