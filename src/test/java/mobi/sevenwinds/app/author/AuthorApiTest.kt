package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import mobi.sevenwinds.app.budget.*
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
            BudgetTable.deleteAll()
            AuthorTable.deleteAll()
            exec("ALTER SEQUENCE author_id_seq RESTART WITH 1")// сброс счетчика id в таблице author
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

                Assert.assertEquals("Mr. Wight", response.items[0].fio)
                Assert.assertEquals("Jesse", response.items[1].fio)
                Assert.assertEquals(70, response.totalByType[BudgetType.Расход.name])
            }
    }

    @Test
    fun filterAuthor () {
        addAuthor(AuthorRecord("Mr. Wight"))    // id 1
        addAuthor(AuthorRecord("Jesse"))        // id 2
        addAuthor(AuthorRecord("Soul Goodman")) // id 3
        addAuthor(AuthorRecord("Gus"))          // id 4

        addRecord(BudgetRecord(2019, 5, 100, BudgetType.Расход, 2))
        addRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход, 1))
        addRecord(BudgetRecord(2021, 4, 20, BudgetType.Расход, 2))
        addRecord(BudgetRecord(2020, 2, 10, BudgetType.Приход, 4))
        addRecord(BudgetRecord(2020, 8, 120, BudgetType.Приход, 1))
        addRecord(BudgetRecord(2020, 1, 60, BudgetType.Расход, 1))
        addRecord(BudgetRecord(2020, 10, 80, BudgetType.Расход, 3))
        addRecord(BudgetRecord(2020, 12, 90, BudgetType.Расход, 4))
        addRecord(BudgetRecord(2022, 3, 10, BudgetType.Расход, 4))

        RestAssured.given()
            .queryParam("fio","mr. WiGhT") // провека ввода с разным регистром
            .queryParam("limit", 100)
            .queryParam("offset", 0)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType} ")

                Assert.assertEquals(3, response.total) // у Mr. Wight должно быть 3 записи за 20й год
                Assert.assertEquals("Mr. Wight", response.items[0].fio) // проверка фильтра
                Assert.assertEquals(170, response.totalByType[BudgetType.Приход.name])
            }

        RestAssured.given()
            .queryParam("fio","gus") // провека ввода с разным регистром
            .queryParam("limit", 100)
            .queryParam("offset", 0)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("${response.total} / ${response.items} / ${response.totalByType} ")

                Assert.assertEquals(2, response.total) // у Gus должно быть 2 записи за 20й год
                Assert.assertEquals("Gus", response.items[0].fio) // проверка фильтра
                Assert.assertEquals(10, response.totalByType[BudgetType.Приход.name])
            }
    }

    private fun addRecord(record: BudgetRecord) {
        RestAssured.given()
            .jsonBody(record)
            .post("/budget/add")
            .toResponse<BudgetResponse>().let { response ->
                Assert.assertEquals(record.year, response.year)
                Assert.assertEquals(record.month, response.month)
                Assert.assertEquals(record.amount, response.amount)
                Assert.assertEquals(record.type, response.type)
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