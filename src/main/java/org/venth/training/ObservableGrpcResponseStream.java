package org.venth.training;

import io.grpc.stub.StreamObserver;
import rx.internal.operators.UnicastSubject;
import rx.subjects.Subject;

/**
 * @author Venth on 18/03/2016
 */
public class ObservableGrpcResponseStream<V> implements StreamObserver<V> {

    public final Subject<V, V> observableResponse;

    public ObservableGrpcResponseStream() {
        this.observableResponse = UnicastSubject.create();
    }

    @Override
    public void onCompleted() {
        observableResponse.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        observableResponse.onError(e);
    }

    @Override
    public void onNext(V v) {
        observableResponse.onNext(v);
    }
}
