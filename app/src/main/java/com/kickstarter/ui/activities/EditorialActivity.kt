package com.kickstarter.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Pair
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.jakewharton.rxbinding.view.RxView
import com.kickstarter.libs.ACCESS_COARSE_LOCATION
import com.kickstarter.libs.BaseActivity
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel
import com.kickstarter.libs.rx.transformers.Transformers.observeForUI
import com.kickstarter.libs.utils.ViewUtils
import com.kickstarter.ui.fragments.DiscoveryFragment
import com.kickstarter.viewmodels.EditorialViewModel
import kotlinx.android.synthetic.main.activity_editorial.*

@RequiresActivityViewModel(EditorialViewModel.ViewModel::class)
class EditorialActivity : BaseActivity<EditorialViewModel.ViewModel>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.kickstarter.R.layout.activity_editorial)

        this.viewModel.outputs.description()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { editorial_description.setText(it) }

        this.viewModel.outputs.graphic()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { editorial_graphic.setImageResource(it) }

        this.viewModel.outputs.title()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { editorial_title.setText(it) }

        this.viewModel.outputs.discoveryParams()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { discoveryFragment().updateParams(it) }

        this.viewModel.outputs.rootCategories()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { discoveryFragment().takeCategories(it) }

        this.viewModel.outputs.refreshDiscoveryFragment()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { discoveryFragment().refresh() }

        this.viewModel.outputs.requestLocation()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { checkLocation() }

        this.viewModel.outputs.retryContainerIsGone()
                .compose(observeForUI())
                .compose(bindToLifecycle())
                .subscribe { ViewUtils.setGone(editorial_retry_container, it) }

        RxView.clicks(editorial_retry_container)
                .compose(bindToLifecycle())
                .subscribe { this.viewModel.inputs.retryContainerClicked() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun checkLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this, com.kickstarter.R.style.AlertDialog)
                        .setCancelable(false)
                        .setTitle(com.kickstarter.R.string.Remove_this_card)
                        .setMessage(com.kickstarter.R.string.Are_you_sure_you_wish_to_remove_this_card)
                        .setNegativeButton(com.kickstarter.R.string.No_thanks) { d, _ -> d.dismiss() }
                        .setPositiveButton(com.kickstarter.R.string.general_alert_buttons_ok) { d, _ ->
                            run {
                                this.viewModel.inputs.locationDialogConfirmed()
                                d.dismiss()
                            }
                        }
                        .create()
                        .show()
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                         arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        ACCESS_COARSE_LOCATION)
            }
        } else {
            // Permission has already been granted
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                        if (location != null) {
                            // Logic to handle location object
                            this.viewModel.inputs.location(Pair.create(location.latitude, location.longitude))
                        }
                    }
        }
    }

    private fun discoveryFragment(): DiscoveryFragment = supportFragmentManager.findFragmentById(com.kickstarter.R.id.fragment_discovery) as DiscoveryFragment

    //No-op because we have retry behavior
    override fun onNetworkConnectionChanged(isConnected: Boolean) {}
}
