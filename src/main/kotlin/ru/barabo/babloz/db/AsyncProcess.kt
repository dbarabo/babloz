package ru.barabo.babloz.db

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AsyncProcess(private val process: ()->Unit, private val waitTime: Long = 100L) {

    @Volatile private var isBusy = false

    @Volatile private var isNeedUpdate = false


    fun asyncProcess() {
        if(isBusy) {
            isNeedUpdate = true
            return
        }

        isBusy = true

        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            suspendProcess()
        }
    }

    private suspend fun suspendProcess() {

        do {
            delay(waitTime)

            isNeedUpdate = false

            process()
        } while(isNeedUpdate)

        isBusy = false
    }
}