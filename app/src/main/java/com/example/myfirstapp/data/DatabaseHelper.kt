package com.example.myfirstapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myfirstapp.model.CartItem
import com.example.myfirstapp.model.Listing
import com.example.myfirstapp.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context, DB_NAME, null, DB_VERSION
) {
    companion object {
        private const val DB_NAME    = "MonitorMarketplace.db"
        private const val DB_VERSION = 5

        private const val T_USERS       = "users"
        private const val T_LISTINGS    = "listings"
        private const val T_CART        = "cart_items"
        private const val T_ORDERS      = "orders"
        private const val T_ORDER_ITEMS = "order_items"

        private const val COL_ID = "id"

        // users
        private const val U_NAME     = "name"
        private const val U_EMAIL    = "email"
        private const val U_PASSWORD = "password"
        private const val U_ROLE     = "role"
        private const val U_DISABLED = "disabled"

        // listings
        private const val L_SELLER_ID   = "sellerId"
        private const val L_TITLE       = "title"
        private const val L_DESC        = "description"
        private const val L_BRAND       = "brand"
        private const val L_SCREEN_SIZE = "screenSize"
        private const val L_RESOLUTION  = "resolution"
        private const val L_CONDITION   = "condition"
        private const val L_CATEGORY    = "category"
        private const val L_PRICE       = "price"
        private const val L_PHOTO_URI   = "photoUri"
        private const val L_STATUS      = "status"
        private const val L_CREATED_AT  = "createdAt"

        // cart
        private const val C_USER_ID    = "userId"
        private const val C_LISTING_ID = "listingId"
        private const val C_QTY        = "quantity"

        // orders
        private const val O_USER_ID  = "userId"
        private const val O_TOTAL    = "total"
        private const val O_SHIPPING = "shippingInfo"
        private const val O_PAYMENT  = "paymentMethod"
        private const val O_CREATED  = "createdAt"

        // order items
        private const val OI_ORDER_ID = "orderId"
        private const val OI_TITLE    = "title"
        private const val OI_PRICE    = "price"
        private const val OI_QTY      = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $T_USERS (
                $COL_ID     INTEGER PRIMARY KEY AUTOINCREMENT,
                $U_NAME     TEXT NOT NULL,
                $U_EMAIL    TEXT NOT NULL UNIQUE,
                $U_PASSWORD TEXT NOT NULL,
                $U_ROLE     TEXT NOT NULL,
                $U_DISABLED INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $T_LISTINGS (
                $COL_ID        INTEGER PRIMARY KEY AUTOINCREMENT,
                $L_SELLER_ID   INTEGER NOT NULL,
                $L_TITLE       TEXT NOT NULL,
                $L_DESC        TEXT NOT NULL,
                $L_BRAND       TEXT NOT NULL,
                $L_SCREEN_SIZE TEXT NOT NULL,
                $L_RESOLUTION  TEXT NOT NULL,
                $L_CONDITION   TEXT NOT NULL,
                $L_CATEGORY    TEXT NOT NULL DEFAULT 'General',
                $L_PRICE       REAL NOT NULL,
                $L_PHOTO_URI   TEXT,
                $L_STATUS      TEXT NOT NULL DEFAULT 'ACTIVE',
                $L_CREATED_AT  INTEGER NOT NULL,
                FOREIGN KEY($L_SELLER_ID) REFERENCES $T_USERS($COL_ID)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $T_CART (
                $COL_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $C_USER_ID    INTEGER NOT NULL,
                $C_LISTING_ID INTEGER NOT NULL,
                $C_QTY        INTEGER NOT NULL,
                FOREIGN KEY($C_USER_ID)    REFERENCES $T_USERS($COL_ID),
                FOREIGN KEY($C_LISTING_ID) REFERENCES $T_LISTINGS($COL_ID)
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $T_ORDERS (
                $COL_ID     INTEGER PRIMARY KEY AUTOINCREMENT,
                $O_USER_ID  INTEGER NOT NULL,
                $O_TOTAL    REAL NOT NULL,
                $O_SHIPPING TEXT NOT NULL,
                $O_PAYMENT  TEXT NOT NULL,
                $O_CREATED  INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE $T_ORDER_ITEMS (
                $COL_ID       INTEGER PRIMARY KEY AUTOINCREMENT,
                $OI_ORDER_ID  INTEGER NOT NULL,
                $OI_TITLE     TEXT NOT NULL,
                $OI_PRICE     REAL NOT NULL,
                $OI_QTY       INTEGER NOT NULL,
                FOREIGN KEY($OI_ORDER_ID) REFERENCES $T_ORDERS($COL_ID)
            )
        """.trimIndent())

        // Seed admin
        val adminCv = ContentValues().apply {
            put(U_NAME,     "Admin")
            put(U_EMAIL,    "admin@mavs.uta.edu")
            put(U_PASSWORD, "admin123")
            put(U_ROLE,     "ADMIN")
            put(U_DISABLED, 0)
        }
        db.insert(T_USERS, null, adminCv)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $T_ORDER_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $T_ORDERS")
        db.execSQL("DROP TABLE IF EXISTS $T_CART")
        db.execSQL("DROP TABLE IF EXISTS $T_LISTINGS")
        db.execSQL("DROP TABLE IF EXISTS $T_USERS")
        onCreate(db)
    }

    // ─── USERS ────────────────────────────────────────────────────────────────

    fun addUser(user: User): Long {
        val cv = ContentValues().apply {
            put(U_NAME,     user.name)
            put(U_EMAIL,    user.email.lowercase())
            put(U_PASSWORD, user.password)
            put(U_ROLE,     user.role)
            put(U_DISABLED, 0)
        }
        return writableDatabase.insert(T_USERS, null, cv)
    }

    fun getUserByEmail(email: String): User? {
        val cursor = readableDatabase.query(
            T_USERS, null,
            "$U_EMAIL=?", arrayOf(email.lowercase()),
            null, null, null
        )
        return cursor.use { if (it.moveToFirst()) cursorToUser(it) else null }
    }

    fun getUserById(userId: Long): User? {
        val cursor = readableDatabase.query(
            T_USERS, null,
            "$COL_ID=?", arrayOf(userId.toString()),
            null, null, null
        )
        return cursor.use { if (it.moveToFirst()) cursorToUser(it) else null }
    }

    fun login(email: String, password: String): User? {
        val cursor = readableDatabase.query(
            T_USERS, null,
            "$U_EMAIL=? AND $U_PASSWORD=?",
            arrayOf(email.lowercase(), password),
            null, null, null
        )
        return cursor.use { if (it.moveToFirst()) cursorToUser(it) else null }
    }

    fun getAllUsers(): List<User> {
        val cursor = readableDatabase.query(
            T_USERS, null, null, null, null, null, null
        )
        return cursor.use {
            val out = mutableListOf<User>()
            while (it.moveToNext()) out.add(cursorToUser(it))
            out
        }
    }

    fun setUserDisabled(userId: Long, disabled: Boolean): Int {
        val cv = ContentValues().apply { put(U_DISABLED, if (disabled) 1 else 0) }
        return writableDatabase.update(T_USERS, cv, "$COL_ID=?", arrayOf(userId.toString()))
    }

    fun updateUserProfile(userId: Long, name: String): Int {
        val cv = ContentValues().apply { put(U_NAME, name) }
        return writableDatabase.update(T_USERS, cv, "$COL_ID=?", arrayOf(userId.toString()))
    }

    private fun cursorToUser(c: Cursor) = User(
        id       = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
        name     = c.getString(c.getColumnIndexOrThrow(U_NAME)),
        email    = c.getString(c.getColumnIndexOrThrow(U_EMAIL)),
        password = c.getString(c.getColumnIndexOrThrow(U_PASSWORD)),
        role     = c.getString(c.getColumnIndexOrThrow(U_ROLE)),
        disabled = c.getInt(c.getColumnIndexOrThrow(U_DISABLED)) == 1
    )

    // ─── LISTINGS ─────────────────────────────────────────────────────────────

    fun addListing(listing: Listing): Long {
        val cv = ContentValues().apply {
            put(L_SELLER_ID,   listing.sellerId)
            put(L_TITLE,       listing.title)
            put(L_DESC,        listing.description)
            put(L_BRAND,       listing.brand)
            put(L_SCREEN_SIZE, listing.screenSize)
            put(L_RESOLUTION,  listing.resolution)
            put(L_CONDITION,   listing.condition)
            put(L_CATEGORY,    listing.category)
            put(L_PRICE,       listing.price)
            put(L_PHOTO_URI,   listing.photoUri)
            put(L_STATUS,      listing.status)
            put(L_CREATED_AT,  listing.createdAt)
        }
        return writableDatabase.insert(T_LISTINGS, null, cv)
    }

    fun getAllListingsGlobal(): List<Listing> {
        val cursor = readableDatabase.query(
            T_LISTINGS, null,
            "$L_STATUS=?", arrayOf("ACTIVE"),
            null, null, "$L_CREATED_AT DESC"
        )
        return cursor.use { toListings(it) }
    }

    fun getListingsBySeller(sellerId: Long): List<Listing> {
        val cursor = readableDatabase.query(
            T_LISTINGS, null,
            "$L_SELLER_ID=?", arrayOf(sellerId.toString()),
            null, null, "$L_CREATED_AT DESC"
        )
        return cursor.use { toListings(it) }
    }

    fun getAllListingsForAdmin(): List<Listing> {
        val cursor = readableDatabase.query(
            T_LISTINGS, null, null, null, null, null,
            "$L_CREATED_AT DESC"
        )
        return cursor.use { toListings(it) }
    }

    fun getListingById(listingId: Long): Listing? {
        val cursor = readableDatabase.query(
            T_LISTINGS, null,
            "$COL_ID=?", arrayOf(listingId.toString()),
            null, null, null
        )
        return cursor.use { if (it.moveToFirst()) cursorToListing(it) else null }
    }

    fun updateListing(listing: Listing): Int {
        val cv = ContentValues().apply {
            put(L_TITLE,       listing.title)
            put(L_DESC,        listing.description)
            put(L_BRAND,       listing.brand)
            put(L_SCREEN_SIZE, listing.screenSize)
            put(L_RESOLUTION,  listing.resolution)
            put(L_CONDITION,   listing.condition)
            put(L_CATEGORY,    listing.category)
            put(L_PRICE,       listing.price)
            put(L_PHOTO_URI,   listing.photoUri)
            put(L_STATUS,      listing.status)
        }
        return writableDatabase.update(T_LISTINGS, cv, "$COL_ID=?", arrayOf(listing.id.toString()))
    }

    fun deleteListing(listingId: Long): Int {
        return writableDatabase.delete(T_LISTINGS, "$COL_ID=?", arrayOf(listingId.toString()))
    }

    fun markListingRemoved(listingId: Long): Int {
        val cv = ContentValues().apply { put(L_STATUS, "REMOVED") }
        return writableDatabase.update(T_LISTINGS, cv, "$COL_ID=?", arrayOf(listingId.toString()))
    }

    private fun toListings(cursor: Cursor): List<Listing> {
        val out = mutableListOf<Listing>()
        while (cursor.moveToNext()) out.add(cursorToListing(cursor))
        return out
    }

    private fun cursorToListing(c: Cursor) = Listing(
        id          = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
        sellerId    = c.getLong(c.getColumnIndexOrThrow(L_SELLER_ID)),
        title       = c.getString(c.getColumnIndexOrThrow(L_TITLE)),
        description = c.getString(c.getColumnIndexOrThrow(L_DESC)),
        brand       = c.getString(c.getColumnIndexOrThrow(L_BRAND)),
        screenSize  = c.getString(c.getColumnIndexOrThrow(L_SCREEN_SIZE)),
        resolution  = c.getString(c.getColumnIndexOrThrow(L_RESOLUTION)),
        condition   = c.getString(c.getColumnIndexOrThrow(L_CONDITION)),
        category    = c.getString(c.getColumnIndexOrThrow(L_CATEGORY)),
        price       = c.getDouble(c.getColumnIndexOrThrow(L_PRICE)),
        photoUri    = c.getString(c.getColumnIndexOrThrow(L_PHOTO_URI)),
        status      = c.getString(c.getColumnIndexOrThrow(L_STATUS)),
        createdAt   = c.getLong(c.getColumnIndexOrThrow(L_CREATED_AT))
    )

    // ─── CART ─────────────────────────────────────────────────────────────────

    fun addToCart(userId: Long, listingId: Long, qty: Int = 1): Long {
        val existing = getCartItemByIds(userId, listingId)
        return if (existing != null) {
            updateCartItemQuantity(existing.id, existing.quantity + qty)
            existing.id
        } else {
            val cv = ContentValues().apply {
                put(C_USER_ID,    userId)
                put(C_LISTING_ID, listingId)
                put(C_QTY,        qty)
            }
            writableDatabase.insert(T_CART, null, cv)
        }
    }

    fun getCartItems(userId: Long): List<CartItem> {
        val cursor = readableDatabase.query(
            T_CART, null,
            "$C_USER_ID=?", arrayOf(userId.toString()),
            null, null, null
        )
        return cursor.use {
            val out = mutableListOf<CartItem>()
            while (it.moveToNext()) {
                out.add(CartItem(
                    id        = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                    userId    = it.getLong(it.getColumnIndexOrThrow(C_USER_ID)),
                    listingId = it.getLong(it.getColumnIndexOrThrow(C_LISTING_ID)),
                    quantity  = it.getInt(it.getColumnIndexOrThrow(C_QTY))
                ))
            }
            out
        }
    }

    fun updateCartItemQuantity(cartItemId: Long, newQty: Int): Int {
        val cv = ContentValues().apply { put(C_QTY, newQty) }
        return writableDatabase.update(T_CART, cv, "$COL_ID=?", arrayOf(cartItemId.toString()))
    }

    fun removeCartItem(cartItemId: Long): Int {
        return writableDatabase.delete(T_CART, "$COL_ID=?", arrayOf(cartItemId.toString()))
    }

    fun clearCart(userId: Long): Int {
        return writableDatabase.delete(T_CART, "$C_USER_ID=?", arrayOf(userId.toString()))
    }

    private fun getCartItemByIds(userId: Long, listingId: Long): CartItem? {
        val cursor = readableDatabase.query(
            T_CART, null,
            "$C_USER_ID=? AND $C_LISTING_ID=?",
            arrayOf(userId.toString(), listingId.toString()),
            null, null, null
        )
        return cursor.use {
            if (!it.moveToFirst()) return null
            CartItem(
                id        = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                userId    = it.getLong(it.getColumnIndexOrThrow(C_USER_ID)),
                listingId = it.getLong(it.getColumnIndexOrThrow(C_LISTING_ID)),
                quantity  = it.getInt(it.getColumnIndexOrThrow(C_QTY))
            )
        }
    }

    // ─── ORDERS ───────────────────────────────────────────────────────────────

    fun placeOrder(userId: Long, shippingInfo: String, paymentMethod: String): Long {
        val items = getCartItems(userId)
        if (items.isEmpty()) return -1L

        var total = 0.0
        for (ci in items) {
            val listing = getListingById(ci.listingId) ?: continue
            total += listing.price * ci.quantity
        }

        val cv = ContentValues().apply {
            put(O_USER_ID,  userId)
            put(O_TOTAL,    total)
            put(O_SHIPPING, shippingInfo)
            put(O_PAYMENT,  paymentMethod)
            put(O_CREATED,  System.currentTimeMillis())
        }
        val orderId = writableDatabase.insert(T_ORDERS, null, cv)

        if (orderId > 0) {
            for (ci in items) {
                val listing = getListingById(ci.listingId) ?: continue
                val itemCv = ContentValues().apply {
                    put(OI_ORDER_ID, orderId)
                    put(OI_TITLE,    listing.title)
                    put(OI_PRICE,    listing.price)
                    put(OI_QTY,      ci.quantity)
                }
                writableDatabase.insert(T_ORDER_ITEMS, null, itemCv)
            }
            clearCart(userId)
        }
        return orderId
    }

    fun getOrdersByUser(userId: Long): List<Map<String, Any>> {
        val cursor = readableDatabase.query(
            T_ORDERS, null,
            "$O_USER_ID=?", arrayOf(userId.toString()),
            null, null, "$O_CREATED DESC"
        )
        return cursor.use {
            val out = mutableListOf<Map<String, Any>>()
            while (it.moveToNext()) {
                out.add(mapOf(
                    "id"      to it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                    "total"   to it.getDouble(it.getColumnIndexOrThrow(O_TOTAL)),
                    "payment" to it.getString(it.getColumnIndexOrThrow(O_PAYMENT)),
                    "created" to it.getLong(it.getColumnIndexOrThrow(O_CREATED))
                ))
            }
            out
        }
    }

    fun getOrderItems(orderId: Long): List<Map<String, Any>> {
        val cursor = readableDatabase.query(
            T_ORDER_ITEMS, null,
            "$OI_ORDER_ID=?", arrayOf(orderId.toString()),
            null, null, null
        )
        return cursor.use {
            val out = mutableListOf<Map<String, Any>>()
            while (it.moveToNext()) {
                out.add(mapOf(
                    "title"    to it.getString(it.getColumnIndexOrThrow(OI_TITLE)),
                    "price"    to it.getDouble(it.getColumnIndexOrThrow(OI_PRICE)),
                    "quantity" to it.getInt(it.getColumnIndexOrThrow(OI_QTY))
                ))
            }
            out
        }
    }
}