package com.shredder.unittestingflow

import app.cash.turbine.Event
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
@ExperimentalTime
class ColdAssetRepositoryTest {

    @get:Rule
    val rule = TestCoroutineRule()

    private lateinit var underTest: AssetRepository

    private fun given(vararg emit: String ) {
        underTest = ColdAssetRepository(emit.toList())
    }

    @Test
    fun `Google Recommends - use first() for very simple tests`() {
        runBlocking {
            given("Asset1", "Asset2")

            //when
            val actual = underTest.assets.first()

            //then
            Assertions.assertThat(actual).isEqualTo("Asset1")
        }
    }


    /**
     * Unit Testing Cold Flows the Android Google Recommends way
     */

    @Test
    fun `Google Recommends - when using the Android Google Recommends way - take() - to collect a specific number of items we get a Success!`() {
        runBlocking {
            given("Asset1", "Asset2")

            // when
            val actual = mutableListOf<String>()
            underTest.assets
                .take(2)
                .collect { actual.add(it) }

            //then
            Assertions.assertThat(actual).containsExactly("Asset1", "Asset2")
        }
    }

    @Test
    fun `Google Recommends - Expected Failure - When we take too little the Android Google Recommends way gives success! Oh no!`() {
        runBlocking {
            given("Asset1", "Asset2", "Asset3")

            // when
            val actual = mutableListOf<String>()
            underTest.assets
                .take(2)
                .collect { actual.add(it) }

            // then
            Assertions.assertThat(actual)
                .containsExactly("Asset1", "Asset2") // We want this to fail!
        }
    }

    @Test
    fun `Google Recommends - Expected Failure - when we take more than we give the test should fail`() {
        runBlocking {
            given("Asset1")

            //when
            val actual = mutableListOf<String>()
            underTest.assets
                .take(2)
                .collect { actual.add(it) }

            //then
            Assertions.assertThat(actual)
                .containsExactly("Asset1", "Asset2")
        }
    }


    /**
     * Unit Testing Cold Flows with Turbine
     */

    @Test
    fun `Turbine - when collecting items from the flow with Turbine we also check the flow completes `() {
        runBlocking {
            given("Asset1", "Asset2")

            //when
            underTest.assets.test {

                //then
                Assertions.assertThat(expectItem()).isEqualTo("Asset1")
                Assertions.assertThat(expectItem()).isEqualTo("Asset2")
                expectComplete()
            }
        }
    }

    @Test
    fun `Turbine - an alternative style to the test above`() {
        runBlocking {
            given("Asset1", "Asset2")

            //when
            underTest.assets.test {

                //then
                val actual = cancelAndConsumeRemainingEvents()
                Assertions.assertThat(actual).containsExactly(
                    Event.Item("Asset1"),
                    Event.Item("Asset2"),
                    Event.Complete
                )
            }
        }
    }

    @Test
    fun `Turbine - Expected Failure - when we take too little, Turbine catches our implementation change!`() {
        runBlocking {
            given("Asset1", "Asset2", "Asset3")

            //when
            underTest.assets.test {

                //then
                Assertions.assertThat(expectItem()).isEqualTo("Asset1")
                Assertions.assertThat(expectItem()).isEqualTo("Asset2")
                expectComplete()
            }
        }
    }

    @Test
    fun `Turbine - Expected Failure - when we take take too much, Turbine catches our implementation change!`() {
        runBlocking {
            given("Asset1")

            //when
            underTest.assets.test {

                //then
                Assertions.assertThat(expectItem()).isEqualTo("Asset1")
                Assertions.assertThat(expectItem()).isEqualTo("Asset2")
                expectComplete()
            }
        }
    }

}