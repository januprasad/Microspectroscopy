package com.github.microspectroscopy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.microspectroscopy.ui.theme.MicrospectroscopyTheme

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
                    App()
                }
            }
        }
    }
}

@Composable
fun EquationComponent() {

}

@Composable
fun App() {
    val viewModel: MainViewModel = viewModel()
    EquationComponent()
    var equaltion by remember { mutableStateOf(TextFieldValue("")) }

    val isValidNumber by remember {
        derivedStateOf {
            equaltion.text.isNotBlank() && equaltion.text.last().toString().onlyNumbers()
        }
    }

    var isButtonVisible by remember { mutableStateOf(false) }
    var isTextBoxVisible by remember { mutableStateOf(false) }

    val isValidEquation by remember(equaltion.text) {
        derivedStateOf {
            equaltion.text.atLeastOneAlpha()
        }
    }

    val variableNames by remember {
        derivedStateOf {
            extractVariables(equaltion.text).distinct()
        }
    }
    LaunchedEffect(key1 = variableNames.size) {
        if (variableNames.isEmpty()) {
            viewModel.clearVariables()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            value = equaltion,
            singleLine = true,
            onValueChange = {
                equaltion = it
            },
            textStyle = TextStyle(
                fontSize = 30.sp
            ),
            label = { Text(text = "Please type/paste a string to evaluate") },
            placeholder = { Text(text = "Something like PI*r^2") }
        )

        if (isValidEquation) {
            if (variableNames.isNotEmpty()) {
                viewModel.trigger(variableNames.last().toString())
                if (variableNames.size == viewModel.itemsState.size) {
                    Column {
                        variableNames.forEachIndexed { index, item ->
                            Log.v("App", "Variable generation yes")
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = viewModel.itemsState[index].content,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = { Text(text = "Please input value for ${viewModel.itemsState[index].hint}") },
                                onValueChange = {
                                    viewModel.onEvents(
                                        UiEvent.PassThrough(
                                            TextBox(
                                                hint = item.toString(),
                                                content = it,
                                                isTextBoxVisible = true
                                            )
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        } else if (isValidNumber) {
            if (!isButtonVisible) {
                isButtonVisible = true
            }
            viewModel.onEvents(
                UiEvent.Evaluate(
                    expression = equaltion.text
                )
            )
        } else {
            if (isButtonVisible) {
                isButtonVisible = false
            }
            viewModel.clearVariables()
        }
        Button(
            enabled = isValidEquation,
            onClick = {
                if (!isButtonVisible) {
                    isButtonVisible = true
                }
                viewModel.onEvents(
                    UiEvent.Evaluate(
                        expression = equaltion.text
                    )
                )
            }
        ) {
            Text("Evaluate Equation")
        }
        AnimatedVisibility(
            visible = isButtonVisible,
            enter = fadeIn(animationSpec = tween(1000)),
            exit = fadeOut(animationSpec = tween(1000))
        ) {
            Text(
                text = viewModel.resultState.value,
                fontSize = 30.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    MicrospectroscopyTheme {
        App()
    }
}
