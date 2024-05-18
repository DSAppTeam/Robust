package com.meituan.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.meituan.robust.patch.RobustModify
import com.meituan.robust.patch.annotaion.Add
import com.meituan.robust.patch.annotaion.Modify
import java.lang.reflect.Field
import kotlin.jvm.Throws

class KotlinActivity : AppCompatActivity(), View.OnClickListener {
    private var listView: ListView? = null
    private val multiArr = arrayOf("列表1", "列表2", "列表3", "列表4")
    @Modify
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        listView = findViewById<View>(R.id.listview) as ListView
        val textView = findViewById<View>(R.id.secondtext) as TextView
//        textView.setOnClickListener { v: View? ->
//                    RobustModify.modify();
//            Toast.makeText(this@KotlinActivity, "fix", Toast.LENGTH_LONG).show()
//        }
        textView.setOnClickListener { v: View? ->
            Toast.makeText(this@KotlinActivity, "occur", Toast.LENGTH_LONG).show()
        }
        //change text on the  SecondActivity
        textView.text = getTextInfo()

        //test array
        val adapter: BaseAdapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, multiArr)
        listView!!.adapter = adapter
        printLog("robust", arrayOf(arrayOf("1", "2", "3"), arrayOf("4", "5", "6")))
    }//

//    @Modify
//    private fun getTextInfo(): String {
//        array
//        return "error fixed";
//    }
//
//    @Add
//    class Test() {
//        fun onClick(v: View) {
//
//        }
//    }
    private fun getTextInfo(): String {
        array
        return "error occur";
    }

    val array: Array<String>
        get() = arrayOf("hello", "world")

    override fun onClick(v: View) {
        Toast.makeText(this@KotlinActivity, "from implements onclick ", Toast.LENGTH_SHORT).show()
    }

    private fun printLog(tag: String, args: Array<Array<String>>) {
        var i = 0
        var j = 0
        for (array in args) {
            for (arg in array) {
                Log.d(tag, "args[$i][$j] is: $arg")
                j++
            }
            i++
        }
    }

    companion object {
        protected var name = "KotlinActivity"

        @Throws(NoSuchFieldException::class)
        fun getReflectField(name: String, instance: Any): Field {
            var clazz: Class<*>? = instance.javaClass
            while (clazz != null) {
                try {
                    val field = clazz.getDeclaredField(name)
                    if (!field.isAccessible) {
                        field.isAccessible = true
                    }
                    return field
                } catch (e: NoSuchFieldException) {
                    // ignore and search next
                }
                clazz = clazz.superclass
            }
            throw NoSuchFieldException("Field " + name + " not found in " + instance.javaClass)
        }

        fun getFieldValue(name: String, instance: Any): Any? {
            try {
                return getReflectField(name, instance)[instance]
            } catch (e: Exception) {
                Log.d("robust", "getField error $name   target   $instance")
                e.printStackTrace()
            }
            return null
        }
    }
}