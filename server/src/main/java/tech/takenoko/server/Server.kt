package tech.takenoko.server

import kotlinx.coroutines.*
import tech.takenoko.server.grpc.GrpcServer
import tech.takenoko.server.rest.RestServer

suspend fun main(args: Array<String>) {
    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.Default + job)

    scope.launch { GrpcServer().start() }
    scope.launch { RestServer().start() }

    job.join()
}
