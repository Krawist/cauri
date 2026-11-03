package com.cauri.core.data.repository

import com.cauri.core.data.database.CauriDatabase
import com.cauri.core.data.database.entity.AccountEntity.Companion.CASH_ACCOUNT_ID
import com.cauri.core.data.database.entity.TransactionEntity
import com.cauri.core.data.database.entity.TransactionOrigin
import com.cauri.core.data.database.entity.TransactionStatus
import com.cauri.core.data.database.entity.TransactionType
import com.cauri.core.data.database.entity.VisitEntity
import com.cauri.core.data.database.inMemoryDatabase
import com.cauri.core.data.model.NewPlace
import com.cauri.core.data.model.PlaceKind
import com.cauri.core.data.service.PlaceServiceImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class PlaceRepositoryImplTest {
    private lateinit var database: CauriDatabase
    private lateinit var repository: PlaceRepositoryImpl

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        repository = PlaceRepositoryImpl(database.placeDao(), PlaceServiceImpl())
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Places {
        @Test
        fun `should list the places by name with their visits`() = runTest {
            // given
            val marketId = repository.addPlace(place(name = "Marché Mokolo"))
            repository.addPlace(place(name = "boulangerie Saker"))
            database.visitDao().insert(VisitEntity(placeId = marketId, enteredAt = Instant.EPOCH, leftAt = null))

            // when
            val places = repository.places.first()

            // then
            places.map { it.name to it.visits } shouldContainExactly
                listOf("boulangerie Saker" to 0, "Marché Mokolo" to 1)
        }
    }

    @Nested
    inner class AddPlace {
        @Test
        fun `should save the place with its name trimmed`() = runTest {
            // when
            val id = repository.addPlace(place(name = "  Marché Mokolo "))

            // then
            repository.getPlace(id).shouldNotBeNull().name shouldBe "Marché Mokolo"
        }

        @Test
        fun `should never remind about home`() = runTest {
            // when
            val id = repository.addPlace(place(name = "Maison").copy(kind = PlaceKind.HOME, remindAfterVisit = true))

            // then
            repository.getPlace(id).shouldNotBeNull().remindAfterVisit shouldBe false
        }

        @Test
        fun `should keep the neighbourhood of the place`() = runTest {
            // when
            val id = repository.addPlace(place(name = "Chez maman").copy(area = "Odza"))

            // then
            repository.getPlace(id).shouldNotBeNull().area shouldBe "Odza"
        }
    }

    @Nested
    inner class UpdatePlace {
        @Test
        fun `should replace the values of the place`() = runTest {
            // given
            val id = repository.addPlace(place(name = "Marché Mokolo"))

            // when
            repository.updatePlace(id, place(name = "Marché Central").copy(radiusMeters = 150))

            // then
            repository.getPlace(id).shouldNotBeNull().let { it.name to it.radiusMeters } shouldBe
                ("Marché Central" to 150)
        }
    }

    @Nested
    inner class DeletePlace {
        @Test
        fun `should delete the place and its visits, the transactions made there losing only their place`() = runTest {
            // given
            val id = repository.addPlace(place(name = "Marché Mokolo"))
            database.visitDao().insert(VisitEntity(placeId = id, enteredAt = Instant.EPOCH, leftAt = null))
            val transactionId = database.transactionDao().insert(expense(placeId = id))

            // when
            repository.deletePlace(id)

            // then
            repository.getPlace(id).shouldBeNull()
            database.visitDao().getOngoing().shouldBeEmpty()
            database.transactionDao().getById(transactionId).shouldNotBeNull().placeId.shouldBeNull()
        }
    }

    private fun place(name: String) = NewPlace(
        name = name,
        latitude = 3.88,
        longitude = 11.50,
        radiusMeters = 80,
        kind = PlaceKind.SHOP,
        defaultCategoryId = null,
        remindAfterVisit = true,
        area = null,
        address = null,
    )

    private fun expense(placeId: Long) = TransactionEntity(
        accountId = CASH_ACCOUNT_ID,
        type = TransactionType.EXPENSE,
        status = TransactionStatus.CONFIRMED,
        origin = TransactionOrigin.MANUAL,
        amount = 2_000,
        occurredAt = Instant.EPOCH,
        categoryId = null,
        placeId = placeId,
        rawSignalId = null,
        transferGroup = null,
        counterparty = null,
        note = null,
        foreignAmount = null,
        foreignCurrency = null,
    )
}
