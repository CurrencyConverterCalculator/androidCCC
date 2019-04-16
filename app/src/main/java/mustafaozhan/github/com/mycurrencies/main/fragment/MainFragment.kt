package mustafaozhan.github.com.mycurrencies.main.fragment

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.crashlytics.android.Crashlytics
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_main.adView
import kotlinx.android.synthetic.main.fragment_main.layoutBar
import kotlinx.android.synthetic.main.fragment_main.mRecViewCurrency
import kotlinx.android.synthetic.main.item_currency.view.txtAmount
import kotlinx.android.synthetic.main.layout_bar.imgBase
import kotlinx.android.synthetic.main.layout_bar.mSpinner
import kotlinx.android.synthetic.main.layout_bar.txtResult
import kotlinx.android.synthetic.main.layout_keyboard_content.btnAc
import kotlinx.android.synthetic.main.layout_keyboard_content.btnDelete
import kotlinx.android.synthetic.main.layout_keyboard_content.btnDivide
import kotlinx.android.synthetic.main.layout_keyboard_content.btnDot
import kotlinx.android.synthetic.main.layout_keyboard_content.btnDoubleZero
import kotlinx.android.synthetic.main.layout_keyboard_content.btnEight
import kotlinx.android.synthetic.main.layout_keyboard_content.btnFive
import kotlinx.android.synthetic.main.layout_keyboard_content.btnFour
import kotlinx.android.synthetic.main.layout_keyboard_content.btnMinus
import kotlinx.android.synthetic.main.layout_keyboard_content.btnMultiply
import kotlinx.android.synthetic.main.layout_keyboard_content.btnNine
import kotlinx.android.synthetic.main.layout_keyboard_content.btnOne
import kotlinx.android.synthetic.main.layout_keyboard_content.btnPercent
import kotlinx.android.synthetic.main.layout_keyboard_content.btnPlus
import kotlinx.android.synthetic.main.layout_keyboard_content.btnSeven
import kotlinx.android.synthetic.main.layout_keyboard_content.btnSix
import kotlinx.android.synthetic.main.layout_keyboard_content.btnThree
import kotlinx.android.synthetic.main.layout_keyboard_content.btnTwo
import kotlinx.android.synthetic.main.layout_keyboard_content.btnZero
import kotlinx.android.synthetic.main.layout_main_toolbar.txtMainToolbar
import mustafaozhan.github.com.mycurrencies.R
import mustafaozhan.github.com.mycurrencies.base.BaseMvvmFragment
import mustafaozhan.github.com.mycurrencies.extensions.addText
import mustafaozhan.github.com.mycurrencies.extensions.calculateResultByCurrency
import mustafaozhan.github.com.mycurrencies.extensions.loadAd
import mustafaozhan.github.com.mycurrencies.extensions.reObserve
import mustafaozhan.github.com.mycurrencies.extensions.setBackgroundByName
import mustafaozhan.github.com.mycurrencies.main.fragment.adapter.CurrencyAdapter
import mustafaozhan.github.com.mycurrencies.settings.SettingsFragment
import mustafaozhan.github.com.mycurrencies.tools.Currencies
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by Mustafa Ozhan on 2018-07-12.
 */
@Suppress("TooManyFunctions", "SetTextI18n", "LargeClass")
class MainFragment : BaseMvvmFragment<MainFragmentViewModel>() {

    companion object {
        fun newInstance(): MainFragment = MainFragment()
    }

    override fun getViewModelClass(): Class<MainFragmentViewModel> =
        MainFragmentViewModel::class.java

    override fun getLayoutResId(): Int = R.layout.fragment_main

    private val currencyAdapter: CurrencyAdapter by lazy { CurrencyAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initViews()
        setListeners()
        setKeyboard()
        setRx()
        initLiveData()
        checkAppData()
    }

    private fun checkAppData() {
        compositeDisposable.add(
            Flowable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .onBackpressureLatest()
                .doOnNext {
                    if (viewModel.loadResetData()) {
                        clearAppData()
                    }
                }
                .subscribe())
    }

    private fun setRx() {
        txtMainToolbar.textChanges()
            .subscribe { txt ->
                viewModel.currencyListLiveData.value?.let { currencyList ->
                    if (currencyList.size > 1) {
                        viewModel.calculateOutput(txt.toString())
                        viewModel.getCurrencies()

                        txtResult.text = viewModel.getOutputText()
                    } else {
                        snacky(getString(R.string.choose_at_least_two_currency), getString(R.string.select)) {
                            getBaseActivity()?.replaceFragment(
                                SettingsFragment.newInstance(),
                                true)
                        }
                    }
                }
            }.addTo(compositeDisposable)
    }

    private fun initLiveData() {
        viewModel.ratesLiveData.reObserve(this, Observer { rates ->
            viewModel.currencyListLiveData.value?.let { currencyList ->
                currencyList.forEach { it.rate = calculateResultByCurrency(it.name, viewModel.output, rates) }
                if (rates == null) {
                    if (currencyList.size > 1) {
                        snacky(getString(R.string.rate_not_available_offline), getString(R.string.change)) {
                            mSpinner.expand()
                        }
                    }
                    currencyAdapter.refreshList(mutableListOf(), viewModel.mainData.currentBase)
                } else {
                    currencyAdapter.refreshList(currencyList, viewModel.mainData.currentBase)
                }
            }
        })
    }

    private fun initViews() {
        context?.let { ctx ->
            mRecViewCurrency.layoutManager = LinearLayoutManager(ctx)
            mRecViewCurrency.adapter = currencyAdapter
        }
        currencyAdapter.onItemClickListener = { currency, itemView: View, _: Int ->
            txtMainToolbar.text = itemView.txtAmount.text.toString().replace(" ", "")
            viewModel.updateCurrentBase(currency.name)
            viewModel.getCurrencies()
            viewModel.calculateOutput(itemView.txtAmount.text.toString().replace(" ", ""))
            txtResult.text = viewModel.getOutputText()
            viewModel.currencyListLiveData.value?.let { mSpinner.selectedIndex = it.indexOf(currency) }
            imgBase.setBackgroundByName(currency.name)
        }
        currencyAdapter.onItemLongClickListener = { currency, _ ->
            snacky(
                "${viewModel.getClickedItemRate(currency.name)} ${currency.getVariablesOneLine()}",
                setIcon = currency.name,
                isLong = false)

            true
        }
    }

    private fun updateUi() {
        doAsync {
            viewModel.refreshData()
            viewModel.getCurrencies()
            uiThread {
                try {
                    updateBar()
                } catch (e: Exception) {
                    compositeDisposable.add(
                        Flowable.timer(1, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.computation())
                            .onBackpressureLatest()
                            .doOnComplete {
                                clearAppData()
                                Crashlytics.logException(e)
                                Crashlytics.log(
                                    Log.ERROR,
                                    getString(R.string.error_tag_updating_ui),
                                    getString(R.string.error_message_updating_ui)
                                )
                                updateBar()
                            }.subscribe())
                }
            }
        }
    }

    @Suppress("NestedBlockDepth")
    private fun updateBar() {
        viewModel.currencyListLiveData.value?.let { currencyList ->
            val spinnerList = currencyList.filter { it.isActive == 1 }.map { it.name }
            if (spinnerList.size < 2) {
                snacky(getString(R.string.choose_at_least_two_currency), getString(R.string.select)) {
                    getBaseActivity()?.replaceFragment(SettingsFragment.newInstance(), true)
                }
                imgBase.setBackgroundByName("transparent")
                mSpinner.setItems("")
            } else {
                mSpinner.setItems(spinnerList)
                if (viewModel.mainData.currentBase == Currencies.NULL && currencyList.isNotEmpty()) {
                    currencyList.firstOrNull { it.isActive == 1 }?.name.let { firsActive ->
                        viewModel.updateCurrentBase(firsActive)
                        mSpinner.selectedIndex = spinnerList.indexOf(firsActive)
                    }
                } else {
                    if (viewModel.mainData.currentBase == Currencies.NULL) {
                        viewModel.updateCurrentBase(currencyList.firstOrNull { it.isActive == 1 }?.name)
                    }
                    if (currencyList.any { c ->
                            c.isActive == 1 &&
                                c.name == viewModel.mainData.currentBase.toString()
                        }
                    ) {
                        mSpinner.selectedIndex =
                            spinnerList.indexOf(viewModel.mainData.currentBase.toString())
                    }
                }
                imgBase.setBackgroundByName(mSpinner.text.toString())
            }
            currencyAdapter.refreshList(currencyList, viewModel.mainData.currentBase)
        }
    }

    private fun setListeners() {
        mSpinner.setOnItemSelectedListener { _, _, _, item ->
            viewModel.apply {
                updateCurrentBase(item.toString())
                getCurrencies()
            }
            txtResult.text = viewModel.getOutputText()
            imgBase.setBackgroundByName(item.toString())
        }

        layoutBar.setOnClickListener {
            if (mSpinner.isActivated) {
                mSpinner.collapse()
            } else {
                mSpinner.expand()
            }
        }
    }

    private fun setKeyboard() {
        btnSeven.setOnClickListener { txtMainToolbar.addText("7") }
        btnEight.setOnClickListener { txtMainToolbar.addText("8") }
        btnNine.setOnClickListener { txtMainToolbar.addText("9") }
        btnDivide.setOnClickListener { txtMainToolbar.addText("/") }
        btnFour.setOnClickListener { txtMainToolbar.addText("4") }
        btnFive.setOnClickListener { txtMainToolbar.addText("5") }
        btnSix.setOnClickListener { txtMainToolbar.addText("6") }
        btnMultiply.setOnClickListener { txtMainToolbar.addText("*") }
        btnOne.setOnClickListener { txtMainToolbar.addText("1") }
        btnTwo.setOnClickListener { txtMainToolbar.addText("2") }
        btnThree.setOnClickListener { txtMainToolbar.addText("3") }
        btnMinus.setOnClickListener { txtMainToolbar.addText("-") }
        btnDot.setOnClickListener { txtMainToolbar.addText(".") }
        btnZero.setOnClickListener { txtMainToolbar.addText("0") }
        btnPercent.setOnClickListener { txtMainToolbar.addText("%") }
        btnPlus.setOnClickListener { txtMainToolbar.addText("+") }
        btnDoubleZero.setOnClickListener { txtMainToolbar.addText("000") }
        btnAc.setOnClickListener {
            txtMainToolbar.text = ""
            txtResult.text = ""
        }
        btnDelete.setOnClickListener {
            if (txtMainToolbar.text.toString() != "") {
                txtMainToolbar.text = txtMainToolbar.text.toString()
                    .substring(0, txtMainToolbar.text.toString().length - 1)
            }
        }
    }

    private fun clearAppData() {
        snacky(getString(R.string.init_app_data))
        val cacheDirectory = context?.cacheDir
        val applicationDirectory = File(cacheDirectory?.parent)
        if (applicationDirectory.exists()) {
            val fileNames = applicationDirectory.list()
            for (fileName in fileNames) {
                if (fileName != "lib") {
                    deleteFile(File(applicationDirectory, fileName))
                }
            }
        }
        viewModel.refreshData()
    }

    private fun deleteFile(file: File?): Boolean {
        var deletedAll = true
        if (file != null) {
            if (file.isDirectory) {
                val children = file.list()
                for (i in children.indices) {
                    deletedAll = deleteFile(File(file, children[i])) && deletedAll
                }
            } else {
                deletedAll = file.delete()
            }
        }
        return deletedAll
    }

    override fun onPause() {
        viewModel.savePreferences()
        super.onPause()
    }

    override fun onResume() {
        viewModel.rates = null
        viewModel.getCurrencies()
        updateUi()
        adView.loadAd(R.string.banner_ad_unit_id_main)
        super.onResume()
    }
}