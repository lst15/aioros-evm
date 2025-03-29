package com.aioros.aioros.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica que o método deve ser tratado como um método de inicialização customizada.
 *
 * Esta anotação é usada para marcar métodos que devem ser executados automaticamente
 * após a construção ou durante a fase de inicialização de um objeto em um ciclo de vida.
 *
 * Detalhes:
 * - **Target**: Pode ser aplicada apenas em métodos.
 * - **Retention**: Disponível em tempo de execução, permitindo o uso por reflexão.
 *
 * Possíveis Usos:
 * - Métodos anotados podem ser reconhecidos e executados automaticamente por frameworks
 *   ou containers para finalização ou configuração pós-construção.
 * - Ideal para inicialização de valores, registros, configurações ou outras tarefas específicas.
 *
 * Exemplos de Uso:
 * <pre>
 * public class MyClass {
 *
 *     @SerCustomInit
 *     public void initialize() {
 *         // Código de inicialização customizada
 *         System.out.println("Inicialização customizada executada.");
 *     }
 * }
 * </pre>
 *
 * @Target({ElementType.METHOD}) Indica que a anotação é aplicada somente a métodos.
 * @Retention(RetentionPolicy.RUNTIME) Disponível para processamento em tempo de execução.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerCustomInit {
}