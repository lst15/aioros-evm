package com.aioros.aioros.services;

import com.aioros.aioros.interfaces.IPropertyDefinition;
import com.aioros.aioros.interfaces.IPropertyDefinitionGroup;
import com.aioros.aioros.interfaces.IPropertyDefinitionManager;
import com.aioros.aioros.interfaces.IPropertyType;
import com.aioros.aioros.utils.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Gerencia a definição de propriedades e suas relações em uma hierarquia de grupos e regiões.
 *
 * A classe `PropertyDefinitionManager` atua como um ponto central para organizar, validar,
 * registrar e gerenciar definições de propriedades de forma estruturada e hierárquica.
 * Ela fornece funcionalidades para lidar com definições específicas, grupos, herança de
 * pais/filhos e infraestrutura de validação e organização de propriedades.
 *
 * Principais Funcionalidades:
 * 1. **Definições de Propriedades**:
 *    - Permite adicionar, remover e consultar propriedades individualmente.
 *    - Valida nomes de propriedades e suas características semânticas.
 *
 * 2. **Grupos de Propriedades**:
 *    - Suporte à criação e manipulação de grupos de propriedades.
 *    - Permite associar propriedades a grupos e reatribuir propriedades de grupos
 *      removidos para um grupo padrão.
 *
 * 3. **Hierarquia Pai-Filho**:
 *    - Suporta hierarquias entre gerenciadores de definição de propriedades.
 *    - Oferece métodos para associar uma instância ao pai ou registrar filhos
 *      em uma coleção baseada em regiões únicas.
 *
 * 4. **Regiões e Namespace**:
 *    - Cada instância é associada a uma região (nome identificador) e opera em
 *      um namespace organizado com base na hierarquia.
 *    - Suporta validação de nomes de regiões conforme padrões de identificadores Java.
 *
 * 5. **Logs e Validação**:
 *    - Gera logs para inconsistências e erros durante operações como adição de
 *      definições ou estruturação de hierarquias.
 *    - Trata exceções claras para casos de duplicação, inconsistência ou violações
 *      de regras específicas de nomenclatura.
 *
 * Aplicações:
 * - Este gerenciador pode ser usado para criar uma estrutura hierárquica de configurações
 *   e propriedades dinâmicas, sendo ideal para sistemas que necessitam de organização e
 *   manipulação estruturada de propriedades.
 *
 * @see IPropertyDefinition
 * @see IPropertyDefinitionGroup
 * @see IPropertyDefinitionManager
 * @see IPropertyType
 */
public class PropertyDefinitionManager implements IPropertyDefinitionManager {

    private static final Logger log = LoggerFactory.getLogger(PropertyDefinitionManager.class);
    private final Map<String, IPropertyDefinitionManager> children;
    private final Map<String, IPropertyDefinition> definitions;
    private final Map<String, IPropertyDefinitionGroup> groups;
    private final int flags;
    private final String description;
    private final String region;
    private IPropertyDefinitionManager parent;

    /**
     * Constrói uma nova instância de PropertyDefinitionManager, inicializando suas variáveis
     * e configurando construtores relacionados a definições, hierarquias e grupos.
     *
     * Regras de validação:
     * 1. O nome da região (`region`) fornecido não pode ser nulo e deve ser um identificador
     *    válido em Java, de acordo com o método `isValidRegionName`.
     *    Caso contrário, lança uma IllegalArgumentException.
     * 2. Se a região fornecida for nula, ela será substituída por uma string vazia (`""`).
     * 3. Se um objeto pai (`parent`) for informado, o objeto atual será anexado como filho
     *    na hierarquia do pai com o método `attachToParent`.

     * Configurações adicionais:
     * - As coleções `children`, `definitions` e `groups` são inicializadas como LinkedHashMap
     *   para usar ordem de inserção.
     * - Um grupo padrão, identificado pela chave vazia `""`, é adicionado na inicialização.

     * @param region O nome da região associada a esta definição. Deve ser válido ou
     *               será substituído por uma string vazia.
     * @param parent O pai (implementação de IPropertyDefinitionManager) ao qual este objeto
     *               será anexado, ou `null` se não houver pai.
     * @param description Uma descrição associada à definição gerenciada.
     * @param flags Um valor inteiro usado para fornecer configurações e comportamento adicional.
     *
     * @throws IllegalArgumentException Se o nome da região for inválido.
     */
    public PropertyDefinitionManager(String region, IPropertyDefinitionManager parent, String description, int flags) {
        // Inicializa coleções para children, definitions e groups
        this.children = new LinkedHashMap<>();
        this.definitions = new LinkedHashMap<>();
        this.groups = new LinkedHashMap<>();

        // Valida o nome da região, garantindo que seja um identificador válido
        if (region != null && !isValidRegionName(region)) {
            throw new IllegalArgumentException(
                    region + " is an invalid region name: it must be a valid Java identifier name"
            );
        }

        // Se a região for nula, define uma string vazia como padrão
        if (region == null) {
            region = "";
        }

        // Define as variáveis da instância com os valores fornecidos
        this.region = region;
        this.description = description;
        this.flags = flags;

        // Se um pai for fornecido, anexa esta instância como filha
        if (parent != null) {
            this.attachToParent(parent);
        }

        // Adiciona o grupo padrão (chave vazia) na coleção de grupos
        this.groups.put("", new Group());
    }

    public PropertyDefinitionManager(String var1, IPropertyDefinitionManager var2) {
        this(var1, var2, (String)null, 0);
    }

    public PropertyDefinitionManager(String var1) {
        this(var1, (IPropertyDefinitionManager)null);
    }

    public PropertyDefinitionManager() {
        this((String)null, (IPropertyDefinitionManager)null);
    }

    /**
     * Associa a instância atual (`PropertyDefinitionManager`) a um pai fornecido,
     * validando e registrando a relação na hierarquia. Caso alguma condição seja violada,
     * é lançada uma exceção apropriada.
     *
     * Regras de validação:
     * 1. O pai fornecido (`var1`) deve ser diferente de `null` e não pode ser a própria instância atual.
     * 2. A instância atual não pode já estar associada a outro pai.
     * 3. Uma instância de raiz (sem região configurada) não pode ser vinculada a um pai.
     * 4. O registro como filho (`var1.registerChild(this)`) deve ser bem-sucedido.
     *
     * Exceções levantadas:
     * - `IllegalArgumentException`: O pai fornecido é inválido (nulo ou a própria instância).
     * - `IllegalStateException`:
     *      - Se a instância já possui um pai.
     *      - Se a instância é uma raiz e, portanto, não pode ter pai.
     * - `RuntimeException`: O registro como filho no pai fornecido falhou.
     *
     * @param parent O pai (`IPropertyDefinitionManager`) que será associado a esta instância.
     * @throws IllegalArgumentException Se o pai fornecido for inválido.
     * @throws IllegalStateException Se a instância já tiver pai ou for uma raiz.
     * @throws RuntimeException Se o registro como filho for mal-sucedido.
     */
    public void attachToParent(IPropertyDefinitionManager parent) {
        // Verifica se o pai fornecido é válido
        if (parent == null || parent == this) {
            throw new IllegalArgumentException("Invalid parent");
        }

        // Garante que esta instância não tenha um pai
        if (this.parent != null) {
            throw new IllegalStateException("PDM already has a parent");
        }

        // Impede que uma instância "raiz" sem região seja associada a um pai
        if (this.region == null) {
            throw new IllegalStateException("A root PDM cannot have a parent");
        }

        // Tenta registrar esta instância como filho do pai fornecido
        if (!parent.registerChild(this)) {
            throw new RuntimeException("Could not be registered as a child of the provided parent");
        }

        // Associa o pai à instância atual
        this.parent = parent;
    }

    /**
     * Registra uma instância de `IPropertyDefinitionManager` como filho da instância atual.
     *
     * O método verifica se a região do filho fornecido (`var1.getRegion()`) já está registrada
     * no mapeamento `children`. Caso já exista um filho associado à região, o registro falhará
     * e o método retornará `false`. Caso contrário, o filho será registrado com sucesso.
     *
     * @param var1 A instância de `IPropertyDefinitionManager` a ser registrada como filho.
     * @return `true` se o registro foi bem-sucedido, ou `false` se a região já estava ocupada.
     */
    public boolean registerChild(IPropertyDefinitionManager var1) {
        // Verifica se a região do filho já está registrada
        if (this.children.containsKey(var1.getRegion())) {
            return false; // Registro falhou, região já ocupada
        }

        // Adiciona o novo filho ao mapeamento de children
        this.children.put(var1.getRegion(), var1);
        return true; // Registro bem-sucedido
    }

    public String getRegion() {
        return this.region;
    }

    public boolean isRoot() {
        return this.region.isEmpty();
    }

    public int getFlags() {
        return this.flags;
    }

    public String getDescription() {
        return this.description;
    }

    public IPropertyDefinitionManager getParent() {
        return this.parent;
    }

    /**
     * Retorna o namespace da instância atual com base na hierarquia dos pais e na região associada.
     *
     * Regras:
     * 1. Se a instância não possuir um pai (`parent == null`):
     *    - Se a região for vazia, retorna uma string vazia (`""`).
     *    - Caso contrário, retorna a região precedida de um ponto (`".<region>"`).
     *
     * 2. Se a instância possuir um pai (`parent != null`):
     *    - Retorna o namespace concatenado com o prefixo fixo `"slaqpohessa"` e a região
     *      atual, no formato `"slaqpohessa.<region>"`.
     *
     * Obs.: O prefixo `"slaqpohessa"` usado para o namespace do pai parece ser um texto provisório ou fixo,
     * que deve ser ajustado para usar os dados do pai para a construção do namespace.
     *
     * @return O namespace completo da instância atual.
     */
    public String getNamespace() {
        // Verifica se a instância não possui um pai
        if (this.parent == null) {
            // Retorna a região diretamente ou precedida por um ponto, se não está vazia
            return this.region.isEmpty() ? this.region : "." + this.region;
        } else {
            // Combina um prefixo fixo com a região da instância (provisório)
            String prefix = this.parent.getNamespace();
            return prefix + "." + this.region;
        }
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public Collection<IPropertyDefinitionManager> getChildren() {
        return Collections.unmodifiableCollection(this.children.values());
    }

    public IPropertyDefinitionManager getChild(String var1) {
        return (IPropertyDefinitionManager)this.children.get(var1);
    }

    public boolean hasDefinitions() {
        return !this.definitions.isEmpty();
    }

    public IPropertyDefinition getDefinition(String var1) {
        return (IPropertyDefinition)this.definitions.get(var1);
    }

    public Collection<IPropertyDefinition> getDefinitions() {
        return Collections.unmodifiableCollection(this.definitions.values());
    }

    public IPropertyDefinition addDefinition(String var1, IPropertyType var2, String var3) {
        return this.addDefinition(var1, var2, var3, 0, true, (String)null);
    }

    public IPropertyDefinition addDefinition(String var1, IPropertyType var2, String var3, int var4) {
        return this.addDefinition(var1, var2, var3, var4, true, (String)null);
    }

    public IPropertyDefinition addInternalDefinition(String var1, IPropertyType var2) {
        return this.addDefinition(var1, var2, (String)null, 1, true, (String)null);
    }

    public IPropertyDefinition addInternalDefinition(String var1, IPropertyType var2, String var3) {
        return this.addDefinition(var1, var2, var3, 1, true, (String)null);
    }

    /**
     * Adiciona uma nova definição de propriedade ao gerenciador atual.
     *
     * O método verifica se o nome da propriedade é válido e único (caso substituições não sejam permitidas),
     * cria uma nova definição de propriedade (`PropertyDefinition`) e a registra tanto no mapeamento de definições
     * quanto no grupo correspondente.
     *
     * Regras:
     * 1. O nome da propriedade é simplificado usando o método `simplifyName`.
     * 2. Se já existir uma definição com o mesmo nome:
     *    - Se `var5` for `false`, uma exceção será lançada para evitar duplicação.
     * 3. O nome fornecido deve ser válido; caso contrário, uma exceção será lançada.
     * 4. Caso a descrição seja `null` e já exista uma definição com o mesmo nome, reutiliza-se a descrição antiga.
     * 5. Agrupa a propriedade no grupo especificado (`var6`) ou no grupo padrão (`""`).
     *
     * @param propertyName O nome da propriedade.
     * @param propertyType O tipo da propriedade, representado por um objeto `IPropertyType`.
     * @param description A descrição da propriedade (opcional).
     * @param flag Um flag ou valor associado à propriedade.
     * @param allowReplace Se `true`, permite substituir propriedades já cadastradas com o mesmo nome.
     * @param group O grupo ao qual a propriedade será associada (ou `null` para o grupo padrão).
     * @return A nova definição de propriedade criada e registrada.
     * @throws RuntimeException Se já existir uma propriedade com o mesmo nome e substituição não for permitida.
     * @throws IllegalArgumentException Se o nome da propriedade for inválido.
     */
    IPropertyDefinition addDefinition(String propertyName, IPropertyType propertyType, String description, int flag, boolean allowReplace, String group) {
        // Simplifica o nome da propriedade
        propertyName = this.simplifyName(propertyName);

        // Verifica se já existe uma definição com o mesmo nome
        IPropertyDefinition var7 = this.definitions.get(propertyName);
        if (var7 != null && !allowReplace) {
            throw new RuntimeException("A property definition with that name already exists.");
        }

        // Valida o nome da propriedade
        if (!isValidPropertyName(propertyName)) {
            throw new IllegalArgumentException("Invalid property name");
        }

        // Reutiliza a descrição antiga, caso a nova seja nula
        if (description == null && var7 != null) {
            description = var7.getDescription();
        }

        // Cria uma nova instância de PropertyDefinition
        PropertyDefinition var8 = new PropertyDefinition(this, propertyName, description, propertyType, flag);

        // Adiciona ao mapeamento de definições
        this.definitions.put(propertyName, var8);

        // Trata o grupo: usa o padrão ("") se `var6` for nulo
        if (group == null) {
            group = "";
        }

        // Registra no grupo
        ((Group) this.groups.get(group)).defs.add(var8);

        // Retorna a nova propriedade registrada
        return var8;
    }

    /**
     * Simplifica um nome qualificado, extraindo apenas a última parte (nome base).
     *
     * O método verifica se o nome recebido contém um separador (`"."`). Caso exista, ele separa
     * a última parte do nome (nome base) e valida se o nome completo é consistente com
     * o namespace atual. Se houver inconsistências, um aviso será registrado no log.
     *
     * Regras:
     * 1. Se o nome não contiver um ponto (`"."`):
     *    - Retorna o nome original.
     * 2. Se o nome for qualificado:
     *    - Extrai a última parte após o último ponto.
     *    - Contrasta o nome esperado (gerado a partir do namespace) com o nome fornecido.
     *    - Registra uma mensagem de aviso no log caso haja inconsistências entre os dois nomes.
     *
     * @param qualifiedName O nome qualificado a ser simplificado.
     * @return O nome simplificado, que é a última parte do nome qualificado.
     */
    private String simplifyName(String qualifiedName) {
        // Obtém a última posição de um ponto no nome
        int lastDotIndex = qualifiedName.lastIndexOf('.');

        // Se não houver pontos, o nome não é qualificado: retorna o original
        if (lastDotIndex < 0) {
            return qualifiedName;
        }

        // Extrai a última parte do nome (após o último ponto)
        String simplifiedName = qualifiedName.substring(lastDotIndex + 1);

        // Constrói o nome totalmente qualificado esperado a partir do namespace
        String namespace = this.getNamespace();
        String expectedQualifiedName = namespace + "." + simplifiedName;

        // Verifica se o nome qualificado fornecido é consistente com o esperado
        if (!qualifiedName.equals(expectedQualifiedName)) {
            String warningMessage = "Inconsistent fully-qualified name for property definition: %s (expected: %s)";
            log.warn(warningMessage);
        }

        // Retorna o nome simplificado (última parte do nome qualificado)
        return simplifiedName;
    }

    public void removeDefinition(String definitionName) {
        definitionName = this.simplifyName(definitionName);
        this.definitions.remove(definitionName);

        for(IPropertyDefinitionGroup var3 : this.groups.values()) {
            var3.removeDefinition(definitionName);
        }

    }

    /**
     * Valida se o nome de região fornecido é um identificador Java válido.
     *
     * Um nome de região é considerado válido se:
     * 1. Não for uma string vazia.
     * 2. O primeiro caractere for um caractere válido para o início de um identificador Java
     *    (letra, sublinhado (`_`) ou cifrão (`$`)).
     * 3. Todos os demais caracteres forem válidos como parte de um identificador Java
     *    (letras, dígitos, sublinhado (`_`) ou cifrão (`$`)).
     *
     * @param regionName O nome da região a ser validado.
     * @return `true` se o nome for válido, ou `false` caso contrário.
     */
    private static boolean isValidRegionName(@NotNull String regionName) {
        // Verifica se a string está vazia
        if (regionName.length() <= 0) {
            return false;
        }

        // Verifica se o primeiro caractere é válido para o início de um identificador
        if (!Character.isJavaIdentifierStart(regionName.charAt(0))) {
            return false;
        }

        // Verifica se os caracteres restantes são válidos como parte de um identificador
        for (int i = 1; i < regionName.length(); i++) {
            if (!Character.isJavaIdentifierPart(regionName.charAt(i))) {
                return false;
            }
        }

        // Se todas as verificações passarem, o nome é válido
        return true;
    }

    /**
     * Valida se o nome de uma propriedade segue as regras específicas de nomenclatura.
     *
     * As regras são:
     * 1. O nome não pode ser vazio.
     * 2. O primeiro caractere deve ser uma letra maiúscula e válido para o início de um identificador Java
     *    (letra, sublinhado (`_`) ou cifrão (`$`)).
     * 3. Todos os caracteres subsequentes devem ser válidos como parte de um identificador Java
     *    (letras, números, sublinhado (`_`) ou cifrão (`$`)).
     *
     * @param propertyName O nome da propriedade que será validado.
     * @return `true` se o nome for válido, ou `false` caso contrário.
     */
    private static boolean isValidPropertyName(@NotNull String propertyName) {
        // Verifica se a string está vazia
        if (propertyName.length() <= 0) {
            return false;
        }

        // Verifica se o primeiro caractere é uma letra maiúscula
        if (!Character.isUpperCase(propertyName.charAt(0))) {
            return false;
        }

        // Verifica se o primeiro caractere é válido para identificadores Java
        if (!Character.isJavaIdentifierStart(propertyName.charAt(0))) {
            return false;
        }

        // Verifica os caracteres restantes
        for (int i = 1; i < propertyName.length(); i++) {
            if (!Character.isJavaIdentifierPart(propertyName.charAt(i))) {
                return false;
            }
        }

        // Retorna true se todas as condições forem satisfeitas
        return true;
    }

    public String toString() {
        return StringUtils.ff("PDM:%s:%d", new Object[]{StringUtils.safe2(this.region, "<root>"), this.children.size()});
    }

    /**
     * Adiciona um novo grupo de definições de propriedades ao gerenciador.
     *
     * Este método cria um grupo com o nome fornecido e o adiciona à coleção interna de grupos.
     * Caso já exista um grupo com o mesmo nome, uma exceção será lançada.
     *
     * @param groupName O nome do grupo a ser criado.
     * @return O grupo recém-criado.
     * @throws IllegalArgumentException Se já existir um grupo com o nome fornecido.
     */
    public IPropertyDefinitionGroup addGroup(String groupName) {
        // Verifica se o grupo já existe
        if (this.groups.containsKey(groupName)) {
            throw new IllegalArgumentException("Group already exists: " + groupName);
        }

        // Cria um novo grupo com o nome fornecido
        Group newGroup = new Group(groupName);

        // Adiciona o grupo à coleção interna
        this.groups.put(groupName, newGroup);

        // Retorna o grupo recém-criado
        return newGroup;
    }

    public Collection<IPropertyDefinitionGroup> getGroups() {
        return Collections.unmodifiableCollection(this.groups.values());
    }

    public IPropertyDefinitionGroup getGroup(String groupName) {
        return (IPropertyDefinitionGroup)this.groups.get(groupName);
    }

    /**
     * Remove um grupo de definições de propriedades do gerenciador.
     *
     * Este método remove o grupo identificado pelo nome fornecido. Caso o grupo seja removido
     * com sucesso, as definições de propriedades associadas a ele são transferidas para um
     * grupo padrão vazio (identificado pela chave `""`).
     *
     * Regras:
     * 1. Se o nome do grupo for inválido (nulo, vazio ou em branco), o método retorna `false`.
     * 2. Se o grupo não existir no mapa interno, o método retorna `false`.
     * 3. Caso a remoção seja bem-sucedida, as definições de propriedades do grupo
     *    removido são reatribuídas ao grupo padrão.
     *
     * @param groupName O nome do grupo a ser removido.
     * @return `true` se o grupo foi removido com sucesso, ou `false` caso contrário.
     */
    public boolean removeGroup(String groupName) {
        // Valida o nome do grupo
        if (Strings.isBlank(groupName)) {
            return false;
        }

        // Remove o grupo da coleção de grupos
        IPropertyDefinitionGroup removedGroup = this.groups.remove(groupName);
        if (removedGroup == null) {
            return false;
        }

        // Obtém o grupo padrão (chave: "")
        Group defaultGroup = (Group) this.groups.get("");

        // Transfere as definições do grupo removido para o grupo padrão
        defaultGroup.defs.addAll(removedGroup.getDefinitions());

        // Indica que o grupo foi removido com sucesso
        return true;
    }

    class Group implements IPropertyDefinitionGroup {
        String grpname;
        List<IPropertyDefinition> defs = new ArrayList<>();

        Group() {
            this.grpname = "";
        }

        /**
         * Cria uma nova instância de grupo com o nome fornecido.
         *
         * Regras:
         * 1. O nome do grupo não pode ser nulo, vazio ou composto apenas por espaços em branco.
         * 2. Caso o nome seja inválido, será lançada uma exceção `IllegalArgumentException`.
         *
         * @param groupName O nome do grupo.
         * @throws IllegalArgumentException Se o nome do grupo for nulo, vazio ou em branco.
         */
        Group(String groupName) {
            if (Strings.isBlank(groupName)) {
                throw new IllegalArgumentException("Illegal group name: " + groupName);
            } else {
                this.grpname = groupName;
            }
        }

        public String getName() {
            return this.grpname;
        }

        public List<IPropertyDefinition> getDefinitions() {
            return Collections.unmodifiableList(this.defs);
        }

        public IPropertyDefinition addDefinition(String propertyName, IPropertyType propertyType, String description, int flag) {
                return PropertyDefinitionManager.this.addDefinition(propertyName, propertyType, description, flag, true, this.grpname);
        }

        /**
         * Remove uma definição da coleção com base no nome fornecido.
         *
         * Este método percorre a coleção de definições e remove todas as definições cujo nome
         * corresponde ao nome fornecido (`var1`).
         *
         * @param propertyName O nome da definição a ser removida.
         *             Caso nenhuma definição corresponda ao nome, nenhuma ação será realizada.
         */
        public void removeDefinition(String propertyName) {
            this.defs.removeIf(o -> propertyName.equals(o.getName()));
        }
    }
}
