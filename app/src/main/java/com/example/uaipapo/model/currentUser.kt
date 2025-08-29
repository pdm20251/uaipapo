package com.example.uaipapo.model

object currentUser {
    var id: String = ""
    var name: String = ""
    var pic: String = ""

    fun setUserData(user: UserModel) {
        this.id = user.id
        this.name = user.name
        this.pic = user.pic
    }
}