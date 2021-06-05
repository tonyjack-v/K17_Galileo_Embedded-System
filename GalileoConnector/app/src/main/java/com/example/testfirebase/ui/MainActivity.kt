package com.example.testfirebase.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.testfirebase.R
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(){
    lateinit var edt_Email: EditText
    lateinit var edt_Pass: EditText
    lateinit var btn_Signin: Button
    lateinit var tv_email: TextView
    lateinit var tv_pass: TextView
    lateinit var fetch: Button
    lateinit var clear: Button
    lateinit var ref: DatabaseReference
    lateinit var ref1: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        ref = FirebaseDatabase.getInstance().getReference("CESLAB")
        ref1 = FirebaseDatabase.getInstance().reference
        edt_Email = findViewById(R.id.username)
        edt_Pass = findViewById(R.id.password)
        btn_Signin = findViewById(R.id.login)
        fetch = findViewById(R.id.fetch)
        clear = findViewById(R.id.clear)

        clear.setOnClickListener{
            tv_email.setText("")
            tv_pass.setText("")
        }
        //fetch data
        fetch.setOnClickListener{
            Log.d("TAG","go to onclick")
            ref.addValueEventListener(object : ValueEventListener{

                override fun onCancelled(error: DatabaseError) {
                    Log.d("TAG","go to onCancled: $error")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("TAG","go to changedata $snapshot")
                    Log.d("TAG","go to changedata ${snapshot.value}")
                    snapshot.let{
                        for (h in snapshot.children){
                            Log.d("TAG","Snapshot.children: ${h}")
                            val a = h.getValue(Device::class.java)
                            if (a != null) {
                                Log.d("TAG","LED: ${a.LED}, humid: ${a.Humidity}, uuid: ${a.MAC_Address}")
                            }
                        }
                    }
                }
            })
        }
        //push data
//        btn_Signin.setOnClickListener{
//            var email = edt_Email.text.toString().trim()
//            var pass = edt_Pass.text.toString().trim()
//            if(email.isEmpty()){
//                edt_Email.error = "Please enter a name"
//            }
//            else if(pass.isEmpty()){
//                edt_Pass.error = "Please enter password"
//            }else{
//                var account = Account(email,pass)
//                var reference = FirebaseDatabase.getInstance().getReference("accounts")
//                val accountID = ref.push().key
//                accountID?.let {
//                    reference.child(accountID).setValue(account).addOnCompleteListener{
//                        Toast.makeText(this,"push complete!",Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//
//        }
    }
}