package com.aioros.aioros.implementations;

import com.aioros.aioros.annotations.SerCustomInit;
import com.aioros.aioros.annotations.SerTransient;
import com.aioros.aioros.interfaces.IEvent;
import com.aioros.aioros.interfaces.IEventListener;
import com.aioros.aioros.interfaces.IEventSource;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A classe `EventSource` implementa a interface `IEventSource` e fornece uma estrutura para gerenciamento de eventos
 * em um sistema baseado em listeners. Ela permite registrar, notificar e organizar listeners, além de propagar eventos
 * em hierarquias de fontes (parent sources).
 *
 * Principais Funcionalidades:
 * 1. **Gerenciamento de Listeners**:
 *    - Métodos para adicionar, remover, contar e recuperar listeners.
 *    - Listeners registrados são notificados automaticamente quando um evento ocorre.
 *
 * 2. **Notificação de Eventos**:
 *    - Métodos para notificar todos os listeners registrados.
 *    - Suporte para eventos genéricos (`IEvent`) e hierárquicos com propagação para `parentSource` (instâncias relacionadas).
 *    - Possibilidade de interromper a propagação de eventos com a flag `stopPropagation` nos eventos.
 *
 * 3. **Propagação de Eventos em Hierarquias**:
 *    - Capacidade de propagar eventos emitidos para um `parentSource` associado, permitindo suporte a estruturas hierárquicas.
 *    - Proteções para evitar notificações redundantes ou loops de propagação em hierarquias.
 *
 * 4. **Retransmissão de Eventos**:
 *    - Possui um mecanismo para conectar dois objetos `IEventSource`, retransmitindo eventos disparados de um para o outro.
 *
 * Estrutura Geral:
 * - Atributos:
 *   - `listeners`: Lista que armazena objetos `IEventListener` registrados.
 *   - `parentSource`: Referência a uma fonte pai para suportar a propagação de eventos.
 * - Anotações:
 *   - `@SerTransient`: Marca campos que não devem ser serializados.
 *   - `@SerCustomInit`: Executa a inicialização de atributos marcados durante o ciclo de vida do objeto.
 * - Inicialização:
 *   - A lista de listeners é configurada como `CopyOnWriteArrayList` para garantir segurança em acessos concorrentes.
 *
 * Possíveis Usos:
 * - Implementação de sistemas baseados em eventos para comunicar mudanças de estado.
 * - Hierarquias complexas, onde eventos de uma fonte devem ser propagados para um pai ou retransmitidos para outras fontes.
 * - Integração em arquiteturas de aplicações, como frameworks de interface gráfica ou sistemas distribuídos.
 *
 * Exemplos:
 * - Registrar, notificar e propagar eventos:
 *   <pre>
 *   EventSource source = new EventSource();
 *   source.addListener(event -> System.out.println("Evento recebido: " + event));
 *   source.notifyListeners(new CustomEvent());
 *   </pre>
 *
 * - Configurar propagação hierárquica:
 *   <pre>
 *   EventSource child = new EventSource();
 *   EventSource parent = new EventSource();
 *   child.setParentSource(parent);
 *   child.notifyListeners(new CustomEvent()); // Propaga o evento ao parent
 *   </pre>
 */
public class EventSource implements IEventSource {
    @SerTransient
    private List<IEventListener> listeners;
    @SerTransient
    private volatile IEventSource parentSource;

    @SerCustomInit
    private void init() {
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public EventSource() {
        this((EventSource)null);
    }

    public EventSource(EventSource var1) {
        this.parentSource = var1;
        this.init();
    }

    public void setParentSource(IEventSource var1) {
        this.parentSource = var1;
    }

    public IEventSource getParentSource() {
        return this.parentSource;
    }

    public int countListeners() {
        return this.listeners.size();
    }

    public List<IEventListener> getListeners() {
        return Collections.unmodifiableList(this.listeners);
    }

    public void addListener(IEventListener eventListener) {
        if (eventListener != null) {
            this.listeners.add(eventListener);
        }

    }

    public void insertListener(int var1, IEventListener eventListener) {
        if (eventListener != null) {
            this.listeners.add(var1, eventListener);
        }

    }

    public void removeListener(IEventListener eventListener) {
        if (eventListener != null) {
            this.listeners.remove(eventListener);
        }

    }

    /**
     * Notifica todos os listeners registrados com base no tipo do evento fornecido.
     *
     * O método verifica se o evento fornecido é uma instância da classe `Event`.
     * - Caso não seja, ele notifica todos os listeners diretamente com o evento.
     * - Caso seja, ele delega a notificação ao método sobrecarregado `notifyListeners(Event)`, que trata eventos mais específicos.
     *
     * Parâmetro:
     * @param event O evento a ser notificado, implementando a interface `IEvent`.
     */
    public void notifyListeners(IEvent event) {
        // Verifica se o evento não é uma instância da classe Event
        if (!(event instanceof Event)) {
            // Notifica todos os listeners diretamente
            for (IEventListener listener : this.listeners) {
                listener.onEvent(event);
            }
        } else {
            // Delega a notificação ao método específico para eventos do tipo Event
            this.notifyListeners((Event) event);
        }
    }

    public void notifyListeners(Event var1) {
        this.notifyListeners(var1, true);
    }

    /**
     * Notifica todos os listeners registrados para este evento e propaga o evento ao pai, se aplicável.
     *
     * Este método distribui o evento para todos os listeners registrados neste `EventSource`. Além disso,
     * ele pode propagar o evento para um `parentSource` (se existir e for válido), permitindo hierarquias
     * de propagação de eventos.
     *
     * Regras de Funcionamento:
     * 1. **Inicialização do campo `source`**:
     *    - Se o evento ainda não tiver definido quem é sua fonte original (`source`), assume-se que a fonte
     *      é este objeto (`this`).
     *
     * 2. **Propagação ao parentSource**:
     *    - Se `var2` (propagar ao pai) for `true`, e existir um `parentSource` configurado:
     *      - O evento é retransmitido ao `parentSource`, desde que:
     *        - `parentSource` não seja o próprio objeto atual.
     *        - `parentSource` ainda não tenha sido notificado para o evento em questão.
     *
     * 3. **Notificação de Listeners Locais**:
     *    - Cada listener registrado neste `EventSource` será notificado por meio de `onEvent`.
     *    - Listeners que já tenham sido notificados para o evento atual são ignorados.
     *    - Se o evento possuir a flag `stopPropagation` definida como `true`, o loop de notificação é interrompido.
     *
     * @param event O evento a ser emitido, do tipo `Event`.
     * @param propagateToParent Define se o evento deve ser propagado ao `parentSource`.
     */
    public void notifyListeners(Event event, boolean propagateToParent) {
        // 1. Verifica se o campo `source` do evento está definido; se não estiver, define-o como este `EventSource`.
        if (event.source == null) {
            event.source = this;
        }

        // 2. Propaga o evento para o parentSource, se aplicável e válido.
        if (propagateToParent
                && this.parentSource != null
                && this.parentSource != this
                && event.source != this.parentSource
                && !event.notifiedParents.contains(this.parentSource)) {

            // Adicionar o parentSource à lista de fontes notificadas para evitar notificações duplicadas
            event.notifiedParents.add(this.parentSource);

            // Notificar o parentSource com o evento atual
            this.parentSource.notifyListeners(event);
        }

        // 3. Notifica os listeners registrados neste `EventSource`
        for (IEventListener listener : this.listeners) {
            // Notifica apenas listeners que ainda não foram notificados para este evento
            if (event.source != listener && !event.notifiedListeners.contains(listener)) {
                // Adicionar o listener à lista de notificados
                event.notifiedListeners.add(listener);

                // Notificar o listener
                listener.onEvent(event);

                // Interrompe a propagação, se solicitado
                if (event.stopPropagation) {
                    break;
                }
            }
        }
    }


    /**
     * Conecta dois `IEventSource` para que eventos de um sejam retransmitidos automaticamente para o outro.
     *
     * Este método cria um listener intermediário que escuta eventos de um `IEventSource` (source)
     * e os retransmite para outro `IEventSource` (target).
     *
     * Regras de Funcionamento:
     * - O método cria um `IEventListener` que é registrado no `source`.
     * - Sempre que um evento é disparado no `source`, ele é encaminhado para o `target` através
     *   do método `notifyListeners` do `target`.
     *
     * Parâmetros:
     * @param source O `IEventSource` de origem, onde os eventos são escutados.
     * @param target O `IEventSource` de destino, que receberá os eventos retransmitidos.
     * @return O `IEventListener` criado, que pode ser usado posteriormente para remover o listener, se necessário.
     *
     * @throws RuntimeException Se qualquer um dos parâmetros (source ou target) for `null`.
     */
    public static IEventListener relay(IEventSource source, final IEventSource target) {
        // Validação de entradas
        if (source == null || target == null) {
            throw new RuntimeException("Event source is null");
        }

        // Criar um listener anônimo
        IEventListener listener = new IEventListener() {
            @Override
            public void onEvent(IEvent event) {
                // Encaminha eventos do source para o target
                target.notifyListeners(event);
            }
        };

        // Registra o listener no source
        source.addListener(listener);
        return listener; // Retorna o listener criado
    }
}

