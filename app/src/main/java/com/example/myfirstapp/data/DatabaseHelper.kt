package com.example.myfirstapp.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.myfirstapp.model.CartItem
import com.example.myfirstapp.model.Listing
import com.example.myfirstapp.model.User

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "MarketplaceDatabase.db"
        private const val DB_VERSION = 4

        private const val T_USERS = "users"
        private const val T_LISTINGS = "listings"
        private const val T_CART = "cart_items"
        private const val T_ORDERS = "orders"
        private const val T_ORDER_ITEMS = "order_items"

        private const val COL_ID = "id"

        // users
        private const val U_NAME = "name"
        private const val U_EMAIL = "email"
        private const val U_PASSWORD = "password"
        private const val U_ROLE = "role"
        private const val U_DISABLED = "disabled"

        // listings
        private const val L_SELLER_ID = "sellerId"
        private const val L_TITLE = "title"
        private const val L_DESC = "description"
        private const val L_PRICE = "price"
        private const val L_CATEGORY = "category"
        private const val L_EXPIRATION = "expirationDate"
        private const val L_PHOTO_URI = "photoUri"
        private const val L_STATUS = "status"
        private const val L_CREATED_AT = "createdAt"

        // cart
        private const val C_USER_ID = "userId"
        private const val C_LISTING_ID = "listingId"
        private const val C_QTY = "quantity"

        // orders
        private const val O_BUYER_ID = "buyerId"
        private const val O_TOTAL = "total"
        private const val O_PAYMENT = "paymentMethod"
        private const val O_SHIPPING = "shippingInfo"
        private const val O_STATUS = "status"
        private const val O_CREATED_AT = "createdAt"

        // order items
        private const val OI_ORDER_ID = "orderId"
        private const val OI_LISTING_ID = "listingId"
        private const val OI_TITLE = "title"
        private const val OI_PRICE = "price"
        private const val OI_QTY = "quantity"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $T_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $U_NAME TEXT NOT NULL,
                $U_EMAIL TEXT NOT NULL UNIQUE,
                $U_PASSWORD TEXT NOT NULL,
                $U_ROLE TEXT NOT NULL,
                $U_DISABLED INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $T_LISTINGS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $L_SELLER_ID INTEGER NOT NULL,
                $L_TITLE TEXT NOT NULL,
                $L_DESC TEXT NOT NULL,
                $L_PRICE REAL NOT NULL,
                $L_CATEGORY TEXT NOT NULL,
                $L_EXPIRATION TEXT NOT NULL,
                $L_PHOTO_URI TEXT,
                $L_STATUS TEXT NOT NULL,
                $L_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY($L_SELLER_ID) REFERENCES $T_USERS($COL_ID)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $T_CART (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $C_USER_ID INTEGER NOT NULL,
                $C_LISTING_ID INTEGER NOT NULL,
                $C_QTY INTEGER NOT NULL,
                FOREIGN KEY($C_USER_ID) REFERENCES $T_USERS($COL_ID),
                FOREIGN KEY($C_LISTING_ID) REFERENCES $T_LISTINGS($COL_ID)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $T_ORDERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $O_BUYER_ID INTEGER NOT NULL,
                $O_TOTAL REAL NOT NULL,
                $O_PAYMENT TEXT NOT NULL,
                $O_SHIPPING TEXT NOT NULL,
                $O_STATUS TEXT NOT NULL,
                $O_CREATED_AT INTEGER NOT NULL,
                FOREIGN KEY($O_BUYER_ID) REFERENCES $T_USERS($COL_ID)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $T_ORDER_ITEMS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $OI_ORDER_ID INTEGER NOT NULL,
                $OI_LISTING_ID INTEGER NOT NULL,
                $OI_TITLE TEXT NOT NULL,
                $OI_PRICE REAL NOT NULL,
                $OI_QTY INTEGER NOT NULL,
                FOREIGN KEY($OI_ORDER_ID) REFERENCES $T_ORDERS($COL_ID)
            )
            """.trimIndent()
        )

        val adminCv = ContentValues().apply {
            put(U_NAME, "Admin")
            put(U_EMAIL, "admin@admin.com")
            put(U_PASSWORD, "admin")
            put(U_ROLE, "ADMIN")
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

    // ---------- USERS ----------
    fun addUser(user: User): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(U_NAME, user.name)
            put(U_EMAIL, user.email.lowercase())
            put(U_PASSWORD, user.password)
            put(U_ROLE, user.role)
            put(U_DISABLED, if (user.isDisabled) 1 else 0)
        }
        return db.insert(T_USERS, null, cv)
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            T_USERS,
            null,
            "$U_EMAIL=?",
            arrayOf(email.lowercase()),
            null,
            null,
            null
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return cursorToUser(it)
        }
    }

    fun login(email: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            T_USERS,
            null,
            "$U_EMAIL=? AND $U_PASSWORD=?",
            arrayOf(email.lowercase(), password),
            null,
            null,
            null
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return cursorToUser(it)
        }
    }

    fun getAllUsers(): List<User> {
        val db = readableDatabase
        val cursor = db.query(T_USERS, null, null, null, null, null, "$COL_ID DESC")
        return cursor.use {
            val out = mutableListOf<User>()
            while (it.moveToNext()) out.add(cursorToUser(it))
            out
        }
    }

    fun setUserDisabled(userId: Long, disabled: Boolean): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(U_DISABLED, if (disabled) 1 else 0)
        }
        return db.update(T_USERS, cv, "$COL_ID=?", arrayOf(userId.toString()))
    }

    private fun cursorToUser(c: Cursor): User {
        return User(
            id = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
            name = c.getString(c.getColumnIndexOrThrow(U_NAME)),
            email = c.getString(c.getColumnIndexOrThrow(U_EMAIL)),
            password = c.getString(c.getColumnIndexOrThrow(U_PASSWORD)),
            role = c.getString(c.getColumnIndexOrThrow(U_ROLE)),
            isDisabled = c.getInt(c.getColumnIndexOrThrow(U_DISABLED)) == 1
        )
    }

    // ---------- LISTINGS ----------
    fun addListing(listing: Listing): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(L_SELLER_ID, listing.sellerId)
            put(L_TITLE, listing.title)
            put(L_DESC, listing.description)
            put(L_PRICE, listing.price)
            put(L_CATEGORY, listing.category)
            put(L_EXPIRATION, listing.expirationDate)
            put(L_PHOTO_URI, listing.photoUri)
            put(L_STATUS, listing.status)
            put(L_CREATED_AT, listing.createdAt)
        }
        return db.insert(T_LISTINGS, null, cv)
    }

    fun getAllListingsGlobal(): List<Listing> {
        val db = readableDatabase
        val cursor = db.query(
            T_LISTINGS,
            null,
            "$L_STATUS=?",
            arrayOf("ACTIVE"),
            null,
            null,
            "$L_CREATED_AT DESC"
        )
        return cursor.use { toListings(it) }
    }

    fun searchListings(
        keyword: String?,
        category: String?,
        minPrice: Double?,
        maxPrice: Double?
    ): List<Listing> {
        val db = readableDatabase
        val where = mutableListOf<String>()
        val args = mutableListOf<String>()

        where.add("$L_STATUS=?")
        args.add("ACTIVE")

        if (!keyword.isNullOrBlank()) {
            where.add("($L_TITLE LIKE ? OR $L_DESC LIKE ?)")
            args.add("%$keyword%")
            args.add("%$keyword%")
        }

        if (!category.isNullOrBlank() && category != "All") {
            where.add("$L_CATEGORY=?")
            args.add(category)
        }

        if (minPrice != null) {
            where.add("$L_PRICE>=?")
            args.add(minPrice.toString())
        }

        if (maxPrice != null) {
            where.add("$L_PRICE<=?")
            args.add(maxPrice.toString())
        }

        val cursor = db.query(
            T_LISTINGS,
            null,
            where.joinToString(" AND "),
            args.toTypedArray(),
            null,
            null,
            "$L_CREATED_AT DESC"
        )
        return cursor.use { toListings(it) }
    }

    fun getListingsBySeller(sellerId: Long): List<Listing> {
        val db = readableDatabase
        val cursor = db.query(
            T_LISTINGS,
            null,
            "$L_SELLER_ID=?",
            arrayOf(sellerId.toString()),
            null,
            null,
            "$L_CREATED_AT DESC"
        )
        return cursor.use { toListings(it) }
    }

    fun getListingById(listingId: Long): Listing? {
        val db = readableDatabase
        val cursor = db.query(
            T_LISTINGS,
            null,
            "$COL_ID=?",
            arrayOf(listingId.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return cursorToListing(it)
        }
    }

    fun updateListing(listing: Listing): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(L_TITLE, listing.title)
            put(L_DESC, listing.description)
            put(L_PRICE, listing.price)
            put(L_CATEGORY, listing.category)
            put(L_EXPIRATION, listing.expirationDate)
            put(L_PHOTO_URI, listing.photoUri)
            put(L_STATUS, listing.status)
        }
        return db.update(T_LISTINGS, cv, "$COL_ID=?", arrayOf(listing.id.toString()))
    }

    fun setListingStatus(listingId: Long, status: String): Int {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(L_STATUS, status)
        }
        return db.update(T_LISTINGS, cv, "$COL_ID=?", arrayOf(listingId.toString()))
    }

    fun deleteListing(listingId: Long): Int {
        val db = writableDatabase
        return db.delete(T_LISTINGS, "$COL_ID=?", arrayOf(listingId.toString()))
    }

    private fun toListings(cursor: Cursor): List<Listing> {
        val out = mutableListOf<Listing>()
        while (cursor.moveToNext()) {
            out.add(cursorToListing(cursor))
        }
        return out
    }

    private fun cursorToListing(c: Cursor): Listing {
        return Listing(
            id = c.getLong(c.getColumnIndexOrThrow(COL_ID)),
            sellerId = c.getLong(c.getColumnIndexOrThrow(L_SELLER_ID)),
            title = c.getString(c.getColumnIndexOrThrow(L_TITLE)),
            description = c.getString(c.getColumnIndexOrThrow(L_DESC)),
            price = c.getDouble(c.getColumnIndexOrThrow(L_PRICE)),
            category = c.getString(c.getColumnIndexOrThrow(L_CATEGORY)),
            expirationDate = c.getString(c.getColumnIndexOrThrow(L_EXPIRATION)),
            photoUri = c.getString(c.getColumnIndexOrThrow(L_PHOTO_URI)),
            status = c.getString(c.getColumnIndexOrThrow(L_STATUS)),
            createdAt = c.getLong(c.getColumnIndexOrThrow(L_CREATED_AT))
        )
    }

    // ---------- CART ----------
    fun addToCart(userId: Long, listingId: Long, qty: Int = 1): Long {
        val existing = getCartItem(userId, listingId)
        return if (existing != null) {
            updateCartItemQuantity(existing.id, existing.quantity + qty)
            existing.id
        } else {
            val db = writableDatabase
            val cv = ContentValues().apply {
                put(C_USER_ID, userId)
                put(C_LISTING_ID, listingId)
                put(C_QTY, qty)
            }
            db.insert(T_CART, null, cv)
        }
    }

    fun getCartItems(userId: Long): List<CartItem> {
        val db = readableDatabase
        val cursor = db.query(
            T_CART,
            null,
            "$C_USER_ID=?",
            arrayOf(userId.toString()),
            null,
            null,
            null
        )
        return cursor.use {
            val out = mutableListOf<CartItem>()
            while (it.moveToNext()) {
                out.add(
                    CartItem(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                        userId = it.getLong(it.getColumnIndexOrThrow(C_USER_ID)),
                        listingId = it.getLong(it.getColumnIndexOrThrow(C_LISTING_ID)),
                        quantity = it.getInt(it.getColumnIndexOrThrow(C_QTY))
                    )
                )
            }
            out
        }
    }

    fun updateCartItemQuantity(cartItemId: Long, newQty: Int): Int {
        if (newQty <= 0) return removeCartItem(cartItemId)
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(C_QTY, newQty)
        }
        return db.update(T_CART, cv, "$COL_ID=?", arrayOf(cartItemId.toString()))
    }

    fun removeCartItem(cartItemId: Long): Int {
        val db = writableDatabase
        return db.delete(T_CART, "$COL_ID=?", arrayOf(cartItemId.toString()))
    }

    fun clearCart(userId: Long): Int {
        val db = writableDatabase
        return db.delete(T_CART, "$C_USER_ID=?", arrayOf(userId.toString()))
    }

    private fun getCartItem(userId: Long, listingId: Long): CartItem? {
        val db = readableDatabase
        val cursor = db.query(
            T_CART,
            null,
            "$C_USER_ID=? AND $C_LISTING_ID=?",
            arrayOf(userId.toString(), listingId.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return CartItem(
                id = it.getLong(it.getColumnIndexOrThrow(COL_ID)),
                userId = it.getLong(it.getColumnIndexOrThrow(C_USER_ID)),
                listingId = it.getLong(it.getColumnIndexOrThrow(C_LISTING_ID)),
                quantity = it.getInt(it.getColumnIndexOrThrow(C_QTY))
            )
        }
    }

    // ---------- ORDERS ----------
    fun placeOrder(
        buyerId: Long,
        shippingInfo: String,
        paymentMethod: String
    ): Long {
        val db = writableDatabase
        val cart = getCartItems(buyerId)
        if (cart.isEmpty()) return -1L

        var total = 0.0
        val listings = mutableListOf<Pair<CartItem, Listing>>()

        for (ci in cart) {
            val listing = getListingById(ci.listingId) ?: continue
            if (listing.status != "ACTIVE") continue
            total += listing.price * ci.quantity
            listings.add(ci to listing)
        }

        if (listings.isEmpty()) return -1L

        var orderId = -1L
        db.beginTransaction()
        try {
            val orderCv = ContentValues().apply {
                put(O_BUYER_ID, buyerId)
                put(O_TOTAL, total)
                put(O_PAYMENT, paymentMethod)
                put(O_SHIPPING, shippingInfo)
                put(O_STATUS, "PLACED")
                put(O_CREATED_AT, System.currentTimeMillis())
            }

            orderId = db.insert(T_ORDERS, null, orderCv)
            if (orderId <= 0) throw IllegalStateException("Order insert failed")

            for ((ci, listing) in listings) {
                val itemCv = ContentValues().apply {
                    put(OI_ORDER_ID, orderId)
                    put(OI_LISTING_ID, listing.id)
                    put(OI_TITLE, listing.title)
                    put(OI_PRICE, listing.price)
                    put(OI_QTY, ci.quantity)
                }
                db.insert(T_ORDER_ITEMS, null, itemCv)

                val soldCv = ContentValues().apply {
                    put(L_STATUS, "SOLD")
                }
                db.update(T_LISTINGS, soldCv, "$COL_ID=?", arrayOf(listing.id.toString()))
            }

            db.delete(T_CART, "$C_USER_ID=?", arrayOf(buyerId.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return orderId
    }
}