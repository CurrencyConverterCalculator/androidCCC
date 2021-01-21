//
//  MainVMWrapper.swift
//  ios
//
//  Created by Mustafa Ozhan on 21/01/2021.
//  Copyright © 2021 orgName. All rights reserved.
//

import Combine
import client

final class MainVMWrapper: VMWrapper {

    var viewModel: MainViewModel?

    init() {
        LoggerKt.kermit.d(withMessage: {"MainVMWrapper init"})
    }

    func setViewModel(viewModel: MainViewModel) {
        self.viewModel = viewModel
    }

    func startObserving() {
        // No implementation
    }

    func stopObserving() {
        self.viewModel?.onCleared()
    }
}