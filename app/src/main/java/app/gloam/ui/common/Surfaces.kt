package app.gloam.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.gloam.R
import app.gloam.theme.Spacing

/**
 * The shape every detail screen takes: a top bar with a title and a back arrow, and content below.
 *
 * Extracted because three screens had it and the fourth got the padding subtly wrong. A shared
 * scaffold is not about saving lines — it is about the app looking like one app.
 *
 * `onBack` is nullable rather than defaulted to `{}`: a screen with no back arrow and a screen whose
 * back arrow does nothing look identical in a screenshot and are completely different bugs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScaffold(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                // AutoMirrored so the arrow points the other way in a right-to-left
                                // locale. The manifest declares `supportsRtl`, which makes this the
                                // app's problem rather than the platform's.
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
                actions = { actions() },
            )
        },
    ) { insets ->
        content(Modifier.padding(insets))
    }
}

/**
 * What a list shows when it is empty.
 *
 * **Say what to do, not that there is nothing.** "No items yet" tells the user what they can already
 * see; "Tap + to add your first item" tells them what to do about it.
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(Spacing.block),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * **The notification-warning idiom, now that it has a second caller.**
 *
 * A live read that went wrong, said as what the user has lost, with one button that goes to the
 * screen where they can fix it. `DimScreen`'s "No Stop button outside Gloam" is the original and the
 * schedule's battery banner is the second — same shape, same colours, and both self-clear on the
 * resume after the user comes back, because both are re-read rather than remembered.
 *
 * It is deliberately *not* the other Phase 2 idiom. The autostart row is permanent, makes no claim
 * and remembers nothing, because the app cannot read that grant back; this one appears only while
 * the state is actually wrong, which it can only do for a state the app can actually read
 * (ADR-0003, `docs/phase-4.md` §7).
 */
@Composable
fun WarningBanner(
    title: String,
    body: String,
    actionLabel: String,
    onAct: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = Spacing.base, end = Spacing.base, top = Spacing.base),
    ) {
        Column(modifier = Modifier.padding(Spacing.base)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Spacing.tight),
            )
            TextButton(onClick = onAct, modifier = Modifier.padding(top = Spacing.tight)) {
                Text(actionLabel)
            }
        }
    }
}

/**
 * A title, a line of supporting text, and a switch — **and the whole row is the target**.
 *
 * A 48dp strip the width of the screen is far easier to hit than a 32dp control at the edge of it,
 * which is the reason this is a `Row` with a `clickable` on it rather than a `Switch` with a label
 * beside it. Settings owned it while it had one caller; the schedule's own toggle is the second, and
 * two hand-built versions of this row is how the padding drifts (see [DetailScaffold]).
 *
 * `enabled = false` is for the third caller, the dim screen's backlight switch on a device that
 * cannot honour it: the row is drawn and inert rather than absent, because a control that is simply
 * missing leaves the user unable to tell Gloam from a Gloam that behaves differently on their phone
 * than on someone else's. It disables the row's own `clickable` as well as the switch — a row that
 * still took taps and did nothing would be worse than one that says no.
 */
@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onChange(!checked) }
                .padding(horizontal = Spacing.base, vertical = Spacing.snug),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

/** Where a [LinkRow] goes, which decides its trailing icon. */
enum class LinkTarget {
    /** Another screen of this app: a chevron. */
    Inside,

    /** Another app or a system screen: "open in new". */
    Outside,

    /** Nowhere: an action done on this screen, at most behind a dialog. No icon. */
    Here,
}

/**
 * A row that goes somewhere.
 *
 * A row that **leaves** the app — a mail composer, the Play listing, a system settings page — takes
 * [LinkTarget.Outside] and a subtitle saying where, because a tap that hands the user to another app
 * with no warning is the kind they back out of and never repeat.
 */
@Composable
fun LinkRow(
    title: String,
    target: LinkTarget,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = Spacing.base, vertical = Spacing.snug),
        horizontalArrangement = Arrangement.spacedBy(Spacing.base),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.hair)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val icon =
            when (target) {
                LinkTarget.Inside -> rememberVectorPainter(Icons.AutoMirrored.Filled.KeyboardArrowRight)
                LinkTarget.Outside -> painterResource(R.drawable.ic_open_in_new)
                LinkTarget.Here -> null
            }
        if (icon != null) {
            Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * A section heading.
 *
 * The padding here is the rhythm rule from [Spacing]: [Spacing.section] above, [Spacing.tight]
 * below. Equal padding on both sides is the most common spacing mistake in a hand-built UI — it
 * leaves the header floating between two sections, belonging to neither.
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier.padding(
                start = Spacing.base,
                end = Spacing.base,
                top = Spacing.section,
                bottom = Spacing.tight,
            ),
    )
}
