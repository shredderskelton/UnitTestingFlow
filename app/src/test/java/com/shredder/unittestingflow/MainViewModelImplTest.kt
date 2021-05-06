package com.shredder.unittestingflow

import org.junit.Rule

class MainViewModelImplTest {

    @get:Rule
    val rule = TestCoroutineRule()

    lateinit var underTest : MainViewModel

    fun setup(){
        underTest = MainViewModelImpl(rule.testDispatcherProvider)
    }
}