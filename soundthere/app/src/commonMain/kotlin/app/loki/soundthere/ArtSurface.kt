package app.loki.soundthere

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The arrangement is drawn by the app from the session's state (Arrangement.kt), not rendered by the engine. */
@Composable
fun ArtSurface(native: SurfaceEngine, revision: Int, modifier: Modifier) = Arrangement(modifier)
