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

class AppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MicrospectroscopyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val viewModel: MainViewModel = viewModel()
                    App(viewModel)
                }
            }
        }
    }
}

@Composable
fun App(viewModel: MainViewModel) {
    var isTextBoxVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        EquationComponent(
            variablePool = viewModel.itemsState,
            result = viewModel.resultState.value,
            clearVariables = viewModel::clearVariables,
            triggerVariables = {
                viewModel.trigger(it)
            },
            onVariableValuePassThrough = {
                viewModel.onEvents(it)
            },
            onEvaluate = {
                viewModel.onEvents(it)
            }
//            onClearEverything = {
//                viewModel.onEvents(it)
//            }
        )
    }
}

@Composable
fun EquationComponent(
    variablePool: List<TextBox>,
    result: String,
    clearVariables: () -> Unit,
    triggerVariables: (String) -> Unit,
    onVariableValuePassThrough: (UiEvent.PassThrough) -> Unit,
    onEvaluate: (UiEvent.Evaluate) -> Unit
//    onClearEverything: (UiEvent.Clear) -> Unit,
) {
    var equation by remember { mutableStateOf(TextFieldValue("")) }

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

    LaunchedEffect(key1 = variableNames.size) {
        if (variableNames.isEmpty()) {
            clearVariables()
        }
    }

    EquationTextBox(
        equation,
        updateEquation = { equation = it }
    )

    VariablesComponent(
        isValidEquation,
        variableNames,
        variablePool,
        triggerVariables = { triggerVariables(variableNames.last().toString()) },
        onVariableValuePassThrough = onVariableValuePassThrough
    )
    ButtonComponent(isValidEquation, isValidNumber, result, onEvaluate = {
        onEvaluate(
            UiEvent.Evaluate(
                expression = equation.text
            )
        )
    }, onClearVars = clearVariables)
}

@Composable
fun EquationTextBox(equation: TextFieldValue, updateEquation: (TextFieldValue) -> Unit) {
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
fun ButtonComponent(
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
        onClearVars()
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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

@Composable
fun VariablesComponent(
    isValidEquation: Boolean,
    variableNames: List<Char>,
    variablePool: List<TextBox>,
    triggerVariables: () -> Unit,
    onVariableValuePassThrough: (UiEvent.PassThrough) -> Unit
//    forceClearVariablePool: () -> Unit
) {
    if (isValidEquation) {
        if (variableNames.isNotEmpty()) {
            triggerVariables()
            if (variableNames.size == variablePool.size) {
                Column {
                    variableNames.forEachIndexed { index, item ->
                        Log.v(TAG, "Recomposing...")
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = variablePool[index].content,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            label = {
                                Text(
                                    text = buildString {
                                        append(stringResource(R.string.var_hint).plus(" "))
                                        append(variablePool[index].hint)
                                    }
                                )
                            },
                            onValueChange = {
                                onVariableValuePassThrough(
                                    UiEvent.PassThrough(
                                        TextBox(
                                            hint = item.toString(),
                                            content = it
                                        )
                                    )
                                )
                            }
                        )
                    }
                }
            } else {
                Log.v(TAG, "forceClearVariablePool")
//                forceClearVariablePool()
            }
        }
    }
}
