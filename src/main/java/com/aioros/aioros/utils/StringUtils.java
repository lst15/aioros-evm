package com.aioros.aioros.utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

public class StringUtils {

    /**
     * Retorna uma representação em string segura de um objeto.
     *
     * Este método verifica se o objeto fornecido não é nulo. Caso não seja, ele retorna
     * o valor da chamada `toString()` do objeto. Caso contrário, retorna uma string vazia.
     *
     * Utilização:
     * - Este método é útil para evitar exceções (`NullPointerException`) quando objetos
     *   podem ser nulos, especialmente ao exibi-los em logs ou interfaces de usuário.
     *
     * @param var0 O objeto a ser convertido em string.
     * @return A representação em string do objeto, ou uma string vazia se o objeto for nulo.
     */
    public static String safe(Object var0) {
        return var0 != null ? var0.toString() : "";
    }

    public static String ff(String var0, Object... var1) {
        return ff((Locale)null, (Appendable)null, var0, var1).toString();
    }

    /**
     * Retorna uma representação segura em string de um objeto, com um valor padrão.
     *
     * Este método verifica se um objeto é nulo ou se sua representação em string está vazia.
     * Caso o objeto seja nulo ou tenha uma string vazia, retorna o valor padrão especificado.
     * Se o valor padrão for `null` ou vazio, uma exceção será lançada.
     *
     * Regras:
     * 1. Se o valor padrão (`var1`) for nulo ou vazio, lança uma `IllegalArgumentException`.
     * 2. Se o objeto (`var0`) for nulo, retorna o valor padrão.
     * 3. Se a representação em string do objeto for vazia, retorna o valor padrão.
     * 4. Caso contrário, retorna a string gerada pelo método `toString()` do objeto.
     *
     * Utilização:
     * - Este método é útil para evitar `NullPointerException` e fornecer valores padrão
     *   em casos onde variáveis podem ser nulas ou vazias.
     *
     * @param var0 O objeto a ser convertido em string.
     * @param var1 O valor padrão a ser retornado caso o objeto seja nulo ou vazio.
     * @return A representação em string do objeto, ou o valor padrão se ele for nulo ou vazio.
     * @throws IllegalArgumentException Se o valor padrão fornecido for nulo ou vazio.
     */
    public static String safe2(Object value, String defaultValue) {
        if (defaultValue == null || defaultValue.isEmpty()) {
            throw new IllegalArgumentException("Default value cannot be null or empty");
        }

        if (value == null) {
            return defaultValue;
        }

        String stringValue = value.toString();
        return stringValue.isEmpty() ? defaultValue : stringValue;
    }


    /**
     * Formata uma string personalizada para um objeto do tipo `Appendable` usando especificadores de formato.
     *
     * Suporta especificadores como `%s`, `%d`, `%X`, `%x`, `%c`, `%b` etc., com opções adicionais
     * para manipulação de alinhamento, hexadecimais e formatos personalizados.
     *
     * @param locale O locale a ser usado.
     * @param appendable O objeto `Appendable` (ex.: StringBuilder) no qual o texto será adicionado.
     * @param format A string de formato, contendo especificadores (%s, %d, etc.).
     * @param args Os objetos que servirão como parâmetros do formato.
     * @return O mesmo objeto `Appendable` com o texto formatado.
     * @throws RuntimeException Se houver inconsistências no formato ou exceções de I/O.
     */
    public static Appendable ff(Locale locale, Appendable appendable, String format, Object... args) {
        try {
            // Inicialização da estrutura de string formatada
            StringBuilder result = new StringBuilder(format.length() + (args.length * 16));

            int formatIndex = 0;    // Índice do caractere da string de formato
            int argsIndex = 0;      // Índice dos argumentos

            // Iterar sobre cada caractere da string de formato
            while (formatIndex < format.length()) {
                char currentChar = format.charAt(formatIndex);

                if (currentChar == '%') {
                    // Processar especificadores de formato
                    formatIndex = processFormatSpecifier(format, formatIndex, args, argsIndex, result);
                    argsIndex++; // Incrementar após usar o argumento
                } else {
                    // Adicionar caracteres normais ao resultado
                    result.append(currentChar);
                }
                formatIndex++;
            }

            // Validar se todos os argumentos foram usados
            if (argsIndex != args.length) {
                throw new RuntimeException("Nem todos os parâmetros foram usados! format=" + format + ", params=" + Arrays.toString(args));
            }

            // Adicionar o resultado ao Appendable fornecido (se não for null)
            if (appendable != null) {
                appendable.append(result);
                return appendable;
            }

            // Retornar o StringBuilder preenchido se appendable for null
            return result;

        } catch (IOException ex) {
            throw new RuntimeException("Erro de I/O durante a formatação", ex);
        } catch (Exception ex) {
            // Capturar exceções genéricas e usar fallback
            return formatUsingFallback(locale, appendable, format, args);
        }
    }

    /**
     * Processa um especificador de formato e o substitui pelo valor correspondente.
     *
     * @param format A string de formato contendo os especificadores.
     * @param index O índice atual do caractere na string de formato.
     * @param args Os argumentos fornecidos para o formato.
     * @param argsIndex O índice do argumento atual.
     * @param result O `StringBuilder` onde o resultado deve ser adicionado.
     * @return O novo índice após processar o especificador.
     * @throws RuntimeException Se houver inconsistência nos especificadores.
     */
    private static int processFormatSpecifier(String format, int index, Object[] args, int argsIndex, StringBuilder result) {
        if (index + 1 >= format.length()) {
            throw new RuntimeException("Especificador de formato incompleto no índice " + index);
        }

        char specifier = format.charAt(index + 1); // Caractere após o '%'
        Object argument = args[argsIndex];        // Argumento correspondente

        switch (specifier) {
            case '%':
                result.append('%'); // Literal '%'
                break;

            case 's':
                result.append(argument != null ? argument.toString() : "null"); // String
                break;

            case 'd':
                appendNumber(argument, result); // Número decimal
                break;

            case 'X': // Hexadecimal em maiúsculas
            case 'x': // Hexadecimal em minúsculas
                appendHexadecimal(argument, result, specifier == 'X');
                break;

            case 'c': // Caracter
                appendCharacter(argument, result);
                break;

            case 'b': // Booleano
                result.append((argument instanceof Boolean) ? argument : Boolean.toString(false));
                break;

            case 'n': // Nova linha
                result.append(System.lineSeparator());
                break;

            default:
                throw new RuntimeException("Especificador de formato desconhecido: %" + specifier);
        }

        return index + 1; // Avançar o índice após o especificador
    }

    /**
     * Adiciona um número ao resultado, lidando com diferentes tipos numéricos.
     */
    private static void appendNumber(Object argument, StringBuilder result) {
        if (argument instanceof Number) {
            result.append(argument);
        } else {
            throw new IllegalArgumentException("Esperava-se um número, mas foi fornecido: " + argument);
        }
    }

    /**
     * Adiciona um valor hexadecimal ao resultado.
     *
     * @param argument O argumento a ser convertido em hexadecimal.
     * @param result O `StringBuilder` onde o valor será adicionado.
     * @param uppercase Se true, o valor será em letras maiúsculas; caso contrário, em minúsculas.
     */
    private static void appendHexadecimal(Object argument, StringBuilder result, boolean uppercase) {
        if (argument instanceof Number) {
            String hex = (argument instanceof Long)
                    ? Long.toHexString(((Number) argument).longValue())
                    : Integer.toHexString(((Number) argument).intValue());

            result.append(uppercase ? hex.toUpperCase() : hex.toLowerCase());
        } else {
            throw new IllegalArgumentException("Esperava-se um número para conversão hexadecimal, mas foi fornecido: " + argument);
        }
    }

    /**
     * Adiciona um caractere ao resultado, lidando com conversões de tipos.
     */
    private static void appendCharacter(Object argument, StringBuilder result) {
        if (argument instanceof Character) {
            result.append(argument);
        } else if (argument instanceof Number) {
            result.append((char) ((Number) argument).intValue());
        } else {
            throw new IllegalArgumentException("Esperava-se um caractere, mas foi fornecido: " + argument);
        }
    }

    /**
     * Usa um fallback formatador para situações de erro.
     */
    private static Appendable formatUsingFallback(Locale locale, Appendable appendable, String format, Object... args) {
        try {
            String formatted = String.format(locale, format, args);
            if (appendable != null) {
                appendable.append(formatted);
                return appendable;
            }
            return new StringBuilder(formatted);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
