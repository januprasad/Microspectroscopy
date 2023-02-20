package com.github.microspectroscopy

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.ViewModel
import com.nishant.complexcalculator.ComplexCalculator

class MainVM : ViewModel() {
    val resultState = mutableStateOf("")
    fun eventsHandler(event: AppEvent) {
        when (event) {
            is AppEvent.NewTextBox -> {
                val newTextBox = event.newTextBox
                textBoxPropsState.value = textBoxPropsState.value.copy(
                    isValidNumber = newTextBox.isValidNumber,
                    isValidEquation = newTextBox.isValidEquation,
                    variables = newTextBox.variables,
                    numbers = newTextBox.numbers,
                    content = newTextBox.content
                )
            }

            is AppEvent.Validate -> {
                validate(event.expression) {
                }
            }

            is AppEvent.Evaluate -> {
                evaluate(
                    event.expression,
                    map = event.variables
                ) {
                    Log.v(TAG, it)
                    resultState.value = it
                }
            }
        }
    }

    private fun validate(expression: String, function: () -> Unit) {
        val tokens =
            expression.replace("\\s+".toRegex(), "").toLowerCase().split("(?<=[-+*/()])|(?=[-+*/()])".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
    }

    val textBoxPropsState = mutableStateOf(
        TextBoxProps(
            contentValue = "",
            content = CharArray(0),
            numbers = CharArray(0),
            variables = CharArray(0),
            isValidNumber = false,
            isValidEquation = false
        )
    )

    private fun evaluate(text: String, map: Map<Char, Double>? = null, result: (String) -> Unit) {
        val calc = ComplexCalculator.fromString(text.toLowerCase())
        try {
            if (!map.isNullOrEmpty()) {
                result(calc.compute(map).toString())
            } else {
                result(calc.compute().toString())
            }
        } catch (ex1: ArithmeticException) {
            result(ex1.localizedMessage)
        } catch (ex2: IllegalArgumentException) {
            result(ex2.localizedMessage)
        }
    }
}

data class Variable(
    var value: TextFieldValue,
    val name: String
)

data class TextBoxProps(
    val contentValue: String,
    val content: CharArray,
    val numbers: CharArray,
    val variables: CharArray,
    val isValidNumber: Boolean,
    val isValidEquation: Boolean
)

sealed class AppEvent {
    data class NewTextBox(val newTextBox: TextBoxProps) : AppEvent()
    data class Evaluate(val expression: String, val variables: MutableMap<Char, Double>) :
        AppEvent()

    data class Validate(val expression: String) : AppEvent()
}
