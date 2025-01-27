package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun NavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (selected) MaterialTheme.colors.primary.copy(alpha = 0.12f)
               else MaterialTheme.colors.surface,
        shape = MaterialTheme.shapes.small
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) MaterialTheme.colors.primary
                           else MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = label,
                    style = MaterialTheme.typography.body2,
                    color = if (selected) MaterialTheme.colors.primary
                           else MaterialTheme.colors.onSurface
                )
            }
        }
    }
} 