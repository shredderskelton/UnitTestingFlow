package com.shredder.unittestingflow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow

interface AssetRepository {
    val assets: Flow<String>
}

class ColdAssetRepository(emissions: List<String>) : AssetRepository {
    override val assets = emissions.asFlow()
}

class HotAssetRepository(pushService: PushService) : AssetRepository {

    override val assets = MutableSharedFlow<String>()

    init {
        // Mocking a service pushing assets into the repo
        pushService.push = {
            assets.emit(it)
        }
    }
}

interface PushService {
    var push: suspend (String) -> Unit
}