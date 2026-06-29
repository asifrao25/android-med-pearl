package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun InboxHeaderButton(
    theme: TabTheme,
    inboxBadgeCount: Int,
    onClick: () -> Unit,
) {
    val badgeLabel = when {
        inboxBadgeCount <= 0 -> null
        inboxBadgeCount > 99 -> "99+"
        else -> inboxBadgeCount.toString()
    }

    HeaderIconButton(theme = theme, onClick = onClick) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = if (badgeLabel != null) {
                    "Inbox, $inboxBadgeCount unread"
                } else {
                    "Inbox"
                },
                tint = theme.primary,
                modifier = Modifier.size(PearlLayout.headerIconSize),
            )
            badgeLabel?.let { label ->
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-3).dp)
                        .background(Color(0xFFFF3B30), RoundedCornerShape(999.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
        }
    }
}
