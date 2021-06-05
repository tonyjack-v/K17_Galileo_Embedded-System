package com.example.testfirebase

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    @Test
    fun testmap(){
        var map = mutableMapOf<String,String>()
        map.put("1","Minh")
        map.put("2","Dep")
        map.put("3","Trai")
        print("1: "+map["1"])
    }
}