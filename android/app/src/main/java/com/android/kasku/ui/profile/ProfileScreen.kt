package com.android.kasku.ui.me

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.kasku.R
import com.android.kasku.data.profile.UserData
import com.android.kasku.data.profile.UserProfile
import com.android.kasku.ui.profile.ProfileUiState
import com.android.kasku.ui.profile.ProfileViewModel
import com.android.kasku.ui.theme.KasKuTheme
import java.text.NumberFormat
import java.util.*
// Import ViewModelFactory jika Anda membuatnya secara manual
// import com.android.kasku.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile(context)
    }

    Scaffold { paddingValues ->
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Success -> {
                ProfileContent(paddingValues, state.userData, viewModel)
            }
            is ProfileUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(paddingValues: PaddingValues, userData: UserData, profileViewModel: ProfileViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }

    // Editable states
    var username by remember { mutableStateOf(userData.username) }
    var occupation by remember { mutableStateOf(userData.userProfile.occupation) }
    var income by remember { mutableStateOf(userData.userProfile.income.toString()) }
    var goal by remember { mutableStateOf(userData.userProfile.financialGoals) }
    val isUpdating by profileViewModel.updateInProgress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture
        Image(
            painter = painterResource(id = R.drawable.user_icon),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Info
        Text(text = username, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = userData.email, fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(
                text = "Detail Akun",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                icon = Icons.Default.Work,
                label = "Pekerjaan",
                value = occupation,
                isEditing = isEditing,
                onValueChange = { occupation = it }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                icon = Icons.Default.AccountBalanceWallet,
                label = "Pendapatan",
                value = income,
                isEditing = isEditing,
                onValueChange = { income = it }
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                icon = Icons.Default.Flag,
                label = "Tujuan Keuangan",
                value = goal,
                isEditing = isEditing,
                onValueChange = { goal = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save/Edit Button
        Button(
            onClick = {
                if (isEditing) {
                    profileViewModel.updateUserProfile(
                        occupation,
                        income.toLongOrNull() ?: 0,
                        goal,
                        context
                    ) { success, message ->
                        if (success) {
                            Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                            isEditing = false
                        } else {
                            Toast.makeText(context, message ?: "Gagal update", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    isEditing = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isUpdating,
            shape = RoundedCornerShape(4.dp),
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (isEditing) "Save" else "Edit Profile", fontSize = 16.sp)
            }
        }
    }
}


@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Normal)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    KasKuTheme {
        val previewUserData = UserData(
            uid = "preview123",
            username = "Mizan12",
            email = "mizan@example.com",
            userProfile = UserProfile(
                occupation = "Mahasiswa",
                income = 1000000,
                financialGoals = "Buy a computer",
                currency = "IDR"
            )
        )
        ProfileContent(
            paddingValues = PaddingValues(0.dp), userData = previewUserData,
            profileViewModel = TODO()
        )
    }
}