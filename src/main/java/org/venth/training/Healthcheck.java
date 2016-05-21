package org.venth.training;


import io.grpc.stub.StreamObserver;

import org.venth.training.grpc.api.EchoMessage;
import org.venth.training.grpc.api.HealthcheckGrpc;
import org.venth.training.grpc.api.Message;

/**
 * @author Venth on 21/05/2016
 */
public class Healthcheck implements HealthcheckGrpc.Healthcheck {
    @Override
    public void echo(Message request, StreamObserver<EchoMessage> responseObserver) {
        responseObserver.onNext(EchoMessage.newBuilder().setText("123").build());
        responseObserver.onNext(EchoMessage.newBuilder().setText("123").build());
        responseObserver.onCompleted();
    }
}
