package ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.AppInfo
import viewmodel.ViewModelFactory

@Composable
fun AboutScreen(viewModelFactory: ViewModelFactory) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoItem("应用名称", AppInfo.appName)
                    InfoItem("版本", AppInfo.version)
                    InfoItem("作者", AppInfo.author)
                    InfoItem("开源协议", AppInfo.license)
                    InfoItem("项目地址", AppInfo.repositoryUrl)
                    // InfoItem("网站", AppInfo.websiteUrl)
                    InfoItem("描述", AppInfo.description)
                    InfoItem("版权", AppInfo.copyright)
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2
        )
    }
    Divider(modifier = Modifier.padding(vertical = 8.dp))
} 