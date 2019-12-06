package com.cs407.team15.redstone.ui.scavangerhunt


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.widget.TextView
import com.cs407.team15.redstone.model.Scavanger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.sql.Time
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.Period
import java.util.*
import kotlin.collections.ArrayList

public class HintList : Fragment(),onWinListener {
    lateinit var locationGPS:Location
    var clock= Calendar.getInstance().time
    lateinit var scavName:String
    lateinit var gameScavanger:Scavanger
    lateinit var clockWin:TextView
    lateinit var userName:String
    lateinit var winScav:Scavanger
    lateinit var idScav:String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.hint_list_fragment, container, false)

        clockWin = root.findViewById<TextView>(R.id.clock_text_view)
        //.getInstance().collection("scavager").document("rzt9RNeX45rA2SXCYl6m").set(Scavanger(null,null,null,null,null,null,null,null,null,null))
         scavName = arguments?.getString("name")!!
       userName = arguments?.getString("username")!!
        val type = arguments?.getString("type")!!
        val uid = arguments?.getString("uid")!!
        val hammer = arguments?.getBoolean("hammer")!!
        val locations = arguments?.getStringArrayList("location")!!
        val hints = arguments?.getStringArrayList("hints")!!
        val tagsOnTour = arguments?.getStringArrayList("tags")!!
        val initialVotes = arguments?.getInt("votes")!!
        val leadUser=arguments?.getStringArrayList("leaderboardUsername")
        val leadScore=arguments?.getIntegerArrayList("leaderboardTime")

         gameScavanger = Scavanger(scavName,uid,type,hammer,locations,hints,tagsOnTour,leadScore,leadUser,initialVotes)
        val hintListRecyclerView = root.findViewById<RecyclerView>(R.id.hint_list_recycle_view)
       // val listHintData = arguments?.getStringArrayList("locationsHintList")!!.toMutableList()

        hintListRecyclerView.layoutManager = LinearLayoutManager(activity)
        hintListRecyclerView.itemAnimator = DefaultItemAnimator()

        val hintListRecyclerViewAdapter = HintListAdapter( hints.toMutableList(),locations,this)
        hintListRecyclerView.adapter=hintListRecyclerViewAdapter

        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            var locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val hasNetwork=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if(hasGps){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,(0).toFloat(),object:LocationListener{
                    override fun onLocationChanged(location: Location?) {
                        locationGPS=location!!
                        hintListRecyclerViewAdapter.updateLocation(locationGPS)
                    }

                    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                        Log.v("error",p0) //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderEnabled(p0: String?) {
                        Log.v("error",p0)//To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProviderDisabled(p0: String?) {
                        Log.v("error",p0) //To change body of created functions use File | Settings | File Templates.
                    }

                })
            }
                //getSystemService(Context.LOCATION_SERVICE) as LocationManager
        } else {
//            Toast.makeText(context,"Location not showing",Toast.LENGTH_LONG).show()
            // Show rationale and request permission.
            requestPermissions( arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), targetRequestCode)

        }


        return root
    }

    override fun onWin() {
        var winTime=Calendar.getInstance().time
        winTime.compareTo(clock)
        Log.v("hello",winTime.toString()+" "+clock.toString())
        var diference = winTime.time-clock.time
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var mil=Duration.ofMillis(diference)

            Log.v("hella",(mil.toString()+" "+(mil.toMillis()/100)%60).toString()+" "+ (mil.toMinutes()%60).toString()+" "+mil.toHours().toString())
            clockWin.text=(  mil.toHours().toString()+":"+ (mil.toMinutes()%60).toString() + ":" +((mil.toMillis()/1000)%60).toString())

            FirebaseFirestore.getInstance().collection("scavanger").get().addOnSuccessListener {
                scavange->

                for(name in scavange.documents){
                        if(name.get("name")==scavName){
                            //FirebaseFirestore.getInstance().collection("scavager").
                            //String name,String uid,String type,Boolean hammer,List<String> locations,List<String> hints,List<String> tags,List<Long> leaderboardTime,List<String> leaderboardUsername,Integer votes){
                            var uid=FirebaseAuth.getInstance().uid.toString()
                            var username=""

                            var scoreLeader= (name.get("leaderboardTime") as ArrayList<Int>)
                            scoreLeader.add(diference.toInt())
                            var userLeader= (name.get("leaderboardUsername") as ArrayList<String>)
                            userLeader.add(userName)
                            var dif=name.id
                            var scav=Scavanger(name.get("name") as String,name.get("uid") as String,
                                name.get("type") as String,name.get("hammer") as Boolean,name.get("locations") as ArrayList<String>,
                                name.get("hints") as ArrayList<String>,name.get("tags") as ArrayList<String>,
                                scoreLeader,
                                userLeader,
                                (name.get("votes") as Long).toInt())
                            Log.v("hello",name.id)
                            winScav=scav
                            idScav=name.id
                            //Log.v("hello",name.id+" "+user.toString()+" "+scav.leaderboardTime.toString()+scav.leaderboardUsername.toString())


                            /*FirebaseFirestore.getInstance().collection("users").get().addOnSuccessListener {
                            users->
                                Log.v("this","go here")
                                for(user in users.documents){
                                    Log.v("this","go there")


                                    if(user.get("uid")==uid){
                                       var scoreLeader= (name.get("leaderboardTime") as ArrayList<Int>)
                                           scoreLeader.add(diference.toInt())
                                        var userLeader= (name.get("leaderboardUsername") as ArrayList<String>)
                                            userLeader.add(user.get("username") as String)
                                        var dif=name.id
                                        var scav=Scavanger(name.get("name") as String,name.get("uid") as String,
                                            name.get("type") as String,name.get("hammer") as Boolean,name.get("locations") as ArrayList<String>,
                                            name.get("hints") as ArrayList<String>,name.get("tags") as ArrayList<String>,
                                            scoreLeader,
                                            userLeader,
                                            (name.get("votes") as Long).toInt())
                                        Log.v("hello",name.id)
                                        //Log.v("hello",name.id+" "+user.toString()+" "+scav.leaderboardTime.toString()+scav.leaderboardUsername.toString())
                                        FirebaseFirestore.getInstance().collection("scavager").document("aaaa").set(scav).addOnSuccessListener{
                                            Log.v("hello","success")
                                        }.addOnFailureListener{
                                            Log.v("hello","failure")
                                        }

                                         }
                                    }
                                }*/
                            }




                           }
                    submitSchool()
                }
                //FirebaseFirestore.getInstance().collection("scavager").
            }
        }
    fun submitSchool(){

    FirebaseFirestore.getInstance().collection("scavager").document(idScav).set(winScav, SetOptions.merge())

    }
    }

interface onWinListener{
    fun onWin()
}