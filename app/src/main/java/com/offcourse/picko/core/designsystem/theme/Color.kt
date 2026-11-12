package com.offcourse.picko.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// ───────────────────────── 1층: Figma Style Guide 팔레트 ─────────────────────────
// 이름은 Figma 이름을 PascalCase로 옮긴 것이다. 값에는 역할이 없고, 역할은 PickoColors가 정한다.

// Brand / Accent
val Green = Color(0xFF30CF72)
val MintLight = Color(0xFF91F7D1)
val BluePrimary = Color(0xFF416DE9)
val BlueButton = Color(0xFF315FD6)
val ButtonSecondary = Color(0xFFD6E3FC)
val AccentBlue = Color(0xFF2385F5)
val BlueLavender = Color(0xFFA7C4FF)
val BlueCornflower = Color(0xFF61A6F8)
val BlueIris = Color(0xFF416FE9)
val BlueGrayLight = Color(0xFFD1DFF0)

// Background / Surface
val WhiteSoft = Color(0xFFFEFEFF)
val BlueIce = Color(0xFFE4F0FF)
val BluePastel = Color(0xFFA3D1FF)
val BlueSky = Color(0xFF91BFFF)
val WhiteBlue = Color(0xFFF5F9FF)
val BluePale = Color(0xFFD9E9FF)
val BlueMuted = Color(0xFFB6CBEA)
val BlueFrost = Color(0xFFE0EDFF)
val CyanPale = Color(0xFFEAF8FF)
val BackgroundLight = Color(0xFFF4F7FF)
val BackgroundSoft = Color(0xFFF2F7FF)

// Text / Neutral
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val Charcoal = Color(0xFF131417)
val GrayLight = Color(0xFFD9D9D9)
val NavyText = Color(0xFF171D32)
val TextSecondary = Color(0xFF526583)
val TextMuted = Color(0xFF637087)
val GrayBlue = Color(0xFFADBDD6)

// Transparency / Alpha (알파는 Figma 퍼센트를 반올림한 값)
val GlassSurface = Color(0xE0FFFFFF) // 88%
val NavySurfaceAlpha88 = Color(0xE01C2945) // 다크 pickup_indicator 면: navy_surface 88%
val GlassBorder = Color(0xE6C2DBF5) // 90%
val WhiteAlpha94 = Color(0xF0FFFFFF) // 94%
val BlueIceAlpha66 = Color(0xA8E4F0FF) // 66%
val BluePaleAlpha90 = Color(0xE6D9E9FF) // 90%
val BlueSkyAlpha30 = Color(0x4DCCE8FF) // 30%
val BlueMistAlpha30 = Color(0x4DBAD6FF) // 30%
val OverlayDim75 = Color(0xBF090E1B) // 75%

// Other / UI
val NavyBlue = Color(0xFF173A75)
val NavyDark = Color(0xFF162A4D)
val SlateBlue = Color(0xFF425979)
val NavyMedium = Color(0xFF24436B)
val BlueDeep = Color(0xFF244FC5)
val Navy = Color(0xFF172D51)
val Night = Color(0xFF090E1C)
val BlueBright = Color(0xFF0F4DEB)
val BlueVivid = Color(0xFF1F63FF)
val NightBlue = Color(0xFF101C36)
val NavySurface = Color(0xFF1C2945)
val NavySoft = Color(0xFF1B253C)
val LabelBlue = Color(0xFF305E99)
val NavyDeep = Color(0xFF0D1120)
val GrayMedium = Color(0xFF7C7C7D)
val SlateDeep = Color(0xFF2F476C)
val NavyCard = Color(0xFF162743)
val SlateBorder = Color(0xFF374F70)
val WhiteCool = Color(0xFFF1F7FF)
val NavyLabel = Color(0xFF1C2E4D)

// 모드별 값을 가진 Figma 변수 (basic/*, background/primary, divider/*)
val Basic100Light = Color(0xFFEDEEF2)
val Basic100Dark = Color(0xFF313236)
val BackgroundPrimaryLight = Color(0xFFF7F8FA)
val BackgroundPrimaryDark = Color(0xFF1D1E21)
val DividerSecondaryLight = Color(0x1A131417)
val DividerSecondaryDark = Color(0x1AFFFFFF)
val DividerTertiaryLight = Color(0x33131417)
val DividerTertiaryDark = Color(0x33FFFFFF)

// ───────────────────────── 그라데이션 ─────────────────────────

/**
 * CSS `linear-gradient(<angle>, ...)`와 같은 방식으로 그려지는 [Brush]를 만든다.
 * 각도는 위쪽이 0도이고 시계 방향으로 증가한다. 그려질 크기에 맞춰 시작점과 끝점이 계산되므로 크기에 의존하지 않는다.
 */
fun cssLinearGradient(angleDeg: Float, vararg colorStops: Pair<Float, Color>): Brush =
    object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val (from, to) = cssGradientLine(angleDeg, size)

            return LinearGradientShader(
                from = from,
                to = to,
                colors = colorStops.map { it.second },
                colorStops = colorStops.map { it.first },
            )
        }
    }

/** CSS 규칙대로 그라데이션 선의 시작점과 끝점을 구한다. 선의 길이는 모서리까지 닿도록 `|w·sin| + |h·cos|`이다. */
internal fun cssGradientLine(angleDeg: Float, size: Size): Pair<Offset, Offset> {
    val rad = Math.toRadians(angleDeg.toDouble())
    val dx = sin(rad).toFloat()
    val dy = -cos(rad).toFloat()
    val half = (abs(size.width * dx) + abs(size.height * dy)) / 2f
    val center = size.center

    return Offset(center.x - dx * half, center.y - dy * half) to Offset(center.x + dx * half, center.y + dy * half)
}

// 버튼 그라데이션: 각도와 정지점을 시안 프레임에서 직접 읽은 값이다.
val ButtonPrimaryLightGradient = cssLinearGradient(
    168.69f,
    0.064f to Color(0xFF678AED),
    0.48f to Color(0xFF416DE9),
    0.864f to Color(0xFF2E5AD2),
)
val ButtonPrimaryDarkGradient = cssLinearGradient(
    168.69f,
    0.064f to Color(0xFF40A6FF),
    0.464f to Color(0xFF246EE8),
    0.864f to Color(0xFF1A4DC4),
)
val ButtonSecondaryLightGradient = cssLinearGradient(
    150.07f,
    0.064f to Color(0xFFFFFFFF),
    0.48f to Color(0xFFF5F9FF),
    0.864f to Color(0xFFE4F0FF),
)
val ButtonSecondaryDarkGradient = cssLinearGradient(
    150.07f,
    0.064f to Color(0xFF3B5482),
    0.48f to Color(0xFF263B63),
    0.864f to Color(0xFF1F2E4F),
)

// 스플래시 배경: 시안 프레임에서 읽은 값이다.
val SplashLightGradient = cssLinearGradient(
    131.3f,
    0.10949f to Color(0xFF214AB2),
    0.48905f to Color(0xFF406EE8),
    0.83942f to Color(0xFF709EFF),
)
val SplashDarkGradient = cssLinearGradient(
    137.12f,
    0.10714f to Color(0xFF090E21),
    0.46429f to Color(0xFF121F47),
    0.82143f to Color(0xFF263D78),
)

// Style Guide 그라데이션: 색은 표기된 값이고, 방향과 중간 정지점 위치는 스와치를 눈으로 보고 정했다.
// TODO(figma): 사용하는 화면을 구현할 때 해당 프레임에서 각도와 정지점을 확인해 보정한다.
val GradientBackgroundLight = cssLinearGradient(135f, 0f to White, 1f to Color(0xFFEDEEF2))
val GradientAccent = cssLinearGradient(180f, 0f to Color(0xFF50C0FF), 1f to Color(0xFF5364FA))
val GradientWhiteSoft = cssLinearGradient(180f, 0f to White, 1f to Color(0xFFF0F4FD))
val GradientBlueSoft = cssLinearGradient(180f, 0f to White, 1f to Color(0xFFD1DAF0))
val GradientSurfaceLight = cssLinearGradient(180f, 0f to White, 1f to Color(0xFFF0F4FD))
val GradientButtonLight = cssLinearGradient(
    90f,
    0f to Color(0xFF244FC5),
    0.5f to Color(0xFF416DE9),
    1f to Color(0xFF719BFF),
)
val GradientButtonDark = cssLinearGradient(
    90f,
    0f to Color(0xFFD8EAFF),
    0.5f to Color(0xFFBBD6FF),
    1f to Color(0xFF9FC5FF),
)

// ───────────────────────── 2층: 역할 기반 색상 ─────────────────────────

/**
 * 화면 코드는 팔레트 상수 대신 이 클래스의 역할 필드만 읽는다. 라이트·다크 분기는 이 클래스의 인스턴스가 맡는다.
 * 값은 시안의 Light·Dark 쌍에서 확인한 것이다. `?`는 용도를 확인하지 못한 값이고, TODO(figma)는 다크 짝을 아직 찾지 못한 값이다.
 */
@Immutable
data class PickoColors(
    // Background / Surface
    val background: Color, // basic/00
    val backgroundSub: Color, // background/primary
    val backgroundStrong: Color, // basic/100 (용도 미확인)
    val surface: Color, // 정보 카드 면: white_blue ↔ navy_surface
    val surfaceRaised: Color, // 선택된 결과 카드, 연락처 액션 카드 면
    val surfaceCard: Color, // 선택되지 않은 결과 카드 면
    val backgroundLight: Color,
    val backgroundSoft: Color,
    val surfaceIce: Color,
    val surfacePastel: Color,
    val surfaceSky: Color,
    val surfacePale: Color,
    val surfaceMuted: Color,
    val surfaceFrost: Color,
    val surfaceCyan: Color,

    // Text / Neutral
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textMeta: Color, // 결과 카드의 거리·시간
    val textIndicator: Color, // SMART PICKUP 표시 글자
    val onAccent: Color,
    val black: Color,
    val white: Color,
    val charcoal: Color,
    val grayLight: Color,
    val grayBlue: Color,

    // Brand / Accent
    val accent: Color,
    val accentStrong: Color, // 도착 시간 같은 강조 숫자
    val accentText: Color, // 강조 문구
    val accentBright: Color,
    val accentLavender: Color,
    val accentCornflower: Color,
    val secondaryContainer: Color,
    val success: Color,
    val mint: Color,

    // Border / Divider
    val borderCard: Color,
    val borderSelected: Color,
    val borderModal: Color,
    val separator: Color,
    val divider: Color,
    val dividerStrong: Color,

    // Component
    val avatarSurface: Color,
    val avatarContent: Color,
    val surfaceModal: Color,
    val buttonNeutral: Color,
    val onButtonNeutral: Color,
    val markerSurface: Color,
    val markerTitle: Color,
    val markerBody: Color,
    val dotInactive: Color,
    val brandLogo: Color,
    val micIcon: Color,
    val emblemRing: Color,
    val emblemFill: Color,
    val onSplash: Color,

    // Transparency / Alpha
    val glassSurface: Color,
    val glassBorder: Color,
    val ctaBorder: Color,
    val buttonSecondaryBorder: Color,
    val overlayDim: Color,

    // Gradients
    val buttonPrimary: Brush,
    val buttonSecondary: Brush,
    val splashBackground: Brush,
)

val lightPickoColors = PickoColors(
    background = White,
    backgroundSub = BackgroundPrimaryLight,
    backgroundStrong = Basic100Light,
    surface = WhiteBlue,
    surfaceRaised = WhiteSoft,
    surfaceCard = WhiteAlpha94,
    backgroundLight = BackgroundLight,
    backgroundSoft = BackgroundSoft,
    surfaceIce = BlueIce,
    surfacePastel = BluePastel,
    surfaceSky = BlueSky,
    surfacePale = BluePale,
    surfaceMuted = BlueMuted,
    surfaceFrost = BlueFrost,
    surfaceCyan = CyanPale,

    textPrimary = NavyText,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    textMeta = TextSecondary,
    textIndicator = LabelBlue,
    onAccent = White,
    black = Black,
    white = White,
    charcoal = Charcoal,
    grayLight = GrayLight,
    grayBlue = GrayBlue,

    accent = BluePrimary,
    accentStrong = BlueButton,
    accentText = BluePrimary,
    accentBright = AccentBlue,
    accentLavender = BlueLavender,
    accentCornflower = BlueCornflower,
    secondaryContainer = ButtonSecondary,
    success = Green,
    mint = MintLight,

    borderCard = BlueGrayLight,
    borderSelected = BlueIris,
    borderModal = SlateBorder,
    separator = BlueMuted,
    divider = DividerSecondaryLight,
    dividerStrong = DividerTertiaryLight,

    avatarSurface = BlueIce,
    avatarContent = BlueSky,
    surfaceModal = White,
    buttonNeutral = BackgroundSoft,
    onButtonNeutral = Navy,
    markerSurface = White,
    markerTitle = Navy,
    markerBody = TextSecondary,
    dotInactive = BluePastel,
    brandLogo = BluePrimary,
    micIcon = BlueDeep,
    emblemRing = BluePastel,
    emblemFill = White,
    onSplash = White,

    glassSurface = GlassSurface,
    glassBorder = GlassBorder,
    ctaBorder = BlueIceAlpha66,
    buttonSecondaryBorder = BluePaleAlpha90,
    overlayDim = OverlayDim75,

    buttonPrimary = ButtonPrimaryLightGradient,
    buttonSecondary = ButtonSecondaryLightGradient,
    splashBackground = SplashLightGradient,
)

val darkPickoColors = PickoColors(
    background = Charcoal,
    backgroundSub = BackgroundPrimaryDark,
    backgroundStrong = Basic100Dark,
    surface = NavySurface,
    surfaceRaised = NavySurface,
    surfaceCard = NavyCard,
    // TODO(figma): 아래 9개는 다크 짝을 확인하지 못해 라이트와 같은 값이다.
    backgroundLight = BackgroundLight,
    backgroundSoft = BackgroundSoft,
    surfaceIce = BlueIce,
    surfacePastel = BluePastel,
    surfaceSky = BlueSky,
    surfacePale = BluePale,
    surfaceMuted = BlueMuted,
    surfaceFrost = BlueFrost,
    surfaceCyan = CyanPale,

    textPrimary = WhiteBlue,
    textSecondary = GrayBlue,
    textMuted = GrayBlue,
    textMeta = BlueMuted,
    textIndicator = BlueFrost,
    onAccent = White,
    black = Black,
    white = White,
    charcoal = Charcoal,
    grayLight = GrayLight,
    grayBlue = GrayBlue,

    accent = BluePrimary,
    accentStrong = BluePastel,
    accentText = BluePastel,
    accentBright = AccentBlue,
    accentLavender = BlueLavender,
    accentCornflower = BlueCornflower,
    secondaryContainer = ButtonSecondary, // TODO(figma)
    success = Green,
    mint = MintLight,

    borderCard = SlateBorder,
    borderSelected = BlueCornflower,
    borderModal = SlateBorder,
    separator = SlateBlue,
    divider = DividerSecondaryDark,
    dividerStrong = DividerTertiaryDark,

    avatarSurface = NavyMedium,
    avatarContent = BluePastel,
    surfaceModal = NavyDark,
    buttonNeutral = SlateBlue,
    onButtonNeutral = White,
    markerSurface = Navy,
    markerTitle = White,
    markerBody = White,
    dotInactive = BlueIce,
    brandLogo = WhiteBlue,
    micIcon = CyanPale,
    emblemRing = BlueDeep,
    emblemFill = NavySurface,
    onSplash = BackgroundLight,

    glassSurface = NavySurfaceAlpha88,
    glassBorder = GlassBorder,
    ctaBorder = BlueSkyAlpha30,
    buttonSecondaryBorder = BlueMistAlpha30,
    overlayDim = OverlayDim75,

    buttonPrimary = ButtonPrimaryDarkGradient,
    buttonSecondary = ButtonSecondaryDarkGradient,
    splashBackground = SplashDarkGradient,
)
