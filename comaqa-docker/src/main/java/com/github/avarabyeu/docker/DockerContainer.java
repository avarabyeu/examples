package com.github.avarabyeu.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstraction for creating/removing docker containers through API
 *
 * @author Andrei Varabyeu
 */
public class DockerContainer extends AbstractIdleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerContainer.class);

    private final DockerClient docker;
    private final String image;
    private final String name;
    private final Map<Integer, Integer> portMapping;
    private final Map<String, String> env;
    private final String network;

    private String containerId;
    private Map<Integer, Integer> ports;

    public DockerContainer(DockerClient docker, String image, String name, Map<Integer, Integer> portMapping,
            Map<String, String> env, String network) {
        this.docker = Preconditions.checkNotNull(docker, "Client should not be null");
        this.image = Preconditions.checkNotNull(image, "Image should not be null");
        this.name = name;
        this.portMapping = portMapping;
        this.env = env;
        this.network = network;
    }

    public DockerContainer(DockerClient docker, String image, String name, Map<Integer, Integer> portMapping,
            Map<String, String> env) {
        this(docker, image, name, portMapping, env, null);
    }

    @Override
    protected void startUp() throws Exception {
        docker.listContainersCmd().withShowAll(true).exec().stream()
                .filter(container -> Arrays.asList(container.getNames()).contains("/" + name)).findAny()
                .ifPresent(container -> killContainer(container.getId()));

        if (null != portMapping) {
            docker.listContainersCmd().exec().stream()
                    .flatMap(container -> Arrays.stream(container.getPorts()))
                    .filter(port -> portMapping.containsKey(port.getPublicPort())).findAny()
                    .ifPresent(port -> {
                        throw new IllegalStateException("Port is already taken");
                    });
        }

        CreateContainerCmd createCommand = docker.createContainerCmd(image)
                .withRestartPolicy(RestartPolicy.alwaysRestart());
        if (null != name) {
            createCommand.withName(name);
        }

        if (null != portMapping) {
            createCommand.withPortBindings(portMapping.entrySet().stream()
                    .map(entry -> new PortBinding(new Ports.Binding(null, entry.getKey().toString()),
                            new ExposedPort(entry.getValue()))).collect(
                            Collectors.toList()));
        } else {
            createCommand.withPublishAllPorts(true);
        }

        if (null != env) {
            createCommand.withEnv(
                    env.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.toList()));
        }
        LOGGER.info("Starting container with name {}", name);
        containerId = createCommand.exec().getId();

        docker.startContainerCmd(containerId).exec();
        if (null != network) {
            docker.connectToNetworkCmd().withContainerId(containerId).withNetworkId(network).exec();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> this.stopAsync().awaitTerminated()));

        this.ports = docker.inspectContainerCmd(containerId).exec().getNetworkSettings()
                .getPorts()
                .getBindings().entrySet().stream().collect(
                        Collectors.toMap(entry -> entry.getKey().getPort(),
                                //possible issue here. Binding may be empty or either contains port range
                                entry -> Integer.parseInt(entry.getValue()[0].getHostPortSpec())));
        LOGGER.info("Container has started!\nImage: {}\nPorts: {}", image, ports);
    }

    public Map<Integer, Integer> getExposedPorts() {
        Preconditions.checkState(this.isRunning(), "Container is not running");
        return this.ports;
    }

    public String getInternalIP() {
        Preconditions.checkState(this.isRunning(), "Container is not running");
        final NetworkSettings networkSettings = docker.inspectContainerCmd(containerId).exec().getNetworkSettings();
        return networkSettings.getNetworks().entrySet().iterator().next().getValue().getIpAddress();
    }

    @Override
    protected void shutDown() throws Exception {
        killContainer(containerId);
    }

    private void killContainer(String id) {
        LOGGER.info("Killing container with name {}", name);
        docker.removeContainerCmd(id).withForce(true).exec();
    }
}
