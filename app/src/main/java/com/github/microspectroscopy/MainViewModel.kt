package com.github.microspectroscopy

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import com.nishant.complexcalculator.ComplexCalculator

class MainViewModel : ViewModel() {
    private val _itemsState = mutableStateListOf<TextBox>()
    val itemsState: List<TextBox> = _itemsState
    val resultState = mutableStateOf("")
    fun trigger(variable: String) {
        val position = _itemsState.indexOfFirst { element ->
            element.hint == variable
        }
        if (position == -1) {
            _itemsState.add(TextBox(hint = variable, content = TextFieldValue(""), isTextBoxVisible = true))
        }
    }

    fun onEvents(event: UiEvent) {
        when (event) {
            is UiEvent.PassThrough -> {
                updateVariableValue(event.textBoxValue)
            }

            is UiEvent.Evaluate -> {
                evaluate(
                    event.expression,
                    map = itemsState.map {
                        if (it.content.text.isNotBlank()) {
                            it.hint[0] to it.content.text.toDouble()
                        } else it.hint[0] to 0.toDouble()
                    }.toMap()
                ) {
                    resultState.value = it
                }
            }
        }
    }

    private fun updateVariableValue(item: TextBox) {
        val position = _itemsState.indexOfFirst { element ->
            element.hint == item.hint
        }
        _itemsState[position] = itemsState[position].copy(
            hint = item.hint,
            content = item.content,
        )
    }

    private fun evaluate(text: String, map: Map<Char, Double>? = null, result: (String) -> Unit) {
        val calc = ComplexCalculator.fromString(text)
        try {
            if (map != null) {
                result(calc.compute(map).toString())
            } else {
                result(calc.compute().toString())
            }
        } catch (e: ArithmeticException) {
            result(e.localizedMessage)
        }
    }

    fun clearVariables() {
        _itemsState.clear()
        resultState.value = ""
    }
}

sealed class UiEvent {
    data class PassThrough(val textBoxValue: TextBox) : UiEvent()
    data class Evaluate(val expression: String) : UiEvent()
}

data class TextBox(
    val content: TextFieldValue,
    val hint: String,
    val isTextBoxVisible: Boolean,
)
