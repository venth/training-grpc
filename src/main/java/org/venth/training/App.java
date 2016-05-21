package org.venth.training;

import io.grpc.internal.ServerImpl;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.ProtocolNegotiators;

import org.venth.training.grpc.api.HealthcheckGrpc;

import java.io.IOException;

/**
 * Hello world!
 */
public class App {

    protected static final int DEFAULT_LISTENING_PORT = 7070;

    public static void main(String[] args) throws InterruptedException, IOException {
        ServerImpl server = createServer(DEFAULT_LISTENING_PORT);
        try {
            server.start();
            server.awaitTermination();
        } finally {
            server.shutdown();
        }
    }

    protected static ServerImpl createServer(int port) {
        return NettyServerBuilder.forPort(port)
                .protocolNegotiator(ProtocolNegotiators.serverPlaintext())
                .addService(HealthcheckGrpc.bindService(new Healthcheck()))
                .build();
    }
}
