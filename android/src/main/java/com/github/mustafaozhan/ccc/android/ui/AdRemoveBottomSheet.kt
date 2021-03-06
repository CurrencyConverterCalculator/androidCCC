/*
 * Copyright (c) 2021 Mustafa Ozhan. All rights reserved.
 */

package com.github.mustafaozhan.ccc.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.github.mustafaozhan.basemob.adapter.BaseVBRecyclerViewAdapter
import com.github.mustafaozhan.basemob.bottomsheet.BaseVBBottomSheetDialogFragment
import com.github.mustafaozhan.ccc.android.util.showDialog
import com.github.mustafaozhan.ccc.android.util.showLoading
import com.github.mustafaozhan.ccc.android.util.showSnack
import com.github.mustafaozhan.ccc.client.model.PurchaseHistory
import com.github.mustafaozhan.ccc.client.model.RemoveAdData
import com.github.mustafaozhan.ccc.client.model.RemoveAdType
import com.github.mustafaozhan.ccc.client.viewmodel.adremove.AdRemoveEffect
import com.github.mustafaozhan.ccc.client.viewmodel.adremove.AdRemoveEvent
import com.github.mustafaozhan.ccc.client.viewmodel.adremove.AdRemoveViewModel
import com.github.mustafaozhan.logmob.kermit
import com.github.mustafaozhan.scopemob.mapTo
import com.github.mustafaozhan.scopemob.whether
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mustafaozhan.github.com.mycurrencies.R
import mustafaozhan.github.com.mycurrencies.databinding.BottomSheetAdRemoveBinding
import mustafaozhan.github.com.mycurrencies.databinding.ItemAdRemoveBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@Suppress("TooManyFunctions")
class AdRemoveBottomSheet : BaseVBBottomSheetDialogFragment<BottomSheetAdRemoveBinding>(),
    PurchaseHistoryResponseListener,
    PurchasesUpdatedListener,
    SkuDetailsResponseListener,
    BillingClientStateListener,
    AcknowledgePurchaseResponseListener {

    private lateinit var billingClient: BillingClient

    private val adRemoveViewModel: AdRemoveViewModel by viewModel()

    private lateinit var removeAdsAdapter: RemoveAdsAdapter

    private lateinit var skuDetails: List<SkuDetails>
    private var acknowledgePurchaseParams: AcknowledgePurchaseParams? = null

    override fun getViewBinding() = BottomSheetAdRemoveBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kermit.d { "AdRemoveBottomSheet onViewCreated" }
        initViews()
        observeStates()
        observeEffect()
        setupBillingClient()
    }

    override fun onDestroyView() {
        billingClient.endConnection()
        binding.recyclerViewBar.adapter = null
        super.onDestroyView()
    }

    private fun initViews() {
        removeAdsAdapter = RemoveAdsAdapter(adRemoveViewModel.event)
        binding.recyclerViewBar.adapter = removeAdsAdapter
    }

    private fun observeStates() = adRemoveViewModel.state
        .flowWithLifecycle(lifecycle)
        .onEach {
            with(it) {
                binding.loadingView.showLoading(loading)
                removeAdsAdapter.submitList(adRemoveTypes)
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

    private fun observeEffect() = adRemoveViewModel.effect
        .flowWithLifecycle(lifecycle)
        .onEach { viewEffect ->
            kermit.d { "AdRemoveBottomSheet observeEffect ${viewEffect::class.simpleName}" }
            when (viewEffect) {
                is AdRemoveEffect.LaunchRemoveAdFlow -> {
                    if (viewEffect.removeAdType == RemoveAdType.VIDEO) {
                        prepareRewardedAdFlow()
                    } else {
                        launchBillingFlow(viewEffect.removeAdType.data.skuId)
                    }
                }
                is AdRemoveEffect.AdsRemoved -> {
                    if (viewEffect.removeAdType == RemoveAdType.VIDEO) {
                        restartActivity()
                    } else {
                        acknowledgePurchaseParams?.let {
                            billingClient.acknowledgePurchase(it, this)
                        } ?: run {
                            restartActivity()
                        }
                    }
                }
                AdRemoveEffect.AlreadyAdFree -> showSnack(
                    requireView(),
                    R.string.txt_ads_already_disabled
                )
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

    private fun prepareRewardedAdFlow() {
        showDialog(
            requireActivity(),
            R.string.txt_remove_ads,
            R.string.txt_remove_ads_text,
            R.string.txt_watch
        ) {
            adRemoveViewModel.showLoadingView(true)
            prepareRewardedAd()
        }
    }

    private fun restartActivity() = activity?.run {
        finish()
        startActivity(intent)
    }

    private fun setupBillingClient() {
        adRemoveViewModel.showLoadingView(true)
        billingClient = BillingClient
            .newBuilder(requireContext().applicationContext)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        billingClient.startConnection(this)
    }

    private fun launchBillingFlow(skuId: String) = skuDetails
        .firstOrNull { it.sku == skuId }
        ?.let {
            val billingFlowParams = BillingFlowParams
                .newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
        }

    private fun prepareRewardedAd() = context?.applicationContext?.let { applicationContext ->
        RewardedAd.load(
            applicationContext,
            getString(R.string.android_rewarded_ad_unit_id),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    kermit.d { "AdRemoveBottomSheet onRewardedAdFailedToLoad" }
                    adRemoveViewModel.showLoadingView(false)
                    view?.let { showSnack(it, R.string.error_text_unknown) }
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    adRemoveViewModel.showLoadingView(false)
                    kermit.d { "AdRemoveBottomSheet onRewardedAdLoaded" }

                    activity?.let {
                        rewardedAd.show(it) {
                            kermit.d { "AdRemoveBottomSheet onUserEarnedReward" }
                            adRemoveViewModel.updateAddFreeDate(RemoveAdType.VIDEO)
                        }
                    }
                }
            }
        )
    }

    override fun onSkuDetailsResponse(
        billingResult: BillingResult,
        skuDetailsList: MutableList<SkuDetails>?
    ) {
        kermit.d { "AdRemoveBottomSheet onSkuDetailsResponse ${billingResult.responseCode}" }

        skuDetailsList?.whether {
            billingResult.responseCode == BillingClient.BillingResponseCode.OK
        }?.let { detailsList ->
            skuDetails = detailsList
            adRemoveViewModel.addInAppBillingMethods(detailsList.map {
                RemoveAdData(it.price, it.description, it.sku)
            })
        } ?: run {
            adRemoveViewModel.showLoadingView(false)
        }
    }

    override fun onPurchaseHistoryResponse(
        billingResult: BillingResult,
        purchaseHistoryList: MutableList<PurchaseHistoryRecord>?
    ) {
        kermit.d { "AdRemoveBottomSheet onPurchaseHistoryResponse ${billingResult.responseCode}" }

        purchaseHistoryList?.mapNotNull { historyRecord ->
            RemoveAdType.getBySku(historyRecord.skus.firstOrNull())?.let {
                PurchaseHistory(historyRecord.purchaseTime, it)
            }
        }?.let { adRemoveViewModel.restorePurchase(it) }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchaseList: MutableList<Purchase>?
    ) {
        kermit.d { "AdRemoveBottomSheet onPurchasesUpdated ${billingResult.responseCode}" }

        purchaseList?.firstOrNull()
            ?.also {
                acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()
            }?.mapTo { RemoveAdType.getBySku(skus.firstOrNull()) }
            ?.let {
                adRemoveViewModel.updateAddFreeDate(it)
            }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        kermit.d { "AdRemoveBottomSheet onBillingSetupFinished ${billingResult.responseCode}" }

        adRemoveViewModel.showLoadingView(false)
        billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, this)

        billingClient.whether(
            { isReady },
            { billingResult.responseCode == BillingClient.BillingResponseCode.OK }
        )?.apply {
            val skuDetailsParams = SkuDetailsParams.newBuilder()
                .setSkusList(RemoveAdType.getSkuList())
                .setType(BillingClient.SkuType.INAPP)
                .build()
            querySkuDetailsAsync(skuDetailsParams, this@AdRemoveBottomSheet)
        }
    }

    override fun onBillingServiceDisconnected() {
        kermit.d { "AdRemoveBottomSheet onBillingServiceDisconnected" }
        adRemoveViewModel.showLoadingView(false)
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        kermit.d { "AdRemoveBottomSheet onAcknowledgePurchaseResponse" }
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            restartActivity()
        }
    }
}

class RemoveAdsAdapter(
    private val removeAdsEvent: AdRemoveEvent
) : BaseVBRecyclerViewAdapter<RemoveAdType, ItemAdRemoveBinding>(RemoveAdDiffer()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = CalculatorVBViewHolder(
        ItemAdRemoveBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    inner class CalculatorVBViewHolder(itemBinding: ItemAdRemoveBinding) :
        BaseVBViewHolder<RemoveAdType, ItemAdRemoveBinding>(itemBinding) {

        override fun onItemBind(item: RemoveAdType) = with(itemBinding) {
            root.setOnClickListener { removeAdsEvent.onAdRemoveItemClick(item) }
            txtReward.text = item.data.reward
            txtCost.text = item.data.cost
        }
    }

    class RemoveAdDiffer : DiffUtil.ItemCallback<RemoveAdType>() {
        override fun areItemsTheSame(oldItem: RemoveAdType, newItem: RemoveAdType) = false

        override fun areContentsTheSame(oldItem: RemoveAdType, newItem: RemoveAdType) = false
    }
}
