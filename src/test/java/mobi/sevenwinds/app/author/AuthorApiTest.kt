package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import io.restassured.parsing.Parser
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
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun createAuthor () {

        val authorRecord = AuthorRecord("Mr. Wight")

        RestAssured.given()
            .jsonBody(authorRecord)
            .post("/author/add")
            .toResponse<AuthorResponse>().let { authorResponse ->
                Assert.assertEquals(authorRecord.fio, authorResponse.fio)
            }
    }

}