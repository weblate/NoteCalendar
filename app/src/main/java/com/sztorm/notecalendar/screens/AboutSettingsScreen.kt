package com.sztorm.notecalendar.screens

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults.outlinedIconButtonColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mikepenz.aboutlibraries.LibsBuilder
import com.sztorm.notecalendar.AppInfo
import com.sztorm.notecalendar.viewmodels.MainViewModel
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.components.preferences.CategoryPreference
import com.sztorm.notecalendar.components.preferences.Preference
import com.sztorm.notecalendar.components.preferences.SubpreferenceScreen
import kotlinx.coroutines.launch

@Composable
private fun Link(
    text: String,
    url: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontSize: TextUnit = 16.sp,
    color: Color = MaterialTheme.colorScheme.secondary
) = Text(
    text = buildAnnotatedString {
        withLink(
            LinkAnnotation.Url(
                url = url,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = color,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(text)
        }
    },
    style = style,
    fontSize = fontSize,
    modifier = modifier
)

@Composable
private fun CopyButton(
    iconColor: Color,
    borderColor: Color,
    pressedBackgroundColor: Color,
    onClick: () -> Unit
) = OutlinedIconButton(
    onClick = onClick,
    border = BorderStroke(width = 1.dp, color = borderColor),
    colors = outlinedIconButtonColors(
        contentColor = pressedBackgroundColor,
    ),
    modifier = Modifier
        .padding(end = 8.dp)
        .size(24.dp)
) {
    Icon(
        imageVector = ImageVector
            .vectorResource(R.drawable.icon_outline_content_copy),
        contentDescription = "copy",
        tint = iconColor,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
fun AboutSettingsScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val themeColors = viewModel.state.themeColors
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    SubpreferenceScreen(
        title = "About", // TODO: add to strings.xml
        iconTint = themeColors.textColor,
        onBackButtonClick = { navController.navigateUp() }
    ) {
        CategoryPreference(
            title = "Basic", // TODO: add to strings.xml
            titleColor = themeColors.secondaryColor
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "Version", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    CopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "version", AppInfo.VERSION
                                    )
                                )
                            )
                        }
                    }
                    Text(
                        text = AppInfo.VERSION,
                        fontSize = 16.sp
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "Contact", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    CopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "contact email", AppInfo.CONTACT_EMAIL
                                    )
                                )
                            )
                        }
                    }
                    Text(
                        text = AppInfo.CONTACT_EMAIL,
                        fontSize = 16.sp
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "Source code", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    CopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "source code url", AppInfo.SOURCE_CODE_GITHUB_URL
                                    )
                                )
                            )
                        }
                    }
                    Link(
                        text = AppInfo.SOURCE_CODE_GITHUB,
                        url = AppInfo.SOURCE_CODE_GITHUB_URL
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "License", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    CopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "license url", AppInfo.LICENSE_URL
                                    )
                                )
                            )
                        }
                    }
                    Link(
                        text = AppInfo.LICENSE,
                        url = AppInfo.LICENSE_URL
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "Privacy policy", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    CopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "privacy policy url", AppInfo.PRIVACY_POLICY_URL
                                    )
                                )
                            )
                        }
                    }
                    Link(
                        text = AppInfo.SOURCE_CODE_GITHUB,
                        url = AppInfo.PRIVACY_POLICY_URL
                    )
                }
            }
        }
        CategoryPreference(
            title = "Advanced", // TODO: add to strings.xml
            titleColor = themeColors.secondaryColor
        ) {
            Preference(
                title = "Library licenses", // TODO: add to strings.xml
                titleColor = themeColors.textColor,
                icon = painterResource(R.drawable.icon_outline_rounded_license),
                iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                onClick = {
                    context.startActivity(
                        LibsBuilder()
                            .withActivityTitle("Library licenses") // TODO: add to strings.xml
                            .withEdgeToEdge(true)
                            .withSearchEnabled(true)
                            .intent(context)
                    )
                }
            )
        }
    }
}