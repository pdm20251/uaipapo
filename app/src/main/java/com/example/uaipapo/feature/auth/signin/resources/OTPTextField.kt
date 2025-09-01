package com.example.uaipapo.feature.auth.signin.resources

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uaipapo.ui.theme.BrightRed
import com.example.uaipapo.ui.theme.Gray
import com.example.uaipapo.ui.theme.LighGray

@Composable
fun OtpTextField(
    otpText: String,
    onOtpTextChange: (String, Boolean) -> Unit
) {

    BasicTextField(
        value = TextFieldValue(otpText, selection = TextRange(otpText.length)),
        onValueChange = {
            if (it.text.length <= 6) {
                onOtpTextChange.invoke(it.text, it.text.length == 6)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword
        ),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    var isFocused = otpText.length == index
                    Text(
                        modifier = Modifier
                            .width(40.dp)
                            .height(50.dp)
                            .border(
                                if(isFocused) 2.dp
                                else 1.dp,
                                BrightRed,
                                RoundedCornerShape(8.dp)
                            )
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .padding(2.dp),
                        text = char,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    )
}

@Preview
@Composable
fun OtpTextFieldPreview() {
    var otpText by remember { mutableStateOf("") }
    OtpTextField(
        otpText = otpText,
        onOtpTextChange = { value, _ ->
            otpText = value
        })
}