package com.svobnick.thisorthat.service

import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject

class BottomMenuState {
    var currentMenuItem = 0
    val switchedFragment: Subject<Int> = BehaviorSubject.createDefault(currentMenuItem)

    fun switchFragment(switched: Int) {
        this.currentMenuItem = switched
        switchedFragment.onNext(currentMenuItem)
    }
}