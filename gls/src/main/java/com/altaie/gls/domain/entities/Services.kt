package com.altaie.gls.domain.entities

sealed class Services {
    object Google : Services()
    object Huawei : Services()
    object None : Services()
}
