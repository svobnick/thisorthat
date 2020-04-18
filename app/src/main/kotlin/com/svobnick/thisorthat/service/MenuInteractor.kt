package com.svobnick.thisorthat.service

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject

class MenuInteractor {
    private var fragmentNumber = 0
    val switchedFragment: Subject<Int> = BehaviorSubject.createDefault(fragmentNumber)

    fun switchFragment(switched: Int) {
        this.fragmentNumber = switched
        switchedFragment.onNext(fragmentNumber)
    }
}