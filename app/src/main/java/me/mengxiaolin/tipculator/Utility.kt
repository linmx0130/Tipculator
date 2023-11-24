package me.mengxiaolin.tipculator

fun calculateTips(subTotal: Int, tax: Int, tipsRate: Int, isRoundToDollar: Boolean): Int {
    val simpleTips = ((subTotal * tipsRate).toFloat() / 100.0f).toInt()
    return if (!isRoundToDollar) {
        simpleTips
    } else {
        val simpleTotal = subTotal + tax + simpleTips
        val roundTotal = (simpleTotal + 50) / 100 * 100
        roundTotal - subTotal - tax
    }
}