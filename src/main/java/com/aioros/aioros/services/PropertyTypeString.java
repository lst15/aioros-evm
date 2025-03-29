package com.aioros.aioros.services;

import com.aioros.aioros.interfaces.IPropertyTypeString;

import java.util.Map;
import java.util.WeakHashMap;

public class PropertyTypeString implements IPropertyTypeString {
    private static Map<PropertyTypeString, PropertyTypeString> map = new WeakHashMap();

    private int min;
    private int max;
    private String def;

    private PropertyTypeString(int var1, int var2, String var3) {
        this.min = var1;
        this.max = var2;
        this.def = var3;
    }

    /**
     * Método de fábrica que cria e gerencia instâncias da classe PropertyTypeString.
     *
     * Este método utiliza um padrão Flyweight com auxílio de um WeakHashMap para evitar a criação de objetos duplicados.
     * Caso uma instância equivalente já exista no cache, ela será reutilizada; caso contrário, uma nova instância será criada
     * e armazenada no cache. Isso promove o uso eficiente de memória e melhora a performance da aplicação.
     *
     * @param min Valor mínimo permitido para o comprimento do valor padrão. Deve ser maior ou igual a 0.
     * @param max Valor máximo permitido para o comprimento do valor padrão. Deve ser maior ou igual a `min`.
     * @param defaultValue O valor padrão que será associado à instância. Não pode ser nulo e seu comprimento deve estar
     *                     dentro do intervalo definido por [min, max].
     *
     * @return Uma instância de PropertyTypeString baseada nos parâmetros fornecidos. O objeto retornado pode ser
     *         uma nova instância ou uma instância reutilizada do cache.
     *
     * @throws RuntimeException Se o valor padrão (defaultValue) for nulo.
     * @throws IllegalArgumentException Se o comprimento do valor padrão estiver fora do intervalo [min, max].
     * @throws IllegalArgumentException Se o valor mínimo (min) for negativo.
     */
    public static PropertyTypeString createOrReuse(int min, int max, String defaultValue) {

        if (defaultValue == null) {
            throw new RuntimeException("O valor padrão (defaultValue) não pode ser nulo.");
        }

        // Verifica se o comprimento do valor padrão está dentro do intervalo permitido
        if (defaultValue.length() < min || defaultValue.length() > max) {
            throw new IllegalArgumentException("O valor padrão está fora do intervalo permitido.");
        }

        if (min < 0) {
            throw new IllegalArgumentException("O valor mínimo (min) não pode ser negativo.");
        }

        // Cria uma nova instância do objeto para usar como potencial chave no cache
        PropertyTypeString newProperty = new PropertyTypeString(min, max, defaultValue);

        // Tenta recuperar uma instância existente do cache
        PropertyTypeString cachedProperty = map.get(newProperty);

        // Se nenhuma instância equivalente for encontrada no cache, armazena a nova instância
        if (cachedProperty == null) {
            cachedProperty = newProperty;
            map.put(newProperty, newProperty);
        }

        // Retorna a instância encontrada ou recém-criada
        return cachedProperty;
    }
}