package com.aioros.aioros.services;

import com.aioros.aioros.annotations.SerDisabled;
import com.aioros.aioros.interfaces.IClientContext;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.BuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;


import java.io.File;
import java.io.IOException;

@SerDisabled
public class AbstractClientContext implements IClientContext {
    private PropertiesConfiguration cfg;

    public AbstractClientContext () {}
    /**
     * Cria e/ou carrega um arquivo de configuração no formato .properties para o caminho especificado.
     *
     * Funcionalidades:
     * - Garante que o arquivo especificado exista; se não existir, será criado.
     * - Configura um builder para gerenciar o arquivo como um PropertiesConfiguration.
     * - Habilita o salvamento automático, garantindo que alterações sejam persistidas
     *   no arquivo físico sem a necessidade de chamadas manuais para salvar.
     *
     * @param path O caminho (relativo ou absoluto) do arquivo de configuração .properties.
     * @return Um objeto PropertiesConfiguration para manipular o arquivo.
     * @throws ConfigurationException Se houver falhas na criação ou manipulação do arquivo.
     */
    private PropertiesConfiguration createPropertiesConfiguration(String path) throws ConfigurationException {
        // Garante que o arquivo existe
        ensureFileExists(path);

        // Cria uma instância do construtor de configuração para o arquivo de propriedades
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = createConfigurationBuilder(path);

        // Habilita o salvamento automático
        builder.setAutoSave(true);

        // Retorna a configuração gerenciada
        return builder.getConfiguration();
    }

    /**
     * Garante que o arquivo existe, criando-o caso ele ainda não exista.
     *
     * @param path Caminho do arquivo de propriedades
     * @return Um objeto File representando o arquivo
     * @throws RuntimeException Se ocorrer um erro ao criar o arquivo
     */
    private File ensureFileExists(String path) {
        File file = new File(path);
        if (!file.isFile()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Não foi possível criar o arquivo: " + path, e);
            }
        }
        return file;
    }

    /**
     * Cria e configura um construtor de arquivo de propriedades para o caminho fornecido.
     *
     * @param path Caminho do arquivo de propriedades
     * @return Um FileBasedConfigurationBuilder configurado para o arquivo
     */
    private FileBasedConfigurationBuilder<PropertiesConfiguration> createConfigurationBuilder(String path) {
        Parameters parameters = new Parameters();
        return new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                .configure(parameters.properties().setFileName(path));
    }
}
//        (this.pm = new PropertyManager(
//        this.pdm,
//        new CommonsConfigurationWrapper(this.cfg)
//        )).addListener(new IEventListener() {
//    public void onEvent(IEvent event) {

//        (this.pdm = new PropertyDefinitionManager()).addInternalDefinition(".Uuid", PropertyTypeString.create());
