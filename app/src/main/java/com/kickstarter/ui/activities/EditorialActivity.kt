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
import com.kickstarter.R
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
    private var hasSeenRationale = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editorial)

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
        when (requestCode) {
            ACCESS_COARSE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.viewModel.inputs.locationDialogConfirmed()
                }
            }
        }
    }

    private fun checkLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) && !this.hasSeenRationale) {
                AlertDialog.Builder(this, R.style.AlertDialog)
                        .setCancelable(true)
                        .setMessage(R.string.Support_creative_spaces_and_businesses_affected_by)
                        .setNegativeButton(R.string.No_thanks) { d, _ -> d.dismiss() }
                        .setPositiveButton(R.string.general_alert_buttons_ok) { d, _ ->
                            run {
                                this.viewModel.inputs.locationDialogConfirmed()
                                d.dismiss()
                            }
                        }
                        .create()
                        .show()
                this.hasSeenRationale = true
            } else {
                ActivityCompat.requestPermissions(this,
                         arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        ACCESS_COARSE_LOCATION)
            }
        } else {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                        if (location != null) {
                            this.viewModel.inputs.location(Pair.create(location.latitude, location.longitude))
                        }
                    }
        }
    }

    private fun discoveryFragment(): DiscoveryFragment = supportFragmentManager.findFragmentById(R.id.fragment_discovery) as DiscoveryFragment

    //No-op because we have retry behavior
    override fun onNetworkConnectionChanged(isConnected: Boolean) {}
}
