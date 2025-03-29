package com.aioros.aioros.interfaces;

public interface IPropertyDefinitionManager {
//    IPropertyDefinition addInternalDefinition(String var1, IPropertyType var2);
    boolean registerChild(IPropertyDefinitionManager var1);
    String getRegion();
    String getNamespace();
}
