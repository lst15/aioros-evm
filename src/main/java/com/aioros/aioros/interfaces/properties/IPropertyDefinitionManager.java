package com.aioros.aioros.interfaces.properties;

public interface IPropertyDefinitionManager {
    IPropertyDefinition addInternalDefinition(String propertyName, IPropertyType propertyType);
    boolean registerChild(IPropertyDefinitionManager var1);
    String getRegion();
    String getNamespace();
}
