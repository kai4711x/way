package org.zellview.way.util

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

import org.zellview.way.activity.MapsActivity

private val EMAIL_LOG_REQUEST_CODE = 1

private val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
private var mapsActivity = MapsActivity.mapsActivity

private var line: String = ""


    private fun writeHeader(fos: FileOutputStream) {
        line =
                "time,"     +
                "provider," +
                "latitude," +
                "longitude,"+
                "altitude," +
                "accuracy," +
                "speed,"    +
                "bearing"   +
                "\n"

                fos.write(line.toByteArray())
    }

    private fun writeLogLine(fos: FileOutputStream, point: Location) {
        line =
            point.time.toString()       + "," +
            point.provider.toString()   + "," +
            point.latitude.toString()   + "," +
            point.longitude.toString()  + "," +
            point.altitude.toString()   + "," +
            point.accuracy.toString()   + "," +
            point.speed.toString()

            line += ","
            if (point.hasBearing())
                line += point.bearing.toString()
            else
                line += "-1"
            line += "\n"

        fos.write(line.toByteArray())
    }


    fun createLogfile(points: MutableList<Location>) {
        val filename = "csvRoute-" + format.format(Date()) + ".csv"
        val file = File(mapsActivity.getExternalFilesDir(null), filename)

        var fos = FileOutputStream(file.getAbsolutePath())

        writeHeader(fos)

        for (point in points) {
            writeLogLine(fos, point)
        }

        fos.flush()
        fos.close()

        val emailClient = Intent(Intent.ACTION_SENDTO)
        emailClient.data = Uri.parse("mailto:")
        //emailClient.data = Uri.parse("")
        emailClient.putExtra(Intent.EXTRA_EMAIL, arrayOf("kietzke.kai@gmail.com"))
        emailClient.putExtra(Intent.EXTRA_SUBJECT, "Routelogging")
        //emailClient.putExtra(Intent.EXTRA_TEXT, mapsActivity.getString(R.string.report_email_body, vehicle, km, source))
        emailClient.putExtra(Intent.EXTRA_TEXT, "Logmail")
        emailClient.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)) //attachment
        //emailClient .setType("text/html");
        if (emailClient.resolveActivity(mapsActivity.getPackageManager()) != null) {
            //mapsActivity.startActivity(Intent.createChooser(emailClient, "Share using"));
            mapsActivity.startActivityForResult(emailClient, EMAIL_LOG_REQUEST_CODE)

        }
    }





