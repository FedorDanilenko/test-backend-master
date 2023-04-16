package mobi.sevenwinds.app.author

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.format.DateTimeFormat

object AuthorTable : IntIdTable("author") {
    val fio = text("fio")
    val dataCreated = date("date")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fio by AuthorTable.fio
    var dataCreated by AuthorTable.dataCreated

    fun toResponse(): AuthorResponse {
        return AuthorResponse(
            id.value,
            fio,
            dataCreated.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")) // перевод данных в нужный формат
        )
    }

}