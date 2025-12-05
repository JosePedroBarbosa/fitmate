package com.example.fitmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitmate.model.LeaderboardUser
import com.example.fitmate.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.example.fitmate.data.local.DatabaseProvider
import com.example.fitmate.data.local.entity.CachedLeaderboardEntryEntity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LeaderboardScreen() {
    var leaderboardUsers by remember { mutableStateOf<List<LeaderboardUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appContext = context.applicationContext

    val topUsersFlow = remember { FirebaseRepository.subscribeTopUsersByPoints(limit = 10) }
    val topProfiles by topUsersFlow.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        val cached = try {
            withContext(Dispatchers.IO) {
                DatabaseProvider.get(appContext).cachedLeaderboardDao().getAll()
            }
        } catch (_: Exception) { emptyList() }

        if (cached.isNotEmpty()) {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            leaderboardUsers = cached.map { e ->
                LeaderboardUser(
                    name = e.name,
                    points = e.points,
                    rank = e.rank,
                    isCurrentUser = e.uid == currentUid
                )
            }
            isLoading = false
        }

    }

    LaunchedEffect(topProfiles) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        if (topProfiles.isEmpty()) {
            FirebaseRepository.fetchUserProfile { profile ->
                val single = profile?.let {
                    listOf(
                        LeaderboardUser(
                            name = it.name,
                            points = it.points,
                            rank = 1,
                            isCurrentUser = true
                        )
                    )
                } ?: emptyList()
                leaderboardUsers = single
                isLoading = false
            }
        } else {
            val mapped = topProfiles.take(10).mapIndexed { index, u ->
                LeaderboardUser(
                    name = u.name,
                    points = u.points,
                    rank = index + 1,
                    isCurrentUser = u.uid == currentUid
                )
            }
            leaderboardUsers = mapped
            isLoading = false
            scope.launch(Dispatchers.IO) {
                val dao = DatabaseProvider.get(appContext).cachedLeaderboardDao()
                val toSave = topProfiles.take(10).mapIndexed { index, u ->
                    CachedLeaderboardEntryEntity(
                        rank = index + 1,
                        uid = u.uid,
                        name = u.name,
                        points = u.points
                    )
                }
                dao.clear()
                dao.upsertAll(toSave)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val first = leaderboardUsers.getOrNull(0) ?: LeaderboardUser(stringResource(id = com.example.fitmate.R.string.leaderboard_no_data), 0, 1)
                    val second = leaderboardUsers.getOrNull(1) ?: LeaderboardUser(stringResource(id = com.example.fitmate.R.string.leaderboard_no_data), 0, 2)
                    val third = leaderboardUsers.getOrNull(2) ?: LeaderboardUser(stringResource(id = com.example.fitmate.R.string.leaderboard_no_data), 0, 3)
                    PodiumSection(
                        first = first,
                        second = second,
                        third = third
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(id = com.example.fitmate.R.string.leaderboard_top_performers),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    stringResource(id = com.example.fitmate.R.string.leaderboard_top_10_label),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            val listItems = leaderboardUsers.drop(3)
            itemsIndexed(listItems.take(7)) { index, user ->
                LeaderboardItem(
                    user = user,
                    isTopPerformer = true,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PodiumSection(
    first: LeaderboardUser,
    second: LeaderboardUser,
    third: LeaderboardUser
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            PodiumPosition(second, 2, 140.dp, Color(0xFFC0C0C0))
            PodiumPosition(first, 1, 180.dp, Color(0xFFFFD700))
            PodiumPosition(third, 3, 120.dp, Color(0xFFCD7F32))
        }
    }
}

@Composable
fun PodiumPosition(
    user: LeaderboardUser,
    rank: Int,
    height: Dp,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            if (rank == 1) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier
                        .size(32.dp)
                        .offset(y = (-8).dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            user.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (rank == 1) FontWeight.Bold else FontWeight.SemiBold
            ),
            maxLines = 1
        )

        Text(
            stringResource(id = com.example.fitmate.R.string.leaderboard_points_short_format, user.points),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    "#$rank",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (rank == 1) 48.sp else 40.sp
                    ),
                    color = color.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    user: LeaderboardUser,
    isTopPerformer: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (user.isCurrentUser) 4.dp else 2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (user.isCurrentUser)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (user.isCurrentUser) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isTopPerformer)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            "#${user.rank}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isTopPerformer)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (user.isCurrentUser)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = if (user.isCurrentUser)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            user.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (user.isCurrentUser) FontWeight.Bold else FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                        if (user.isCurrentUser) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    stringResource(id = com.example.fitmate.R.string.leaderboard_you_chip),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(id = com.example.fitmate.R.string.leaderboard_points_format, user.points),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isTopPerformer) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
