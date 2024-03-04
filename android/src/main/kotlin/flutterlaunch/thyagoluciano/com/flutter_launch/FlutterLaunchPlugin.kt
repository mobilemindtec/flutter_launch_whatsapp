package flutterlaunch.thyagoluciano.com.flutter_launch

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.net.URLEncoder

class FlutterLaunchPlugin: MethodCallHandler, FlutterPlugin, ActivityAware {

  companion object {

    val CHANNEL_NAME = "flutter_launch";

    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), CHANNEL_NAME)
      channel.setMethodCallHandler(FlutterLaunchPlugin(registrar, channel))
    }
  }

  private var channel: MethodChannel? = null
  private var application: Application? = null
  private var activity: Activity? = null

  private constructor(registrar: PluginRegistry.Registrar, channel: MethodChannel) {
    this.channel = channel
    this.application = registrar.context() as Application
    this.activity = registrar.activity()
  }

  constructor() {
  }

  override fun onMethodCall(call: MethodCall, result: Result): Unit {
    try {

      val whatsappPackages = listOf(
        "com.whatsapp.w4b",
        "com.whatsapp.wb4",
        "com.whatsapp"
      )

      val pm: PackageManager = this.activity!!.packageManager
      if (call.method.equals("launchWathsApp")) {

        val phone: String? = call.argument("phone")
        val message: String? = call.argument("message")

        val url = "https://api.whatsapp.com/send?phone=$phone&text=${URLEncoder.encode(message, "UTF-8")}"

        var wappPack = whatsappPackages.firstOrNull({ pack: String -> appInstalledOrNot(pack) })

        if (wappPack != null) {
          val intent = Intent(Intent.ACTION_VIEW)
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
          intent.setPackage(wappPack)
          intent.data = Uri.parse(url)

          if (intent.resolveActivity(pm) != null) {
            this.activity!!.startActivity(intent)
          }
        }
      }

      if (call.method.equals("hasApp")) {
        val app: String? = call.argument("name");

        when(app) {
          "facebook" -> result.success(appInstalledOrNot("com.facebook.katana"))
          "whatsapp" -> result.success(whatsappPackages.any({ pack: String -> appInstalledOrNot(pack) }))
          else -> {
            result.error("App not found", "", null)
          }
        }
      }
    } catch (e: PackageManager.NameNotFoundException) {
      result.error("Name not found", e.message, null)
    }
  }

  private fun appInstalledOrNot(uri: String) : Boolean {
    val pm: PackageManager = this.activity!!.packageManager
    var appInstalled: Boolean

    try {
      pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
      appInstalled = true
    } catch (e: PackageManager.NameNotFoundException) {
      appInstalled = false
    }

    return appInstalled
  }

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, CHANNEL_NAME)
    channel!!.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(p0: FlutterPlugin.FlutterPluginBinding) {
    if(channel != null)
      channel!!.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(activityPluginBinding: ActivityPluginBinding) {
    this.activity = activityPluginBinding.activity;
    this.application = activityPluginBinding.activity.application;
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(p0: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onDetachedFromActivity() {
    this.activity = null;
    this.application = null;
  }

}
