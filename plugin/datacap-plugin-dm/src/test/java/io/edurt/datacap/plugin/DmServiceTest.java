package io.edurt.datacap.plugin;

import io.edurt.datacap.plugin.container.DmContainer;
import io.edurt.datacap.plugin.utils.PluginPathUtils;
import io.edurt.datacap.spi.PluginService;
import io.edurt.datacap.spi.model.Configure;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

import static io.edurt.datacap.plugin.container.DMImages.DAMENG_8;

@Slf4j
public class DmServiceTest
{
    private static final DmContainer CONTAINER = new DmContainer(DAMENG_8)
            .withXa()
            .withLockWaitTimeout(50_000L);
    private static final String pluginName = "Dm";

    private PluginManager pluginManager;
    private Plugin plugin;
    private Configure configure;

    @Before
    public void init()
    {
        this.initPluginManager();
    }

    @After
    public void destroy()
    {
        CONTAINER.stop();
        pluginManager.destroy();
    }

    private void initPluginManager()
    {
        CONTAINER.start();

        URL configFile = this.getClass().getClassLoader().getResource("config.properties");
        log.info("Specified config file: {}", configFile);

        Path projectRoot = PluginPathUtils.findProjectRoot();
        log.info("Project root: {}", projectRoot);
        PluginConfigure config = PluginConfigure.builder()
                .pluginsDir(projectRoot.resolve(Path.of(configFile.getPath())))
                .build();

        log.info("Initializing plugin manager");
        pluginManager = new PluginManager(config);
        pluginManager.start();
        plugin = pluginManager.getPlugin(pluginName)
                .orElseThrow(() -> new IllegalStateException("Plugin not found: " + pluginName));

        initConfigure();
    }

    private void initConfigure()
    {
        if (!CONTAINER.isRunning()) {
            throw new RuntimeException("Container is stopped.");
        }

        configure = Configure.builder()
                .username(Optional.of(CONTAINER.getUsername()))
                .password(Optional.of(CONTAINER.getPassword()))
                .port(CONTAINER.getPort())
                .host(CONTAINER.getHost())
                .plugin(plugin)
                .pluginManager(pluginManager)
                .build();
    }

    @Test
    public void validator()
    {
        PluginService service = plugin.getService(PluginService.class);
        Assert.assertTrue(service.execute(configure, service.validator()).getIsSuccessful());
    }
}
