package com.github.microspectroscopy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.microspectroscopy.ui.theme.MicrospectroscopyTheme

const val TAG = "S.C_Calculator"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MicrospectroscopyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val viewModel: MainVM = viewModel()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth().padding(16.dp)
                    ) {
                        View(
                            viewModel.textBoxPropsState.value,
                            viewModel.resultState.value,
                            update = {
                                viewModel.eventsHandler(it)
                            }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun View(textBoxPropsState: TextBoxProps, result: String, update: (AppEvent) -> Unit) {
    var equation by remember { mutableStateOf(TextFieldValue("")) }
    var variableValues = remember {
        mutableStateListOf(
            Variable(
                name = "",
                value = TextFieldValue()
            )
        )
    }
    val isValidNumber by remember {
        derivedStateOf {
            equation.text.isNotBlank() && equation.text.last().toString().onlyNumbers()
        }
    }
    val isValidEquation by remember(equation.text) {
        derivedStateOf {
            equation.text.atLeastOneAlpha()
        }
    }

    val variableNames by remember {
        derivedStateOf {
            extractVariables(equation.text).distinct()
        }
    }

    val numbers by remember {
        derivedStateOf {
            extractNumbers(equation.text).distinct()
        }
    }

    val variableNameSize by remember {
        derivedStateOf {
            extractVariables(equation.text).distinct().size
        }
    }
    LaunchedEffect(key1 = equation.text) {
//        Log.w(TAG, "isValidNumber $isValidNumber")
//        Log.w(TAG, "isValidEquation $isValidEquation")
//        Log.w(TAG, "variableNames $variableNames")
//        Log.w(TAG, "numbers $numbers")
//        Log.w(TAG, "variableNameSize $variableNameSize")
//        Log.w(TAG, "content ${equation.text}")
//        Log.w(TAG, "content Length ${equation.text.length}")
//        Log.w(TAG, "variableValues ${variableValues.size}")
        variableValues.clear()
        if (isValidEquation || equation.text.isEmpty()) {
            variableNames.forEachIndexed { index, item ->
                val variable = Variable(
                    name = item.toString(),
                    value = TextFieldValue("")
                )
                variableValues.add(variable)
            }
            Log.w(TAG, "variableValues after extraction ${variableValues.size}")
        }
    }
    EquationTextField(
        equation,
        updateEquation = {
            equation = it
        }
    )
    if (isValidEquation) {
        Column {
            variableValues.forEachIndexed { index, item ->
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = variableValues[index].value,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = {
                        Text(
                            text = buildString {
                                append(stringResource(R.string.var_hint).plus(" "))
                                append(variableValues[index].name)
                            }
                        )
                    },
                    onValueChange = {
                        variableValues[index] = variableValues[index].copy(
                            value = it
                        )
                    }
                )
            }
        }
    }
    EvaluateButton(
        isValidEquation,
        isValidNumber,
        result,
        {
            val variables: MutableMap<Char, Double> = variableValues.associate {
                try {
                    if (it.name.isNotBlank() && it.value.text.isNotBlank()) {
                        it.name[0] to it.value.text.toDouble()
                    } else it.name[0] to 0.toDouble()
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                    it.name[0] to 0.toDouble()
                }
            } as MutableMap<Char, Double>
            update(
                AppEvent.Evaluate(
                    expression = equation.text,
                    variables = variables
                )
            )
        },
        {
            equation = TextFieldValue("")
        }
    )
}

@Composable
fun EquationTextField(equation: TextFieldValue, updateEquation: (TextFieldValue) -> Unit) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        value = equation,
        singleLine = true,
        onValueChange = updateEquation,
        textStyle = TextStyle(
            fontSize = 30.sp
        ),
        label = { Text(text = stringResource(R.string.eqn_label)) },
        placeholder = { Text(text = stringResource(R.string.eqn_place_holder)) }
    )
}

@Composable
fun EvaluateButton(
    isValidEquation: Boolean,
    isValidNumber: Boolean,
    result: String,
    onEvaluate: () -> Unit,
    onClearVars: () -> Unit
) {
    var isButtonVisible by remember { mutableStateOf(false) }
    if (isValidNumber) {
        if (!isButtonVisible) {
            isButtonVisible = true
        }
        onEvaluate()
    } else if (!isValidEquation) {
        if (isButtonVisible) {
            isButtonVisible = false
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            enabled = isValidEquation,
            onClick = {
                if (!isButtonVisible) {
                    isButtonVisible = true
                }
                onEvaluate()
            }
        ) {
            Text(stringResource(R.string.btn_label))
        }
        Button(
            onClick = {
                onClearVars()
            }
        ) {
            Text(stringResource(R.string.clear))
        }
    }
    AnimatedVisibility(
        visible = isButtonVisible,
        enter = fadeIn(animationSpec = tween(1000)),
        exit = fadeOut(animationSpec = tween(1000))
    ) {
        Text(
            text = result,
            fontSize = 30.sp,
            fontFamily = FontFamily.Serif
        )
    }
}
