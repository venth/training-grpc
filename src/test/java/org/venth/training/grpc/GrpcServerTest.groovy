package org.venth.training.grpc

import io.grpc.Server
import io.grpc.netty.NettyChannelBuilder
import io.grpc.netty.NettyServerBuilder
import io.grpc.netty.ProtocolNegotiators
import org.venth.training.App
import org.venth.training.Healthcheck
import org.venth.training.ObservableGrpcResponseStream
import org.venth.training.grpc.api.HealthcheckGrpc
import org.venth.training.grpc.api.Message
import spock.lang.Specification

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * @author Venth on 15/02/2016
 */
class GrpcServerTest extends Specification {

    def 'after launch the grpc server is alive'() {
        given: 'server is started up on port'
        int serverPort = 7070
        Server server = App.createServer(serverPort)
        server.start()

        and: "a server's client is ready"
        def channel = NettyChannelBuilder.forAddress('127.0.0.1', serverPort)
                .usePlaintext(true)
                .protocolNegotiator(ProtocolNegotiators.plaintext())
                .build()

        def healthcheck = HealthcheckGrpc.newStub(channel)

        when: 'server echo service is called'
        def responseStream = new ObservableGrpcResponseStream()
        healthcheck.echo(
                Message.newBuilder().setText('').build(),
                responseStream
        )

        def reflectedEchosStream = responseStream.observable.toList().toBlocking()
        def reflectedEchos = reflectedEchosStream.single()

        then: 'server is alive'
        reflectedEchos.size() > 0

        cleanup: 'server is shutdown'
        server.shutdownNow()
        channel.shutdownNow()
    }

    def 'never ending wait for response because of wrong protocol settings'() {
        given: 'server is started up on port 7070'
        int serverPort = 7070
        Server server = NettyServerBuilder.forPort(serverPort)
                .protocolNegotiator(ProtocolNegotiators.plaintext())
                .addService(HealthcheckGrpc.bindService(new Healthcheck()))
                .build();
        server.start()

        and: "a server's client is ready"
        def channel = NettyChannelBuilder.forAddress('127.0.0.1', serverPort)
                .usePlaintext(true)
                .protocolNegotiator(ProtocolNegotiators.plaintext())
                .build()

        def healthcheck = HealthcheckGrpc.newStub(channel)

        and: 'server echo service is called'
        def responseStream = new ObservableGrpcResponseStream()
        healthcheck.echo(
                Message.newBuilder().setText('').build(),
                responseStream
        )

        def reflectedEchosStream = responseStream.observable.toList().timeout(200, TimeUnit.MILLISECONDS).toBlocking()

        when: "waiting for a server's response"
        reflectedEchosStream.single()

        then: "timeout occurred because client is stuck on waiting for a server's response"
        def ex = thrown(RuntimeException)
        TimeoutException == ex.cause.class

        cleanup: 'server is shutdown'
        server.shutdownNow()
        channel.shutdownNow()
    }
}
