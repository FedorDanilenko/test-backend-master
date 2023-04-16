package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = AuthorEntity.find { AuthorTable.id eq body.authorId }.firstOrNull() // Добавить автора записи если был указан его id
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            var baseQuery = (BudgetTable leftJoin AuthorTable)
                .select { BudgetTable.year eq param.year } // Выбор записи с нужным годом


            if (!param.fio.isNullOrBlank()) { // если передано ФИО автора, то добавляем фильтр
                baseQuery = baseQuery.andWhere {AuthorTable.fio.lowerCase() like "%${param.fio!!.toLowerCase()}%"
                }
            }

            val query = baseQuery
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC) //Сортировка
                .run { BudgetEntity.wrapRows(this) }


            val total = query.count() // подсчет общего количества записей
            val data = query.limit(param.limit, param.offset).map { it.toResponse() } // Записи для текущей страницы (пагинация)

            val sumByType = query.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } } // Сумма для каждого типа записи

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}