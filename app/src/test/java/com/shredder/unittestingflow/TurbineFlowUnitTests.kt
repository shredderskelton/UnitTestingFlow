package com.shredder.unittestingflow

import app.cash.turbine.Event
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class TurbineFlowUnitTests {

    @get:Rule
    val rule = TestCoroutineRule()

    /**
     * Unit Testing Cold Flows with Turbine
     */

    @Test
    fun `cold flow - take multiple - Turbine`() {
        runBlocking {
            flow {
                emit("test")
                emit("test")
            }.test {
                assertThat(expectItem()).isEqualTo("test")
                assertThat(expectItem()).isEqualTo("test")
                expectComplete()
            }
        }
    }

    @Test
    fun `cold flow - take multiple - alternative - Turbine`() {
        runBlocking {
            flow {
                emit("test")
                emit("test")
            }.test {
                val actual = cancelAndConsumeRemainingEvents()
                assertThat(actual).containsExactly(
                    Event.Item("test"),
                    Event.Item("test"),
                    Event.Complete
                )
            }
        }
    }

    @Test
    fun `cold flow - take too little - Turbine - catches our implementation change!`() {
        runBlocking {
            flow {
                emit("test")
                emit("test")
                emit("test") // Added an extra emission, simulating an implementation change
            }.test {
                assertThat(expectItem()).isEqualTo("test")
                assertThat(expectItem()).isEqualTo("test")
                expectComplete()
            }
        }
    }

    @Test
    fun `cold flow - take too much - Turbine -- catches our implementation change!`() {
        runBlocking {
            flow {
                emit("test")
                // Deleted the second emission, simulating an implementation change
            }.test {
                assertThat(expectItem()).isEqualTo("test")
                assertThat(expectItem()).isEqualTo("test")
                expectComplete()
            }
        }
    }


    @Test
    fun `hot flow - Turbine`() {
        val myProperty = MutableStateFlow("test")
        runBlocking {
            myProperty.test {
                assertThat(expectItem()).isEqualTo("test")
                assertThat(expectItem()).isEqualTo("test")
                assertThat(expectItem()).isEqualTo("test")
                cancelAndIgnoreRemainingEvents()
            }
        }
        runBlocking {
            myProperty.value = "test"
            myProperty.value = "test"
        }
    }

    @Test
    fun `hot flow alternative - Turbine`() {
        runBlocking {
            MutableStateFlow("test").test {
                assertThat(expectItem()).isEqualTo("test")
                //cancelAndConsumeRemainingEvents()
            }
        }
    }
}