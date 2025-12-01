package ec.edu.uce.cupcake.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.uce.cupcake.data.OrderCounterRepository
import ec.edu.uce.cupcake.data.OrderUiState
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PRICE_PER_CAKE = 2.00
private const val PRICE_FOR_SAME_DAY_PICKUP = 3.00
private const val PRICE_FOR_SPECIAL_FLAVOR = 0.50

class OrderViewModel(application: Application) : AndroidViewModel(application) {

    private val orderCounterRepository = OrderCounterRepository(application)

    private val _uiState = MutableStateFlow(OrderUiState(pickupOptions = pickupOptions()))
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            orderCounterRepository.orderCounter.collect { counter ->
                _uiState.update { currentState ->
                    currentState.copy(orderNumber = counter)
                }
            }
        }
    }

    fun sendOrder() {
        viewModelScope.launch {
            orderCounterRepository.incrementOrderCounter()
        }
    }

    fun setQuantity(numberCakes: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                quantity = numberCakes,
                price = calculatePrice(quantity = numberCakes, flavor = currentState.flavor, pickupDate = currentState.date)
            )
        }
    }

    fun setFlavor(desiredFlavor: String) {
        _uiState.update { currentState ->
            currentState.copy(
                flavor = desiredFlavor,
                price = calculatePrice(quantity = currentState.quantity, flavor = desiredFlavor, pickupDate = currentState.date)
            )
        }
    }

    fun setDate(pickupDate: String) {
        _uiState.update { currentState ->
            currentState.copy(
                date = pickupDate,
                price = calculatePrice(quantity = currentState.quantity, flavor = currentState.flavor, pickupDate = pickupDate)
            )
        }
    }

    fun resetOrder() {
        _uiState.value = OrderUiState(pickupOptions = pickupOptions())
    }

    private fun calculatePrice(
        quantity: Int,
        flavor: String,
        pickupDate: String
    ): String {
        var calculatedPrice = quantity * PRICE_PER_CAKE

        if (flavor == "Sabor Especial") { // Assuming "Sabor Especial" is the special flavor
            calculatedPrice += quantity * PRICE_FOR_SPECIAL_FLAVOR
        }

        if (pickupOptions()[0] == pickupDate) {
            calculatedPrice += PRICE_FOR_SAME_DAY_PICKUP
        }

        val formattedPrice = NumberFormat.getCurrencyInstance().format(calculatedPrice)
        return formattedPrice
    }

    private fun pickupOptions(): List<String> {
        val dateOptions = mutableListOf<String>()
        val formatter = SimpleDateFormat("E MMM d", Locale.getDefault())
        val calendar = Calendar.getInstance()
        repeat(4) {
            dateOptions.add(formatter.format(calendar.time))
            calendar.add(Calendar.DATE, 1)
        }
        return dateOptions
    }
}
