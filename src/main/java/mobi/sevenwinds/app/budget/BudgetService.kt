package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val author = AuthorEntity.find { AuthorTable.id eq body.authorId }.firstOrNull()
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = author
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = (BudgetTable leftJoin AuthorTable)
                .select{ BudgetTable.year eq param.year } // Выбор записи с нужным годом
//                .also { query ->
//                    param.authorName?.let {
//                        query.andWhere {
//                            AuthorTable.fio.
//                        }
//                    }
//                }
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC) //Сортировка
                .run { BudgetEntity.wrapRows(this) } //Преобразование строк в сущности


            val total = query.count() // подсчет общего количества
            val data = query.limit(param.limit, param.offset).map { it.toResponse() } // Записи для текущей страницы

            val sumByType = query.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } } // Сумма для каждого типа записи

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}