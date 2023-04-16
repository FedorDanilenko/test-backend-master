package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import io.restassured.parsing.Parser
import mobi.sevenwinds.app.budget.BudgetRecord
import mobi.sevenwinds.app.budget.BudgetTable
import mobi.sevenwinds.app.budget.BudgetType
import mobi.sevenwinds.app.budget.BudgetYearStatsResponse
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthorApiTest : ServerTest() {


    @BeforeEach
    internal fun setUp() {
        transaction {
//            AuthorTable.deleteAll()
            BudgetTable.deleteAll()
        }
    }

    @Test
    fun addAuthorInBudget () {
        addAuthor(AuthorRecord("Mr. Wight"))
        addAuthor(AuthorRecord("Jesse"))
        addAuthor(AuthorRecord("Soul Goodman"))

        addRecord(BudgetRecord(2020, 4, 40, BudgetType.Расход))
        addRecord(BudgetRecord(2021, 1, 50, BudgetType.Приход, 1))
        addRecord(BudgetRecord(2021, 3, 30, BudgetType.Расход, 2))


    }

    private fun addRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecord>().let { response ->
                Assert.assertEquals(record, response)
            }
    }

    private fun addAuthor ( record: AuthorRecord) {

        RestAssured.given()
            .jsonBody(record)
            .post("/author/add")
            .toResponse<AuthorResponse>().let { authorResponse ->
                Assert.assertEquals(record.fio, authorResponse.fio)
            }
    }

}