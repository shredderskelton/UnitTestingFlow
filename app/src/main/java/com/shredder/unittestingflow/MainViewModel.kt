package com.shredder.unittestingflow

import kotlinx.coroutines.flow.Flow

interface MainViewModel {

    val name: Flow<String>

}
