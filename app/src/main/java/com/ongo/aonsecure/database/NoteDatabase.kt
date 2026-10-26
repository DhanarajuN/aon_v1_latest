package com.ongo.aonsecure.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ongo.aonsecure.model.*
import com.ongo.aonsecure.utils.CreateSubConverter
import com.ongo.aonsecure.utils.JobConverter
import com.ongo.aonsecure.utils.JobCommentsConverter
import com.ongo.aonsecure.utils.JobConverter1

@Database(
    entities = [
        Zone::class,
        Job::class,
        Insurer::class,
        ProdCat::class,
        Product::class,
        Tenure::class,
        LoanType::class,
        Calculate::class,
        CreatedSubJobs::class,
        JobComments::class,
        Tax::class,
        Calculate1::class,
        Terms::class,
     //   ProductType::class
    ],
    version = 12
)
@TypeConverters(
    JobConverter::class,
    JobConverter1::class,
    CreateSubConverter::class,
    JobCommentsConverter::class
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun zoneDao(): ZoneDao
    abstract fun prodCatDao(): ProdCatDao
    abstract fun prodctDao(): ProductDao
    abstract fun insurerDao(): InsurerDao
    abstract fun jobDao(): JobDao
    abstract fun taxDao(): TaxDao
    abstract fun tenureDao(): TenureDao
    abstract fun createdSubJobDao(): CreatedSubJobsDao
    abstract fun jobCommentDao(): JobCommentDao
    abstract fun quoteDao(): QuoteDao
    abstract fun calculateDao(): CalculateDao
    abstract fun loanTypeDao(): LoanTypeDao
    abstract fun termsDao(): TermsDao
  //  abstract fun productTypeDao(): ProductTypeDao

    companion object {
        private var instance: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase? {
            if (instance == null) {
                synchronized(NoteDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteDatabase::class.java, "note_database"
                    )
                        .addMigrations(
                            MIGRATION_9_10,
                            MIGRATION_10_11,
                           // MIGRATION_11_12
                        )
                        .fallbackToDestructiveMigration()
                        .addCallback(roomCallback)
                        .build()
                }
            }
            return instance
        }

        fun destroyInstance() {
            instance = null
        }

        private val roomCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // DB Initialization if needed
            }
        }
    }
}

// Migration from version 9 to 10
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE job_table ADD COLUMN `Minimum Age` TEXT")
        database.execSQL("ALTER TABLE job_table ADD COLUMN `Maximum Age` TEXT")
        database.execSQL("ALTER TABLE job_table ADD COLUMN TERMS_LINK TEXT")
        database.execSQL("ALTER TABLE job_table ADD COLUMN IS_TERMS_ENABLE TEXT")
        database.execSQL("ALTER TABLE job_table ADD COLUMN `Maximum Sum Assured` TEXT")
    }
}

// Migration from version 10 to 11
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""ALTER TABLE calculate_table1 ADD COLUMN "ProductType" TEXT""")
        database.execSQL("""ALTER TABLE calculate_table1 ADD COLUMN "Type" TEXT""")

    }

}




// Migration from version 11 to 12
//val MIGRATION_11_12 = object : Migration(11, 12) {
//    override fun migrate(database: SupportSQLiteDatabase) {
//        database.execSQL(
//            """
//            CREATE TABLE IF NOT EXISTS PRODUCT_TYPE_EMPTY _table (
//                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                name TEXT
//            )
//            """.trimIndent()
//        )
//    }
//}
