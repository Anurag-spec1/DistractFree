package com.example.distractfree.dataclasses

data class OnBoardData(
    var head1: String,
    var head2: String,
    var image: Int,
    var description: String = "",
) {
    constructor() : this("", "", 0, "")
}