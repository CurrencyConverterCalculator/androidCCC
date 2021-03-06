/*
 * Copyright (c) 2021 Mustafa Ozhan. All rights reserved.
 */
@file:Suppress("TooManyFunctions")

package com.github.mustafaozhan.ccc.client.util

import com.github.mustafaozhan.ccc.client.model.Currency
import com.github.mustafaozhan.ccc.client.model.RemoveAdType
import com.github.mustafaozhan.ccc.common.model.CurrencyResponse
import com.github.mustafaozhan.ccc.common.model.CurrencyType
import com.github.mustafaozhan.ccc.common.model.Rates
import com.github.mustafaozhan.ccc.common.util.nowAsInstant
import com.github.mustafaozhan.ccc.common.util.nowAsLong
import com.github.mustafaozhan.scopemob.whether
import com.github.mustafaozhan.scopemob.whetherNot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private const val BIGGEST_DIGIT = 9

expect fun Double.getFormatted(): String

fun CoroutineScope.launchIgnored(function: suspend () -> Unit) {
    launch {
        function()
    }
}

fun Long.isWeekPassed(): Boolean {
    return nowAsLong() - this >= WEEK
}

fun Long.isRewardExpired(): Boolean {
    return nowAsLong() - this >= VIDEO_REWARD * DAY
}

fun Long.toInstant() = Instant.fromEpochMilliseconds(this)

fun Long.toDateString(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
) = toInstant().toDateString(timeZone)

fun Instant.toDateString(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
) = toLocalDateTime(timeZone).run {
    "${hour.doubleDigits()}:${minute.doubleDigits()} " +
        "${dayOfMonth.doubleDigits()}.${monthNumber.doubleDigits()}.${year.doubleDigits()}"
}

fun Int.doubleDigits() = if (this <= BIGGEST_DIGIT) "0$this" else "$this"

fun CurrencyResponse.toRates() = rates.copy(base = base, date = nowAsInstant().toDateString())

fun Rates?.calculateResult(name: String, value: String?) =
    this?.whetherNot { value.isNullOrEmpty() }
        ?.getConversionByName(name)
        ?.times(value?.toSupportedCharacters()?.toStandardDigits()?.toDouble() ?: 0.0)
        ?: 0.0

fun String.toSupportedCharacters() =
    replace(",", ".")
        .replace("٫", ".")
        .replace(" ", "")
        .replace("−", "-")

fun String.isEmptyOrNullString() = isEmpty() || equals("null", true)

fun String.toStandardDigits(): String {
    val builder = StringBuilder()
    forEach { char ->
        char.toString().toIntOrNull()
            ?.whether { it >= 0 }
            ?.let { builder.append(it) }
            ?: run { builder.append(char) }
    }
    return builder.toString()
}

fun Currency.getCurrencyConversionByRate(base: String, rate: Rates?) =
    "1 $base = ${rate?.getConversionByName(name)} ${getVariablesOneLine()}"

fun List<Currency>?.toValidList(currentBase: String) =
    this?.filter {
        it.name != currentBase &&
            it.isActive &&
            it.rate.toString() != "NaN" &&
            it.rate.toString() != "0.0"
    } ?: mutableListOf()

@Suppress("MagicNumber")
fun RemoveAdType.calculateAdRewardEnd(startDate: Long = nowAsLong()) = when (this) {
    RemoveAdType.VIDEO -> startDate.toInstant().plus(
        VIDEO_REWARD,
        DateTimeUnit.DAY,
        TimeZone.currentSystemDefault()
    ).toEpochMilliseconds()
    RemoveAdType.MONTH -> startDate.toInstant().plus(
        1,
        DateTimeUnit.MONTH,
        TimeZone.currentSystemDefault()
    ).toEpochMilliseconds()
    RemoveAdType.QUARTER -> startDate.toInstant().plus(
        3,
        DateTimeUnit.MONTH,
        TimeZone.currentSystemDefault()
    ).toEpochMilliseconds()
    RemoveAdType.HALF_YEAR -> startDate.toInstant().plus(
        6,
        DateTimeUnit.MONTH,
        TimeZone.currentSystemDefault()
    ).toEpochMilliseconds()
    RemoveAdType.YEAR -> startDate.toInstant().plus(
        1,
        DateTimeUnit.YEAR,
        TimeZone.currentSystemDefault()
    ).toEpochMilliseconds()
}

@Suppress("ComplexMethod", "LongMethod")
fun Rates.getConversionByName(name: String) = when (name.uppercase()) {
    CurrencyType.AED.toString() -> aed
    CurrencyType.AFN.toString() -> afn
    CurrencyType.ALL.toString() -> all
    CurrencyType.AMD.toString() -> amd
    CurrencyType.ANG.toString() -> ang
    CurrencyType.AOA.toString() -> aoa
    CurrencyType.ARS.toString() -> ars
    CurrencyType.AUD.toString() -> aud
    CurrencyType.AWG.toString() -> awg
    CurrencyType.AZN.toString() -> azn
    CurrencyType.BAM.toString() -> bam
    CurrencyType.BBD.toString() -> bbd
    CurrencyType.BDT.toString() -> bdt
    CurrencyType.BGN.toString() -> bgn
    CurrencyType.BHD.toString() -> bhd
    CurrencyType.BIF.toString() -> bif
    CurrencyType.BMD.toString() -> bmd
    CurrencyType.BND.toString() -> bnd
    CurrencyType.BOB.toString() -> bob
    CurrencyType.BRL.toString() -> brl
    CurrencyType.BSD.toString() -> bsd
    CurrencyType.BTC.toString() -> btc
    CurrencyType.BTN.toString() -> btn
    CurrencyType.BWP.toString() -> bwp
    CurrencyType.BYN.toString() -> byn
    CurrencyType.BZD.toString() -> bzd
    CurrencyType.CAD.toString() -> cad
    CurrencyType.CDF.toString() -> cdf
    CurrencyType.CHF.toString() -> chf
    CurrencyType.CLF.toString() -> clf
    CurrencyType.CLP.toString() -> clp
    CurrencyType.CNH.toString() -> cnh
    CurrencyType.CNY.toString() -> cny
    CurrencyType.COP.toString() -> cop
    CurrencyType.CRC.toString() -> crc
    CurrencyType.CUC.toString() -> cuc
    CurrencyType.CUP.toString() -> cup
    CurrencyType.CVE.toString() -> cve
    CurrencyType.CZK.toString() -> czk
    CurrencyType.DJF.toString() -> djf
    CurrencyType.DKK.toString() -> dkk
    CurrencyType.DOP.toString() -> dop
    CurrencyType.DZD.toString() -> dzd
    CurrencyType.EGP.toString() -> egp
    CurrencyType.ERN.toString() -> ern
    CurrencyType.ETB.toString() -> etb
    CurrencyType.EUR.toString() -> eur
    CurrencyType.FJD.toString() -> fjd
    CurrencyType.FKP.toString() -> fkp
    CurrencyType.GBP.toString() -> gbp
    CurrencyType.GEL.toString() -> gel
    CurrencyType.GGP.toString() -> ggp
    CurrencyType.GHS.toString() -> ghs
    CurrencyType.GIP.toString() -> gip
    CurrencyType.GMD.toString() -> gmd
    CurrencyType.GNF.toString() -> gnf
    CurrencyType.GTQ.toString() -> gtq
    CurrencyType.GYD.toString() -> gyd
    CurrencyType.HKD.toString() -> hkd
    CurrencyType.HNL.toString() -> hnl
    CurrencyType.HRK.toString() -> hrk
    CurrencyType.HTG.toString() -> htg
    CurrencyType.HUF.toString() -> huf
    CurrencyType.IDR.toString() -> idr
    CurrencyType.ILS.toString() -> ils
    CurrencyType.IMP.toString() -> imp
    CurrencyType.INR.toString() -> inr
    CurrencyType.IQD.toString() -> iqd
    CurrencyType.IRR.toString() -> irr
    CurrencyType.ISK.toString() -> isk
    CurrencyType.JEP.toString() -> jep
    CurrencyType.JMD.toString() -> jmd
    CurrencyType.JOD.toString() -> jod
    CurrencyType.JPY.toString() -> jpy
    CurrencyType.KES.toString() -> kes
    CurrencyType.KGS.toString() -> kgs
    CurrencyType.KHR.toString() -> khr
    CurrencyType.KMF.toString() -> kmf
    CurrencyType.KPW.toString() -> kpw
    CurrencyType.KRW.toString() -> krw
    CurrencyType.KWD.toString() -> kwd
    CurrencyType.KYD.toString() -> kyd
    CurrencyType.KZT.toString() -> kzt
    CurrencyType.LAK.toString() -> lak
    CurrencyType.LBP.toString() -> lbp
    CurrencyType.LKR.toString() -> lkr
    CurrencyType.LRD.toString() -> lrd
    CurrencyType.LSL.toString() -> lsl
    CurrencyType.LYD.toString() -> lyd
    CurrencyType.MAD.toString() -> mad
    CurrencyType.MDL.toString() -> mdl
    CurrencyType.MGA.toString() -> mga
    CurrencyType.MKD.toString() -> mkd
    CurrencyType.MMK.toString() -> mmk
    CurrencyType.MNT.toString() -> mnt
    CurrencyType.MOP.toString() -> mop
    CurrencyType.MRO.toString() -> mro
    CurrencyType.MRU.toString() -> mru
    CurrencyType.MUR.toString() -> mur
    CurrencyType.MVR.toString() -> mvr
    CurrencyType.MWK.toString() -> mwk
    CurrencyType.MXN.toString() -> mxn
    CurrencyType.MYR.toString() -> myr
    CurrencyType.MZN.toString() -> mzn
    CurrencyType.NAD.toString() -> nad
    CurrencyType.NGN.toString() -> ngn
    CurrencyType.NIO.toString() -> nio
    CurrencyType.NOK.toString() -> nok
    CurrencyType.NPR.toString() -> npr
    CurrencyType.NZD.toString() -> nzd
    CurrencyType.OMR.toString() -> omr
    CurrencyType.PAB.toString() -> pab
    CurrencyType.PEN.toString() -> pen
    CurrencyType.PGK.toString() -> pgk
    CurrencyType.PHP.toString() -> php
    CurrencyType.PKR.toString() -> pkr
    CurrencyType.PLN.toString() -> pln
    CurrencyType.PYG.toString() -> pyg
    CurrencyType.QAR.toString() -> qar
    CurrencyType.RON.toString() -> ron
    CurrencyType.RSD.toString() -> rsd
    CurrencyType.RUB.toString() -> rub
    CurrencyType.RWF.toString() -> rwf
    CurrencyType.SAR.toString() -> sar
    CurrencyType.SBD.toString() -> sbd
    CurrencyType.SCR.toString() -> scr
    CurrencyType.SDG.toString() -> sdg
    CurrencyType.SEK.toString() -> sek
    CurrencyType.SGD.toString() -> sgd
    CurrencyType.SHP.toString() -> shp
    CurrencyType.SLL.toString() -> sll
    CurrencyType.SOS.toString() -> sos
    CurrencyType.SRD.toString() -> srd
    CurrencyType.SSP.toString() -> ssp
    CurrencyType.STD.toString() -> std
    CurrencyType.STN.toString() -> stn
    CurrencyType.SVC.toString() -> svc
    CurrencyType.SYP.toString() -> syp
    CurrencyType.SZL.toString() -> szl
    CurrencyType.THB.toString() -> thb
    CurrencyType.TJS.toString() -> tjs
    CurrencyType.TMT.toString() -> tmt
    CurrencyType.TND.toString() -> tnd
    CurrencyType.TOP.toString() -> top
    CurrencyType.TRY.toString() -> `try`
    CurrencyType.TTD.toString() -> ttd
    CurrencyType.TWD.toString() -> twd
    CurrencyType.TZS.toString() -> tzs
    CurrencyType.UAH.toString() -> uah
    CurrencyType.UGX.toString() -> ugx
    CurrencyType.USD.toString() -> usd
    CurrencyType.UYU.toString() -> uyu
    CurrencyType.UZS.toString() -> uzs
    CurrencyType.VES.toString() -> ves
    CurrencyType.VND.toString() -> vnd
    CurrencyType.VUV.toString() -> vuv
    CurrencyType.WST.toString() -> wst
    CurrencyType.XAF.toString() -> xaf
    CurrencyType.XAG.toString() -> xag
    CurrencyType.XAU.toString() -> xau
    CurrencyType.XCD.toString() -> xcd
    CurrencyType.XDR.toString() -> xdr
    CurrencyType.XOF.toString() -> xof
    CurrencyType.XPD.toString() -> xpd
    CurrencyType.XPF.toString() -> xpf
    CurrencyType.XPT.toString() -> xpt
    CurrencyType.YER.toString() -> yer
    CurrencyType.ZAR.toString() -> zar
    CurrencyType.ZMW.toString() -> zmw
    CurrencyType.ZWL.toString() -> zwl
    else -> 0.0
}
