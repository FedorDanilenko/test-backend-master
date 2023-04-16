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
        addRecord(BudgetRecord(2020, 1, 50, BudgetType.Приход, 1))
        addRecord(BudgetRecord(2020, 3, 30, BudgetType.Расход, 2))

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType} ")

                Assert.assertEquals("Mr. Wight", response.items[0].authorName)
                Assert.assertEquals("Jesse", response.items[1].authorName)
                Assert.assertEquals(70, response.totalByType[BudgetType.Расход.name])
            }
    }

    private fun addRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetRecord>().let { response ->
                Assert.assertEquals(record.year, response.year)
                Assert.assertEquals(record.month, response.month)
                Assert.assertEquals(record.amount, response.amount)
                Assert.assertEquals(record.type, response.type)
                Assert.assertEquals(record.authorId, response.authorId)
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