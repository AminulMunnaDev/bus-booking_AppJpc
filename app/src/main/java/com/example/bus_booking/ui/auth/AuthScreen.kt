package com.example.bus_booking.ui.auth


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bus_booking.ui.theme.Green50
import com.example.bus_booking.ui.theme.Green600
import com.example.bus_booking.ui.theme.Green700
import com.example.bus_booking.ui.theme.Red100
import com.example.bus_booking.ui.theme.Red600
import com.example.bus_booking.ui.theme.Red700
import com.example.bus_booking.ui.theme.Slate200
import com.example.bus_booking.ui.theme.Slate300
import com.example.bus_booking.ui.theme.Slate50
import com.example.bus_booking.ui.theme.Slate500
import com.example.bus_booking.ui.theme.Slate600
import com.example.bus_booking.ui.theme.Slate900
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    AuthScreenShell(
        title = "Welcome back",
        subtitle = "Sign in to search routes, pick seats, and manage tickets.",
        footerPrompt = "Don't have an account?",
        footerActionText = "Register",
        onFooterActionClick = onNavigateToRegister
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = Slate500
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = RoundedCornerShape(10.dp),
            colors = busGoFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Slate500
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Slate500
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    viewModel.login(email, password)
                }
            ),
            shape = RoundedCornerShape(10.dp),
            colors = busGoFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        ErrorBanner(message = uiState.errorMessage)

        Spacer(modifier = Modifier.height(16.dp))

        BusGoPrimaryButton(
            text = "Login",
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.login(email, password)
            }
        )
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onRegisterSuccess()
        }
    }

    AuthScreenShell(
        title = "Create Account",
        subtitle = "Set up your passenger profile for faster bookings.",
        footerPrompt = "Already have an account?",
        footerActionText = "Login",
        onFooterActionClick = onNavigateToLogin
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Slate500
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = RoundedCornerShape(10.dp),
            colors = busGoFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = Slate500
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = RoundedCornerShape(10.dp),
            colors = busGoFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone (Optional)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = Slate500
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            shape = RoundedCornerShape(10.dp),
            colors = busGoFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 6 chars)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Slate500
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) {
                            Icons.Outlined.VisibilityOff
                        } else {
                            Icons.Outlined.Visibility
                        },
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = Slate500
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    viewModel.register(name, email, phone, password)
                }
            ),
            shape = RoundedCornerShape(10.dp),
            colors = busGoFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        ErrorBanner(message = uiState.errorMessage)

        Spacer(modifier = Modifier.height(16.dp))

        BusGoPrimaryButton(
            text = "Register",
            isLoading = uiState.isLoading,
            enabled = !uiState.isLoading,
            onClick = {
                focusManager.clearFocus()
                viewModel.register(name, email, phone, password)
            }
        )
    }
}

@Composable
private fun AuthScreenShell(
    title: String,
    subtitle: String,
    footerPrompt: String,
    footerActionText: String,
    onFooterActionClick: () -> Unit,
    formContent: @Composable () -> Unit
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        appeared = true
    }

    val headerAlpha by animateFloatAsState(
        targetValue = if (appeared) 1f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "headerAlpha"
    )
    val headerOffset by animateFloatAsState(
        targetValue = if (appeared) 0f else 18f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "headerOffset"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        color = Slate50
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .padding(top = headerOffset.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Green50)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsBus,
                        contentDescription = "BusGo",
                        tint = Green600.copy(alpha = headerAlpha),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "BusGo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Green600.copy(alpha = headerAlpha)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900.copy(alpha = headerAlpha),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600.copy(alpha = headerAlpha),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Minimalist flat card (12dp corners, 1dp border, no shadow)
            AnimatedVisibility(
                visible = appeared,
                enter = fadeIn(tween(320)) + slideInVertically(
                    initialOffsetY = { it / 8 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Slate200),
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        formContent()
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = footerPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = footerActionText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Green700,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onFooterActionClick)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String?) {
    AnimatedVisibility(
        visible = !message.isNullOrBlank(),
        enter = fadeIn(tween(180)) + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = fadeOut(tween(150)) + shrinkVertically()
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Red100,
            border = BorderStroke(1.dp, Red600.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Red700,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun BusGoPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green600,
            contentColor = Color.White,
            disabledContainerColor = Slate300,
            disabledContentColor = Slate500
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .scale(scale)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun busGoFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Green600,
    unfocusedBorderColor = Slate300,
    focusedLabelColor = Green700,
    unfocusedLabelColor = Slate500,
    cursorColor = Green600,
    focusedTextColor = Slate900,
    unfocusedTextColor = Slate900
)