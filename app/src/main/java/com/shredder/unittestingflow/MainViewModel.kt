package com.shredder.unittestingflow

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

interface MainViewModel {
    val name: Flow<String>
    val nameLiveData: LiveData<String>
}

class MainViewModelImpl(
    private val dispatchProvider: DispatcherProvider = DefaultDispatcherProvider
) : MainViewModel {

    suspend fun getCount(): Int  {
        val name = getSomething()
        return useSomething(name) // this is returned
    }
    private suspend fun getSomething(): String {
        return "Yo"
    }

    private suspend fun useSomething(name: String): Int {
        return 100
    }

    override val name: Flow<String>
        get() = flowOf("Nick")

    override val nameLiveData = TODO() // name.asLiveData()

}