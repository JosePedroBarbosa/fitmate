package com.example.fitmate.ui.components

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.model.UserProfile
import com.example.fitmate.ui.activities.AuthActivity
import com.example.fitmate.ui.navigation.NavRoutes
import kotlinx.coroutines.launch

private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueDark = Color(0xFF1557B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onMenuClick: () -> Unit
) {
    var showNotifications by remember { mutableStateOf(false) }

    Surface(
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            },
            title = {},
            actions = {
                Box {
                    IconButton(onClick = { showNotifications = !showNotifications }) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error
                                ) {
                                    Text("3", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showNotifications,
                        onDismissRequest = { showNotifications = false },
                        modifier = Modifier.width(320.dp)
                    ) {
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(16.dp)
                        )

                        HorizontalDivider()

                        NotificationItem(
                            title = "New Challenge Available",
                            description = "7-Day Active Challenge is now live!",
                            time = "2h ago",
                            onClick = { showNotifications = false }
                        )

                        NotificationItem(
                            title = "Goal Progress",
                            description = "You're 75% closer to your fitness goal",
                            time = "5h ago",
                            onClick = { showNotifications = false }
                        )

                        NotificationItem(
                            title = "Workout Reminder",
                            description = "Don't forget today's upper body workout",
                            time = "1d ago",
                            onClick = { showNotifications = false }
                        )

                        HorizontalDivider()

                        TextButton(
                            onClick = { showNotifications = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text("View all notifications")
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun NotificationItem(
    title: String,
    description: String,
    time: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        GoogleBlue.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = GoogleBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AppNavigationDrawer(
    navController: androidx.navigation.NavHostController,
    drawerState: DrawerState,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints {
        val drawerWidth = maxWidth * 0.65f

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(drawerWidth),
                    drawerShape = RoundedCornerShape(0.dp)
                ) {
                    DrawerContent(
                        navController = navController,
                        onClose = {
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                }
            },
            content = content
        )
    }
}

@Composable
private fun DrawerContent(
    navController: androidx.navigation.NavHostController,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedItem by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        FirebaseRepository.fetchUserProfile { profile ->
            userProfile = profile
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(72.dp),
                shadowElevation = 6.dp,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile Avatar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(24.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .shimmerEffect()
                )
            } else {
                Text(
                    text = userProfile?.name ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = userProfile?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 16.dp))

        DrawerLinkItem(
            icon = Icons.Outlined.Person,
            label = "Profile",
            isSelected = selectedItem == "profile",
            onClick = {
                selectedItem = "profile"
                onClose()
                navController.navigate(NavRoutes.PROFILE) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        DrawerLinkItem(
            icon = Icons.Outlined.EmojiEvents,
            label = "Goal",
            isSelected = selectedItem == "goal",
            onClick = {
                selectedItem = "goal"
                onClose()
                navController.navigate(NavRoutes.GOAL) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        DrawerLinkItem(
            icon = Icons.Outlined.LocationOn,
            label = "Find Gym",
            isSelected = selectedItem == "gym",
            onClick = {
                selectedItem = "gym"
                onClose()
                navController.navigate(NavRoutes.GYMS) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        Spacer(Modifier.weight(1f))

        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        DrawerLinkItem(
            icon = Icons.Outlined.Logout,
            label = "Logout",
            isSelected = false,
            isDanger = true,
            onClick = {
                FirebaseRepository.logout()
                val intent = Intent(context, AuthActivity::class.java)
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerLinkItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> colors.primary.copy(alpha = 0.12f)
            isDanger -> colors.error.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        label = ""
    )
    val contentColor by animateColorAsState(
        targetValue = when {
            isDanger -> colors.error
            isSelected -> colors.primary
            else -> colors.onSurfaceVariant
        },
        label = ""
    )

    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
        },
        label = {
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            )
        },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = containerColor,
            selectedContainerColor = containerColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
    )
}