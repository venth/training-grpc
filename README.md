# training-grpc
This is just a playground to find out possibilities offered by GRPC protocol and protobuf3

# Findings

## maven support and protoc
Maven support for protoc comes from [https://github.com/xolstice/protobuf-maven-plugin](xolstice).

Used technologies:
* protobuf3 (beta) - cool ;),
* proto3 syntax - cool ;),
* grpc 0.14.0 - for streaming capabilities,
* netty 4.x - for nio and epoll

I had couple of issues with proper configuration of this plugin. Issues:

1. protoc binary
    At first I downloaded the protoc binary directly and configured os to find it. Then I provided protoc path in
    the configuration of protobuf-maven-plugin. It worked, but as I could expect only on my osx system. Googled a bit
    about it and found a solution:
    * use extension that introduce property: _os.detected.classifier_ it will help to applied os depended classifier
    ```
    <extension>
        <groupId>kr.motd.maven</groupId>
        <artifactId>os-maven-plugin</artifactId>
        <version>${os-maven-plugin.version}</version>
    </extension>
    ```
    * and then use the property as classifier of protocArtifact
    ```
    <plugin>
        <groupId>org.xolstice.maven.plugins</groupId>
        <artifactId>protobuf-maven-plugin</artifactId>
        <version>${protobuf-maven-plugin.version}</version>
        <extensions>true</extensions>
        <executions>
            <execution>
                ...
                <configuration>
                    ...
                    <protocArtifact>com.google.protobuf:protoc:${protoc.version}:exe:${os.detected.classifier}</protocArtifact>
                    ...
                </configuration>
            </execution>
        </executions>
        ...
    </plugin>

    ```
    Finally maven downloads protoc binary os depended without need of separate download.
2. use protoc to generate java stubs, proxies, model and API

    The default configuration of _protobuf-maven-plugin_ generates generic stubs and model.
    I had hard time to figure out the way of generation proper classes. Finally after digging into
    the plugin's documentation, I discovered goals: _compile and test-compile_
    produce only generic stuff.

    In order to generate language specific code the plugin's goals and configuration need
    to be extended by changes as follows:
    * goals
    ```
    <goal>compile-custom</goal>
    <goal>test-compile-custom</goal>

    ```
    * the configuration
    ```
    <pluginId>grpc-java</pluginId>
    <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
    ```
    * and it's good to mention about protobuf java dependency in the plugin's dependency section
    ```
    <dependency>
        <groupId>com.google.protobuf</groupId>
        <artifactId>protobuf-java</artifactId>
        <version>${protoc.version}</version>
    </dependency>
    ```
    Thanks to the introduced changes the generated proto started to look properly.
3. options in *.proto files

    It appeared that for java there is a need to add several java specific options inside proto description.
    I expected that the language specific stuff will be introduced only in the generation part so a proto file will
    remain clean non depended on a specific language. Right now I failed to find a way of removing such options from
    proto file.

    The options I mentioned are as follows:
    * option java_multiple_files = true;
    * option java_package = "org.venth.training.grpc.api";
    * option java_outer_classname = "HealthcheckProto";
    * option java_generic_services = false;

## Interesting behaviours
1. never ending wait for a response

    It happens when during launch of the server wrong protocol configuration is provided.
    The example of never ending wait is provided in the test: _never ending wait for response because of wrong protocl settings_

    The server's configuration:
    ```
    Server server = NettyServerBuilder.forPort(serverPort)
                    .protocolNegotiator(ProtocolNegotiators.plaintext())
                    .addService(HealthcheckGrpc.bindService(new Healthcheck()))
                    .build();
    ```

    The spoiling part is: ****.protocolNegotiator(ProtocolNegotiators.plaintext())****
    To correct the error jsut use: ****.protocolNegotiator(ProtocolNegotiators.serverPlaintext())****

    The error is signalized by following log entry (level DEBUG):
    ```
    i.n.channel.DefaultChannelPipeline - Discarded inbound message PooledUnsafeDirectByteBuf(ridx: 0, widx: 64, cap: 1024) that reached at the tail of the pipeline. Please check your pipeline configuration.
    ```

    At first glace it tells nothing about configuration. After a while spent on digging in sources, I figured out that
    the server's configuration is responsible for the deadlock.

    The corrected server configuration looks as follows:

    ```
        .protocolNegotiator(ProtocolNegotiators.serverPlaintext())
    ```

    As always, it seems that timeouts need to be provided.