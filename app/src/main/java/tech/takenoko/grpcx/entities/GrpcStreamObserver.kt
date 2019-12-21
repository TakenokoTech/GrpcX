package tech.takenoko.grpcx.entities

import tech.takenoko.grpcx.proto.HelloReply

sealed class GrpcStreamObserver<T> {
    class OnNext<T>(val value: T?): GrpcStreamObserver<T>()
    class OnError<T>(val t: Throwable?): GrpcStreamObserver<T>()
    class OnCompleted<T>: GrpcStreamObserver<T>()
}