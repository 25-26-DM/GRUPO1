package ec.edu.uce.cupcake.data

import ec.edu.uce.cupcake.R

object DataSource {
    val flavors = listOf(
        R.string.vanilla,
        R.string.chocolate,
        R.string.red_velvet,
        R.string.salted_caramel,
        R.string.coffee
    )

    val quantityOptions = listOf(
        Pair(R.string.one_cake, 1),
        Pair(R.string.six_cakes, 6),
        Pair(R.string.twelve_cakes, 12)
    )
}
