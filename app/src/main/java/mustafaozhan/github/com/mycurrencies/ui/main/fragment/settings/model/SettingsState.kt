package mustafaozhan.github.com.mycurrencies.ui.main.fragment.settings.model

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.github.mustafaozhan.basemob.model.BaseState
import mustafaozhan.github.com.mycurrencies.model.Currency

data class SettingsState(val observer: SettingsViewStateObserver) : BaseState() {
    val searchQuery: MutableLiveData<String> = observer.searchQuery
    val currencyList: MutableLiveData<MutableList<Currency>> = MutableLiveData(mutableListOf())
    val noResult: MutableLiveData<Boolean> = MutableLiveData(false)
}

data class SettingsViewStateObserver(
    val searchQuery: MediatorLiveData<String> = MediatorLiveData<String>()
)