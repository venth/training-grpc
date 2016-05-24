package org.venth.training;

import io.grpc.stub.StreamObserver;
import rx.Observable;
import rx.internal.operators.UnicastSubject;
import rx.schedulers.Schedulers;
import rx.subjects.Subject;

/**
 * @author Venth on 18/03/2016
 */
public class ObservableGrpcResponseStream<V> implements StreamObserver<V> {

    private Subject<V, V> responseStream;
    public final Observable<V> observable;

    public ObservableGrpcResponseStream(int bufferCapacity) {
        this.responseStream = UnicastSubject.create(bufferCapacity);
        observable = this.responseStream.subscribeOn(Schedulers.io());
    }

    public ObservableGrpcResponseStream() {
        this(5);
    }

    @Override
    public void onCompleted() {
        responseStream.onCompleted();
    }

    @Override
    public void onError(Throwable e) {
        responseStream.onError(e);
    }

    @Override
    public void onNext(V v) {
        responseStream.onNext(v);
    }
}
