package org.venth.training.grpc

import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import spock.lang.Specification

/**
 * @author Venth on 15/02/2016
 */
class GrpcServerTest extends Specification {

    def 'after run grpc server is alive'() {
        given: "a server's client is ready"
        def grpcClient

        def channel = NettyChannelBuilder.forAddress('127.0.0.1', 7070)
            .negotiationType(NegotiationType.PLAINTEXT)
            .build()

        def stub = RouteGuidePrpc

        when: 'server ping service is called'
        def serverResponded = grpcClient.ping()

        then: 'server is alive'
        serverResponded == 'pong'
    }
}
