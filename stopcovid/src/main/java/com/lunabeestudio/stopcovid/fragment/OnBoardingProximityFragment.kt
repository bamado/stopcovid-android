/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Authors
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Created by Lunabee Studio / Date - 2020/04/05 - for the STOP-COVID project
 */

package com.lunabeestudio.stopcovid.fragment

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lunabeestudio.stopcovid.R
import com.lunabeestudio.stopcovid.coreui.UiConstants
import com.lunabeestudio.stopcovid.coreui.extension.openAppSettings
import com.lunabeestudio.stopcovid.coreui.extension.showPermissionRationale
import com.lunabeestudio.stopcovid.coreui.fastitem.captionItem
import com.lunabeestudio.stopcovid.fastitem.logoItem
import com.lunabeestudio.stopcovid.coreui.fastitem.spaceItem
import com.lunabeestudio.stopcovid.coreui.fastitem.titleItem
import com.lunabeestudio.stopcovid.manager.ProximityManager
import com.mikepenz.fastadapter.GenericItem

class OnBoardingProximityFragment : OnBoardingFragment() {

    override fun getTitleKey(): String = "onboarding.proximityController.title"
    override fun getButtonTitle(): String? = strings["onboarding.proximityController.allowProximity"]
    override fun getOnButtonClickListener(): View.OnClickListener = View.OnClickListener {
        if (ContextCompat.checkSelfPermission(requireContext(), ProximityManager.getManifestLocationPermission())
            != PackageManager.PERMISSION_GRANTED) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(strings["common.permissionsNeeded"])
                .setMessage(strings["onboarding.proximityController.allowProximity.warning"])
                .setPositiveButton(strings["common.understand"]) { _, _ ->
                    requestPermissions(arrayOf(ProximityManager.getManifestLocationPermission()),
                        UiConstants.Permissions.LOCATION.ordinal)
                }
                .show()
        } else if (ProximityManager.hasFeatureBLE(requireContext()) && !ProximityManager.isBluetoothOn(requireContext())) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, UiConstants.Activity.BLUETOOTH.ordinal)
        } else {
            startNextController()
        }
    }

    override fun getItems(): List<GenericItem> {
        val items = arrayListOf<GenericItem>()

        items += logoItem {
            imageRes = R.drawable.proximity
            identifier = items.size.toLong()
        }
        items += spaceItem {
            spaceRes = R.dimen.spacing_xlarge
            identifier = items.size.toLong()
        }
        items += titleItem {
            text = strings["onboarding.proximityController.mainMessage.title"]
            gravity = Gravity.CENTER
            identifier = items.size.toLong()
        }
        items += captionItem {
            text = strings["onboarding.proximityController.mainMessage.subtitle"]
            gravity = Gravity.CENTER
            identifier = items.size.toLong()
        }

        return items
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == UiConstants.Permissions.LOCATION.ordinal) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ProximityManager.hasFeatureBLE(requireContext()) && !ProximityManager.isBluetoothOn(requireContext())) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, UiConstants.Activity.BLUETOOTH.ordinal)
                } else {
                    startNextController()
                }
            } else if (!shouldShowRequestPermissionRationale(ProximityManager.getManifestLocationPermission())) {
                context?.showPermissionRationale(strings, "common.needLocalisationAccessToScan", "common.settings") {
                    openAppSettings()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UiConstants.Activity.BLUETOOTH.ordinal) {
            if (resultCode == Activity.RESULT_OK) {
                startNextController()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun startNextController() {
        if (!ProximityManager.isBatteryOptimizationOn(requireContext())) {
            findNavController()
                .navigate(OnBoardingProximityFragmentDirections.actionOnBoardingProximityFragmentToOnBoardingBatteryFragment())
        } else {
            findNavController()
                .navigate(OnBoardingProximityFragmentDirections.actionOnBoardingProximityFragmentToOnBoardingNotificationFragment())
        }
    }
}