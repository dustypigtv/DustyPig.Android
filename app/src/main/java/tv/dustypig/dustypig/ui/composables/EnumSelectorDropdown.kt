package tv.dustypig.dustypig.ui.composables

import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EnumSelectorDropdown(
    label: String,
    values: Array<T>,
    exclude: Array<T>,
    currentValue: T,
    onChanged: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
        }
    ) {

        OutlinedTextField(
            value = currentValue.toString(),
            onValueChange = { },
            label = {
                Text(text = label)
            },
            singleLine = true,
            readOnly = true,
            modifier = Modifier
                .width(300.dp)
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            for (value in values) {
                if (!exclude.contains(value)) {
                    DropdownMenuItem(
                        text = {
                            Text(text = value.toString())
                        },
                        onClick = {
                            expanded = false
                            onChanged(value)
                        }
                    )
                }
            }
        }
    }
}