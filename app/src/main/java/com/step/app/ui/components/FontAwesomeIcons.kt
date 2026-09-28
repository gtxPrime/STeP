package com.step.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Authentic FontAwesome Solid Vector Icons for Jetpack Compose.
 * Replaces all emojis with crisp, professional sovereign vector iconography.
 */
object FontAwesomeIcons {

    private fun buildIcon(name: String, viewportWidth: Float, viewportHeight: Float, pathData: String): ImageVector {
        val nodes = PathParser().parsePathString(pathData).toNodes()
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight
        ).addPath(
            pathData = nodes,
            fill = SolidColor(Color.Black)
        ).build()
    }

    object Solid {
        // Tab Navigation Icons
        val House: ImageVector by lazy {
            buildIcon(
                name = "fa-house",
                viewportWidth = 576f,
                viewportHeight = 512f,
                pathData = "M575.8 255.5c0 18-15 32.1-32 32.1h-32l.7 160.2c0 17-14 32.2-32 32.2h-64c-17 0-32-14.2-32-32.2v-96c0-17-14-32-32-32h-64c-17 0-32 15-32 32v96c0 18-15 32.2-32 32.2h-64c-17 0-32-15.2-32-32.2L96 287.6H64c-18 0-32-14.1-32-32.1 0-9 4-17 10-23l224-200c12-11 31-11 43 0l224 200c6 6 10 14 10 23z"
            )
        }

        val FilePen: ImageVector by lazy {
            buildIcon(
                name = "fa-file-pen",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M368.4 18.3L312 74.7 437.3 200l56.4-56.4c24.4-24.4 24.4-64 0-88.4L456.8 18.3c-24.4-24.4-64-24.4-88.4 0zM289.4 97.4L64 322.8V448h125.2l225.4-225.4L289.4 97.4z"
            )
        }

        val Timeline: ImageVector by lazy {
            buildIcon(
                name = "fa-timeline",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M64 64c0-17.7-14.3-32-32-32S0 46.3 0 64V448c0 17.7 14.3 32 32 32s32-14.3 32-32V64zm96 32c-17.7 0-32 14.3-32 32v64c0 17.7 14.3 32 32 32h288c17.7 0 32-14.3 32-32V128c0-17.7-14.3-32-32-32H160zm0 160c-17.7 0-32 14.3-32 32v64c0 17.7 14.3 32 32 32h192c17.7 0 32-14.3 32-32v-64c0-17.7-14.3-32-32-32H160z"
            )
        }

        val Headset: ImageVector by lazy {
            buildIcon(
                name = "fa-headset",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 32C132.3 32 32 132.3 32 256v112c0 26.5 21.5 48 48 48h32c17.7 0 32-14.3 32-32V256c0-17.7-14.3-32-32-32H80.5C92.4 146.9 166.4 80 256 80s163.6 66.9 175.5 144H400c-17.7 0-32 14.3-32 32v128c0 17.7 14.3 32 32 32h32c26.5 0 48-21.5 48-48V256c0-123.7-100.3-224-224-224zm-64 416c0 17.7 14.3 32 32 32h64c17.7 0 32-14.3 32-32s-14.3-32-32-32h-64c-17.7 0-32 14.3-32 32z"
            )
        }

        // Functional Icons
        val Wallet: ImageVector by lazy {
            buildIcon(
                name = "fa-wallet",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M64 32C28.7 32 0 60.7 0 96V416c0 35.3 28.7 64 64 64H448c35.3 0 64-28.7 64-64V192c0-35.3-28.7-64-64-64H80c-8.8 0-16-7.2-16-16s7.2-16 16-16H448c17.7 0 32-14.3 32-32s-14.3-32-32-32H64zM416 272a32 32 0 1 1 0 64 32 32 0 1 1 0-64z"
            )
        }

        val Bell: ImageVector by lazy {
            buildIcon(
                name = "fa-bell",
                viewportWidth = 448f,
                viewportHeight = 512f,
                pathData = "M224 0c-17.7 0-32 14.3-32 32V49.9C119.7 61.6 64 124.6 64 200v33.4c0 45.4-15.5 89.5-43.8 124.9L5.3 377c-5.8 7.2-6.9 17.1-2.9 25.4S14.9 416 24 416H424c9.1 0 17.6-5.3 21.6-13.6s2.9-18.2-2.9-25.4l-14.9-18.6C399.5 322.9 384 278.8 384 233.4V200c0-75.4-55.7-138.4-128-150.1V32c0-17.7-14.3-32-32-32zm0 512c26.5 0 48-21.5 48-48H176c0 26.5 21.5 48 48 48z"
            )
        }

        val User: ImageVector by lazy {
            buildIcon(
                name = "fa-user",
                viewportWidth = 448f,
                viewportHeight = 512f,
                pathData = "M224 256A128 128 0 1 0 224 0a128 128 0 1 0 0 256zm-45.7 48C79.8 304 0 383.8 0 482.3C0 498.7 13.3 512 29.7 512H418.3c16.4 0 29.7-13.3 29.7-29.7C448 383.8 368.2 304 269.7 304H178.3z"
            )
        }

        val CircleCheck: ImageVector by lazy {
            buildIcon(
                name = "fa-circle-check",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 512A256 256 0 1 0 256 0a256 256 0 1 0 0 512zM369 209L241 337c-9.4 9.4-24.6 9.4-33.9 0l-64-64c-9.4-9.4-9.4-24.6 0-33.9s24.6-9.4 33.9 0l47 47L335 175c9.4-9.4 24.6-9.4 33.9 0s9.4 24.6 0 33.9z"
            )
        }

        val CircleXmark: ImageVector by lazy {
            buildIcon(
                name = "fa-circle-xmark",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 512A256 256 0 1 0 256 0a256 256 0 1 0 0 512zM175 175c9.4-9.4 24.6-9.4 33.9 0l47 47 47-47c9.4-9.4 24.6-9.4 33.9 0s9.4 24.6 0 33.9l-47 47 47 47c9.4 9.4 9.4 24.6 0 33.9s-24.6 9.4-33.9 0l-47-47-47 47c-9.4 9.4-24.6 9.4-33.9 0s-9.4-24.6 0-33.9l47-47-47-47c-9.4-9.4-9.4-24.6 0-33.9z"
            )
        }

        val TriangleExclamation: ImageVector by lazy {
            buildIcon(
                name = "fa-triangle-exclamation",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 32c14.2 0 27.3 7.5 34.5 19.8l216 368c7.3 12.4 7.3 27.7 .2 40.1S486.3 480 472 480H40c-14.3 0-27.4-7.7-34.7-20.1s-7.1-27.7 .2-40.1l216-368C228.7 39.5 241.8 32 256 32zm0 128c-13.3 0-24 10.7-24 24V296c0 13.3 10.7 24 24 24s24-10.7 24-24V184c0-13.3-10.7-24-24-24zm32 224a32 32 0 1 0 -64 0 32 32 0 1 0 64 0z"
            )
        }

        val WandMagicSparkles: ImageVector by lazy {
            buildIcon(
                name = "fa-wand-magic-sparkles",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M472.3 8.3c11.1-11.1 29.1-11.1 40.2 0s11.1 29.1 0 40.2l-40 40-40.2-40.2 40-40zm-128 128l40.2 40.2-40 40c-11.1 11.1-29.1 11.1-40.2 0s-11.1-29.1 0-40.2l40-40zm-80-80L368 160 160 368 56.3 264.3 264.3 56.3zm-176 352l64-64 40.2 40.2-64 64c-11.1 11.1-29.1 11.1-40.2 0s-11.1-29.1 0-40.2z"
            )
        }

        val ArrowRight: ImageVector by lazy {
            buildIcon(
                name = "fa-arrow-right",
                viewportWidth = 448f,
                viewportHeight = 512f,
                pathData = "M438.6 278.6c12.5-12.5 12.5-32.8 0-45.3l-160-160c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L338.8 224 32 224c-17.7 0-32 14.3-32 32s14.3 32 32 32l306.7 0-105.5 105.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0l160-160z"
            )
        }

        val ArrowLeft: ImageVector by lazy {
            buildIcon(
                name = "fa-arrow-left",
                viewportWidth = 448f,
                viewportHeight = 512f,
                pathData = "M9.4 233.4c-12.5 12.5-12.5 32.8 0 45.3l160 160c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L109.2 288 416 288c17.7 0 32-14.3 32-32s-14.3-32-32-32l-306.8 0 105.4-105.4c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0l-160 160z"
            )
        }

        val Camera: ImageVector by lazy {
            buildIcon(
                name = "fa-camera",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M149.1 64l45.2-45.2C202.9 10.3 214.3 4.7 226.3 4.7h59.4c12 0 23.4 5.6 32 14.1L362.9 64H432c44.2 0 80 35.8 80 80v272c0 44.2-35.8 80-80 80H80c-44.2 0-80-35.8-80-80V144c0-44.2 35.8-80 80-80h69.1zM256 416a128 128 0 1 0 0-256 128 128 0 1 0 0 256zm0-48a80 80 0 1 1 0-160 80 80 0 1 1 0 160z"
            )
        }

        val Microphone: ImageVector by lazy {
            buildIcon(
                name = "fa-microphone",
                viewportWidth = 384f,
                viewportHeight = 512f,
                pathData = "M192 0C139 0 96 43 96 96V256c0 53 43 96 96 96s96-43 96-96V96c0-53-43-96-96-96zM64 216c0-13.3-10.7-24-24-24s-24 10.7-24 24v40c0 89.1 66.2 162.7 152 174.4V464H120c-13.3 0-24 10.7-24 24s10.7 24 24 24h144c13.3 0 24-10.7 24-24s-10.7-24-24-24H216V430.4c85.8-11.7 152-85.3 152-174.4V216c0-13.3-10.7-24-24-24s-24 10.7-24 24v40c0 70.7-57.3 128-128 128s-128-57.3-128-128V216z"
            )
        }

        val PaperPlane: ImageVector by lazy {
            buildIcon(
                name = "fa-paper-plane",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M498.1 5.6c10.1 7 15.4 19.1 13.5 31.2l-64 416c-1.5 9.7-7.4 18.2-16 23s-18.9 5.4-28 1.6L284 427.7l-68.5 74.1c-8.9 9.7-22.9 14.2-35.6 11.6S160 499.2 160 486V384l256-256L143.6 348.6 32.3 293C20.6 287.1 13.5 275.2 14.1 262.2s8.5-24.1 20.3-29.4l432-192c10.6-4.7 23-3.2 31.7 4.8z"
            )
        }

        val GraduationCap: ImageVector by lazy {
            buildIcon(
                name = "fa-graduation-cap",
                viewportWidth = 640f,
                viewportHeight = 512f,
                pathData = "M320 32L0 192l320 160 256-128V368c0 26.5 21.5 48 48 48s48-21.5 48-48V192L320 32zM128 320v96c0 53 86 96 192 96s192-43 192-96v-96L320 416 128 320z"
            )
        }

        val BuildingColumns: ImageVector by lazy {
            buildIcon(
                name = "fa-building-columns",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M243.4 2.6l-224 96c-11.8 5-19.4 16.6-19.4 29.4s7.6 24.4 19.4 29.4l224 96c8 3.4 17.2 3.4 25.2 0l224-96c11.8-5 19.4-16.6 19.4-29.4s-7.6-24.4-19.4-29.4l-224-96c-8-3.4-17.2-3.4-25.2 0zM64 256H32v160h32V256zm96 0h-32v160h32V256zm128 0h-32v160h32V256zm96 0h-32v160h32V256zm96 0h-32v160h32V256zM0 480c0 17.7 14.3 32 32 32h448c17.7 0 32-14.3 32-32s-14.3-32-32-32H32c-17.7 0-32 14.3-32 32z"
            )
        }

        val FileLines: ImageVector by lazy {
            buildIcon(
                name = "fa-file-lines",
                viewportWidth = 384f,
                viewportHeight = 512f,
                pathData = "M64 0C28.7 0 0 28.7 0 64V448c0 35.3 28.7 64 64 64H320c35.3 0 64-28.7 64-64V160H256c-17.7 0-32-14.3-32-32V0H64zm192 0v128h128L256 0zM96 256c0-8.8 7.2-16 16-16H272c8.8 0 16 7.2 16 16s-7.2 16-16 16H112c-8.8 0-16-7.2-16-16zm0 64c0-8.8 7.2-16 16-16H272c8.8 0 16 7.2 16 16s-7.2 16-16 16H112c-8.8 0-16-7.2-16-16zm0 64c0-8.8 7.2-16 16-16H208c8.8 0 16 7.2 16 16s-7.2 16-16 16H112c-8.8 0-16-7.2-16-16z"
            )
        }

        val Clock: ImageVector by lazy {
            buildIcon(
                name = "fa-clock",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 512A256 256 0 1 0 256 0a256 256 0 1 0 0 512zm0-384c13.3 0 24 10.7 24 24V240l76.7 44.3c11.5 6.6 15.5 21.3 8.8 32.8s-21.3 15.5-32.8 8.8l-88-50.8c-7.3-4.2-11.7-12-11.7-20.4V152c0-13.3 10.7-24 24-24z"
            )
        }

        val Check: ImageVector by lazy {
            buildIcon(
                name = "fa-check",
                viewportWidth = 448f,
                viewportHeight = 512f,
                pathData = "M438.6 105.4c12.5 12.5 12.5 32.8 0 45.3l-256 256c-12.5 12.5-32.8 12.5-45.3 0l-128-128c-12.5-12.5-12.5-32.8 0-45.3s32.8-12.5 45.3 0L160 338.7 393.4 105.4c12.5-12.5 32.8-12.5 45.3 0z"
            )
        }

        val ShieldCheck: ImageVector by lazy {
            buildIcon(
                name = "fa-shield-check",
                viewportWidth = 512f,
                viewportHeight = 512f,
                pathData = "M256 0c4.6 0 9.2 1 13.4 2.9L457.7 82.8c22 9.3 36.3 30.8 36.3 54.5v149.9c0 140.3-97.4 262.8-232.5 292.8c-3.6 .8-7.3 1.2-11 1.2s-7.4-.4-11-1.2C104.4 550 7 427.5 7 287.2V137.3c0-23.7 14.3-45.2 36.3-54.5L242.6 2.9C246.8 1 251.4 0 256 0zm105 217c9.4-9.4 9.4-24.6 0-33.9s-24.6-9.4-33.9 0l-95 95-47-47c-9.4-9.4-24.6-9.4-33.9 0s-9.4 24.6 0 33.9l64 64c9.4 9.4 24.6 9.4 33.9 0l112-112z"
            )
        }
    }
}
