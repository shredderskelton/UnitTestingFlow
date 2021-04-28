package com.shredder.unittestingflow

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class HotAssetRepositoryTest {

    @get:Rule
    val rule = TestCoroutineRule()

    private lateinit var underTest: AssetRepository

    private val service = object : PushService {
        override var push: suspend (String) -> Unit = {}
    }

    @Before
    fun given() {
        underTest = HotAssetRepository(service)
    }

    /**
     * Unit Testing Hot Flows the Android Google Recommends way
     */

    @Ignore
    @Test
    fun `Problem - use first() for very simple tests without TestDispatcher Hangs`() {
        runBlocking {

            //when
            val actual = underTest.assets.first()

            service.push("Asset1")

            //then
            Assertions.assertThat(actual).isEqualTo("Asset1")
        }
    }

    @Test
    fun `Problem - use first() for very simple tests with TestDispatcher - This job has not completed`() {
        rule.testDispatcher.runBlockingTest {

            //when
            val actual = underTest.assets.first()

            service.push("Asset1")

            //then
            Assertions.assertThat(actual).isEqualTo("Asset1")
        }
    }

    @Test
    fun `Solution - use first() for very simple tests with multiple coroutines`() {
        runBlocking {
            launch {
                //given
                val actual = underTest.assets.first()
                //then
                Assertions.assertThat(actual).isEqualTo("Asset1")
            }
            launch {
                //when
                service.push("Asset1")
            }
        }
    }

    @Test
    fun `Google Recommends - when taking 2 and expecting 2`() {
        val actual = mutableListOf<String>()
        runBlocking {
            launch {
                underTest.assets.take(2).toList(actual)
            }
            launch {
                service.push("Asset1")
                service.push("Asset2")
            }
        }
        Assertions.assertThat(actual).containsExactly("Asset1", "Asset2")
    }

    @Test
    fun `Google Recommends - Possible Missing Failure - when taking 2 and ignoring the rest`() {
        val actual = mutableListOf<String>()
        runBlocking {
            launch {
                underTest.assets.take(2).toList(actual)
            }
            launch {
                service.push("Asset1")
                service.push("Asset2")
                service.push("Asset3") // What about this guy!
            }
        }
        Assertions.assertThat(actual).containsExactly("Asset1", "Asset2")
    }


    @Test
    fun `Google Recommends - Expected failure - when taking 2 but only one arrives - Test Hangs!`() {
        val actual = mutableListOf<String>()
        rule.testDispatcher.runBlockingTest {
            launch {
                underTest.assets.take(2).toList(actual)
            }
            launch {
                service.push("Asset1")
            }
        }
        Assertions.assertThat(actual).containsExactly("Asset1", "Asset2")
    }

    /**
     * Unit Testing Hot Flows with Turbine
     */

    @Test
    fun `Turbine - when three assets are pushed, three are detected`() {
         runBlocking {
            launch {
                underTest.assets.test {
                    println("Waiting for first condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset1")
                    println("First condition passed")
                    println("Waiting for second condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset2")
                    println("Second condition passed")
                    println("Waiting for third condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset3")
                    println("Third condition passed")
                    println("Expecting compolete")
                    expectComplete()
                }
            }
            launch {
                println("Pushing first")
                service.push("Asset1")
                println("Pushing second")
                service.push("Asset2")
                println("Pushing third")
                service.push("Asset3")
            }
        }
    }
    @Test
    fun `Turbine - Expected Failure - when four assets are pushed, but three are expected`() {
         runBlocking {
           launch {
                underTest.assets.test {
                    println("Waiting for first condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset1")
                    println("First condition passed")
                    println("Waiting for second condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset2")
                    println("Second condition passed")
                    println("Waiting for third condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset3")
                    println("Third condition passed")
                    println("Expecting complete")
                    expectComplete()
                    println("Complete")
                }
            }
          launch {
                println("Pushing first")
                service.push("Asset1")
                println("Pushing second")
                service.push("Asset2")
                println("Pushing third")
                service.push("Asset3")
                println("Pushing fourth")
                service.push("Asset4")
            }
        }
    }

    @Test
    fun `Turbine - Expected Failure - when two assets are pushed, but three are expected`() {
        runBlocking {
            launch{
                underTest.assets.test {
                    println("Waiting for first condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset1")
                    println("First condition passed")
                    println("Waiting for second condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset2")
                    println("Second condition passed")
                    println("Waiting for third condition...")
                    Assertions.assertThat(expectItem()).isEqualTo("Asset3")
                    println("Third condition passed")
                    println("Expecting complete")
                    expectComplete()
                    println("Complete")
                }
            }
            launch {
                println("Pushing first")
                service.push("Asset1")
                println("Pushing second")
                service.push("Asset2")
            }
        }
    }
}