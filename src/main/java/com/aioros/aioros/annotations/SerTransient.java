package com.aioros.aioros.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indica que o campo deve ser tratado como "transitório" por processos personalizados.
 *
 * Esta anotação é usada para marcar campos que devem ser ignorados por algum sistema
 * ou framework durante operações específicas, como serialização ou armazenamento.
 *
 * Detalhes:
 * - **Target**: Pode ser aplicada apenas em campos de uma classe.
 * - **Retention**: Disponível em tempo de execução, permitindo o uso por mecanismos de reflexão.
 *
 * Exemplos de Uso:
 * - Ignorar um campo ao serializar/deserializar em JSON.
 * - Excluir o campo de persistência no banco de dados.
 *
 * @Target({ElementType.FIELD}) Indica que a anotação é aplicada somente em atributos.
 * @Retention(RetentionPolicy.RUNTIME) Disponível para processamento em tempo de execução.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerTransient {
}