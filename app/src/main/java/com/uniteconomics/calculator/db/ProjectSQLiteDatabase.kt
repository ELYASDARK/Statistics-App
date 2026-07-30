package com.uniteconomics.calculator.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.calculator.app.data.CalculationResult
import com.calculator.app.data.CalculatorInputs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class ProjectEntity(
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val retailPrice: Double = 35000.0,
    val sourcingCost: Double = 10000.0,
    val aovMultiplier: Double = 1.0,
    val discounts: Double = 0.0,
    val cac: Double = 5000.0,
    val adFee: Double = 3.0,
    val ltvMultiplier: Double = 1.1,
    val shippingCost: Double = 4000.0,
    val opsCost: Double = 750.0,
    val rejectionRate: Double = 20.0,
    val rtsShippingFee: Double = 0.0,
    val refundRate: Double = 1.0,
    val refundPenalty: Double = 5000.0,
    val damageRate: Double = 5.0,
    val deadstockRate: Double = 2.0,
    val fixedMonthlyExpenses: Double = 450000.0,
    val targetProfitGoal: Double = 1000000.0,
    val projectDurationDays: Double = 30.0,
    val capitalRemittanceFrequency: Double = 7.0,
    val revenue: Double = 0.0,
    val netProfitTotal: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val dailyCashBurn: Double = 0.0,
    val isLoss: Boolean = false
) {
    fun toCalculatorInputs(): CalculatorInputs {
        return CalculatorInputs(
            retailPrice = retailPrice,
            sourcingCost = sourcingCost,
            aovMultiplier = aovMultiplier,
            discounts = discounts,
            cac = cac,
            adFee = adFee,
            ltvMultiplier = ltvMultiplier,
            shippingCost = shippingCost,
            opsCost = opsCost,
            rejectionRate = rejectionRate,
            rtsShippingFee = rtsShippingFee,
            refundRate = refundRate,
            refundPenalty = refundPenalty,
            damageRate = damageRate,
            deadstockRate = deadstockRate,
            fixedMonthlyExpenses = fixedMonthlyExpenses,
            targetProfitGoal = targetProfitGoal,
            projectDurationDays = projectDurationDays,
            capitalRemittanceFrequency = capitalRemittanceFrequency,
            retail = retailPrice,
            sourcing = sourcingCost,
            aov = aovMultiplier,
            discount = discounts,
            ltv = ltvMultiplier,
            shipping = shippingCost,
            ops = opsCost,
            reject = rejectionRate,
            returnFeeRTS = rtsShippingFee,
            refund = refundRate,
            damage = damageRate,
            deadstock = deadstockRate,
            fixedMonthly = fixedMonthlyExpenses,
            goal = targetProfitGoal,
            days = projectDurationDays,
            remit = capitalRemittanceFrequency
        )
    }

    companion object {
        fun fromInputsAndResult(
            id: Long = 0,
            name: String,
            inputs: CalculatorInputs,
            result: CalculationResult
        ): ProjectEntity {
            return ProjectEntity(
                id = id,
                name = name,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                retailPrice = inputs.effRetailPrice,
                sourcingCost = inputs.effSourcingCost,
                aovMultiplier = inputs.effAovMultiplier,
                discounts = inputs.effDiscounts,
                cac = inputs.cac,
                adFee = inputs.adFee,
                ltvMultiplier = inputs.effLtvMultiplier,
                shippingCost = inputs.effShippingCost,
                opsCost = inputs.effOpsCost,
                rejectionRate = inputs.effRejectionRate,
                rtsShippingFee = inputs.effRtsShippingFee,
                refundRate = inputs.effRefundRate,
                refundPenalty = inputs.refundPenalty,
                damageRate = inputs.effDamageRate,
                deadstockRate = inputs.effDeadstockRate,
                fixedMonthlyExpenses = inputs.effFixedMonthlyExpenses,
                targetProfitGoal = inputs.effTargetProfitGoal,
                projectDurationDays = inputs.effProjectDurationDays,
                capitalRemittanceFrequency = inputs.effCapitalRemittanceFrequency,
                revenue = result.revenue,
                netProfitTotal = result.netProfitTotal,
                totalExpenses = result.totalProjectExpenses,
                dailyCashBurn = result.dailyCashBurn,
                isLoss = result.isLoss
            )
        }
    }
}

class ProjectSQLiteDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    private val _projectsFlow = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val projectsFlow: StateFlow<List<ProjectEntity>> = _projectsFlow.asStateFlow()

    companion object {
        private const val DATABASE_NAME = "unit_economics_calculator_sqlite.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_PROJECTS = "projects"

        @Volatile
        private var INSTANCE: ProjectSQLiteDatabase? = null

        fun getInstance(context: Context): ProjectSQLiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = ProjectSQLiteDatabase(context.applicationContext)
                INSTANCE = instance
                instance.refreshProjects()
                instance
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS $TABLE_PROJECTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                retailPrice REAL NOT NULL,
                sourcingCost REAL NOT NULL,
                aovMultiplier REAL NOT NULL,
                discounts REAL NOT NULL,
                cac REAL NOT NULL,
                adFee REAL NOT NULL,
                ltvMultiplier REAL NOT NULL,
                shippingCost REAL NOT NULL,
                opsCost REAL NOT NULL,
                rejectionRate REAL NOT NULL,
                rtsShippingFee REAL NOT NULL,
                refundRate REAL NOT NULL,
                refundPenalty REAL NOT NULL,
                damageRate REAL NOT NULL,
                deadstockRate REAL NOT NULL,
                fixedMonthlyExpenses REAL NOT NULL,
                targetProfitGoal REAL NOT NULL,
                projectDurationDays REAL NOT NULL,
                capitalRemittanceFrequency REAL NOT NULL,
                revenue REAL NOT NULL,
                netProfitTotal REAL NOT NULL,
                totalExpenses REAL NOT NULL,
                dailyCashBurn REAL NOT NULL,
                isLoss INTEGER NOT NULL
            );
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROJECTS")
        onCreate(db)
    }

    private val dbScope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO + kotlinx.coroutines.SupervisorJob())

    fun refreshProjects() {
        dbScope.launch {
            try {
                val list = mutableListOf<ProjectEntity>()
                val db = readableDatabase
                val cursor = db.rawQuery("SELECT * FROM $TABLE_PROJECTS ORDER BY updatedAt DESC", null)
                cursor.use { c ->
                    val idIdx = c.getColumnIndex("id")
                    val nameIdx = c.getColumnIndex("name")
                    val createdAtIdx = c.getColumnIndex("createdAt")
                    val updatedAtIdx = c.getColumnIndex("updatedAt")
                    val retailPriceIdx = c.getColumnIndex("retailPrice")
                    val sourcingCostIdx = c.getColumnIndex("sourcingCost")
                    val aovMultiplierIdx = c.getColumnIndex("aovMultiplier")
                    val discountsIdx = c.getColumnIndex("discounts")
                    val cacIdx = c.getColumnIndex("cac")
                    val adFeeIdx = c.getColumnIndex("adFee")
                    val ltvMultiplierIdx = c.getColumnIndex("ltvMultiplier")
                    val shippingCostIdx = c.getColumnIndex("shippingCost")
                    val opsCostIdx = c.getColumnIndex("opsCost")
                    val rejectionRateIdx = c.getColumnIndex("rejectionRate")
                    val rtsShippingFeeIdx = c.getColumnIndex("rtsShippingFee")
                    val refundRateIdx = c.getColumnIndex("refundRate")
                    val refundPenaltyIdx = c.getColumnIndex("refundPenalty")
                    val damageRateIdx = c.getColumnIndex("damageRate")
                    val deadstockRateIdx = c.getColumnIndex("deadstockRate")
                    val fixedMonthlyExpensesIdx = c.getColumnIndex("fixedMonthlyExpenses")
                    val targetProfitGoalIdx = c.getColumnIndex("targetProfitGoal")
                    val projectDurationDaysIdx = c.getColumnIndex("projectDurationDays")
                    val capitalRemittanceFrequencyIdx = c.getColumnIndex("capitalRemittanceFrequency")
                    val revenueIdx = c.getColumnIndex("revenue")
                    val netProfitTotalIdx = c.getColumnIndex("netProfitTotal")
                    val totalExpensesIdx = c.getColumnIndex("totalExpenses")
                    val dailyCashBurnIdx = c.getColumnIndex("dailyCashBurn")
                    val isLossIdx = c.getColumnIndex("isLoss")

                    while (c.moveToNext()) {
                        list.add(
                            ProjectEntity(
                                id = if (idIdx >= 0) c.getLong(idIdx) else 0L,
                                name = if (nameIdx >= 0) c.getString(nameIdx) else "",
                                createdAt = if (createdAtIdx >= 0) c.getLong(createdAtIdx) else 0L,
                                updatedAt = if (updatedAtIdx >= 0) c.getLong(updatedAtIdx) else 0L,
                                retailPrice = if (retailPriceIdx >= 0) c.getDouble(retailPriceIdx) else 35000.0,
                                sourcingCost = if (sourcingCostIdx >= 0) c.getDouble(sourcingCostIdx) else 10000.0,
                                aovMultiplier = if (aovMultiplierIdx >= 0) c.getDouble(aovMultiplierIdx) else 1.0,
                                discounts = if (discountsIdx >= 0) c.getDouble(discountsIdx) else 0.0,
                                cac = if (cacIdx >= 0) c.getDouble(cacIdx) else 5000.0,
                                adFee = if (adFeeIdx >= 0) c.getDouble(adFeeIdx) else 3.0,
                                ltvMultiplier = if (ltvMultiplierIdx >= 0) c.getDouble(ltvMultiplierIdx) else 1.1,
                                shippingCost = if (shippingCostIdx >= 0) c.getDouble(shippingCostIdx) else 4000.0,
                                opsCost = if (opsCostIdx >= 0) c.getDouble(opsCostIdx) else 750.0,
                                rejectionRate = if (rejectionRateIdx >= 0) c.getDouble(rejectionRateIdx) else 20.0,
                                rtsShippingFee = if (rtsShippingFeeIdx >= 0) c.getDouble(rtsShippingFeeIdx) else 0.0,
                                refundRate = if (refundRateIdx >= 0) c.getDouble(refundRateIdx) else 1.0,
                                refundPenalty = if (refundPenaltyIdx >= 0) c.getDouble(refundPenaltyIdx) else 5000.0,
                                damageRate = if (damageRateIdx >= 0) c.getDouble(damageRateIdx) else 5.0,
                                deadstockRate = if (deadstockRateIdx >= 0) c.getDouble(deadstockRateIdx) else 2.0,
                                fixedMonthlyExpenses = if (fixedMonthlyExpensesIdx >= 0) c.getDouble(fixedMonthlyExpensesIdx) else 450000.0,
                                targetProfitGoal = if (targetProfitGoalIdx >= 0) c.getDouble(targetProfitGoalIdx) else 1000000.0,
                                projectDurationDays = if (projectDurationDaysIdx >= 0) c.getDouble(projectDurationDaysIdx) else 30.0,
                                capitalRemittanceFrequency = if (capitalRemittanceFrequencyIdx >= 0) c.getDouble(capitalRemittanceFrequencyIdx) else 7.0,
                                revenue = if (revenueIdx >= 0) c.getDouble(revenueIdx) else 0.0,
                                netProfitTotal = if (netProfitTotalIdx >= 0) c.getDouble(netProfitTotalIdx) else 0.0,
                                totalExpenses = if (totalExpensesIdx >= 0) c.getDouble(totalExpensesIdx) else 0.0,
                                dailyCashBurn = if (dailyCashBurnIdx >= 0) c.getDouble(dailyCashBurnIdx) else 0.0,
                                isLoss = if (isLossIdx >= 0) c.getInt(isLossIdx) == 1 else false
                            )
                        )
                    }
                }
                _projectsFlow.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun insertOrUpdateProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", project.name)
            put("createdAt", project.createdAt)
            put("updatedAt", System.currentTimeMillis())
            put("retailPrice", project.retailPrice)
            put("sourcingCost", project.sourcingCost)
            put("aovMultiplier", project.aovMultiplier)
            put("discounts", project.discounts)
            put("cac", project.cac)
            put("adFee", project.adFee)
            put("ltvMultiplier", project.ltvMultiplier)
            put("shippingCost", project.shippingCost)
            put("opsCost", project.opsCost)
            put("rejectionRate", project.rejectionRate)
            put("rtsShippingFee", project.rtsShippingFee)
            put("refundRate", project.refundRate)
            put("refundPenalty", project.refundPenalty)
            put("damageRate", project.damageRate)
            put("deadstockRate", project.deadstockRate)
            put("fixedMonthlyExpenses", project.fixedMonthlyExpenses)
            put("targetProfitGoal", project.targetProfitGoal)
            put("projectDurationDays", project.projectDurationDays)
            put("capitalRemittanceFrequency", project.capitalRemittanceFrequency)
            put("revenue", project.revenue)
            put("netProfitTotal", project.netProfitTotal)
            put("totalExpenses", project.totalExpenses)
            put("dailyCashBurn", project.dailyCashBurn)
            put("isLoss", if (project.isLoss) 1 else 0)
        }

        val resultId = if (project.id > 0) {
            db.update(TABLE_PROJECTS, values, "id = ?", arrayOf(project.id.toString()))
            project.id
        } else {
            db.insert(TABLE_PROJECTS, null, values)
        }
        refreshProjects()
        resultId
    }

    suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete(TABLE_PROJECTS, "id = ?", arrayOf(project.id.toString()))
        refreshProjects()
    }
}
