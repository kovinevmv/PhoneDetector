package com.leti.phonedetector

// TODO add image, tags, date, time and other
class PhoneInfo(name_ : String? = null, number_ : String? = null, isSpam_ : Boolean = false){
    val name = name_ ?: "Undefined User"
    val number = number_ ?: "+7800553535"
    val isSpam = isSpam_
}