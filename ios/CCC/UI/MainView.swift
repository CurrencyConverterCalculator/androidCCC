//
//  MainView.swift
//  CCC
//
//  Created by Mustafa Ozhan on 28/01/2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import SwiftUI
import NavigationStack
import Client
import GoogleMobileAds

typealias MainObservable = ObservableSEED<MainViewModel, BaseState, MainEffect, MainEvent, MainData>

struct MainView: View {

    @StateObject var observable: MainObservable = koin.get()

    @Environment(\.scenePhase) var scenePhase

    var body: some View {

        NavigationStackView(
            transitionType: .default,
            easing: Animation.easeInOut(duration: 0.5)
        ) {
            if observable.viewModel.isFistRun() {
                SliderView()
            } else {
                CalculatorView()
            }
        }
        .onAppear {
            observable.startObserving()
            observable.event.onResume()
        }
        .onDisappear {
            observable.stopObserving()
            observable.event.onPause()
        }
        .onReceive(observable.effect) { onEffect(effect: $0) }
    }

    private func onEffect(effect: MainEffect) {
        LoggerKt.kermit.d(withMessage: {effect.description})
        switch effect {
        case is MainEffect.ShowInterstitialAd:
            InterstitialAd().show()
        default:
            LoggerKt.kermit.d(withMessage: {"MainView unknown effect"})
        }
    }
}
