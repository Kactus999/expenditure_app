package com.example.expenditureapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserDBHelper(context: Context) : SQLiteOpenHelper(context,"USERDB",null,1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE IF NOT EXISTS USERS(USERID INTEGER PRIMARY KEY AUTOINCREMENT, WALLET TEXT, BASE_AMOUNT DOUBLE)")
        db?.execSQL("CREATE TABLE IF NOT EXISTS TRANSACTIONS(_id INTEGER PRIMARY KEY AUTOINCREMENT, AMOUNT DOUBLE, TYPE TEXT, CATEGORY TEXT, TRANS_DATE TEXT , WALLET TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }


}