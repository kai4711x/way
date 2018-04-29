package org.zellview.way

import org.acra.ACRA
import org.acra.ReportingInteractionMode
import org.acra.annotation.ReportsCrashes

import com.rareventure.gps2.R
import com.rareventure.gps2.reviewer.SettingsActivity

import android.app.Application
import android.content.Intent

import pl.tajchert.nammu.Nammu

/**
 * Does stuff that is common to all gps trailer reviewer type things
 */
@ReportsCrashes(formUri = "https://collector.tracepot.com/c38489a7", mode = ReportingInteractionMode.SILENT, resToastText = R.string.crash_toast_text, resDialogText = R.string.crash_dialog_text, resDialogIcon = android.R.drawable.ic_dialog_info, resDialogTitle = R.string.crash_dialog_title, resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, resDialogOkToast = R.string.crash_dialog_ok_toast)//		formKey="dFp0X2pTTTV1am5kNHczbk1lTE5rYVE6MQ",
//		formKey="",
//		formUri = "http://10.32.13.200:3127/reportCrash",
//        mode = ReportingInteractionMode.DIALOG,
//we need silent here because of the way we forward from one activity to another in case of an
//error. If mode is TOAST or DIALOG, for some reason it ends up in an infinite loop constantly
// repeating the exception (when the exception occurs without user input)
// optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
//optional. default is a warning sign
// optional. default is your application name
// optional. when defined, adds a user text field input with this text resource as a label
// optional. displays a Toast message when the user accepts to send a report.
class GpsTrailerReviewerApplication : Application() {
    override fun onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this)

        Nammu.init(this)

        super.onCreate()
    }
}
