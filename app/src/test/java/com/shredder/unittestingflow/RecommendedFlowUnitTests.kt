package com.shredder.unittestingflow

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
class RecommendedFlowUnitTests {

    @get:Rule
    val rule = TestCoroutineRule()

    /**
     * first()
     * The terminal operator that returns the first element emitted by the flow and then cancels flow's collection.
     * Throws [NoSuchElementException] if the flow was empty.
     */

    @Test
    fun `cold flow - first - success`() {
        runBlocking {
            val actual = flowOf("test").first()
            assertThat(actual).isEqualTo("test")
        }
    }

    @Test
    fun `hot flow - first - success`() {
        runBlocking {
            val actual = MutableStateFlow("test").first()
            assertThat(actual).isEqualTo("test")
        }
    }


    /**
     * single()
     *
     * The terminal operator that awaits for one and only one value to be emitted.
     * Throws [NoSuchElementException] for empty flow and [IllegalStateException] for flow
     * that contains more than one element.
     */

    @Test
    fun `cold flow - single - success`() {
        runBlocking {
            val actual = flowOf("test").single()
            assertThat(actual).isEqualTo("test")
        }
    }

    @Ignore("This test hangs!")
    @Test
    fun `hot flow - single - hangs`() {
        runBlocking {
            val actual = MutableStateFlow("test").single()
            assertThat(actual).isEqualTo("test")
        }
    }

    /**
     * Unit Testing Cold Flows the Android recommended way
     */

    @Test
    fun `cold flow - take multiple - Android recommended - Success!`() {
        runBlocking {
            val actual = mutableListOf<String>()
            flow {
                emit("test")
                emit("test")
            }.take(2).collect {
                actual.add(it)
            }
            assertThat(actual).containsExactly("test", "test")
        }
    }

    @Test
    fun `cold flow - take too little - Android recommended - Success! Oh no!`() {
        runBlocking {
            val actual = mutableListOf<String>()
            flow {
                emit("test")
                emit("test")
                emit("test") // Added an extra emission, simulating an implementation change
            }.take(2).collect {
                actual.add(it)
            }
            assertThat(actual).containsExactly("test", "test") // We want this to fail!
        }
    }

    @Test
    fun `cold flow - take too much - Android recommended way - Fails!`() {
        runBlocking {
            val actual = mutableListOf<String>()
            flow {
                emit("test")
                // Deleted the second emission, simulating an implementation change
            }.take(2).collect {
                actual.add(it)
            }
            assertThat(actual).containsExactly("test", "test") // We want this to fail!
        }
    }


    /**
     * Unit Testing Hot Flows the Android recommended way
     */

    @Ignore
    @Test
    fun `hot flow - hangs`() {
        runBlocking {
            val actual = MutableStateFlow("test").single()
            assertThat(actual).isEqualTo("test")
        }
    }

    @Test
    fun `hot flow - This job has not completed yet`() {
        rule.testDispatcher.runBlockingTest {
            val actual = MutableStateFlow("test").single()
            assertThat(actual).isEqualTo("test")
        }
    }

    @Test
    fun `hot flow first - Success!`() {
        val actual = mutableListOf<String>()
        val myProperty = MutableStateFlow("test")
        rule.testDispatcher.runBlockingTest {
            myProperty.take(2).toList(actual)
            myProperty.value = "test"
            myProperty.value = "test"
            myProperty.value = "test"
            assertThat(actual).containsExactly("test", "test")
        }
    }

}