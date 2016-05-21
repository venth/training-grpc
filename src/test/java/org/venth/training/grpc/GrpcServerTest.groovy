package org.venth.training.grpc

import io.grpc.Server
import io.grpc.netty.NettyChannelBuilder
import io.grpc.netty.ProtocolNegotiators
import org.venth.training.App
import org.venth.training.ObservableGrpcResponseStream
import org.venth.training.grpc.api.HealthcheckGrpc
import org.venth.training.grpc.api.Message
import rx.schedulers.Schedulers
import spock.lang.Shared
import spock.lang.Specification

/**
 * @author Venth on 15/02/2016
 */
class GrpcServerTest extends Specification {

    @Shared
    Server server
    @Shared
    int serverPort

    def 'after launch the grpc server is alive'() {
        given: "a server's client is ready"
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

        def reflectedEchosStream = responseStream.observableResponse.subscribeOn(Schedulers.io()).toList().toBlocking()
        def reflectedEchos = reflectedEchosStream.single()

        then: 'server is alive'
        reflectedEchos.size() > 0
    }

    def setupSpec() {
        serverPort = 7070
        server = App.createServer(serverPort)
        server.start()
    }

    def teardownSpec() {
        server.shutdownNow()
    }
}
